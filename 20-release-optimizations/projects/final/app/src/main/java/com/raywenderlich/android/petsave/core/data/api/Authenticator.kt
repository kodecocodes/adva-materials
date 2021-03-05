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

package com.raywenderlich.android.petsave.core.data.api

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class Authenticator {

  private val publicKey: PublicKey
  private val privateKey: PrivateKey

  init {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    keyPairGenerator.initialize(256)
    val keyPair = keyPairGenerator.genKeyPair()

    publicKey = keyPair.public
    privateKey = keyPair.private
  }

  fun sign(data: ByteArray): ByteArray {
    val signature = Signature.getInstance("SHA512withECDSA")
    signature.initSign(privateKey)
    signature.update(data)
    return signature.sign()
  }

  //You can use this method to test your sign method above
  fun verify(signature: ByteArray, data: ByteArray): Boolean {
    val verifySignature = Signature.getInstance("SHA512withECDSA")
    verifySignature.initVerify(publicKey)
    verifySignature.update(data)
    return verifySignature.verify(signature)
  }

  fun verify(signature: ByteArray, data: ByteArray, publicKeyString: String): Boolean {
    val verifySignature = Signature.getInstance("SHA512withECDSA")
    val bytes = android.util.Base64.decode(publicKeyString, android.util.Base64.NO_WRAP)
    val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes))
    verifySignature.initVerify(publicKey)
    verifySignature.update(data)
    return verifySignature.verify(signature)
  }

  fun publicKey(): String {
    return android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.NO_WRAP)
  }
}
