package com.example.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object ThreatNotificationHelper {
    private const val CHANNEL_ID = "brandshield_critical_threats"
    private const val CHANNEL_NAME = "Critical Digital Risk Alerts"
    private const val CHANNEL_DESC = "Real-time alerts when BrandShield AI detects threats with Risk Score >= 81"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Dispatches a high-priority Android Notification formatted per Section 37 & 38:
     * "⚠ Critical Threat — Possible ABC Bank impersonation detected. Risk Score: 94. Tap to investigate."
     */
    fun sendCriticalThreatNotification(
        context: Context,
        threatId: String,
        brandName: String,
        platform: String,
        targetIdentifier: String,
        riskScore: Int,
        summary: String
    ): Boolean {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_THREAT_ID", threatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            threatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠ Critical Threat ($platform • Risk: $riskScore)")
            .setContentText("Possible $brandName impersonation: $targetIdentifier")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Possible $brandName impersonation detected on $platform ($targetIdentifier).\n" +
                        "Risk Score: $riskScore / 100 (CRITICAL)\n" +
                        "$summary\nTap to investigate."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(threatId.hashCode(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
