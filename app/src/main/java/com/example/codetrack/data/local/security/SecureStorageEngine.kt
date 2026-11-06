package com.example.codetrack.data.local.security

/**
 * Abstraction for cryptographic operations protecting sensitive auth tokens and metadata.
 * Isolates cryptographic algorithms from preferences to facilitate unit testing without
 * mocking platform KeyStore while running real AES-GCM on Android devices.
 */
interface SecureStorageEngine {

    /**
     * Encrypts plaintext and returns a Base64-encoded representation containing
     * the initialization vector (IV) and ciphertext.
     */
    fun encrypt(plainText: String): String

    /**
     * Decrypts a Base64-encoded payload containing IV and ciphertext.
     * Throws an exception if authentication tag validation fails or payload is corrupt.
     */
    fun decrypt(cipherTextWithIv: String): String
}
