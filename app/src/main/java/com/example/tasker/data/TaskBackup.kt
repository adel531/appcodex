package com.example.tasker.data

import kotlinx.serialization.Serializable

@Serializable
data class TaskBackup(
    val title: String,
    val isDone: Boolean = false,
    val isPinned: Boolean = false,
    val isImported: Boolean = false,
    val createdAt: Long
)
