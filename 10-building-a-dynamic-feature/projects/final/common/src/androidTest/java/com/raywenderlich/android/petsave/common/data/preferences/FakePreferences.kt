package com.raywenderlich.android.petsave.common.data.preferences

import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_MAX_DISTANCE
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_POSTCODE
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN_TYPE

class FakePreferences : Preferences {
  private val preferences = mutableMapOf<String, Any>()

  override fun putToken(token: String) {
    preferences[KEY_TOKEN] = token
  }

  override fun putTokenExpirationTime(time: Long) {
    preferences[KEY_TOKEN_EXPIRATION_TIME] = time
  }

  override fun putTokenType(tokenType: String) {
    preferences[KEY_TOKEN_TYPE] = tokenType
  }

  override fun getToken(): String {
    return preferences[KEY_TOKEN] as String
  }

  override fun getTokenExpirationTime(): Long {
    return preferences[KEY_TOKEN_EXPIRATION_TIME] as Long
  }

  override fun getTokenType(): String {
    return preferences[KEY_TOKEN_TYPE] as String
  }

  override fun deleteTokenInfo() {
    with (preferences) {
      remove(KEY_TOKEN)
      remove(KEY_TOKEN_EXPIRATION_TIME)
      remove(KEY_TOKEN_TYPE)
    }
  }

  override fun getPostcode(): String {
    return preferences[KEY_POSTCODE] as String
  }

  override fun putPostcode(postcode: String) {
    preferences[KEY_POSTCODE] = postcode
  }

  override fun getMaxDistanceAllowedToGetAnimals(): Int {
    return preferences[KEY_MAX_DISTANCE] as Int
  }

  override fun putMaxDistanceAllowedToGetAnimals(distance: Int) {
    preferences[KEY_MAX_DISTANCE] = distance
  }


}