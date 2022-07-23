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

package com.realworld.android.petsave.common.data.api.interceptors

import com.realworld.android.petsave.common.data.api.ApiConstants
import com.realworld.android.petsave.common.data.api.ApiConstants.AUTH_ENDPOINT
import com.realworld.android.petsave.common.data.api.ApiParameters.AUTH_HEADER
import com.realworld.android.petsave.common.data.api.ApiParameters.CLIENT_ID
import com.realworld.android.petsave.common.data.api.ApiParameters.CLIENT_SECRET
import com.realworld.android.petsave.common.data.api.ApiParameters.GRANT_TYPE_KEY
import com.realworld.android.petsave.common.data.api.ApiParameters.GRANT_TYPE_VALUE
import com.realworld.android.petsave.common.data.api.ApiParameters.TOKEN_TYPE
import com.realworld.android.petsave.common.data.api.model.ApiToken
import com.realworld.android.petsave.common.data.preferences.Preferences
import com.squareup.moshi.Moshi
import okhttp3.*
import org.threeten.bp.Instant
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    private val preferences: Preferences
): Interceptor {

  companion object {
    const val UNAUTHORIZED = 401
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val token = preferences.getToken()
    val tokenExpirationTime = Instant.ofEpochSecond(preferences.getTokenExpirationTime())
    val request = chain.request()

    // if (chain.request().headers[NO_AUTH_HEADER] != null) return chain.proceed(request)
    val interceptedRequest: Request

    if (tokenExpirationTime.isAfter(Instant.now())) {
      // token is still valid, so we can proceed with the request
      interceptedRequest = chain.createAuthenticatedRequest(token)
    } else {
      // Token expired. Gotta refresh it before proceeding with the actual request
      val tokenRefreshResponse = chain.refreshToken()

      interceptedRequest = if (tokenRefreshResponse.isSuccessful) {
        val newToken = mapToken(tokenRefreshResponse)

        if (newToken.isValid()) {
          storeNewToken(newToken)
          chain.createAuthenticatedRequest(newToken.accessToken!!)
        } else {
          request
        }
      } else {
        request
      }
    }

    return chain.proceedDeletingTokenIfUnauthorized(interceptedRequest)
  }

  private fun Interceptor.Chain.createAuthenticatedRequest(token: String): Request {
    return request()
        .newBuilder()
        .addHeader(AUTH_HEADER, TOKEN_TYPE + token)
        .build()
  }

  private fun Interceptor.Chain.refreshToken(): Response {
    val url = request()
        .url
        .newBuilder(AUTH_ENDPOINT)!!
        .build()

    val body = FormBody.Builder()
        .add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE)
        .add(CLIENT_ID, ApiConstants.KEY)
        .add(CLIENT_SECRET, ApiConstants.SECRET)
        .build()

    val tokenRefresh = request()
        .newBuilder()
        .post(body)
        .url(url)
        .build()

    return proceedDeletingTokenIfUnauthorized(tokenRefresh)
  }

  private fun Interceptor.Chain.proceedDeletingTokenIfUnauthorized(request: Request): Response {
    val response = proceed(request)

    if (response.code == UNAUTHORIZED) {
      preferences.deleteTokenInfo()
    }

    return response
  }

  private fun mapToken(tokenRefreshResponse: Response): ApiToken {
    val moshi = Moshi.Builder().build()
    val tokenAdapter = moshi.adapter(ApiToken::class.java)
    val responseBody = tokenRefreshResponse.body!! // if successful, this should be good :]

    return tokenAdapter.fromJson(responseBody.string()) ?: ApiToken.INVALID
  }

  private fun storeNewToken(apiToken: ApiToken) {
    with(preferences) {
      putTokenType(apiToken.tokenType!!)
      putTokenExpirationTime(apiToken.expiresAt)
      putToken(apiToken.accessToken!!)
    }
  }
}
