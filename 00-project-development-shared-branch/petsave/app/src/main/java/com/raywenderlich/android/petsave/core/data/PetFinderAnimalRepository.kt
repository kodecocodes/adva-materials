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

package com.raywenderlich.android.petsave.core.data

import com.raywenderlich.android.petsave.core.data.api.PetFinderApi
import com.raywenderlich.android.petsave.core.data.api.model.mappers.ApiAnimalMapper
import com.raywenderlich.android.petsave.core.data.api.model.mappers.ApiPaginationMapper
import com.raywenderlich.android.petsave.core.data.cache.Cache
import com.raywenderlich.android.petsave.core.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.raywenderlich.android.petsave.core.data.cache.model.cachedorganization.CachedOrganization
import com.raywenderlich.android.petsave.core.domain.Result
import com.raywenderlich.android.petsave.core.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.core.domain.model.Pagination
import com.raywenderlich.android.petsave.core.domain.model.animal.Animal
import com.raywenderlich.android.petsave.core.domain.model.animal.AnimalWithDetails
import com.raywenderlich.android.petsave.core.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.core.utils.DispatchersProvider
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@ActivityRetainedScoped
class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper,
    dispatchersProvider: DispatchersProvider
): AnimalRepository {

  private val parentJob = SupervisorJob()
  private val repositoryScope = CoroutineScope(parentJob + dispatchersProvider.io())

  override fun getStoredNearbyAnimals(): Flowable<List<Animal>> {
    return cache.getNearbyAnimals()
        .distinctUntilChanged()
        .filter { it.isNotEmpty() }
        .map { animalList ->
          animalList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
        }
  }

  override suspend fun fetchAndStoreNearbyAnimals(
      pageToLoad: Int,
      numberOfItems: Int
  ): Result<Pagination> {
    // fetch these from shared preferences, after storing them in onboarding screen
    val postcode = "07097"
    val maxDistanceMiles = 100

    return try {
      val (apiAnimals, apiPagination) = api.getNearbyAnimals(
          pageToLoad,
          numberOfItems,
          postcode,
          maxDistanceMiles
      )

      val animals = apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty()
      val pagination = apiPaginationMapper.mapToDomain(apiPagination)

      if (animals.isEmpty()) {
        Result.Error(NoMoreAnimalsException("No animals nearby :("))
      } else {

        with(cache) {
          val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }
          storeOrganizations(organizations)
          storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
        }

        Result.Success(pagination)
      }
    } catch (exception: Exception) {
      exception.printStackTrace()
      Result.Error(exception)
    }
  }

  override fun getAllTypes(): Flowable<List<String>> {
    return cache.getAllTypes()
  }

  override fun getAllAges(): List<AnimalWithDetails.Details.Age> {
    return AnimalWithDetails.Details.Age.values().toList()
  }
}
