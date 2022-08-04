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

package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.search.domain.model.SearchParameters
import com.realworld.android.petsave.search.domain.model.SearchResults
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {

  companion object {
    private const val UI_EMPTY_VALUE = "Any"
  }

  operator fun invoke(
      querySubject: BehaviorSubject<String>,
      ageSubject: BehaviorSubject<String>,
      typeSubject: BehaviorSubject<String>
  ): Flowable<SearchResults> {
    val query = querySubject
        .debounce(500L, TimeUnit.MILLISECONDS)
        .map { it.trim() }
        .filter { it.length >= 2 || it.isEmpty() }
        .distinctUntilChanged()

    val age = ageSubject.replaceUIEmptyValue()
    val type = typeSubject.replaceUIEmptyValue()

    return Observable.combineLatest(query, age, type, combiningFunction)
        .toFlowable(BackpressureStrategy.LATEST)
        .filter { it.name.isNotEmpty() }
        .switchMap { parameters: SearchParameters ->
          animalRepository.searchCachedAnimalsBy(parameters)
        }
  }

  private val combiningFunction: Function3<String, String, String, SearchParameters>
    get() = Function3 {query, age, type -> SearchParameters(query, age, type) }

  private fun BehaviorSubject<String>.replaceUIEmptyValue(): Observable<String> {
    return map { if (it == UI_EMPTY_VALUE) "" else it }
  }
}
