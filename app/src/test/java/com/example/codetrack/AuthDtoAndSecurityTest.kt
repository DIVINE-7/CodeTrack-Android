package com.example.codetrack

import android.content.SharedPreferences
import com.example.codetrack.data.local.security.SecureStorageEngine
import com.example.codetrack.data.local.security.TokenManager
import com.example.codetrack.data.remote.dto.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AuthDtoAndSecurityTest {

    private val gson = Gson()

    // -------------------------------------------------------------
    // DTO SERIALIZATION & DESERIALIZATION TESTS
    // -------------------------------------------------------------

    @Test
    fun `test RegisterRequestDto serializes exact JSON keys`() {
        val request = RegisterRequestDto(
            name = "Test User",
            email = "user@test.com",
            password = "SecretPassword123"
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"name\":\"Test User\""))
        assertTrue(json.contains("\"email\":\"user@test.com\""))
        assertTrue(json.contains("\"password\":\"SecretPassword123\""))
    }

    @Test
    fun `test LoginRequestDto serializes exact JSON keys`() {
        val request = LoginRequestDto(
            email = "user@test.com",
            password = "SecretPassword123"
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"email\":\"user@test.com\""))
        assertTrue(json.contains("\"password\":\"SecretPassword123\""))
    }

    @Test
    fun `test ForgotPasswordRequestDto serializes exact JSON keys`() {
        val request = ForgotPasswordRequestDto(email = "user@test.com")
        val json = gson.toJson(request)

        assertTrue(json.contains("\"email\":\"user@test.com\""))
    }

    @Test
    fun `test ResetPasswordRequestDto serializes exact JSON keys including newPassword`() {
        val request = ResetPasswordRequestDto(
            email = "user@test.com",
            token = "reset-token-xyz-123",
            newPassword = "NewSecretPassword456"
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"email\":\"user@test.com\""))
        assertTrue(json.contains("\"token\":\"reset-token-xyz-123\""))
        assertTrue(json.contains("\"newPassword\":\"NewSecretPassword456\""))
    }

    @Test
    fun `test AuthResponseDto deserializes correctly with Long userId`() {
        val json = """
            {
                "message": "Authentication successful",
                "token": "header.payload.signature",
                "tokenType": "Bearer",
                "userId": 42,
                "name": "Jane Developer",
                "email": "jane@example.com"
            }
        """.trimIndent()

        val response = gson.fromJson(json, AuthResponseDto::class.java)

        assertEquals("Authentication successful", response.message)
        assertEquals("header.payload.signature", response.token)
        assertEquals("Bearer", response.tokenType)
        assertEquals(42L, response.userId)
        assertEquals("Jane Developer", response.name)
        assertEquals("jane@example.com", response.email)
    }

    @Test
    fun `test CurrentUserResponseDto handles String userId and conversion to Long`() {
        val json = """
            {
                "userId": "99",
                "email": "current@example.com",
                "name": "Current User"
            }
        """.trimIndent()

        val response = gson.fromJson(json, CurrentUserResponseDto::class.java)

        assertEquals("99", response.userId)
        assertEquals(99L, response.userIdAsLong)
        assertEquals("current@example.com", response.email)
        assertEquals("Current User", response.name)
    }

    @Test
    fun `test ApiErrorResponseDto resolves single message and field error lists`() {
        // Single error message (ResponseStatusException / Spring Boot default)
        val jsonSingle = """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Invalid email or password"
            }
        """.trimIndent()
        val errorSingle = gson.fromJson(jsonSingle, ApiErrorResponseDto::class.java)
        assertEquals("Invalid email or password", errorSingle.userFriendlyMessage())

        // Field validation error list (GlobalExceptionHandler / MethodArgumentNotValidException)
        val jsonValidation = """
            {
                "status": 400,
                "error": "Bad Request",
                "messages": [
                    { "field": "password", "message": "Password must be at least 8 characters." }
                ]
            }
        """.trimIndent()
        val errorValidation = gson.fromJson(jsonValidation, ApiErrorResponseDto::class.java)
        assertEquals("Password must be at least 8 characters.", errorValidation.userFriendlyMessage())
    }

    // -------------------------------------------------------------
    // TOKEN MANAGER & SECURITY ENGINE TESTS
    // -------------------------------------------------------------

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var testStorageEngine: TestStorageEngine
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        testStorageEngine = TestStorageEngine()
        tokenManager = TokenManager(fakePrefs, testStorageEngine)
    }

    @Test
    fun `test saveSession encrypts and stores session attributes`() {
        tokenManager.saveSession(
            token = "jwt-sample-token-abc",
            userId = 123L,
            name = "Ada Lovelace",
            email = "ada@example.com"
        )

        assertTrue(tokenManager.isLoggedIn())
        assertEquals("jwt-sample-token-abc", tokenManager.getToken())

        val session = tokenManager.getUserSession()
        assertNotNull(session)
        assertEquals("jwt-sample-token-abc", session?.token)
        assertEquals(123L, session?.userId)
        assertEquals("Ada Lovelace", session?.name)
        assertEquals("ada@example.com", session?.email)

        // Raw values in SharedPreferences must be encrypted, not plaintext
        val rawToken = fakePrefs.getString("enc_jwt_token", null)
        assertNotNull(rawToken)
        assertNotEquals("jwt-sample-token-abc", rawToken)
    }

    @Test
    fun `test clearSession removes all stored credentials`() {
        tokenManager.saveSession(
            token = "jwt-token-to-clear",
            userId = 456L,
            name = "User To Clear",
            email = "clear@example.com"
        )
        assertTrue(tokenManager.isLoggedIn())

        tokenManager.clearSession()

        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getToken())
        assertNull(tokenManager.getUserSession())
        assertNull(fakePrefs.getString("enc_jwt_token", null))
        assertNull(fakePrefs.getString("enc_user_id", null))
    }

    @Test
    fun `test corrupt encrypted payload recovers gracefully and clears session`() {
        // Put corrupt non-decryptable string in preferences
        fakePrefs.putString("enc_jwt_token", "invalid_corrupted_data_not_base64_or_bad_tag")

        assertNull(tokenManager.getToken())
        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getUserSession())

        // Verifies corrupt token is purged
        assertNull(fakePrefs.getString("enc_jwt_token", null))
    }

    @Test
    fun `test saveSession with blank token clears existing session`() {
        tokenManager.saveSession(token = "initial-token", userId = 1L)
        assertTrue(tokenManager.isLoggedIn())

        tokenManager.saveSession(token = "   ")
        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getToken())
    }

    // -------------------------------------------------------------
    // TEST HELPER IMPLEMENTATIONS
    // -------------------------------------------------------------

    /**
     * Standard AES-GCM engine for JVM unit tests without AndroidKeyStore dependency.
     */
    private class TestStorageEngine : SecureStorageEngine {
        private val secretKey = SecretKeySpec(ByteArray(32) { 7 }, "AES")
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
            if (combined.size < 12 + 16) throw IllegalArgumentException("Too short")
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)
            val cipherText = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, cipherText, 0, cipherText.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            return String(cipher.doFinal(cipherText), Charsets.UTF_8)
        }
    }

    /**
     * Minimal in-memory SharedPreferences for unit testing.
     */
    private class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        fun putString(key: String, value: String?) {
            if (value == null) map.remove(key) else map[key] = value
        }

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
