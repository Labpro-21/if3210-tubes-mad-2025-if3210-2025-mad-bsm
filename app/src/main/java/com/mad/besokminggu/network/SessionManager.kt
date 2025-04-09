package com.mad.besokminggu.network

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.mad.besokminggu.data.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.KeyStore
import java.util.Base64
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
){

    private val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")

    fun storeAccessToken(accessToken: String?, refreshToken: String?) {
        try {
            if (accessToken == null || refreshToken == null) {
                return
            }

            var accessTokenBA = accessToken.toByteArray()
            var refreshTokenBA = refreshToken.toByteArray()

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

            // Generate keys if not already present
            generateKeyIfAbsent("accessToken", keyStore)
            generateKeyIfAbsent("refreshToken", keyStore)

            val accessTokenKey = keyStore.getKey("accessToken", null) as SecretKey
            val refreshTokenKey = keyStore.getKey("refreshToken", null) as SecretKey

            // Encrypt access token
            cipher.init(Cipher.ENCRYPT_MODE, accessTokenKey)
            val accessTokenIv = cipher.iv
            val encryptedAccessToken = cipher.doFinal(accessTokenBA)

            // Encrypt refresh token
            cipher.init(Cipher.ENCRYPT_MODE, refreshTokenKey)
            val refreshTokenIv = cipher.iv
            val encryptedRefreshToken = cipher.doFinal(refreshTokenBA)

            // Store encrypted tokens and IVs in SharedPreferences or another secure storage
            with(sharedPreferences.edit()) {
                putString("encryptedAccessToken", java.util.Base64.getEncoder().encodeToString(encryptedAccessToken))
                putString("accessTokenIv", java.util.Base64.getEncoder().encodeToString(accessTokenIv))
                putString("encryptedRefreshToken", java.util.Base64.getEncoder().encodeToString(encryptedRefreshToken))
                putString("refreshTokenIv", java.util.Base64.getEncoder().encodeToString(refreshTokenIv))
                apply()
            }
        } catch (e: Exception) {
            Log.e("SESSION_MANAGER",e.message.toString())
        }
    }

    fun getToken(): Flow<Pair<String?, String?>> = flow {
        try {
            val encryptedAccessToken = sharedPreferences.getString("encryptedAccessToken", null)
            val accessTokenIv = sharedPreferences.getString("accessTokenIv", null)

            val encryptedRefreshToken = sharedPreferences.getString("encryptedRefreshToken", null)
            val refreshTokenIv = sharedPreferences.getString("refreshTokenIv", null)

            if (encryptedAccessToken == null || accessTokenIv == null || encryptedRefreshToken == null || refreshTokenIv == null) return@flow

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val accessTokenKey = keyStore.getKey("accessToken", null) as SecretKey
            val refreshTokenKey = keyStore.getKey("refreshToken", null) as SecretKey

            val ivAccess = Base64.getDecoder().decode(accessTokenIv)
            val encryptedBytesAccess = Base64.getDecoder().decode(encryptedAccessToken)

            cipher.init(Cipher.DECRYPT_MODE, accessTokenKey, GCMParameterSpec(128, ivAccess))
            val decryptedAccessToken = String(cipher.doFinal(encryptedBytesAccess))

            val ivRefresh = Base64.getDecoder().decode(refreshTokenIv)
            val encryptedBytesRefresh = Base64.getDecoder().decode(encryptedRefreshToken)

            cipher.init(Cipher.DECRYPT_MODE, refreshTokenKey, GCMParameterSpec(128, ivRefresh))
            val decryptedRefreshToken = String(cipher.doFinal(encryptedBytesRefresh))

            emit(Pair(decryptedAccessToken, decryptedRefreshToken))
        } catch (e: Exception) {
            Log.e("SESSION_MANAGER",e.message.toString())
            return@flow
        }
    }

    fun getAccessToken(): Flow<String?> = flow {
        try {
            val encryptedAccessToken = sharedPreferences.getString("encryptedAccessToken", null)
            val accessTokenIv = sharedPreferences.getString("accessTokenIv", null)

            if (encryptedAccessToken == null || accessTokenIv == null) return@flow

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val secretKey = keyStore.getKey("accessToken", null) as SecretKey

            val iv = Base64.getDecoder().decode(accessTokenIv)
            val encryptedBytes = Base64.getDecoder().decode(encryptedAccessToken)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedAccessToken = String(cipher.doFinal(encryptedBytes))
            // TODO("verify token")

            emit(decryptedAccessToken)
        } catch (e: Exception) {
            Log.e("SESSION_MANAGER",e.message.toString())
            return@flow
        }

    }

    fun getRefreshToken(): Flow<String?> = flow {
        try {
            val encryptedRefreshToken = sharedPreferences.getString("encryptedRefreshToken", null)
            val refreshTokenIv = sharedPreferences.getString("refreshTokenIv", null)

            if (encryptedRefreshToken == null || refreshTokenIv == null) return@flow

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

            val secretKey = keyStore.getKey("refreshToken", null) as SecretKey

            val iv = Base64.getDecoder().decode(refreshTokenIv)
            val encryptedBytes = Base64.getDecoder().decode(encryptedRefreshToken)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedAccessToken = String(cipher.doFinal(encryptedBytes))

            emit(decryptedAccessToken)
        } catch (e: Exception) {
            Log.e("SESSION_MANAGER",e.message.toString())
            return@flow
        }
    }

    fun clearToken() {
        with(sharedPreferences.edit()) {
            remove("encryptedAccessToken")
            remove("accessTokenIv")
            remove("encryptedRefreshToken")
            remove("refreshTokenIv")
            apply()
        }

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyStore.deleteEntry("accessToken")
        keyStore.deleteEntry("refreshToken")

        generateKeyIfAbsent("accessToken", keyStore)
        generateKeyIfAbsent("refreshToken", keyStore)
    }

    fun isLoggedIn(): Boolean {
        return true
    }

    fun storeUserProfile(profile: Profile?) {
        if (profile == null) {
            return
        }

        with (sharedPreferences.edit()) {
            putString("userId", profile.id)
            putString("username", profile.username)
            putString("email", profile.email)
            putString("location", profile.location)
            putString("profilePhoto", profile.profilePhoto)
            putString("createdAt", profile.createdAt.toString())
            putString("updatedAt", profile.updatedAt.toString())
            apply()
        }
    }

    fun getUserProfile(): Profile? {
        val userId = sharedPreferences.getString("userId", null)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)
        val location = sharedPreferences.getString("location", null)
        val profilePhoto = sharedPreferences.getString("profilePhoto", null)
        val createdAt = sharedPreferences.getString("createdAt", null)
        val updatedAt = sharedPreferences.getString("updatedAt", null)
        return if (userId != null && username != null && email != null && location != null && profilePhoto != null && createdAt != null && updatedAt != null) {
            Profile(
                id = userId,
                username = username,
                email = email,
                profilePhoto = profilePhoto,
                location = location,
                createdAt = Date(createdAt),
                updatedAt = Date(updatedAt)
            )
        } else {
            null
        }
    }

    fun clearUserProfile() {
        with (sharedPreferences.edit()) {
            remove("userId")
            remove("username")
            remove("email")
            remove("location")
            remove("profilePhoto")
            remove("createdAt")
            remove("updatedAt")
            apply()
        }
    }

    fun refreshAccessToken() {
        // Logic to refresh the access token
        // This could involve making a network request to get a new token
    }

    fun verifyToken(accessToken: String?): Boolean {
        if (accessToken == null) {
            return false
        }

        return true
    }

    fun logout() {
        clearToken()
        clearUserProfile()
    }



    // private methods (used only inside this class)

    private fun generateKeyIfAbsent(keyName: String, keyStore: KeyStore) {
        if (!keyStore.containsAlias(keyName)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }
}