package com.example.tasker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.tasker.MainActivity
import com.example.tasker.R
import com.example.tasker.data.TaskDatabase
import com.example.tasker.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val intent = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val views = RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                setRemoteAdapter(R.id.widget_list, intent)
                setEmptyView(R.id.widget_list, R.id.widget_empty)

                val openIntent = Intent(context, MainActivity::class.java)
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_header, openPendingIntent)

                val templateIntent = Intent(context, TaskWidgetProvider::class.java).apply {
                    action = ACTION_MARK_DONE
                }
                val templatePendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    templateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setPendingIntentTemplate(R.id.widget_list, templatePendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MARK_DONE) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
            if (taskId != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = TaskDatabase.getInstance(context).taskDao()
                    TaskRepository(dao).markDone(taskId)
                    val manager = AppWidgetManager.getInstance(context)
                    val widgetIds = manager.getAppWidgetIds(
                        ComponentName(context, TaskWidgetProvider::class.java)
                    )
                    manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_list)
                }
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "com.example.tasker.widget.ACTION_MARK_DONE"
        const val EXTRA_TASK_ID = "com.example.tasker.widget.EXTRA_TASK_ID"
    }
}
