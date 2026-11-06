package com.example.codetrack

import android.content.SharedPreferences
import com.example.codetrack.data.local.security.SecureStorageEngine
import com.example.codetrack.data.local.security.TokenManager
import com.example.codetrack.data.local.security.UserSession
import com.example.codetrack.data.remote.api.AuthApiService
import com.example.codetrack.data.remote.dto.*
import com.example.codetrack.data.repository.AuthApiException
import com.example.codetrack.data.repository.AuthRepository
import com.example.codetrack.ui.viewmodel.AuthUiState
import com.example.codetrack.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

class AuthRepositoryAndViewModelTest {

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
    // REPOSITORY: LOGIN TESTS
    // -------------------------------------------------------------

    @Test
    fun `login success stores token and returns AuthResponseDto`() = runBlocking {
        fakeApi.loginResult = Response.success(
            AuthResponseDto(
                message = "Login successful",
                token = "jwt-secret-xyz",
                tokenType = "Bearer",
                userId = 101L,
                name = "Alice Developer",
                email = "alice@example.com"
            )
        )

        val result = repository.login("alice@example.com", "Password123")

        assertTrue(result.isSuccess)
        val body = result.getOrNull()
        assertNotNull(body)
        assertEquals("jwt-secret-xyz", body?.token)
        assertEquals(101L, body?.userId)

        // Verify session persistence
        assertTrue(tokenManager.isLoggedIn())
        assertEquals("jwt-secret-xyz", tokenManager.getToken())
        val session = tokenManager.getUserSession()
        assertNotNull(session)
        assertEquals(101L, session?.userId)
        assertEquals("Alice Developer", session?.name)
        assertEquals("alice@example.com", session?.email)
    }

