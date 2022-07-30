/*
 * Copyright (c) 2022 Razeware LLC
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

package com.realworld.android.petsave.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import java.io.File
import java.security.KeyStore
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class Encryption {
  companion object {

    private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
    private const val PROVIDER = "AndroidKeyStore"

    @TargetApi(23)
    fun generateSecretKey() {

    }

    fun createLoginPassword(context: Context): ByteArray {
      return ByteArray(0)
    }

    fun decryptPassword(context: Context, password: ByteArray): ByteArray {
      return ByteArray(0)
    }

    @TargetApi(23)
    fun encryptFile(context: Context, file: File): EncryptedFile? {
      return null
    }

    fun encrypt(dataToEncrypt: ByteArray,
        password: CharArray): HashMap<String, ByteArray> {
      val map = HashMap<String, ByteArray>()

      //TODO: Add custom encrypt code here

      return map
    }

    fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

      var decrypted: ByteArray? = null

      //TODO: Add custom decrypt code here

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