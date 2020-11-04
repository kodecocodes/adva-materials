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

package com.raywenderlich.android.petsave.common.data.api.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.common.data.api.ApiConstants
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.io.InputStream

class FakeServer {
  private val mockWebServer = MockWebServer()

  private val endpointSeparator = "/"
  private val animalsEndpointPath = endpointSeparator + ApiConstants.ANIMALS_ENDPOINT
  private val notFoundResponse = MockResponse().setResponseCode(404)

  val baseEndpoint
    get() = mockWebServer.url(endpointSeparator)

  fun start() {
    mockWebServer.start(8080)
  }

  fun setHappyPathDispatcher() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path ?: return notFoundResponse

        return with(path) {
          when {
            startsWith(animalsEndpointPath) -> {
              MockResponse().setResponseCode(200).setBody(getJson("animals.json"))
            }
            else -> {
              notFoundResponse
            }
          }
        }
      }
    }
  }

  fun shutdown() {
    mockWebServer.shutdown()
  }

  private fun getJson(path: String): String {
    return try {
      val context = InstrumentationRegistry.getInstrumentation().context
      val jsonStream: InputStream = context.assets.open("networkresponses/$path")
      String(jsonStream.readBytes())
    } catch (exception: IOException) {
      Logger.e(exception, "Error reading network response json asset")
      throw exception
    }
  }
}