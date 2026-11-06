package com.example.codetrack

import android.content.SharedPreferences
import com.example.codetrack.data.local.security.SecureStorageEngine
import com.example.codetrack.data.local.security.TokenManager
import com.example.codetrack.data.remote.api.AuthApiService
import com.example.codetrack.data.remote.dto.*
import com.example.codetrack.data.repository.AuthRepository
import com.example.codetrack.ui.screens.auth.AuthValidationUtils
import com.example.codetrack.ui.viewmodel.AuthUiState
import com.example.codetrack.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-end integration tests verifying Step 2.4 UI authentication flows:
 * - Login routes to authenticated state on success, halts on failure.
 * - Registration routes to authenticated state on success, halts on 409 conflict.
 * - Forgot & Reset Password workflow with alphanumeric token (without auto-login).
 * - App startup session restoration:
 *   - Success routes to authenticated content.
 *   - No-token startup routes to unauthenticated content.
 *   - Expired JWT (401) wipes session and routes to login.
 *   - Temporary network failure preserves saved token for retry.
 * - Logout wipes session and resets UI states.
 * - Email validation rules.
 */
class AuthFlowIntegrationTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var fakeStorage: TestStorageEngine
    private lateinit var tokenManager: TokenManager
    private lateinit var fakeApi: FakeAuthApiService
    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        fakeStorage = TestStorageEngine()
        tokenManager = TokenManager(fakePrefs, fakeStorage)
        fakeApi = FakeAuthApiService()
        repository = AuthRepository(apiService = fakeApi, tokenManager = tokenManager)
        viewModel = AuthViewModel(
            authRepository = repository,
            externalScope = testScope,
            dispatcher = Dispatchers.Unconfined
        )
    }

    // -------------------------------------------------------------
    // 1. LOGIN TESTS
    // -------------------------------------------------------------

    @Test
    fun `test login success routes to authenticated content`() {
        fakeApi.loginResult = Response.success(
            AuthResponseDto(
                message = "Login successful",
                token = "jwt-active-session-token",
                tokenType = "Bearer",
                userId = 100L,
                name = "Test Developer",
                email = "developer@codetrack.com"
            )
        )

        viewModel.login("developer@codetrack.com", "SecretPass123")

        assertTrue(viewModel.loginState.value is AuthUiState.Success)
        assertTrue(viewModel.sessionState.value is AuthUiState.Success)
        assertTrue(viewModel.isLoggedIn)
        assertEquals("jwt-active-session-token", tokenManager.getToken())
        assertEquals(100L, viewModel.currentSession?.userId)
    }

    @Test
    fun `test login failure does not route to authenticated content`() {
        fakeApi.loginResult = Response.error(
            401,
            """{"message":"Bad credentials","status":401}"""
                .toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.login("developer@codetrack.com", "WrongPassword")

        assertTrue(viewModel.loginState.value is AuthUiState.Error)
        assertEquals("Bad credentials", (viewModel.loginState.value as AuthUiState.Error).message)
        assertFalse(viewModel.isLoggedIn)
        assertNull(tokenManager.getToken())
        // sessionState must NOT transition to Success
        assertFalse(viewModel.sessionState.value is AuthUiState.Success)
    }

    // -------------------------------------------------------------
    // 2. REGISTRATION TESTS
    // -------------------------------------------------------------

    @Test
    fun `test registration success saves session and authenticates`() {
        fakeApi.registerResult = Response.success(
            201,
            AuthResponseDto(
                message = "Account created successfully",
                token = "jwt-registration-token",
                tokenType = "Bearer",
                userId = 200L,
                name = "New Coder",
                email = "new@codetrack.com"
            )
        )

        viewModel.register("New Coder", "new@codetrack.com", "Password88")

        assertTrue(viewModel.registerState.value is AuthUiState.Success)
        assertTrue(viewModel.isLoggedIn)
        assertEquals("jwt-registration-token", tokenManager.getToken())
        assertEquals("New Coder", viewModel.currentSession?.name)
    }

    @Test
    fun `test registration duplicate email (409) does not authenticate`() {
        fakeApi.registerResult = Response.error(
            409,
            """{"message":"An account with this email already exists","status":409}"""
                .toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.register("Existing Coder", "exists@codetrack.com", "Password88")

        assertTrue(viewModel.registerState.value is AuthUiState.Error)
        assertEquals("An account with this email already exists", (viewModel.registerState.value as AuthUiState.Error).message)
        assertFalse(viewModel.isLoggedIn)
        assertNull(tokenManager.getToken())
    }

    // -------------------------------------------------------------
    // 3. FORGOT & RESET PASSWORD TESTS
    // -------------------------------------------------------------

    @Test
    fun `test forgot password success returns backend confirmation`() {
        fakeApi.forgotPasswordResult = Response.success(
            MessageResponseDto("Password reset link sent to your email")
        )

        viewModel.forgotPassword("user@codetrack.com")

        assertTrue(viewModel.forgotPasswordState.value is AuthUiState.Success)
        assertEquals("Password reset link sent to your email", (viewModel.forgotPasswordState.value as AuthUiState.Success).data)
    }

    @Test
    fun `test forgot password failure does not claim token was sent`() {
        fakeApi.forgotPasswordResult = Response.error(
            404,
            """{"message":"User not found with this email","status":404}"""
                .toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.forgotPassword("unknown@codetrack.com")

        assertTrue(viewModel.forgotPasswordState.value is AuthUiState.Error)
        assertEquals("User not found with this email", (viewModel.forgotPasswordState.value as AuthUiState.Error).message)
    }

    @Test
    fun `test reset password with alphanumeric URL-safe token succeeds without auto-login`() {
        val alphanumericToken = "a1b2-c3d4_E5F6-g7h8_XYZ99"
        fakeApi.resetPasswordResult = Response.success(
            MessageResponseDto("Password has been reset successfully. You can now log in.")
        )

        viewModel.resetPassword("user@codetrack.com", alphanumericToken, "BrandNewPassword123")

        assertTrue(viewModel.resetPasswordState.value is AuthUiState.Success)
        assertEquals("Password has been reset successfully. You can now log in.", (viewModel.resetPasswordState.value as AuthUiState.Success).data)

        // Strict Requirement: Reset password must NOT automatically log the user in
        assertFalse(viewModel.isLoggedIn)
        assertNull(tokenManager.getToken())
    }

    @Test
    fun `test reset password with invalid token shows error`() {
        fakeApi.resetPasswordResult = Response.error(
            400,
            """{"message":"Invalid or expired reset token","status":400}"""
                .toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.resetPassword("user@codetrack.com", "expired-token", "BrandNewPassword123")

        assertTrue(viewModel.resetPasswordState.value is AuthUiState.Error)
        assertEquals("Invalid or expired reset token", (viewModel.resetPasswordState.value as AuthUiState.Error).message)
    }

    // -------------------------------------------------------------
    // 4. STARTUP SESSION RESTORATION TESTS
    // -------------------------------------------------------------

    @Test
    fun `test startup session restoration success with valid saved JWT`() {
        tokenManager.saveSession(
            token = "saved-valid-jwt",
            userId = 12L,
            name = "Active User",
            email = "active@codetrack.com"
        )
        fakeApi.meResult = Response.success(
            CurrentUserResponseDto(userId = "12", email = "active@codetrack.com", name = "Active User")
        )

        viewModel.restoreSession()

        assertTrue(viewModel.sessionState.value is AuthUiState.Success)
        val session = (viewModel.sessionState.value as AuthUiState.Success).data
        assertEquals("saved-valid-jwt", session.token)
        assertEquals(12L, session.userId)
        assertTrue(viewModel.isLoggedIn)
    }

    @Test
    fun `test startup with no saved token fails cleanly`() {
        viewModel.restoreSession()

        assertTrue(viewModel.sessionState.value is AuthUiState.Error)
        assertFalse(viewModel.isLoggedIn)
    }

    @Test
    fun `test startup with expired JWT (401 from me) wipes saved session`() {
        tokenManager.saveSession(token = "expired-jwt", userId = 12L)
        assertTrue(tokenManager.isLoggedIn())

        fakeApi.meResult = Response.error(
            401,
            """{"message":"JWT expired"}""".toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.restoreSession()

        assertTrue(viewModel.sessionState.value is AuthUiState.Error)
        assertEquals("Session expired. Please log in again.", (viewModel.sessionState.value as AuthUiState.Error).message)
        // Session must be cleared on 401
        assertFalse(viewModel.isLoggedIn)
        assertNull(tokenManager.getToken())
    }

    @Test
    fun `test startup with temporary network error preserves saved token`() {
        tokenManager.saveSession(token = "offline-valid-jwt", userId = 12L)
        assertTrue(tokenManager.isLoggedIn())

        fakeApi.meException = IOException("Network unreachable / connection timeout")

        viewModel.restoreSession()

        assertTrue(viewModel.sessionState.value is AuthUiState.Error)
        assertEquals("Unable to connect to server. Please check your internet connection.", (viewModel.sessionState.value as AuthUiState.Error).message)

        // Strict Requirement: Token must NOT be deleted on temporary network error!
        assertTrue(tokenManager.isLoggedIn())
        assertEquals("offline-valid-jwt", tokenManager.getToken())
        assertTrue(viewModel.isLoggedIn)
    }

    // -------------------------------------------------------------
    // 5. LOGOUT TESTS
    // -------------------------------------------------------------

    @Test
    fun `test logout clears session and prevents return to authenticated content`() {
        tokenManager.saveSession(token = "user-jwt", userId = 99L, name = "Logged In", email = "user@test.com")
        assertTrue(viewModel.isLoggedIn)

        viewModel.logout()

        assertFalse(viewModel.isLoggedIn)
        assertNull(tokenManager.getToken())
        assertNull(viewModel.currentSession)
        assertEquals(AuthUiState.Idle, viewModel.sessionState.value)
        assertEquals(AuthUiState.Idle, viewModel.loginState.value)
        assertEquals(AuthUiState.Idle, viewModel.registerState.value)
        assertEquals(AuthUiState.Idle, viewModel.forgotPasswordState.value)
        assertEquals(AuthUiState.Idle, viewModel.resetPasswordState.value)
    }

    // -------------------------------------------------------------
    // 6. VALIDATION UTILS TESTS
    // -------------------------------------------------------------

    @Test
    fun `test email validation logic`() {
        assertTrue(AuthValidationUtils.isValidEmail("alice@example.com"))
        assertTrue(AuthValidationUtils.isValidEmail("john.doe@company.org"))
        assertTrue(AuthValidationUtils.isValidEmail("user+tag@domain.co.uk"))

        assertFalse(AuthValidationUtils.isValidEmail(""))
        assertFalse(AuthValidationUtils.isValidEmail("   "))
        assertFalse(AuthValidationUtils.isValidEmail("notanemail"))
        assertFalse(AuthValidationUtils.isValidEmail("missingdomain@"))
        assertFalse(AuthValidationUtils.isValidEmail("@missingusername.com"))
    }

    // -------------------------------------------------------------
    // TEST DOUBLES
    // -------------------------------------------------------------

    private class FakeAuthApiService : AuthApiService {
        var registerResult: Response<AuthResponseDto>? = null
        var registerException: Throwable? = null
        var loginResult: Response<AuthResponseDto>? = null
        var loginException: Throwable? = null
        var forgotPasswordResult: Response<MessageResponseDto>? = null
        var forgotPasswordException: Throwable? = null
        var resetPasswordResult: Response<MessageResponseDto>? = null
        var resetPasswordException: Throwable? = null
        var meResult: Response<CurrentUserResponseDto>? = null
        var meException: Throwable? = null

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            registerException?.let { throw it }
            return registerResult ?: Response.success(
                AuthResponseDto(message = "OK", token = "reg-token", userId = 1L)
            )
        }

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            loginException?.let { throw it }
            return loginResult ?: Response.success(
                AuthResponseDto(message = "OK", token = "login-token", userId = 1L)
            )
        }

        override suspend fun forgotPassword(request: ForgotPasswordRequestDto): Response<MessageResponseDto> {
            forgotPasswordException?.let { throw it }
            return forgotPasswordResult ?: Response.success(
                MessageResponseDto("Instructions dispatched")
            )
        }

        override suspend fun resetPassword(request: ResetPasswordRequestDto): Response<MessageResponseDto> {
            resetPasswordException?.let { throw it }
            return resetPasswordResult ?: Response.success(
                MessageResponseDto("Password reset successfully")
            )
        }

        override suspend fun getCurrentUser(authorizationHeader: String): Response<CurrentUserResponseDto> {
            meException?.let { throw it }
            return meResult ?: Response.success(
                CurrentUserResponseDto(userId = "1", email = "test@example.com", name = "Test User")
            )
        }
    }

    private class TestStorageEngine : SecureStorageEngine {
        private val secretKey = SecretKeySpec(ByteArray(32) { 9 }, "AES")
        private var ivCounter = 1L

        override fun encrypt(plainText: String): String {
            val iv = ByteArray(12)
            iv[0] = (ivCounter++ and 0xFF).toByte()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            return Base64.getEncoder().encodeToString(combined)
        }

        override fun decrypt(cipherTextWithIv: String): String {
            val combined = Base64.getDecoder().decode(cipherTextWithIv)
            if (combined.size < 12 + 16) throw IllegalArgumentException("Invalid payload")
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)
            val cipherText = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, cipherText, 0, cipherText.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            return String(cipher.doFinal(cipherText), Charsets.UTF_8)
        }
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
        override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = Editor(this)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class Editor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val toRemove = mutableSetOf<String>()
            private var clearFlag = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) toRemove.add(key)
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearFlag = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearFlag) {
                    prefs.map.clear()
                    clearFlag = false
                }
                toRemove.forEach { prefs.map.remove(it) }
                toRemove.clear()
                pending.forEach { (k, v) -> if (v != null) prefs.map[k] = v else prefs.map.remove(k) }
                pending.clear()
            }
        }
    }
}
