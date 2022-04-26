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

package com.raywenderlich.android.petsave.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.onboarding.R
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import com.raywenderlich.android.petsave.onboarding.domain.usecases.StoreOnboardingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingFragmentViewModel @Inject constructor(
    private val storeOnboardingData: StoreOnboardingData,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

  companion object {
    private const val MAX_POSTCODE_LENGTH = 5
  }

  val viewState: StateFlow<OnboardingViewState> get() = _viewState
  val viewEffects: SharedFlow<OnboardingViewEffect> get() = _viewEffects

  private val _viewState = MutableStateFlow(OnboardingViewState())
  private val _viewEffects = MutableSharedFlow<OnboardingViewEffect>()

  fun onEvent(event: OnboardingEvent) {
    when (event) {
      is OnboardingEvent.PostcodeChanged -> validateNewPostcodeValue(event.newPostcode)
      is OnboardingEvent.DistanceChanged -> validateNewDistanceValue(event.newDistance)
      is OnboardingEvent.SubmitButtonClicked -> wrapUpOnboarding()
    }
  }

  private fun validateNewPostcodeValue(newPostcode: String) {
    val validPostcode = newPostcode.length == MAX_POSTCODE_LENGTH

    val postcodeError = if (validPostcode || newPostcode.isEmpty()) {
      R.string.no_error
    } else {
      R.string.postcode_error
    }

    _viewState.value = viewState.value.copy(
        postcode = newPostcode,
        postcodeError = postcodeError
    )
  }

  private fun validateNewDistanceValue(newDistance: String) {
    val distanceError = when {
      newDistance.isNotEmpty() && newDistance.toInt() > 500 -> { R.string.distance_error }
      newDistance.toInt() == 0 -> { R.string.distance_error_cannot_be_zero }
      else -> { R.string.no_error }
    }

    _viewState.value = viewState.value.copy(
        distance = newDistance,
        distanceError = distanceError
    )
  }

  private fun wrapUpOnboarding() {
    val errorMessage = "Failed to store onboarding data"
    val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }
    val (postcode, distance) = viewState.value

    viewModelScope.launch(exceptionHandler) {
      withContext(dispatchersProvider.io()) { storeOnboardingData(postcode, distance) }
      _viewEffects.emit(OnboardingViewEffect.NavigateToAnimalsNearYou)
    }
  }

  private fun onFailure(throwable: Throwable) {
    // TODO: Handle failures
  }
}