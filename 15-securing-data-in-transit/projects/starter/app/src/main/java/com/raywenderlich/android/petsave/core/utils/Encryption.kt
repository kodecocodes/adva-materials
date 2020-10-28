/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.petsave.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Encryption {
  companion object {

    private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
    private const val PROVIDER = "AndroidKeyStore"

    private fun getSecretKey(): SecretKey {
      val keyStore = KeyStore.getInstance(PROVIDER)

      // Before the keystore can be accessed, it must be loaded.
      keyStore.load(null)
      return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    @TargetApi(23)
    private fun getCipher(): Cipher {
      return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
          + KeyProperties.BLOCK_MODE_GCM + "/"
          + KeyProperties.ENCRYPTION_PADDING_NONE)
    }

    @TargetApi(23)
    fun generateSecretKey() {
      val keyGenParameterSpec = KeyGenParameterSpec.Builder(
          KEYSTORE_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM) // 1
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setUserAuthenticationRequired(true) // 2
          //For R+, can use  KeyProperties.AUTH_BIOMETRIC_STRONG and KeyProperties.AUTH_DEVICE_CREDENTIAL
          //.setUserAuthenticationParameters(120, KeyProperties.AUTH_BIOMETRIC_STRONG)
          //Otherwise:
          .setUserAuthenticationValidityDurationSeconds(120) // 3
          .build()
      val keyGenerator = KeyGenerator.getInstance(
          KeyProperties.KEY_ALGORITHM_AES, PROVIDER) // 4
      keyGenerator.init(keyGenParameterSpec)
      keyGenerator.generateKey()
    }

    fun createLoginPassword(context: Context): ByteArray {
      val cipher = getCipher()
      val secretKey = getSecretKey()
      val random = SecureRandom()
      val passwordBytes = ByteArray(256)
      random.nextBytes(passwordBytes) // 1
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val ivParameters = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
      val iv = ivParameters.iv
      PreferencesHelper.saveIV(context, iv) // 2
      return cipher.doFinal(passwordBytes) // 3
    }

    fun decryptPassword(context: Context, password: ByteArray): ByteArray {
      val cipher = getCipher()
      val secretKey = getSecretKey()
      val iv = PreferencesHelper.iv(context) // 1
      val ivParameters = GCMParameterSpec(128, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameters) // 2
      return cipher.doFinal(password) // 3
    }

    @TargetApi(23)
    fun encryptFile(context: Context, file: File): EncryptedFile {
      val spec = KeyGenParameterSpec.Builder(
          MasterKey.DEFAULT_MASTER_KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setKeySize(256)
          .build()

      val masterKey = MasterKey.Builder(context)
          .setKeyGenParameterSpec(spec)
          .build()

      return EncryptedFile.Builder(
          context,
          file,
          masterKey,
          EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
      ).build()
    }

    fun encrypt(dataToEncrypt: ByteArray,
        password: CharArray): HashMap<String, ByteArray> {
      val map = HashMap<String, ByteArray>()

      try {
        // 1
        //Random salt for next step
        val random = SecureRandom()
        val salt = ByteArray(256)
        random.nextBytes(salt)

        // 2
        //PBKDF2 - derive the key from the password, don't use passwords directly
        val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        // 3
        //Create initialization vector for AES
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        // 4
        //Encrypt
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(dataToEncrypt)

        // 5
        map["salt"] = salt
        map["iv"] = iv
        map["encrypted"] = encrypted
      } catch (e: Exception) {
        Log.e("MYAPP", "encryption exception", e)
      }

      return map

    }

    fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {
      var decrypted: ByteArray? = null
      try {
        // 1
        val salt = map["salt"]
        val iv = map["iv"]
        val encrypted = map["encrypted"]

        // 2
        //regenerate key from password
        val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        // 3
        //Decrypt
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        decrypted = cipher.doFinal(encrypted)
      } catch (e: Exception) {
        Log.e("MYAPP", "decryption exception", e)
      }

      return decrypted
    }

    //NOTE: Here's a keystore version of the encryption for your reference :]
    private fun keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {
      val map = HashMap<String, ByteArray>()
      try {

        //Get the key
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry =
            keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        //Encrypt data
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(dataToEncrypt)

        map["iv"] = ivBytes
        map["encrypted"] = encryptedBytes
      } catch (e: Throwable) {
        e.printStackTrace()
      }

      return map
    }

    private fun keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {
      var decrypted: ByteArray? = null
      try {
        //Get the key
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry =
            keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        //Extract info from map
        val encryptedBytes = map["encrypted"]
        val ivBytes = map["iv"]

        //Decrypt data
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        decrypted = cipher.doFinal(encryptedBytes)
      } catch (e: Throwable) {
        e.printStackTrace()
      }

      return decrypted
    }

    @TargetApi(23)
    fun keystoreTest() {

      //TODO - Add Test code here
      val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
      val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          //.setUserAuthenticationRequired(true) // requires lock screen, invalidated if lock screen is disabled
          //.setUserAuthenticationValidityDurationSeconds(120) // only available x seconds from password authentication. -1 requires finger print - every time
          .setRandomizedEncryptionRequired(true) // different ciphertext for same plaintext on each call
          .build()
      keyGenerator.init(keyGenParameterSpec)
      keyGenerator.generateKey()

      val map = keystoreEncrypt("My very sensitive string!".toByteArray(Charsets.UTF_8))
      val decryptedBytes = keystoreDecrypt(map)
      decryptedBytes?.let {
        val decryptedString = String(it, Charsets.UTF_8)
        Log.e("MyApp", "The decrypted string is: $decryptedString")
      }
    }

  }
}