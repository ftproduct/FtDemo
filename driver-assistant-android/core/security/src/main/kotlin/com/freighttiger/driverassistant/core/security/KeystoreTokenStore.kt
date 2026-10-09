package com.freighttiger.driverassistant.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.freighttiger.driverassistant.domain.ports.AccessTokenStore
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the backend access token encrypted with an AES-256-GCM key held in the Android Keystore
 * (non-exportable). Only ciphertext + IV are written to app-private SharedPreferences.
 * The decrypted token is cached in memory for the life of the process.
 */
class KeystoreTokenStore(context: Context) : AccessTokenStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Volatile private var cached: String? = null
    @Volatile private var cachedExpiry: Long = 0

    @Synchronized
    override fun save(token: String, expiresAt: Instant) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(KEY_TOKEN, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .putLong(KEY_EXPIRY, expiresAt.toEpochMilli())
            .apply()
        cached = token
        cachedExpiry = expiresAt.toEpochMilli()
    }

    @Synchronized
    override fun current(): String? {
        val now = System.currentTimeMillis()
        cached?.let { if (now < cachedExpiry) return it }
        val iv = prefs.getString(KEY_IV, null) ?: return null
        val ct = prefs.getString(KEY_TOKEN, null) ?: return null
        val expiry = prefs.getLong(KEY_EXPIRY, 0)
        if (now >= expiry) return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(GCM_TAG_BITS, Base64.decode(iv, Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(ct, Base64.NO_WRAP)), Charsets.UTF_8).also {
                cached = it
                cachedExpiry = expiry
            }
        } catch (e: Exception) {
            // Key invalidated (e.g. device credential reset) or data corrupted: force re-login.
            clear()
            null
        }
    }

    @Synchronized
    override fun clear() {
        cached = null
        cachedExpiry = 0
        prefs.edit().clear().apply()
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "ftda_access_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val PREFS = "ftda_secure_session"
        const val KEY_IV = "iv"
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRY = "expires_at"
    }
}
