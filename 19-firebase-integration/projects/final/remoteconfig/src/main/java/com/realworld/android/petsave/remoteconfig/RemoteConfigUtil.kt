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