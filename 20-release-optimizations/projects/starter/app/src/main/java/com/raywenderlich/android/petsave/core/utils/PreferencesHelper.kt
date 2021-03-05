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

import android.content.Context
import android.util.Base64
import java.text.DateFormat
import java.util.*


class PreferencesHelper {
  companion object {
    fun lastLoggedIn(context: Context): String? {
      val preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
      return preferences.getString("lastLogin", null)
    }

    fun saveLastLoggedInTime(context: Context) {
      val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
      val editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
      editor.putString("lastLogin", currentDateTimeString)
      editor.apply()
    }

    fun iv(context: Context): ByteArray {
      val preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
      val base64Iv = preferences.getString("iv", "")
      return Base64.decode(base64Iv, Base64.NO_WRAP)
    }

    fun saveIV(context: Context, iv: ByteArray) {
      val editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
      val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
      editor.putString("iv", ivString)
      editor.apply()
    }
  }
}