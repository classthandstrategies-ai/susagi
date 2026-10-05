package com.guardian.app.device

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DeviceRegistrationCoordinatorTest {

    @Test
    fun testNoPendingToken_returnsSuccessFalse_andDoesNotCallBackend() = runBlocking {
        var registerCalled = false
        var successMarked = false

        val result = DeviceRegistrationCoordinator.registerToken(
            token = null,
            userId = "user_123",
            deviceId = "device_uuid_456",
            registerCall = { _, _ ->
                registerCalled = true
                Result.success(true)
            },
            onSuccess = {
                successMarked = true
            }
        )

        assertTrue("Empty or null token must return success(false)", result.isSuccess)
        assertFalse("No registration should have taken place", result.getOrThrow())
        assertFalse("Register backend call should not have been made", registerCalled)
        assertFalse("Token should not have been marked synced", successMarked)
    }

    @Test
    fun testBlankPendingToken_returnsSuccessFalse_andDoesNotCallBackend() = runBlocking {
        var registerCalled = false

        val result = DeviceRegistrationCoordinator.registerToken(
            token = "   ",
            userId = "user_123",
            deviceId = "device_uuid_456",
            registerCall = { _, _ ->
                registerCalled = true
                Result.success(true)
            },
            onSuccess = {}
        )

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        assertFalse(registerCalled)
    }

    @Test
    fun testPendingToken_unauthenticated_returnsFailure_andDoesNotCallBackend() = runBlocking {
        var registerCalled = false
        var successMarked = false

        val result = DeviceRegistrationCoordinator.registerToken(
            token = "fcm_pending_token_abc",
            userId = null,
            deviceId = "device_uuid_456",
            registerCall = { _, _ ->
                registerCalled = true
                Result.success(true)
            },
            onSuccess = {
                successMarked = true
            }
        )

        assertTrue("Unauthenticated attempt must result in failure", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("Unauthenticated: Supabase user required for device registration", result.exceptionOrNull()?.message)
        assertFalse("Backend must not be called without authenticated user", registerCalled)
        assertFalse("Token must not be marked synced", successMarked)
    }

    @Test
    fun testPendingToken_authenticated_successfulRegistration_marksSuccess() = runBlocking {
        var capturedDeviceId: String? = null
        var capturedToken: String? = null
        var syncedToken: String? = null

        val result = DeviceRegistrationCoordinator.registerToken(
            token = "fcm_pending_token_abc",
            userId = "user_123",
            deviceId = "device_uuid_456",
            registerCall = { devId, tok ->
                capturedDeviceId = devId
                capturedToken = tok
                Result.success(true)
            },
            onSuccess = { tok ->
                syncedToken = tok
            }
        )

        assertTrue("Registration must succeed", result.isSuccess)
        assertTrue("Coordinator must return true on success", result.getOrThrow())
        assertEquals("device_uuid_456", capturedDeviceId)
        assertEquals("fcm_pending_token_abc", capturedToken)
        assertEquals("fcm_pending_token_abc", syncedToken)
    }

    @Test
    fun testRegistrationFailure_doesNotCrash_retainsPending_andReturnsFailure() = runBlocking {
        var successMarked = false

        val result = DeviceRegistrationCoordinator.registerToken(
            token = "fcm_pending_token_abc",
            userId = "user_123",
            deviceId = "device_uuid_456",
            registerCall = { _, _ ->
                Result.failure(IOException("Network unreachable"))
            },
            onSuccess = {
                successMarked = true
            }
        )

        assertTrue("Backend failure must result in failure", result.isFailure)
        assertEquals("Network unreachable", result.exceptionOrNull()?.message)
        assertFalse("Token must NOT be marked synced on failure, remaining retryable", successMarked)
    }

    @Test
    fun testRegistrationBackendReturnsFalse_doesNotMarkSynced() = runBlocking {
        var successMarked = false

        val result = DeviceRegistrationCoordinator.registerToken(
            token = "fcm_pending_token_abc",
            userId = "user_123",
            deviceId = "device_uuid_456",
            registerCall = { _, _ ->
                Result.success(false)
            },
            onSuccess = {
                successMarked = true
            }
        )

        assertFalse("Unsuccessful backend response must not report success", result.getOrThrow())
        assertFalse("Token must not be marked synced", successMarked)
    }
}
