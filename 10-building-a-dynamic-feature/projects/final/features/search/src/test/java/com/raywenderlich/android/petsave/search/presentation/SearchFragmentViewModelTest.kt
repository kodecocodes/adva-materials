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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.raywenderlich.android.petsave.common.RxImmediateSchedulerRule
import com.raywenderlich.android.petsave.common.TestCoroutineRule
import com.raywenderlich.android.petsave.common.data.FakeRepository
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimals
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class SearchFragmentViewModelTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule = TestCoroutineRule()

  @get:Rule
  val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

  private lateinit var viewModel: SearchFragmentViewModel
  private lateinit var repository: FakeRepository
  private lateinit var getSearchFilters: GetSearchFilters
  private val uiAnimalsMapper = UiAnimalMapper()

  @Before
  fun setup() {
    val dispatchersProvider = object : DispatchersProvider {
      override fun io() = Dispatchers.Main
    }

    repository = FakeRepository()
    getSearchFilters = GetSearchFilters(repository)
    viewModel = SearchFragmentViewModel(
        SearchAnimalsRemotely(repository),
        SearchAnimals(repository),
        getSearchFilters,
        uiAnimalsMapper,
        dispatchersProvider,
        CompositeDisposable()
    )
  }

  @Test
  fun `SearchFragmentViewModel remote search with success`() = testCoroutineRule.runBlockingTest {
    // Given
    val (name, age, type) = repository.remotelySearchableAnimal
    val (ages, types) = getSearchFilters()

    val expectedRemoteAnimals = repository.remoteAnimals.map { uiAnimalsMapper.mapToView(it) }

    viewModel.state.observeForever { }

    val expectedViewState = SearchViewState(
        noSearchQuery = false,
        searchResults = expectedRemoteAnimals,
        ageFilterValues = Event(ages),
        typeFilterValues = Event(types),
        searchingRemotely = false,
        noRemoteResults = false
    )

    // When
    viewModel.onEvent(SearchEvent.PrepareForSearch)
    viewModel.onEvent(SearchEvent.TypeValueSelected(type))
    viewModel.onEvent(SearchEvent.AgeValueSelected(age))
    viewModel.onEvent(SearchEvent.QueryInput(name))

    // Then
    val viewState = viewModel.state.value!!

    assertThat(viewState).isEqualTo(expectedViewState)
  }
}