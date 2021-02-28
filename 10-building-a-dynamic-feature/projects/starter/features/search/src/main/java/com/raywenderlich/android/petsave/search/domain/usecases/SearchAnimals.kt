package com.raywenderlich.android.petsave.search.domain.usecases

import com.raywenderlich.android.petsave.common.domain.model.search.SearchParameters
import com.raywenderlich.android.petsave.common.domain.model.search.SearchResults
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.functions.Function3

class SearchAnimals @Inject constructor(private val animalRepository: AnimalRepository) {

  private val combiningFunction: Function3<String, String, String, SearchParameters>
    get() = Function3 { query, age, type ->
      SearchParameters(query, age, type)
    }

  operator fun invoke(
      querySubject: BehaviorSubject<String>,
      ageSubject: BehaviorSubject<String>,
      typeSubject: BehaviorSubject<String>
  ): Flowable<SearchResults> {

    val query = querySubject
        .debounce(500L, TimeUnit.MILLISECONDS)
        .map { it.trim() }
        .filter { it.length >= 2 }

    val age = ageSubject.replaceUIEmptyValue()
    val type = typeSubject.replaceUIEmptyValue()

    return Observable.combineLatest(query, age, type, combiningFunction)
        .toFlowable(BackpressureStrategy.LATEST)
        .switchMap { parameters: SearchParameters ->
          animalRepository.searchCachedAnimalsBy(parameters)
        }
  }

  private fun BehaviorSubject<String>.replaceUIEmptyValue() = map {
    if (it == GetSearchFilters.NO_FILTER_SELECTED) "" else it
  }
}