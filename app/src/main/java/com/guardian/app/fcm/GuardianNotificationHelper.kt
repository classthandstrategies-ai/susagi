package com.guardian.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.guardian.app.FamilyAlertDetailActivity
import com.guardian.app.MainActivity

object GuardianNotificationHelper {

    private const val CHANNEL_ID = "guardian_family_alerts"
    private const val CHANNEL_NAME = "Family Scam Alerts"
    private const val NOTIFICATION_ID = 100

    private const val CHANNEL_ID_VERIFICATION = "guardian_identity_checks"
    private const val CHANNEL_NAME_VERIFICATION = "Identity Checks"
    private const val NOTIFICATION_ID_VERIFICATION_BASE = 2000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a family member is targeted by a scam"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun createVerificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_VERIFICATION,
                CHANNEL_NAME_VERIFICATION,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications for identity verification requests"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showFamilyAlert(
        context: Context,
        alertType: String,
        protectedUserName: String,
        riskScore: Int,
        scamType: String,
        callerNumber: String,
        transcriptSummary: String
    ) {
        createChannel(context)

        // Intent to open the detailed report
        val detailIntent = Intent(context, FamilyAlertDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("protected_user", protectedUserName)
            putExtra("risk_score", riskScore)
            putExtra("scam_type", scamType)
            putExtra("caller_number", callerNumber)
            putExtra("transcript_summary", transcriptSummary)
            putExtra("alert_type", alertType)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Title and body based on alert type
        val (title, body) = when (alertType) {
            "scam_call" -> "⚠️ Scam call detected" to
                    "$protectedUserName is on a call with a suspected scammer. Risk: $riskScore%"
            "scam_sms" -> "⚠️ Scam SMS detected" to
                    "$protectedUserName received a suspicious message. Risk: $riskScore%"
            "scam_link" -> "⚠️ Scam link detected" to
                    "$protectedUserName tapped a dangerous link. Risk: $riskScore%"
            "scam_qr" -> "⚠️ Scam QR detected" to
                    "$protectedUserName scanned a malicious QR code. Risk: $riskScore%"
            else -> "⚠️ Scam detected" to
                    "$protectedUserName may be targeted. Risk: $riskScore%"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$body\n\nScam type: $scamType\nCaller: $callerNumber\n\nTap for full report."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Renders a high-priority identity verification notification for incoming verification requests.
     */
    fun showVerificationAlert(
        context: Context,
        payload: VerificationPushPayload
    ) {
        createVerificationChannel(context)

        // Notification tap intent points to MainActivity with deep-link extras
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("verificationSessionId", payload.sessionId)
            putExtra("source", "identity_verification")
            putExtra("claimedIdentity", payload.claimedIdentity)
            putExtra("expiresAt", payload.expiresAt)
            payload.requestedAction?.let { putExtra("requestedAction", it) }
        }

        val notificationId = NOTIFICATION_ID_VERIFICATION_BASE +
            (payload.sessionId.hashCode() and 0x7FFFFFFF % 1000)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Identity check requested"
        val body = if (!payload.requestedAction.isNullOrBlank()) {
            "Someone is asking you to verify ${payload.claimedIdentity} for ${payload.requestedAction}"
        } else {
            "Someone is asking you to verify ${payload.claimedIdentity}"
        }

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID_VERIFICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Identity check requested")
            .setContentText("A contact requested identity verification")
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_VERIFICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
