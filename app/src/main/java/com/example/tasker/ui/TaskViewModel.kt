package com.example.tasker.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasker.data.TaskDatabase
import com.example.tasker.data.TaskEntity
import com.example.tasker.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(
        TaskDatabase.getInstance(application).taskDao()
    )

    val tasks = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun addTask(title: String) {
        viewModelScope.launch {
            repository.addTask(title)
        }
    }

    fun toggleDone(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleDone(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun togglePinned(task: TaskEntity, onLimitReached: () -> Unit) {
        viewModelScope.launch {
            val ok = repository.togglePinned(task)
            if (!ok) onLimitReached()
        }
    }

    fun exportTo(uri: Uri, onFailure: () -> Unit) {
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val stream = resolver.openOutputStream(uri)
            if (stream == null) {
                onFailure()
            } else {
                stream.use { repository.exportJson(it) }
            }
        }
    }

    fun importFrom(uri: Uri, onFailure: () -> Unit) {
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val stream = resolver.openInputStream(uri)
            if (stream == null) {
                onFailure()
            } else {
                stream.use { repository.importJson(it) }
            }
        }
    }
}
