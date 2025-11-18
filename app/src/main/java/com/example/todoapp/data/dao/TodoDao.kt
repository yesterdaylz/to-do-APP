package com.example.todoapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.data.entity.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    //按截止日期升序排序
    @Query("SELECT * FROM todo ORDER BY DueDay ASC")
    fun getAllTodos(): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(todo: Todo): Long

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Query("SELECT * FROM todo WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Long): Todo?
}