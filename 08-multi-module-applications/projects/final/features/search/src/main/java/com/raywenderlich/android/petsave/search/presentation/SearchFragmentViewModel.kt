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

package com.raywenderlich.android.petsave.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.common.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.pagination.Pagination
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import com.raywenderlich.android.petsave.common.domain.model.search.SearchParameters
import com.raywenderlich.android.petsave.common.domain.model.search.SearchResults
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimals
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val searchAnimalsRemotely: SearchAnimalsRemotely,
    private val searchAnimals: SearchAnimals,
    private val getSearchFilters: GetSearchFilters,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  val state: LiveData<SearchViewState> get() = _state

  private val _state: MutableLiveData<SearchViewState> = MutableLiveData()
  private val querySubject = BehaviorSubject.create<String>()
  private val ageSubject = BehaviorSubject.createDefault("")
  private val typeSubject = BehaviorSubject.createDefault("")

  private var remoteSearchJob: Job = Job()
  private var currentPage = 0

  init {
    _state.value = SearchViewState()
  }

  fun onEvent(event: SearchEvent) {
    when(event) {
      is SearchEvent.PrepareForSearch -> prepareForSearch()
      else -> onSearchParametersUpdate(event)
    }
  }

  private fun onSearchParametersUpdate(event: SearchEvent) {
    remoteSearchJob.cancel(
        CancellationException("New search parameters incoming!")
    )

    when (event) {
      is SearchEvent.QueryInput -> updateQuery(event.input)
      is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
      is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
    }
  }

  private fun prepareForSearch() {
    loadFilterValues()
    setupSearchSubscription()
  }

  private fun loadFilterValues() {
    val exceptionHandler =
        createExceptionHandler(
            message = "Failed to get filter values!"
        )

    viewModelScope.launch(exceptionHandler) {
      val (ages, types) = withContext(dispatchersProvider.io()) { getSearchFilters() }

      updateStateWithFilterValues(ages, types)
    }
  }

  private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
    return viewModelScope.createExceptionHandler(message) {
      onFailure(it)
    }
  }

  private fun updateStateWithFilterValues(ages: List<String>, types: List<String>) {
    _state.value = state.value!!.updateToReadyToSearch(ages, types)
  }

  private fun setupSearchSubscription() {
    searchAnimals(querySubject, ageSubject, typeSubject)
        .observeOn(AndroidSchedulers.mainThread()) .subscribe(
            { onSearchResults(it) },
            { onFailure(it) }
        )
        .addTo(compositeDisposable)
  }

  private fun onSearchResults(searchResults: SearchResults) {
    val (animals, searchParameters) = searchResults
    if (animals.isEmpty()) {
      onEmptyCacheResults(searchParameters)
    } else {
      onAnimalList(animals)
    }
  }

  private fun onEmptyCacheResults(searchParameters: SearchParameters) {
    _state.value = state.value!!.updateToSearchingRemotely()
    searchRemotely(searchParameters)
  }

  private fun searchRemotely(searchParameters: SearchParameters) {
    val exceptionHandler = createExceptionHandler(message = "Failed to search remotely.")

    remoteSearchJob = viewModelScope.launch(exceptionHandler) {
      val pagination = withContext(dispatchersProvider.io()) {
        Logger.d("Searching remotely...")

        searchAnimalsRemotely(++currentPage, searchParameters)
      }

      onPaginationInfoObtained(pagination)
    }

    remoteSearchJob.invokeOnCompletion { it?.printStackTrace() }
  }

  private fun updateQuery(input: String) {
    resetPagination()
    querySubject.onNext(input)

    if (input.isEmpty()) {
      setNoSearchQueryState()
    } else {
      setSearchingState()
    }
  }

  private fun updateAgeValue(age: String) {
    ageSubject.onNext(age)
  }

  private fun updateTypeValue(type: String) {
    typeSubject.onNext(type)
  }

  private fun setSearchingState() {
    _state.value = state.value!!.updateToSearching()
  }

  private fun setNoSearchQueryState() {
    _state.value = state.value!!.updateToNoSearchQuery()
  }

  private fun onAnimalList(animals: List<Animal>) {
    _state.value =
        state.value!!.updateToHasSearchResults(animals.map { uiAnimalMapper.mapToView(it) })
  }

  private fun resetPagination() {
    currentPage = 0
  }

  private fun onPaginationInfoObtained(pagination: Pagination) {
    currentPage = pagination.currentPage
  }

  private fun onFailure(throwable: Throwable) {
    _state.value = if (throwable is NoMoreAnimalsException) {
      state.value!!.updateToNoResultsAvailable()
    } else {
      state.value!!.updateToHasFailure(throwable)
    }

  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
