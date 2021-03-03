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

import java.io.RandomAccessFile
import java.util.regex.Pattern

class DataValidator {
  companion object {
    private const val EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$"

    fun isValidEmailString(emailString: String): Boolean {
      return emailString.isNotEmpty() && Pattern.compile(EMAIL_REGEX).matcher(emailString).matches()
    }

    fun isValidJPEGAtPath(pathString: String?): Boolean {
      var randomAccessFile: RandomAccessFile? = null
      try {
        randomAccessFile = RandomAccessFile(pathString, "r")
        val length = randomAccessFile.length()
        if (length < 10L) {
          return false
        }
        val start = ByteArray(2)
        randomAccessFile.readFully(start)
        randomAccessFile.seek(length - 2)
        val end = ByteArray(2)
        randomAccessFile.readFully(end)
        return start[0].toInt() == -1 && start[1].toInt() == -40 &&
            end[0].toInt() == -1 && end[1].toInt() == -39
      } finally {
        randomAccessFile?.close()
      }
    }
  }
}