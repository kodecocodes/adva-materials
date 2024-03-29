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

package com.realworld.android.petsave.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.core.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.core.domain.model.animal.Animal
import com.realworld.android.petsave.core.domain.model.pagination.Pagination
import com.realworld.android.petsave.core.presentation.Event
import com.realworld.android.petsave.core.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.core.utils.DispatchersProvider
import com.realworld.android.petsave.core.utils.createExceptionHandler
import com.realworld.android.petsave.search.domain.model.SearchParameters
import com.realworld.android.petsave.search.domain.model.SearchResults
import com.realworld.android.petsave.search.domain.usecases.GetSearchFilters
import com.realworld.android.petsave.search.domain.usecases.SearchAnimals
import com.realworld.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getSearchFilters: GetSearchFilters,
    private val searchAnimals: SearchAnimals,
    private val searchAnimalsRemotely: SearchAnimalsRemotely,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  val state: LiveData<SearchViewState>
    get() = _state


  private val _state: MutableLiveData<SearchViewState> = MutableLiveData()
  private val querySubject = BehaviorSubject.create<String>()
  private val ageSubject = BehaviorSubject.createDefault<String>("")
  private val typeSubject = BehaviorSubject.createDefault<String>("")

  private var runningJobs = mutableListOf<Job>()
  private var isLastPage = false
  private var currentPage = 0

  init {
    _state.value = SearchViewState()
  }

  fun handleEvents(event: SearchEvent) {
    when(event) {
      is SearchEvent.PrepareForSearch -> prepareForSearch()
      is SearchEvent.QueryInput -> updateQuery(event.input)
      is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
      is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
    }
  }

  private fun prepareForSearch() {
    loadMenuValues()
    setupSearchSubscription()
  }

  private fun loadMenuValues() {
    val exceptionHandler = createExceptionHandler(message = "Failed to get menu values!")

    viewModelScope.launch(exceptionHandler) {
      val (ages, types) = withContext(dispatchersProvider.io()) { getSearchFilters() }
      updateStateWithMenuValues(ages, types)
    }
  }

  private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
    return viewModelScope.createExceptionHandler(message) {
      onFailure(it)
    }
  }

  private fun updateStateWithMenuValues(ages: List<String>, types: List<String>) {
    _state.value = state.value!!.copy(
        ageMenuValues = Event(ages),
        typeMenuValues = Event(types)
    )
  }

  private fun setupSearchSubscription() {
    searchAnimals(querySubject, ageSubject, typeSubject)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { runningJobs.map { it.cancel() } }
        .subscribe(
            { onSearchResults(it) },
            { onFailure(it) }
        )
        .addTo(compositeDisposable)
  }

  private fun updateQuery(input: String) {
    resetPagination()

    querySubject.onNext(input)

    if (input.isEmpty()) {
      setNoSearchQueryStateIf()
    } else {
      setSearchingState()
    }
  }


  private fun resetPagination() {
    currentPage = 0
    isLastPage = false
  }

  private fun setSearchingState() {
    _state.value = state.value!!.copy(noResultsState = false)
  }

  private fun setNoSearchQueryStateIf() {
    _state.value = state.value!!.copy(
        noSearchQueryState = true,
        searchResults = emptyList(),
        noResultsState = false
    )
  }

  private fun updateAgeValue(age: String) {
    ageSubject.onNext(age)
  }

  private fun updateTypeValue(type: String) {
    typeSubject.onNext(type)
  }

  private fun onSearchResults(searchResults: SearchResults) {
    val (animals, searchParameters) = searchResults

    if (animals.isEmpty()) {
      onEmptyCacheResults(searchParameters)
    } else {
      onAnimalList(animals)
    }
  }

  private fun onAnimalList(animals: List<Animal>) {
    _state.value = state.value!!.copy(
        noSearchQueryState = false,
        searchResults = animals.map { uiAnimalMapper.mapToView(it) },
        searchingRemotely = false,
        noResultsState = false
    )
  }

  private fun onEmptyCacheResults(searchParameters: SearchParameters) {
    searchRemotely(searchParameters)
    _state.value = state.value!!.copy(
        searchingRemotely = true,
        searchResults = emptyList()
    )
  }

  private fun searchRemotely(searchParameters: SearchParameters) {
    val exceptionHandler = createExceptionHandler(message = "Failed to search remotely.")

    val job = viewModelScope.launch(exceptionHandler) {
      val pagination = withContext(dispatchersProvider.io()) {
        Logger.d("Searching remotely...")

        searchAnimalsRemotely(++currentPage, searchParameters)
      }

      onPaginationInfoObtained(pagination)
    }

    runningJobs.add(job)

    job.invokeOnCompletion {
      it?.printStackTrace()
      runningJobs.remove(job)
    }
  }

  private fun onPaginationInfoObtained(pagination: Pagination) {
    currentPage = pagination.currentPage
    isLastPage = !pagination.canLoadMore
  }

  private fun onFailure(throwable: Throwable) {
    _state.value = if (throwable is NoMoreAnimalsException) {
      state.value!!.copy(searchingRemotely = false, noResultsState = true)
    } else {
      state.value!!.copy(failure = Event(throwable))
    }

  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
