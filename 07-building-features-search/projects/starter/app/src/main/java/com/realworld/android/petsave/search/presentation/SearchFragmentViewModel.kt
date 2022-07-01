/*
 * Copyright (c) 2022 razeware LLC
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  val state: LiveData<SearchViewState> get() = _state

  private val _state: MutableLiveData<SearchViewState> = MutableLiveData()
  private val querySubject = BehaviorSubject.create<String>()
  private val ageSubject = BehaviorSubject.createDefault("")
  private val typeSubject = BehaviorSubject.createDefault("")

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
    // Add code here
  }

  private fun prepareForSearch() {
    // Add code here
  }

  private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
    return viewModelScope.createExceptionHandler(message) {
      onFailure(it)
    }
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
