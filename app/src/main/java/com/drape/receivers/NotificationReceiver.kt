package com.drape.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.drape.MainActivity
import com.drape.R
import com.drape.data.repository.PlannedDaysRepository
import com.drape.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var plannedDaysRepository: PlannedDaysRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val today = DateUtils.today()
                // Check if user has planned anything for today
                val plannedDay = plannedDaysRepository.getPlannedDay(today)
                val hasOutfit = plannedDay != null && plannedDay.items.isNotEmpty()

                if (!hasOutfit) {
                    showNotification(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "daily_outfit_channel_high" // Changed ID to force update settings
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 100

        fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create channel (safe to call repeatedly)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Outfit Reminder",
                    NotificationManager.IMPORTANCE_HIGH // Increased importance
                ).apply {
                    description = "Reminds you to prepare your outfit every day"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val title = "Hai scelto il tuo outfit per oggi? 👗"
            val message = "Il tuo planner è ancora vuoto! Aggiungi un look e inizia la giornata con stile ✨"

            // Custom View with Logo on the Left
            val remoteViews = RemoteViews(context.packageName, R.layout.notification_custom)
            remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.logo)
            remoteViews.setTextViewText(R.id.notification_title, title)
            remoteViews.setTextViewText(R.id.notification_text, message)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Il tuo planner è ancora vuoto.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // Check permission before notifying (although usually not needed if granted, good practice to catch errors)
            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                Toast.makeText(context, "Permesso notifiche mancante!", Toast.LENGTH_LONG).show()
            }
        }

        @JvmStatic
        fun scheduleDailyNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Set the alarm to start at approximately 11:00 AM
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)

                // If 11:00 has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Using setInexactRepeating for battery efficiency
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
