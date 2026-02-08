package com.example.tasker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun observeIncomplete(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY isPinned DESC, createdAt DESC")
    fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun getIncomplete(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE isPinned = 1")
    suspend fun pinnedCount(): Int

    @Query("SELECT * FROM tasks WHERE title = :title LIMIT 1")
    suspend fun findByTitle(title: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tasks SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)
}
