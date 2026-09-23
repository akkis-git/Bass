package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a captured biometric attendance record and its metadata.
 * Stores local attendance history and supports offline queuing & synchronization.
 */
@Entity(tableName = "attendance_records")
data class BiometricAttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val biometricType: String, // e.g., "FINGERPRINT", "IRIS", "RFID"
    val deviceVendor: String, // e.g., "MORPHO", "MANTRA", "IRITECH"
    val deviceId: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSynced: Boolean = false,
    val pidBlock: String? = null, // Encrypted Aadhaar PID block XML
    val status: String = "PENDING_SYNC" // e.g., "SUCCESS", "PENDING_SYNC", "FAILED"
)
