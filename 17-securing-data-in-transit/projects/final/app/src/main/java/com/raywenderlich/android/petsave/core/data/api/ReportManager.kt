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

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import com.raywenderlich.android.petsave.report.presentation.ReportDetailFragment
import java.io.FileInputStream
import java.net.URL
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertPathBuilder
import java.security.cert.PKIXBuilderParameters
import java.security.cert.PKIXRevocationChecker
import java.security.cert.X509CertSelector
import java.util.*
import javax.net.ssl.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class ReportManager {

  private val serverAuthenticator = Authenticator()
  private var clientPublicKeyString = ""

  init {
    System.setProperty("com.sun.net.ssl.checkRevocation", "true")
    Security.setProperty("ocsp.enable", "true")
    //Options to further config OCSP
    /*
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      val ts = KeyStore.getInstance("AndroidKeyStore")
      ts.load(null)
      val kmf =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      // init cert path checking for offered certs and revocation checks against CLRs
      val cpb = CertPathBuilder.getInstance("PKIX")
      val rc: PKIXRevocationChecker = cpb.revocationChecker as PKIXRevocationChecker
      rc.options = (EnumSet.of(
          PKIXRevocationChecker.Option.PREFER_CRLS, // use CLR over OCSP
          PKIXRevocationChecker.Option.ONLY_END_ENTITY,
          PKIXRevocationChecker.Option.NO_FALLBACK)) // no fall back to OCSP
      val pkixParams = PKIXBuilderParameters(ts, X509CertSelector())
      pkixParams.addCertPathChecker(rc)
      tmf.init( CertPathTrustManagerParameters(pkixParams) )
      val ctx = SSLContext.getInstance("TLS")
      ctx.init(kmf.keyManagers, tmf.trustManagers, null)
    }
     */
  }

  fun login(userIDString: String, publicKeyString: String) : String {
    clientPublicKeyString = publicKeyString
    return serverAuthenticator.publicKey()
  }

  fun sendReport(report: Map<String, Any>, callback: (Map<String, Any>) -> Unit) {
    GlobalScope.launch(Default) {
      delay(1000L)
      withContext(Main) {
        var result: Map<String, Any> = mapOf("success" to false)
        if (report.isNotEmpty()) {
          val applicationID = report["application_id"] as Int
          val reportID = report["report_id"] as String
          val reportString = report["report"] as String
          val stringToVerify = "$applicationID+$reportID+$reportString"
          val bytesToVerify = stringToVerify.toByteArray(Charsets.UTF_8)

          val signature = report["signature"] as String
          val signatureBytes = Base64.decode(signature, Base64.NO_WRAP)

          val success = serverAuthenticator.verify(signatureBytes, bytesToVerify,
              clientPublicKeyString)
          if (success) {
            //Process data
            val confirmationCode = UUID.randomUUID().toString()
            val bytesToSign = confirmationCode.toByteArray(Charsets.UTF_8) // 1
            val signedData = serverAuthenticator.sign(bytesToSign) // 2
            val requestSignature = Base64.encodeToString(signedData, Base64.NO_WRAP) // 3
            result = mapOf("success" to true,
                "confirmation_code" to confirmationCode,
                "signature" to requestSignature)
          }
        }
        callback(result)
      } //withContext(Main) {
    } //GlobalScope.launch(Default) {
  }
}
