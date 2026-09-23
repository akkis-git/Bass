package com.example.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.data.BiometricAttendanceRecord
import com.example.model.BiometricMode
import com.example.model.DEFAULT_ENTRY_POINTS
import com.example.model.DeveloperSettings
import com.example.model.DeviceVendor
import com.example.model.OfficeEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class CaptureState {
    object Idle : CaptureState()
    data class Scanning(val progress: Float, val statusMessage: String) : CaptureState()
    data class Success(val record: BiometricAttendanceRecord, val qualityScore: Int, val isMockLocationUsed: Boolean) : CaptureState()
    data class Error(val message: String) : CaptureState()
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    val allRecords: StateFlow<List<BiometricAttendanceRecord>>
    val unsyncedCount: StateFlow<Int>
    val totalCount: StateFlow<Int>

    private val _devSettings = MutableStateFlow(DeveloperSettings())
    val devSettings: StateFlow<DeveloperSettings> = _devSettings.asStateFlow()

    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(database.attendanceDao())

        val recordsFlow = MutableStateFlow<List<BiometricAttendanceRecord>>(emptyList())
        allRecords = recordsFlow

        val unsyncedFlow = MutableStateFlow(0)
        unsyncedCount = unsyncedFlow

        val totalFlow = MutableStateFlow(0)
        totalCount = totalFlow

        viewModelScope.launch {
            repository.allRecords.collect { recordsFlow.value = it }
        }
        viewModelScope.launch {
            repository.unsyncedCount.collect { unsyncedFlow.value = it }
        }
        viewModelScope.launch {
            repository.totalCount.collect { totalFlow.value = it }
        }

        // Initialize default location
        updateSimulatedLocation()
    }

    private fun updateSimulatedLocation() {
        val loc = Location("DevGPSProvider").apply {
            latitude = _devSettings.value.customLatitude
            longitude = _devSettings.value.customLongitude
            accuracy = _devSettings.value.customAccuracyMeters
            time = System.currentTimeMillis()
        }
        _currentLocation.value = loc
    }

    // Developer Settings Controls
    fun setDevOptionsAllowed(enabled: Boolean) {
        _devSettings.update { it.copy(isDevOptionsAllowed = enabled) }
    }

    fun setMockGpsAllowed(enabled: Boolean) {
        _devSettings.update { it.copy(isMockGpsAllowed = enabled) }
    }

    fun setCustomGpsOverride(enabled: Boolean) {
        _devSettings.update { it.copy(isCustomGpsOverrideEnabled = enabled) }
        updateSimulatedLocation()
    }

    fun updateCustomCoordinates(lat: Double, lng: Double, accuracy: Float = 5.0f) {
        _devSettings.update {
            it.copy(
                customLatitude = lat,
                customLongitude = lng,
                customAccuracyMeters = accuracy
            )
        }
        updateSimulatedLocation()
    }

    fun setGeofenceBypassed(bypassed: Boolean) {
        _devSettings.update { it.copy(isGeofenceBypassed = bypassed) }
    }

    fun setWifiCheckBypassed(bypassed: Boolean) {
        _devSettings.update { it.copy(isWifiCheckBypassed = bypassed) }
    }

    fun setBiometricMode(mode: BiometricMode) {
        _devSettings.update { it.copy(activeBiometricMode = mode) }
    }

    fun setDeviceVendor(vendor: DeviceVendor) {
        _devSettings.update { it.copy(selectedVendor = vendor) }
    }

    fun setOfficeEntryPoint(entryPoint: OfficeEntryPoint) {
        _devSettings.update { it.copy(selectedEntryPoint = entryPoint) }
    }

    fun updateEmployeeDetails(empId: String, empName: String, orgName: String) {
        _devSettings.update {
            it.copy(
                activeEmployeeId = empId,
                activeEmployeeName = empName,
                organizationName = orgName
            )
        }
    }

    fun setConsentAccepted(accepted: Boolean) {
        _devSettings.update { it.copy(consentAccepted = accepted) }
    }

    fun resetCaptureState() {
        _captureState.value = CaptureState.Idle
    }

    // Biometric Capture & Attendance Recording
    fun triggerBiometricCapture() {
        if (!_devSettings.value.consentAccepted) {
            _captureState.value = CaptureState.Error("Please check and accept the Aadhaar consent agreement before marking attendance.")
            return
        }

        viewModelScope.launch {
            _captureState.value = CaptureState.Scanning(0.1f, "Initializing RD Service (${_devSettings.value.selectedVendor.vendorName})...")
            delay(400)

            _captureState.value = CaptureState.Scanning(0.4f, "Positioning ${_devSettings.value.activeBiometricMode.label}... Keep still")
            delay(500)

            _captureState.value = CaptureState.Scanning(0.75f, "Capturing PID Block & Quality Analysis...")
            delay(500)

            // Geofence / Location Validation Check
            val settings = _devSettings.value
            val entryPoint = settings.selectedEntryPoint

            val lat = if (settings.isCustomGpsOverrideEnabled) settings.customLatitude else entryPoint.latitude
            val lng = if (settings.isCustomGpsOverrideEnabled) settings.customLongitude else entryPoint.longitude

            val distanceMeters = calculateDistance(
                lat, lng,
                entryPoint.latitude, entryPoint.longitude
            )

            val isWithinGeofence = distanceMeters <= entryPoint.allowedRadiusMeters || settings.isGeofenceBypassed

            val mockLocationUsed = settings.isCustomGpsOverrideEnabled || settings.isMockGpsAllowed

            // In Standard App, Mock GPS or Dev Options block attendance.
            // In THIS DEV EDITION APP, if Dev Options / Mock GPS are enabled, attendance IS ALLOWED with Dev Badge!
            if (!isWithinGeofence && !settings.isDevOptionsAllowed) {
                _captureState.value = CaptureState.Error(
                    "Outside permissible office geofence range (${distanceMeters.toInt()}m from ${entryPoint.name}). Enable Developer Options Override to test."
                )
                return@launch
            }

            // Generate PID XML payload
            val pidXml = generateMockPidXml(settings)
            val qualityScore = (88..99).random()

            val record = BiometricAttendanceRecord(
                employeeId = settings.activeEmployeeId,
                employeeName = settings.activeEmployeeName,
                timestamp = System.currentTimeMillis(),
                biometricType = settings.activeBiometricMode.name,
                deviceVendor = settings.selectedVendor.vendorName,
                deviceId = "DEV-RD-${settings.selectedVendor.name}-${UUID.randomUUID().toString().take(6).uppercase()}",
                latitude = lat,
                longitude = lng,
                isSynced = false,
                pidBlock = pidXml,
                status = if (mockLocationUsed) "SUCCESS_DEV_MOCK" else "SUCCESS"
            )

            val newId = repository.insertRecord(record)
            val savedRecord = record.copy(id = newId)

            _captureState.value = CaptureState.Success(
                record = savedRecord,
                qualityScore = qualityScore,
                isMockLocationUsed = mockLocationUsed
            )
        }
    }

    fun syncRecord(recordId: Long) {
        viewModelScope.launch {
            repository.markAsSynced(recordId, "SUCCESS")
        }
    }

    fun syncAllRecords() {
        viewModelScope.launch {
            val unsyncedList = repository.unsyncedRecords
            allRecords.value.filter { !it.isSynced }.forEach { record ->
                repository.markAsSynced(record.id, "SUCCESS")
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun generateMockPidXml(settings: DeveloperSettings): String {
        val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <PidData>
               <Resp errCode="0" errInfo="Biometric Capture Success" fCount="1" fType="0" iCount="0" pCount="0" pgCount="0"/>
               <DeviceInfo dpId="${settings.selectedVendor.vendorName}.RD" rdsId="AadhaarBASDev.2.1" rdsVer="2.0.1" mi="MFS100" mc="DEV-MC-01" srno="MFS-DEV-${UUID.randomUUID().toString().take(6)}"/>
               <Skey ci="20260923">ENC_DEV_KEY_${UUID.randomUUID().toString().replace("-", "").take(16)}</Skey>
               <Hmac>DEV_HMAC_${UUID.randomUUID().toString().take(12)}</Hmac>
               <Data type="I">${UUID.randomUUID()}</Data>
               <AdditionalInfo>
                  <Param name="DevOptionsAllowed" value="${settings.isDevOptionsAllowed}"/>
                  <Param name="MockGpsAllowed" value="${settings.isMockGpsAllowed}"/>
                  <Param name="EntryPoint" value="${settings.selectedEntryPoint.name}"/>
               </AdditionalInfo>
            </PidData>
        """.trimIndent()
    }
}
