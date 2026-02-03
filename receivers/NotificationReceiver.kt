package com.drape.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.drape.MainActivity
import com.drape.R
import android.widget.Toast
import androidx.core.graphics.createBitmap
import java.util.Calendar
import kotlin.math.max

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
            return
        }

        showNotification(context)
    }

    companion object {
        private const val CHANNEL_ID = "daily_outfit_channel_high"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 100

        fun showNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Outfit Reminder",
                    NotificationManager.IMPORTANCE_HIGH
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

            val largeIcon = resizeBitmapWithPadding(context, R.drawable.logo)

            val title = "È ora del tuo outfit! 👗"
            val message = "Il tuo look perfetto ti aspetta. Entra nel camerino e crea il tuo stile per oggi!"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH) 
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                Toast.makeText(context, "Permesso notifiche mancante!", Toast.LENGTH_LONG).show()
            }
        }

        fun scheduleDailyNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_YEAR, 1)
                }
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        private fun resizeBitmapWithPadding(context: Context, resId: Int): Bitmap? {
            val originalBitmap = BitmapFactory.decodeResource(context.resources, resId) ?: return null
            
            val size = max(originalBitmap.width, originalBitmap.height)
            val paddedBitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(paddedBitmap)
            
            val padding = (size * 0.2f).toInt()
            
            val safeWidth = size - 2 * padding
            val scale = safeWidth.toFloat() / max(originalBitmap.width, originalBitmap.height)
            
            val scaledWidth = originalBitmap.width * scale
            val scaledHeight = originalBitmap.height * scale
            
            val destLeft = (size - scaledWidth) / 2f
            val destTop = (size - scaledHeight) / 2f
            val destRect = Rect(destLeft.toInt(), destTop.toInt(), (destLeft + scaledWidth).toInt(), (destTop + scaledHeight).toInt())
            
            canvas.drawColor(Color.TRANSPARENT)
            
            val paint = Paint().apply { isFilterBitmap = true }
            canvas.drawBitmap(originalBitmap, null, destRect, paint)
             
            return paddedBitmap
        }
    }
}
