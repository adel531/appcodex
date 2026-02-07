package com.example.tasker.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.tasker.R
import com.example.tasker.data.TaskDatabase
import com.example.tasker.data.TaskEntity

class TaskWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks: List<TaskEntity> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        tasks = TaskDatabase.getInstance(context).taskDao().getIncomplete()
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        views.setTextViewText(R.id.widget_task_title, task.title)
        if (task.isImported) {
            views.setTextViewText(R.id.widget_task_imported, "i")
        } else {
            views.setTextViewText(R.id.widget_task_imported, "")
        }

        val fillIntent = Intent().apply {
            putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_task_row, fillIntent)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasks[position].id

    override fun hasStableIds(): Boolean = true
}
