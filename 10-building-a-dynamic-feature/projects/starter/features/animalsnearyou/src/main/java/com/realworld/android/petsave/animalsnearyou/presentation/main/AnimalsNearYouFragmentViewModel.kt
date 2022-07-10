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

package com.realworld.android.petsave.animalsnearyou.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.animalsnearyou.domain.usecases.GetAnimals
import com.realworld.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.realworld.android.petsave.common.domain.model.NetworkException
import com.realworld.android.petsave.common.domain.model.NetworkUnavailableException
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val getAnimals: GetAnimals,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  companion object {
    const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
  }

  private val _state = MutableStateFlow(AnimalsNearYouViewState())
  private var currentPage = 0

  val state: StateFlow<AnimalsNearYouViewState> = _state.asStateFlow()
  var isLoadingMoreAnimals: Boolean = false
  var isLastPage = false

  init {
    subscribeToAnimalUpdates()
  }

  fun onEvent(event: AnimalsNearYouEvent) {
    when(event) {
      is AnimalsNearYouEvent.RequestMoreAnimals -> loadNextAnimalPage()
    }
  }

  private fun subscribeToAnimalUpdates() {
    getAnimals()
        .doOnNext { if (hasNoAnimalsStoredButCanLoadMore(it)) loadNextAnimalPage() }
        .map { animals -> animals.map { uiAnimalMapper.mapToView(it) } }
        .filter { it.isNotEmpty() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { onNewAnimalList(it) },
          { onFailure(it) }
        )
        .addTo(compositeDisposable)
  }

  private fun hasNoAnimalsStoredButCanLoadMore(animals: List<Animal>): Boolean {
    return animals.isEmpty() && !state.value!!.noMoreAnimalsNearby
  }

  private fun onNewAnimalList(animals: List<UIAnimal>) {
    Logger.d("Got more animals!")

    val updatedAnimalSet = (state.value.animals + animals).toSet()

    _state.update { oldState ->
      oldState.copy(loading = false, animals = updatedAnimalSet.toList())
    }
  }

  private fun loadNextAnimalPage() {
    isLoadingMoreAnimals = true

    val errorMessage = "Failed to fetch nearby animals"
    val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

    viewModelScope.launch(exceptionHandler) {
      Logger.d("Requesting more animals.")
      val pagination = requestNextPageOfAnimals(++currentPage)

      onPaginationInfoObtained(pagination)
      isLoadingMoreAnimals = false
    }
  }

  private fun onPaginationInfoObtained(pagination: Pagination) {
    currentPage = pagination.currentPage
    isLastPage = !pagination.canLoadMore
  }

  private fun onFailure(failure: Throwable) {
    when (failure) {
      is NetworkException,
      is NetworkUnavailableException -> {
        _state.update { oldState ->
          oldState.copy(loading = false, failure = Event(failure))
        }
      }
      is NoMoreAnimalsException -> {
        _state.update { oldState ->
          oldState.copy(noMoreAnimalsNearby = true, failure = Event(failure))
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
