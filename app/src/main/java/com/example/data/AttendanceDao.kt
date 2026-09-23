package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for biometric attendance records.
 * Provides reactive Flow queries and asynchronous suspend methods for offline persistence.
 */
@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<BiometricAttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedRecords(): Flow<List<BiometricAttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getRecordsForEmployee(employeeId: String): Flow<List<BiometricAttendanceRecord>>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BiometricAttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: BiometricAttendanceRecord)

    @Query("UPDATE attendance_records SET isSynced = 1, status = :status WHERE id = :id")
    suspend fun markAsSynced(id: Long, status: String = "SUCCESS")

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllRecords()
}