    @Test
    fun `login with blank credentials returns failure immediately without network call`() = runBlocking {
        val result = repository.login("   ", "Password123")
        assertTrue(result.isFailure)
        assertNull(fakeApi.lastLoginRequest)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `login 401 error parses message and does not store token`() = runBlocking {
        val errorJson = """{"message":"Invalid email or password","status":401}"""
        fakeApi.loginResult = Response.error(
            401,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.login("alice@example.com", "WrongPassword")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AuthApiException)
        assertEquals("Invalid email or password", exception?.message)
        assertEquals(401, (exception as AuthApiException).statusCode)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `login 400 validation error with field list joins messages safely`() = runBlocking {
        val errorJson = """
            {
                "status": 400,
                "error": "Bad Request",
                "messages": [
                    {"field": "email", "message": "Email must be well-formed"},
                    {"field": "password", "message": "Password must be at least 8 characters"}
                ]
            }
        """.trimIndent()
        fakeApi.loginResult = Response.error(
            400,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.login("bademail", "short")

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message
        assertTrue(message?.contains("Email must be well-formed") == true)
        assertTrue(message?.contains("Password must be at least 8 characters") == true)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `login network IOException produces safe message and does not save token`() = runBlocking {
        fakeApi.loginException = IOException("Connection refused: 10.0.2.2:8080")

        val result = repository.login("alice@example.com", "Password123")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IOException)
        assertEquals("Unable to connect to server. Please check your internet connection.", exception?.message)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `login response with null token fails safely`() = runBlocking {
        fakeApi.loginResult = Response.success(
            AuthResponseDto(
                message = "Success",
                token = null
            )
        )

        val result = repository.login("alice@example.com", "Password123")

        assertTrue(result.isFailure)
        assertFalse(tokenManager.isLoggedIn())
    }

    // -------------------------------------------------------------
    // REPOSITORY: REGISTER TESTS
    // -------------------------------------------------------------

    @Test
    fun `register success stores session and returns body`() = runBlocking {
        fakeApi.registerResult = Response.success(
            201,
            AuthResponseDto(
                message = "User registered successfully",
                token = "reg-token-456",
                tokenType = "Bearer",
                userId = 202L,
                name = "Bob Coder",
                email = "bob@example.com"
            )
        )

        val result = repository.register("Bob Coder", "bob@example.com", "SecurePass88")

        assertTrue(result.isSuccess)
        assertEquals("reg-token-456", tokenManager.getToken())
        assertEquals(202L, tokenManager.getUserSession()?.userId)
        assertEquals("Bob Coder", tokenManager.getUserSession()?.name)
    }

    @Test
    fun `register 409 conflict returns backend error message`() = runBlocking {
        val errorJson = """{"message":"An account with this email already exists","status":409}"""
        fakeApi.registerResult = Response.error(
            409,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.register("Bob", "bob@example.com", "SecurePass88")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals("An account with this email already exists", exception?.message)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun `register blank field validation fails before network`() = runBlocking {
        val result = repository.register("", "bob@example.com", "Pass")
        assertTrue(result.isFailure)
        assertNull(fakeApi.lastRegisterRequest)
    }

    // -------------------------------------------------------------
    // REPOSITORY: FORGOT & RESET PASSWORD TESTS
    // -------------------------------------------------------------

    @Test
    fun `forgotPassword success returns backend confirmation message`() = runBlocking {
        fakeApi.forgotPasswordResult = Response.success(
            MessageResponseDto("Reset link dispatched to bob@example.com")
        )

        val result = repository.forgotPassword("bob@example.com")

        assertTrue(result.isSuccess)
        assertEquals("Reset link dispatched to bob@example.com", result.getOrNull())
        assertEquals("bob@example.com", fakeApi.lastForgotPasswordRequest?.email)
    }

    @Test
    fun `forgotPassword 404 error returns parsed error message`() = runBlocking {
        val errorJson = """{"message":"No account found with this email","status":404}"""
        fakeApi.forgotPasswordResult = Response.error(
            404,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.forgotPassword("unknown@example.com")

        assertTrue(result.isFailure)
        assertEquals("No account found with this email", result.exceptionOrNull()?.message)
    }

    @Test
    fun `resetPassword sends exact fields and returns confirmation`() = runBlocking {
        fakeApi.resetPasswordResult = Response.success(
            MessageResponseDto("Password reset successfully. You can now log in.")
        )

        val result = repository.resetPassword("bob@example.com", "token-otp-123456", "NewPassword123")

        assertTrue(result.isSuccess)
        assertEquals("bob@example.com", fakeApi.lastResetPasswordRequest?.email)
        assertEquals("token-otp-123456", fakeApi.lastResetPasswordRequest?.token)
        assertEquals("NewPassword123", fakeApi.lastResetPasswordRequest?.newPassword)
    }

    @Test
    fun `resetPassword with invalid token returns error message`() = runBlocking {
        val errorJson = """{"message":"Invalid or expired reset token","status":400}"""
        fakeApi.resetPasswordResult = Response.error(
            400,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.resetPassword("bob@example.com", "bad-token", "NewPassword123")

        assertTrue(result.isFailure)
        assertEquals("Invalid or expired reset token", result.exceptionOrNull()?.message)
    }

    // -------------------------------------------------------------
    // REPOSITORY: CURRENT USER & SESSION RESTORATION TESTS
    // -------------------------------------------------------------

    @Test
    fun `getCurrentUser passes Bearer token and returns profile`() = runBlocking {
        tokenManager.saveSession(token = "my-jwt-token", userId = 5L, name = "Carol", email = "carol@test.com")
        fakeApi.meResult = Response.success(
            CurrentUserResponseDto(userId = "5", email = "carol@test.com", name = "Carol White")
        )

        val result = repository.getCurrentUser()

        assertTrue(result.isSuccess)
        assertEquals("Bearer my-jwt-token", fakeApi.lastMeAuthHeader)
        assertEquals("Carol White", result.getOrNull()?.name)
        assertEquals(5L, result.getOrNull()?.userIdAsLong)
    }

    @Test
    fun `getCurrentUser 401 clears local session immediately`() = runBlocking {
        tokenManager.saveSession(token = "expired-token", userId = 5L)
        assertTrue(tokenManager.isLoggedIn())

        fakeApi.meResult = Response.error(
            401,
            """{"message":"Unauthorized"}""".toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertEquals("Session expired. Please log in again.", result.exceptionOrNull()?.message)
        // Stored session MUST be wiped on 401
        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getToken())
        assertNull(tokenManager.getUserSession())
    }

    @Test
    fun `getCurrentUser network error preserves local session`() = runBlocking {
        tokenManager.saveSession(token = "saved-token-123", userId = 8L, email = "offline@test.com")
        assertTrue(tokenManager.isLoggedIn())

        fakeApi.meException = IOException("Network unreachable")

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        // Stored session MUST be preserved on network failure!
        assertTrue(tokenManager.isLoggedIn())
        assertEquals("saved-token-123", tokenManager.getToken())
    }

    @Test
    fun `getCurrentUser 500 server error preserves local session`() = runBlocking {
        tokenManager.saveSession(token = "saved-token-123", userId = 8L)
        fakeApi.meResult = Response.error(
            500,
            """{"message":"Internal Server Error"}""".toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        // Stored session MUST NOT be cleared on 500
        assertTrue(tokenManager.isLoggedIn())
        assertEquals("saved-token-123", tokenManager.getToken())
    }

    @Test
    fun `restoreSession enriches existing session on successful me validation`() = runBlocking {
        tokenManager.saveSession(token = "valid-token", userId = 10L, name = "Old Name", email = "test@test.com")
        fakeApi.meResult = Response.success(
            CurrentUserResponseDto(userId = "10", email = "test@test.com", name = "Updated Name")
        )

        val result = repository.restoreSession()

        assertTrue(result.isSuccess)
        val restored = result.getOrNull()
        assertEquals("Updated Name", restored?.name)
        assertEquals("valid-token", restored?.token)
        assertEquals("Updated Name", tokenManager.getUserSession()?.name)
    }

    @Test
    fun `restoreSession clears session if me returns 401`() = runBlocking {
        tokenManager.saveSession(token = "old-expired-token", userId = 10L)
        fakeApi.meResult = Response.error(
            401,
            """{"message":"Token expired"}""".toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repository.restoreSession()

        assertTrue(result.isFailure)
        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getToken())
    }

    @Test
    fun `restoreSession preserves session if me throws network IOException`() = runBlocking {
        tokenManager.saveSession(token = "valid-local-token", userId = 10L)
        fakeApi.meException = IOException("DNS lookup failed")

        val result = repository.restoreSession()

        assertTrue(result.isFailure)
        // Crucial test requirement: Preservation of token on network/server errors during session restoration
        assertTrue(tokenManager.isLoggedIn())
        assertEquals("valid-local-token", tokenManager.getToken())
    }

    @Test
    fun `logout clears token and session completely`() {
        tokenManager.saveSession(token = "active-token", userId = 44L, name = "Dave", email = "dave@test.com")
        assertTrue(repository.isLoggedIn())

        repository.logout()

        assertFalse(repository.isLoggedIn())
        assertNull(repository.getStoredSession())
        assertNull(tokenManager.getToken())
    }

    // -------------------------------------------------------------
    // VIEWMODEL TESTS
    // -------------------------------------------------------------

    @Test
    fun `viewModel login transitions Idle to Success and updates sessionState`() {
        assertEquals(AuthUiState.Idle, viewModel.loginState.value)
        assertEquals(AuthUiState.Idle, viewModel.sessionState.value)

        fakeApi.loginResult = Response.success(
            AuthResponseDto(
                message = "Success",
                token = "jwt-vm-token",
                userId = 77L,
                name = "VM User",
                email = "vm@test.com"
            )
        )

        viewModel.login("vm@test.com", "Password123")

        assertTrue(viewModel.loginState.value is AuthUiState.Success)
        val response = (viewModel.loginState.value as AuthUiState.Success).data
        assertEquals("jwt-vm-token", response.token)

        assertTrue(viewModel.sessionState.value is AuthUiState.Success)
        val session = (viewModel.sessionState.value as AuthUiState.Success).data
        assertEquals("jwt-vm-token", session.token)
        assertEquals(77L, session.userId)
    }

    @Test
    fun `viewModel login failure transitions to Error`() {
        val errorJson = """{"message":"Bad credentials","status":401}"""
        fakeApi.loginResult = Response.error(
            401,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        viewModel.login("vm@test.com", "Wrong")

        assertTrue(viewModel.loginState.value is AuthUiState.Error)
        assertEquals("Bad credentials", (viewModel.loginState.value as AuthUiState.Error).message)
    }

    @Test
    fun `viewModel prevents duplicate submission while Loading`() {
        // Manually inspect duplicate click protection
        var executionCount = 0
        val countingApi = object : AuthApiService by fakeApi {
            override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
                executionCount++
                return Response.success(
                    AuthResponseDto(message = "OK", token = "token-$executionCount", userId = 1L)
                )
            }
        }
        val countingRepo = AuthRepository(apiService = countingApi, tokenManager = tokenManager)

        // Create a custom scope that does not run immediately
        val manualVm = AuthViewModel(
            authRepository = countingRepo,
            externalScope = CoroutineScope(Dispatchers.Unconfined),
            dispatcher = Dispatchers.Unconfined
        )

        // Set state to Loading to simulate a flight in progress
        val stateField = AuthViewModel::class.java.getDeclaredField("_loginState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val mutableFlow = stateField.get(manualVm) as kotlinx.coroutines.flow.MutableStateFlow<AuthUiState<AuthResponseDto>>
        mutableFlow.value = AuthUiState.Loading

        // Attempt another login while Loading
        manualVm.login("test@test.com", "pass")

        // Should not have executed because state was already Loading
        assertEquals(0, executionCount)
    }

    @Test
    fun `viewModel state resets clear to Idle`() {
        fakeApi.forgotPasswordResult = Response.success(MessageResponseDto("Instructions sent"))
        viewModel.forgotPassword("user@test.com")
        assertTrue(viewModel.forgotPasswordState.value is AuthUiState.Success)

        viewModel.resetForgotPasswordState()
        assertEquals(AuthUiState.Idle, viewModel.forgotPasswordState.value)

        fakeApi.resetPasswordResult = Response.success(MessageResponseDto("Password reset"))
        viewModel.resetPassword("user@test.com", "token123", "NewPass99")
        assertTrue(viewModel.resetPasswordState.value is AuthUiState.Success)

        viewModel.resetResetPasswordState()
        assertEquals(AuthUiState.Idle, viewModel.resetPasswordState.value)
    }

    @Test
    fun `viewModel logout resets all state flows to Idle and wipes session`() {
        fakeApi.loginResult = Response.success(
            AuthResponseDto(message = "OK", token = "token-1", userId = 1L, name = "A", email = "a@a.com")
        )
        viewModel.login("a@a.com", "pass")
        assertTrue(viewModel.loginState.value is AuthUiState.Success)
        assertTrue(viewModel.sessionState.value is AuthUiState.Success)

        viewModel.logout()

        assertEquals(AuthUiState.Idle, viewModel.loginState.value)
        assertEquals(AuthUiState.Idle, viewModel.sessionState.value)
        assertEquals(AuthUiState.Idle, viewModel.registerState.value)
        assertEquals(AuthUiState.Idle, viewModel.forgotPasswordState.value)
        assertEquals(AuthUiState.Idle, viewModel.resetPasswordState.value)
        assertFalse(viewModel.isLoggedIn)
        assertNull(viewModel.currentSession)
    }

    // -------------------------------------------------------------
    // TEST DOUBLES
    // -------------------------------------------------------------

    private class FakeAuthApiService : AuthApiService {
        var registerResult: Response<AuthResponseDto>? = null
        var registerException: Throwable? = null
        var lastRegisterRequest: RegisterRequestDto? = null

        var loginResult: Response<AuthResponseDto>? = null
        var loginException: Throwable? = null
        var lastLoginRequest: LoginRequestDto? = null

        var forgotPasswordResult: Response<MessageResponseDto>? = null
        var forgotPasswordException: Throwable? = null
        var lastForgotPasswordRequest: ForgotPasswordRequestDto? = null

        var resetPasswordResult: Response<MessageResponseDto>? = null
        var resetPasswordException: Throwable? = null
        var lastResetPasswordRequest: ResetPasswordRequestDto? = null

        var meResult: Response<CurrentUserResponseDto>? = null
        var meException: Throwable? = null
        var lastMeAuthHeader: String? = null

        override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
            lastRegisterRequest = request
            registerException?.let { throw it }
            return registerResult ?: Response.success(
                AuthResponseDto(
                    message = "Registered",
                    token = "default-token",
                    tokenType = "Bearer",
                    userId = 1L,
                    name = request.name,
                    email = request.email
                )
            )
        }

        override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
            lastLoginRequest = request
            loginException?.let { throw it }
            return loginResult ?: Response.success(
                AuthResponseDto(
                    message = "Login success",
                    token = "default-token",
                    tokenType = "Bearer",
                    userId = 1L,
                    name = "Default User",
                    email = request.email
                )
            )
        }

        override suspend fun forgotPassword(request: ForgotPasswordRequestDto): Response<MessageResponseDto> {
            lastForgotPasswordRequest = request
            forgotPasswordException?.let { throw it }
            return forgotPasswordResult ?: Response.success(
                MessageResponseDto("Instructions dispatched")
            )
        }

        override suspend fun resetPassword(request: ResetPasswordRequestDto): Response<MessageResponseDto> {
            lastResetPasswordRequest = request
            resetPasswordException?.let { throw it }
            return resetPasswordResult ?: Response.success(
                MessageResponseDto("Password reset")
            )
        }

        override suspend fun getCurrentUser(authorizationHeader: String): Response<CurrentUserResponseDto> {
            lastMeAuthHeader = authorizationHeader
            meException?.let { throw it }
            return meResult ?: Response.success(
                CurrentUserResponseDto(userId = "1", email = "test@example.com", name = "Test User")
            )
        }
    }

    private class TestStorageEngine : SecureStorageEngine {
        private val secretKey = SecretKeySpec(ByteArray(32) { 5 }, "AES")
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
