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

package com.raywenderlich.android.petsave.animalsnearyou

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.core.domain.Result
import com.raywenderlich.android.petsave.core.domain.model.Pagination
import com.raywenderlich.android.petsave.core.domain.model.animal.Animal
import com.raywenderlich.android.petsave.core.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.core.presentation.Event
import com.raywenderlich.android.petsave.animalsnearyou.model.mappers.AnimalNearYouMapper
import com.raywenderlich.android.petsave.core.utils.DispatchersProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import kotlin.Exception

class AnimalsNearYouViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val animalRepository: AnimalRepository,
    private val animalNearYouMapper: AnimalNearYouMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  companion object {
    const val PAGE_SIZE = 10
  }

  var isLoadingMoreAnimals: Boolean = false
  var isLastPage = false

  val state: LiveData<AnimalsNearYouViewState>
    get() = _state

  private val _state = MutableLiveData<AnimalsNearYouViewState>()
  private var currentPage = 0

  fun handleEvent(event: AnimalsNearYouEvent) {
    when(event) {
      is AnimalsNearYouEvent.LoadAnimals -> loadNextAnimalPage()
    }
  }

  init {
    _state.value = AnimalsNearYouViewState()

    subscribeToDataUpdates()
  }

  private fun subscribeToDataUpdates() {
    animalRepository.getStoredNearbyAnimals()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { onNewAnimalList(it) },
            { onFailure(Exception(it)) }
        )
        .addTo(compositeDisposable)
  }

  private fun loadNextAnimalPage() {
    isLoadingMoreAnimals = true
    val errorMessage = "Failed to fetch nearby animals"
    val exceptionHandler = createExceptionHandler(errorMessage) {
      onFailure(RuntimeException(it))
    }

    viewModelScope.launch(exceptionHandler) {
      val result = withContext(dispatchersProvider.io()) {
        Logger.d("Requesting more animals.")

        animalRepository.fetchAndStoreNearbyAnimals(++currentPage, PAGE_SIZE)
      }

      when (result) {
        is Result.Success<Pagination> -> onPaginationInfoObtained(result.data)
        is Result.Error -> onFailure(result.failure)
      }

      isLoadingMoreAnimals = false
    }
  }

  private inline fun createExceptionHandler(
      message: String,
      crossinline action: (throwable: Throwable) -> Unit
  ) = CoroutineExceptionHandler { _, throwable ->
    Logger.e(throwable, message)

    // The handler can be called from any thread. So, since we want to update the state's LiveData
    // in the main thread (avoiding the call to postValue), we just run a coroutine on the main
    // thread here.
    viewModelScope.launch {
      action(throwable)
    }
  }

  private fun onNewAnimalList(animals: List<Animal>) {
    Logger.d("Got more animals!")
    val animalsNearYou = animals.map { animalNearYouMapper.mapToView(it) }

    // This ensures that new items are added below the already existing ones, thus avoiding
    // repositioning of items that are already visible, as it can provide for a confusing UX.
    val currentList = state.value?.animals.orEmpty()
    val newAnimals = animalsNearYou.subtract(currentList)
    val updatedList = currentList + newAnimals

    _state.value = state.value?.copy(
        loading = false,
        animals = updatedList
    ) ?: AnimalsNearYouViewState(loading = false, animals = updatedList)
  }

  private fun onPaginationInfoObtained(pagination: Pagination) {
    currentPage = pagination.currentPage
    isLastPage = !pagination.canLoadMore
  }

  private fun onFailure(failure: Exception) {
    _state.value = state.value?.copy(failure = Event(failure))
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
