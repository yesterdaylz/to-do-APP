package com.example.todoapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.todoapp.data.entity.TimeRecord
import kotlinx.coroutines.flow.Flow
@Dao
interface TimeRecordDao {
    @Insert
    suspend fun insert(timeRecord : TimeRecord): Long
    @Query("SELECT *FROM time_record WHERE username =:username ORDER BY startTime ASC ")
    fun  getAllRecord(username :String): Flow<List<TimeRecord>>
}