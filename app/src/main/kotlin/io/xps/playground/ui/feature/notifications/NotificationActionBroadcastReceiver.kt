package io.xps.playground.ui.feature.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import io.xps.playground.R
import io.xps.playground.tools.TAG

class NotificationActionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        val notificationId = intent.extras?.getInt(EXTRA_NOTIFICATION_ID) ?: return
        if (intent.action == DISMISS_ACTION) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
        if (intent.action == REPLY_ACTION) {
            val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)
            Log.d(TAG, "Replied $reply $notificationId")

            val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentText("")
                .setTimeoutAfter(500) // Hack to dismiss notification after reply
                .build()

            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            } catch (e: SecurityException) {
            }
        }
    }
}
