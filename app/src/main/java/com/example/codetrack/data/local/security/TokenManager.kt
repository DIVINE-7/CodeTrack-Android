package com.example.codetrack.data.local.security

import android.content.Context
import android.content.SharedPreferences

/**
 * Thread-safe manager responsible for storing, reading, and clearing encrypted user session data.
 *
 * Uses [SecureStorageEngine] to encrypt tokens and session identifiers with Android Keystore AES-GCM
 * before writing them to private [SharedPreferences].
 *
 * Automatically detects corrupt or invalidated payloads, clears unusable session state,
 * and recovers gracefully without throwing unhandled exceptions.
 */
class TokenManager(
    private val preferences: SharedPreferences,
    private val storageEngine: SecureStorageEngine
) {

    companion object {
        const val PREFS_NAME = "codetrack_secure_auth_prefs"
        private const val KEY_ENCRYPTED_TOKEN = "enc_jwt_token"
        private const val KEY_ENCRYPTED_USER_ID = "enc_user_id"
        private const val KEY_ENCRYPTED_NAME = "enc_name"
        private const val KEY_ENCRYPTED_EMAIL = "enc_email"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(
            context: Context,
            customStorageEngine: SecureStorageEngine? = null
        ): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(
                    preferences = context.applicationContext.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                    ),
                    storageEngine = customStorageEngine ?: KeystoreStorageEngine()
                ).also { instance = it }
            }
        }
    }

    /**
     * Stores encrypted session credentials.
     * Never stores passwords.
     */
    @Synchronized
    fun saveSession(
        token: String,
        userId: Long? = null,
        name: String? = null,
        email: String? = null
    ) {
        if (token.isBlank()) {
            clearSession()
            return
        }

        try {
            val encToken = storageEngine.encrypt(token)
            val encUserId = userId?.let { storageEngine.encrypt(it.toString()) }
            val encName = name?.let { storageEngine.encrypt(it) }
            val encEmail = email?.let { storageEngine.encrypt(it) }

            preferences.edit().apply {
                putString(KEY_ENCRYPTED_TOKEN, encToken)
                if (encUserId != null) putString(KEY_ENCRYPTED_USER_ID, encUserId) else remove(KEY_ENCRYPTED_USER_ID)
                if (encName != null) putString(KEY_ENCRYPTED_NAME, encName) else remove(KEY_ENCRYPTED_NAME)
                if (encEmail != null) putString(KEY_ENCRYPTED_EMAIL, encEmail) else remove(KEY_ENCRYPTED_EMAIL)
                apply()
            }
        } catch (_: Throwable) {
            // If encryption fails (e.g. keystore error), clear preferences to maintain secure state
            clearSession()
        }
    }

    /**
     * Retrieves and decrypts the stored JWT token.
     * Returns null if no token is saved or if decryption fails (e.g. corrupted payload or rotated key).
     */
    @Synchronized
    fun getToken(): String? {
        val encToken = preferences.getString(KEY_ENCRYPTED_TOKEN, null) ?: return null
        return try {
            storageEngine.decrypt(encToken).takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            // Corrupt or un-decryptable payload: wipe unusable session
            clearSession()
            null
        }
    }

    /**
     * Retrieves the full decrypted user session, or null if unauthenticated or corrupt.
     */
    @Synchronized
    fun getUserSession(): UserSession? {
        val token = getToken() ?: return null

        val userId = try {
            preferences.getString(KEY_ENCRYPTED_USER_ID, null)?.let {
                storageEngine.decrypt(it).toLongOrNull()
            }
        } catch (_: Throwable) {
            null
        }

        val name = try {
            preferences.getString(KEY_ENCRYPTED_NAME, null)?.let {
                storageEngine.decrypt(it)
            }
        } catch (_: Throwable) {
            null
        }

        val email = try {
            preferences.getString(KEY_ENCRYPTED_EMAIL, null)?.let {
                storageEngine.decrypt(it)
            }
        } catch (_: Throwable) {
            null
        }

        return UserSession(
            token = token,
            userId = userId,
            name = name,
            email = email
        )
    }

    /**
     * Indicates whether a token is stored locally.
     * NOTE: Does not guarantee that the token has not expired on the backend;
     * network validation is handled by GET /api/auth/me in subsequent steps.
     */
    @Synchronized
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    /**
     * Clears all encrypted tokens and user identity metadata.
     */
    @Synchronized
    fun clearSession() {
        preferences.edit().apply {
            remove(KEY_ENCRYPTED_TOKEN)
            remove(KEY_ENCRYPTED_USER_ID)
            remove(KEY_ENCRYPTED_NAME)
            remove(KEY_ENCRYPTED_EMAIL)
            apply()
        }
    }
}
