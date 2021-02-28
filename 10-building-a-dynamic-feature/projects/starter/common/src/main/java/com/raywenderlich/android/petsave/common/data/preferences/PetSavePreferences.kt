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

package com.raywenderlich.android.petsave.common.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_MAX_DISTANCE
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_POSTCODE
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME
import com.raywenderlich.android.petsave.common.data.preferences.PreferencesConstants.KEY_TOKEN_TYPE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetSavePreferences @Inject constructor(
    @ApplicationContext context: Context
): Preferences {

  companion object {
    const val PREFERENCES_NAME = "PET_SAVE_PREFERENCES"
  }

  private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

  override fun putToken(token: String) {
    edit { putString(KEY_TOKEN, token) }
  }

  override fun putTokenExpirationTime(time: Long) {
    edit { putLong(KEY_TOKEN_EXPIRATION_TIME, time) }
  }

  override fun putTokenType(tokenType: String) {
    edit { putString(KEY_TOKEN_TYPE, tokenType) }
  }

  private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
    with(preferences.edit()) {
      block()
      commit()
    }
  }

  override fun getToken(): String {
    return preferences.getString(KEY_TOKEN, "").orEmpty()
  }

  override fun getTokenExpirationTime(): Long {
    return preferences.getLong(KEY_TOKEN_EXPIRATION_TIME, -1)
  }

  override fun getTokenType(): String {
    return preferences.getString(KEY_TOKEN_TYPE, "").orEmpty()
  }

  override fun deleteTokenInfo() {
    edit {
      remove(KEY_TOKEN)
      remove(KEY_TOKEN_EXPIRATION_TIME)
      remove(KEY_TOKEN_TYPE)
    }
  }

  override fun getPostcode(): String {
    return preferences.getString(KEY_POSTCODE, "").orEmpty()
  }

  override fun putPostcode(postcode: String) {
    edit { putString(KEY_POSTCODE, postcode) }
  }

  override fun getMaxDistanceAllowedToGetAnimals(): Int {
    return preferences.getInt(KEY_MAX_DISTANCE, 0)
  }

  override fun putMaxDistanceAllowedToGetAnimals(distance: Int) {
    edit { putInt(KEY_MAX_DISTANCE, distance) }
  }
}