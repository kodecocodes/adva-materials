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

package com.realworld.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.animalsnearyou.domain.usecases.GetAnimals
import com.realworld.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val getAnimals: GetAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  companion object {
    const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
  }

  val state: LiveData<AnimalsNearYouViewState> get() = _state
  var isLoadingMoreAnimals: Boolean = false
  var isLastPage = false

  private val _state = MutableLiveData<AnimalsNearYouViewState>()
  private var currentPage = 0

  init {
    _state.value = AnimalsNearYouViewState()
  }

  fun handleEvent(event: AnimalsNearYouEvent) {
    when(event) {
      is AnimalsNearYouEvent.LoadAnimals -> loadNextAnimalPage()
    }
  }

  init {
    _state.value = AnimalsNearYouViewState()

    subscribeToAnimalUpdates()
  }

  private fun subscribeToAnimalUpdates() {
    getAnimals()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { onNewAnimalList(it) },
            { onFailure(it) }
        )
        .addTo(compositeDisposable)
  }

  private fun loadNextAnimalPage() {
    isLoadingMoreAnimals = true
    val errorMessage = "Failed to fetch nearby animals"
    val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
      onFailure(it)
    }

    viewModelScope.launch(exceptionHandler) {
      val pagination = withContext(dispatchersProvider.io()) {
        Logger.d("Requesting more animals.")

        requestNextPageOfAnimals(++currentPage)
      }

      onPaginationInfoObtained(pagination)

      isLoadingMoreAnimals = false
    }
  }

  private fun onNewAnimalList(animals: List<Animal>) {
    Logger.d("Got more animals!")
    val animalsNearYou = animals.map { uiAnimalMapper.mapToView(it) }

    // This ensures that new items are added below the already existing ones, thus avoiding
    // repositioning of items that are already visible, as it can provide for a confusing UX. A
    // nice alternative to this would be to add an "updatedAt" field to the Room entities, so
    // that we could actually order them by something that we completely control.
    val currentList = state.value?.animals.orEmpty()
    val newAnimals = animalsNearYou.subtract(currentList.toSet())
    val updatedList = currentList + newAnimals

    _state.value = state.value!!.copy(
        loading = false,
        animals = updatedList
    )
  }

  private fun onPaginationInfoObtained(pagination: Pagination) {
    currentPage = pagination.currentPage
    isLastPage = !pagination.canLoadMore
  }

  private fun onFailure(failure: Throwable) {
    val noMoreAnimalsNearby = failure is NoMoreAnimalsException
    _state.value = state.value?.copy(
        noMoreAnimalsNearby = noMoreAnimalsNearby,
        failure = Event(failure)
    )
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
