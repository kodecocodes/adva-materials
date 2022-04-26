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

package com.raywenderlich.android.petsave.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import com.raywenderlich.android.petsave.main.domain.usecases.OnboardingIsComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val onboardingIsComplete: OnboardingIsComplete,
    private val dispatchersProvider: DispatchersProvider
): ViewModel() {

  val viewEffect: SharedFlow<MainActivityViewEffect> get() = _viewEffect

  private val _viewEffect = MutableSharedFlow<MainActivityViewEffect>()

  fun onEvent(event: MainActivityEvent) {
    when (event) {
      is MainActivityEvent.DefineStartDestination -> defineStartDestination()
    }
  }

  private fun defineStartDestination() {
    val errorMessage = "Failed to check if onboarding is complete"
    val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

    viewModelScope.launch(exceptionHandler) {
      val destination = withContext(dispatchersProvider.io()) {
        if (onboardingIsComplete()) {
          R.id.nav_animalsnearyou
        } else {
          R.id.nav_onboarding
        }
      }

      _viewEffect.emit(MainActivityViewEffect.SetStartDestination(destination))
    }
  }

  private fun onFailure(throwable: Throwable) {
    // TODO: Handle failures
  }
}