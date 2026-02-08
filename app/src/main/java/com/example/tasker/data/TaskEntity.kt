package com.example.tasker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean = false,
    val isPinned: Boolean = false,
    val isImported: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
