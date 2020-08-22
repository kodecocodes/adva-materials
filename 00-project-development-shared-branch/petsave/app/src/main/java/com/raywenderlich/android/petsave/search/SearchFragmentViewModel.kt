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

package com.raywenderlich.android.petsave.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.core.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.core.utils.DispatchersProvider
import com.raywenderlich.android.petsave.core.utils.createExceptionHandler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SearchFragmentViewModel @ViewModelInject constructor(
    private val repository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  val viewState: LiveData<SearchViewState>
    get() = _viewState

  private val _viewState: MutableLiveData<SearchViewState> = MutableLiveData()

  fun handleEvents(event: SearchEvent) {
    when(event) {
      is SearchEvent.LoadMenuValues -> loadMenuValues()
      is SearchEvent.QueryInput -> searchAnimals(event.input)
      is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
      is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
    }
  }

  private fun loadMenuValues() {
    val errorMessage = "Failed to get menu values!"
    val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
      handleFailure(MenuValueException(it))
    }

    viewModelScope.launch(exceptionHandler) {
      val (ages, types) = withContext(dispatchersProvider.io()) {
        val ages = repository.getAnimalAges().map {
          it.name.toLowerCase(Locale.ROOT).capitalize()
        }

        val types = repository.getAnimalTypes()

        Pair(ages, types)
      }


    }
  }

  private fun searchAnimals(input: String) {
    TODO("Not yet implemented")
  }

  private fun updateAgeValue(age: String) {
    TODO("Not yet implemented")
  }

  private fun updateTypeValue(type: String) {
    TODO("Not yet implemented")
  }

  private fun handleFailure(exception: Exception) {

  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  class MenuValueException(throwable: Throwable): Exception(throwable)
}