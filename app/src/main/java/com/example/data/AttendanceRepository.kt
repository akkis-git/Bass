package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository bridging the DAO and ViewModels for biometric attendance data.
 * Abstracts local Room database operations for offline queuing and synchronization.
 */
class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    val allRecords: Flow<List<BiometricAttendanceRecord>> = attendanceDao.getAllRecords()
    val unsyncedRecords: Flow<List<BiometricAttendanceRecord>> = attendanceDao.getUnsyncedRecords()
    val unsyncedCount: Flow<Int> = attendanceDao.getUnsyncedCount()
    val totalCount: Flow<Int> = attendanceDao.getTotalCount()

    fun getRecordsForEmployee(employeeId: String): Flow<List<BiometricAttendanceRecord>> {
        return attendanceDao.getRecordsForEmployee(employeeId)
    }

    suspend fun insertRecord(record: BiometricAttendanceRecord): Long {
        return attendanceDao.insertRecord(record)
    }

    suspend fun markAsSynced(id: Long, status: String = "SUCCESS") {
        attendanceDao.markAsSynced(id, status)
    }

    suspend fun updateRecord(record: BiometricAttendanceRecord) {
        attendanceDao.updateRecord(record)
    }

    suspend fun deleteById(id: Long) {
        attendanceDao.deleteRecordById(id)
    }

    suspend fun clearAll() {
        attendanceDao.clearAllRecords()
    }
}
