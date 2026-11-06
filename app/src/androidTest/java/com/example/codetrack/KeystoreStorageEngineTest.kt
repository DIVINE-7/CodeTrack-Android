package com.example.codetrack

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.codetrack.data.local.security.KeystoreStorageEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreStorageEngineTest {

    @Test
    fun testKeystoreStorageEngineRoundTrip() {
        val engine = KeystoreStorageEngine(keyAlias = "test_auth_key_roundtrip")
        val sampleToken = "jwt.test.token.12345"

        val encrypted = engine.encrypt(sampleToken)
        assertNotNull(encrypted)
        assertNotEquals(sampleToken, encrypted)

        val decrypted = engine.decrypt(encrypted)
        assertEquals(sampleToken, decrypted)
    }

    @Test
    fun testDifferentIvPerEncryption() {
        val engine = KeystoreStorageEngine(keyAlias = "test_auth_key_iv")
        val sample = "same_plain_text_for_iv_check"

        val enc1 = engine.encrypt(sample)
        val enc2 = engine.encrypt(sample)

        // Verifies distinct random IVs are generated every time
        assertNotEquals(enc1, enc2)
        assertEquals(sample, engine.decrypt(enc1))
        assertEquals(sample, engine.decrypt(enc2))
    }
}
