package com.guardian.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.guardian.app.ui.theme.GuardianTheme
import com.guardian.app.ui.theme.GxBase
import com.guardian.app.ui.theme.GxBorder
import com.guardian.app.ui.theme.GxDanger
import com.guardian.app.ui.theme.GxDangerSoft
import com.guardian.app.ui.theme.GxPrimary
import com.guardian.app.ui.theme.GxPrimaryGlow
import com.guardian.app.ui.theme.GxSafe
import com.guardian.app.ui.theme.GxSurface
import com.guardian.app.ui.theme.GxSurfaceAlt
import com.guardian.app.ui.theme.GxTextHi
import com.guardian.app.ui.theme.GxTextLo
import com.guardian.app.ui.theme.GxTextMid
import com.guardian.app.ui.theme.GxVoid
import com.guardian.app.ui.theme.GxWarning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.guardian.app.auth.SupabaseAuthManager
import com.guardian.app.device.DeviceRegistrationCoordinator
import com.guardian.app.ui.guardians.VerificationNotificationIntentParser
import com.guardian.app.ui.guardians.VerificationResponseScreen

class MainActivity : ComponentActivity() {
    private var pendingVerificationSessionId by mutableStateOf<String?>(null)

    private val protectionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_PHONE_STATE] == true) {
                try {
                    startService(Intent(this, CallMonitorService::class.java))
                } catch (_: Exception) {}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Route cold-start identity verification intent if present
        val initialRoute = VerificationNotificationIntentParser.parse(intent)
        if (initialRoute != null) {
            pendingVerificationSessionId = initialRoute.sessionId
        }

        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.w("GuardianFCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                android.util.Log.d("GuardianFCM", "FCM token available")
                com.guardian.app.device.DeviceIdentityStore.savePendingFcmToken(applicationContext, token)
                attemptDeviceRegistration()
            }
        } catch (e: Exception) {
            android.util.Log.e("GuardianFCM", "Firebase messaging init fallback", e)
        }

        attemptDeviceRegistration()
        setContent {
            GuardianTheme {
                val verificationId = pendingVerificationSessionId
                if (verificationId != null) {
                    VerificationResponseScreen(
                        sessionId = verificationId,
                        onDismiss = { pendingVerificationSessionId = null }
                    )
                } else {
                    GuardianApp(
                        onOpenCallRisk = {
                            startActivity(Intent(this, CallRiskActivity::class.java))
                        },
                        onOpenQrScanner = {
                            startActivity(Intent(this, QrScannerActivity::class.java))
                        },
                        onPhoneProtectionToggle = { enabled ->
                            if (enabled) {
                                protectionPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                )
                            } else {
                                try {
                                    stopService(Intent(this, CallMonitorService::class.java))
                                } catch (_: Exception) {}
                            }
                        },
                        onMessageProtectionToggle = { enabled ->
                            if (enabled) {
                                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            } else {
                                try {
                                    stopService(Intent(this, MessageListenerService::class.java))
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val route = VerificationNotificationIntentParser.parse(intent)
        if (route != null) {
            pendingVerificationSessionId = route.sessionId
        }
    }

    private fun attemptDeviceRegistration() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val authManager = SupabaseAuthManager()
                val authResult = authManager.ensureAuthenticated()
                if (authResult.isSuccess) {
                    val regResult = DeviceRegistrationCoordinator.registerCurrentTokenIfAvailable(
                        context = applicationContext,
                        authManager = authManager
                    )
                    if (regResult.isFailure) {
                        android.util.Log.w("DeviceReg", "Pending FCM registration deferred: ${regResult.exceptionOrNull()?.message}")
                    }
                } else {
                    android.util.Log.d("DeviceReg", "Auth deferred for registration: ${authResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.w("DeviceReg", "Device registration attempt failed safely", e)
            }
        }
    }
}

// Design Token Aliases for backward-compatibility
val DarkBackground = GxBase
val DarkSurface = GxSurface
val DarkSurfaceElevated = GxSurfaceAlt
val DarkSurfaceVariant = GxSurfaceAlt
val CyberEmerald = GxSafe
val CyberEmeraldGlow = GxPrimaryGlow
val ElectricIndigo = GxPrimary
val CoralRed = GxDanger
val AmberWarning = GxWarning
val TextPrimary = GxTextHi
val TextSecondary = GxTextMid
val TextMuted = GxTextLo
val BorderSubtle = GxBorder