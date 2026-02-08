package com.example.tasker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class TaskRepository(private val dao: TaskDao) {
    val tasks = dao.observeAll()
    val incompleteTasks = dao.observeIncomplete()

    suspend fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        dao.insert(TaskEntity(title = trimmed))
    }

    suspend fun toggleDone(task: TaskEntity) {
        dao.update(task.copy(isDone = !task.isDone))
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteById(task.id)
    }

    suspend fun togglePinned(task: TaskEntity): Boolean {
        if (!task.isPinned && dao.pinnedCount() >= 2) return false
        dao.update(task.copy(isPinned = !task.isPinned))
        return true
    }

    suspend fun markDone(taskId: Long) {
        dao.markDone(taskId)
    }

    suspend fun exportJson(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val items = dao.getAll()
        val payload = items.map {
            TaskBackup(
                title = it.title,
                isDone = it.isDone,
                isPinned = it.isPinned,
                isImported = it.isImported,
                createdAt = it.createdAt
            )
        }
        outputStream.bufferedWriter().use { writer ->
            writer.write(Json.encodeToString(payload))
        }
    }

    suspend fun importJson(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val text = inputStream.bufferedReader().use { it.readText() }
        val items = Json.decodeFromString<List<TaskBackup>>(text)
        items.forEach { item ->
            val existing = dao.findByTitle(item.title)
            if (existing == null) {
                dao.insert(
                    TaskEntity(
                        title = item.title,
                        isDone = item.isDone,
                        isPinned = item.isPinned,
                        isImported = true,
                        createdAt = item.createdAt
                    )
                )
            }
        }
    }
}
