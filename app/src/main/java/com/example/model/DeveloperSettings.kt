package com.example.model

/**
 * Model representing developer options, mock GPS overrides, and client configuration.
 */
data class DeveloperSettings(
    val isDevOptionsAllowed: Boolean = true,
    val isMockGpsAllowed: Boolean = true,
    val isCustomGpsOverrideEnabled: Boolean = false,
    val customLatitude: Double = 28.613912, // Delhi Central Secretariat
    val customLongitude: Double = 77.209021,
    val customAccuracyMeters: Float = 5.0f,
    val isGeofenceBypassed: Boolean = false,
    val isWifiCheckBypassed: Boolean = true,
    val allowSpoofedLocations: Boolean = true,
    val activeEmployeeId: String = "EMP-98240",
    val activeEmployeeName: String = "Rajesh Kumar",
    val organizationName: String = "Ministry of Electronics & IT (MeitY)",
    val selectedEntryPoint: OfficeEntryPoint = DEFAULT_ENTRY_POINTS[0],
    val activeBiometricMode: BiometricMode = BiometricMode.FINGERPRINT,
    val selectedVendor: DeviceVendor = DeviceVendor.MANTRA_MFS100,
    val consentAccepted: Boolean = true
)

data class OfficeEntryPoint(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val allowedRadiusMeters: Double = 200.0
)

enum class BiometricMode(val label: String, val iconName: String) {
    FINGERPRINT("Fingerprint", "Fingerprint"),
    IRIS("Iris Scan", "RemoveRedEye"),
    FACE_RD("Face RD Service", "Face"),
    RFID("Smart Card / RFID", "CreditCard")
}

enum class DeviceVendor(val vendorName: String, val model: String) {
    MANTRA_MFS100("Mantra", "MFS100 Optical Fingerprint Scanner"),
    MORPHO_MSO("Morpho", "MSO1300 E2/E3 Biometric"),
    IRITECH_K7("Iritech", "IriShield-USB MK2120U"),
    STARTEK_FM220("Startek", "FM220U Startek RD"),
    GENERIC_RD("Generic UIDAI", "Aadhaar Face RD Service v2.0")
}

val DEFAULT_ENTRY_POINTS = listOf(
    OfficeEntryPoint("EP001", "Central Gate - Main Block A", 28.613912, 77.209021, 250.0),
    OfficeEntryPoint("EP002", "North Gate - Tech Complex B", 28.614850, 77.208100, 200.0),
    OfficeEntryPoint("EP003", "South Wing - NIC Data Center", 28.612500, 77.210500, 300.0),
    OfficeEntryPoint("EP004", "Regional Office - Test Hub", 28.620000, 77.215000, 500.0)
)
