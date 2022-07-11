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

package com.realworld.android.petsave.remoteconfig

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtil {

  private const val SECRET_IMAGE_URL = "secret_image_url"

  private val DEFAULTS: HashMap<String, Any> =
      hashMapOf(
          SECRET_IMAGE_URL to "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg"
      )

  private lateinit var remoteConfig: FirebaseRemoteConfig

  fun init(debug: Boolean = false) {
    remoteConfig = getFirebaseRemoteConfig(debug)
  }

  private fun getFirebaseRemoteConfig(debug: Boolean): FirebaseRemoteConfig {

    val remoteConfig = Firebase.remoteConfig

    val configSettings = remoteConfigSettings {
      minimumFetchIntervalInSeconds = if (debug) {
        0
      } else {
        60 * 60
      }
    }

    remoteConfig.setConfigSettingsAsync(configSettings)
    remoteConfig.setDefaultsAsync(DEFAULTS)
    remoteConfig.fetchAndActivate()

    return remoteConfig
  }

  fun getSecretImageUrl(): String = remoteConfig.getString(SECRET_IMAGE_URL)

}