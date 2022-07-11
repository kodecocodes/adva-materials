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

package com.realworld.android.petsave.common.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.widget.ImageView
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun ImageView.setImage(url: String) {
  Glide.with(this.context)
      .load(url.ifEmpty { null })
      .error(R.drawable.dog_placeholder)
      .into(this)
}

fun ImageView.setImageWithCrossFade(url: String) {
  Glide.with(this.context)
      .load(url.ifEmpty { null })
      .error(R.drawable.dog_placeholder)
      .centerCrop()
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(this)
}

inline fun CoroutineScope.createExceptionHandler(
    message: String,
    crossinline action: (throwable: Throwable) -> Unit
) = CoroutineExceptionHandler { _, throwable ->
  Logger.e(throwable, message)
  throwable.printStackTrace()

  /**
   * A [CoroutineExceptionHandler] can be called from any thread. So, if [action] is supposed to
   * run in the main thread, you need to be careful and call this function on the a scope that
   * runs in the main thread, such as a [viewModelScope].
  */
  launch {
    action(throwable)
  }
}

/**
 * Extension that returns Yes if a Boolean is true, else No
 */
fun Boolean.toEnglish() = if (this) "Yes" else "No"

const val CHECK_EMOJI = 0x2714
const val CROSS_EMOJI = 0x274C
const val QUESTION_EMOJI = 0x2753

/**
 * Equivalent to toEnglish() but returns emoji unicode instead
 */
fun Boolean?.toEmoji() = if (this != null) {
  String(Character.toChars(if (this) CHECK_EMOJI else CROSS_EMOJI))
} else {
  String(Character.toChars(QUESTION_EMOJI))
}

fun Context.dpToPx(dp: Float) = this.resources.displayMetrics.density * dp

fun Paint.getTextWidth(string: String): Float {
  val rect = Rect()
  this.getTextBounds(string, 0, string.length, rect)
  return rect.width().toFloat()
}