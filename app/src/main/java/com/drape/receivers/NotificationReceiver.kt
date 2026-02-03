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
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
            return
        }

        showNotification(context)
        // Since it's a daily repeating alarm set via setInexactRepeating, 
        // we don't strictly need to reschedule it manually here if the device hasn't rebooted.
        // However, if we used setExactAndAllowWhileIdle due to doze mode, we would need to reschedule.
        // For this implementation, AlarmManager.INTERVAL_DAY handles the repeat.
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


            // Decode and pad the logo to ensure it's fully visible inside the circle crop
            val largeIcon = resizeBitmapWithPadding(context, R.drawable.logo)

            val title = "È ora del tuo outfit! 👗"
            val message = "Il tuo look perfetto ti aspetta. Entra nel camerino e crea il tuo stile per oggi!"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(largeIcon) // Add Large Icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Expandable text
                .setPriority(NotificationCompat.PRIORITY_HIGH) 
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // Check permission before notifying (although usually not needed if granted, good practice to catch errors)
            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
                // Optional: Show toast for testing confirmation
                // Toast.makeText(context, "Notifica inviata!", Toast.LENGTH_SHORT).show()
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

            // Set the alarm to start at approximately 8:00 PM (20:00) or current time + small delay if testing
            // For production: Set to next occurrence of a specific time, e.g., 20:00
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // If 20:00 has already passed today, schedule for tomorrow
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

        private fun resizeBitmapWithPadding(context: Context, resId: Int): Bitmap? {
            val originalBitmap = BitmapFactory.decodeResource(context.resources, resId) ?: return null
            
            // Create a square bitmap
            val size = Math.max(originalBitmap.width, originalBitmap.height)
            val paddedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(paddedBitmap)
            
            // Calculate padding (approx 15-20% to fit in circle)
            val padding = (size * 0.2f).toInt()
            
            // Draw original bitmap in center
            // If original is not square, center it
            val left = (size - originalBitmap.width) / 2f
            val top = (size - originalBitmap.height) / 2f
            
            // Scale down if needed to fit within padding (safe zone)
            // Safe zone width = size - 2*padding
            val safeWidth = size - 2 * padding
            val scale = safeWidth.toFloat() / Math.max(originalBitmap.width, originalBitmap.height)
            
            val scaledWidth = originalBitmap.width * scale
            val scaledHeight = originalBitmap.height * scale
            
            val destLeft = (size - scaledWidth) / 2f
            val destTop = (size - scaledHeight) / 2f
            val destRect = Rect(destLeft.toInt(), destTop.toInt(), (destLeft + scaledWidth).toInt(), (destTop + scaledHeight).toInt())
            
            // Draw transparent background (optional, default is transparent)
             canvas.drawColor(Color.TRANSPARENT)
            
            val paint = Paint().apply { isFilterBitmap = true }
             canvas.drawBitmap(originalBitmap, null, destRect, paint)
             
             return paddedBitmap
        }
    }
}
