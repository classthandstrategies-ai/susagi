package com.guardian.app.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Await helper for Firebase Tasks ensuring clean coroutine cancellation.
 */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

/**
 * Abstraction layer over Firebase Authentication.
 *
 * Responsibilities:
 * - Expose authenticated Firebase UID.
 * - Ensure an authenticated user exists (via anonymous auth for V1 sprint).
 * - Retrieve valid Firebase ID tokens for authenticated backend requests.
 * - Explicitly report all authentication failures.
 *
 * Independent of Compose/UI.
 */
class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Ensures an authenticated user exists. If already signed in, returns existing UID.
     * Otherwise, signs in anonymously.
     */
    suspend fun ensureAuthenticated(): Result<String> {
        val user = auth.currentUser
        if (user != null && user.uid.isNotBlank()) {
            return Result.success(user.uid)
        }
        return signInAnonymously()
    }

    /**
     * Signs in anonymously to obtain a real Firebase UID.
     */
    suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().awaitTask()
            val user = result.user
            if (user != null && user.uid.isNotBlank()) {
                Result.success(user.uid)
            } else {
                Result.failure(IllegalStateException("FirebaseUser was null after anonymous sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a valid Firebase ID token for authenticating against backend endpoints.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("No authenticated Firebase user found"))
        return try {
            val tokenResult = user.getIdToken(forceRefresh).awaitTask()
            val token = tokenResult.token
            if (!token.isNullOrBlank()) {
                Result.success(token)
            } else {
                Result.failure(IllegalStateException("Retrieved null or empty Firebase ID token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut() {
        auth.signOut()
    }
}
