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

package com.realworld.android.petsave.common.data

import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.realworld.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization
import com.realworld.android.petsave.common.domain.model.pagination.PaginatedAnimals
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.animal.details.Age
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.search.domain.model.SearchParameters
import com.realworld.android.petsave.search.domain.model.SearchResults
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Flowable
import io.reactivex.Single
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
) : AnimalRepository {

  private val parentJob = SupervisorJob()
  private val repositoryScope = CoroutineScope(parentJob + dispatchersProvider.io())

  // fetch these from shared preferences, after storing them in onboarding screen
  private val postcode = "07097"
  private val maxDistanceMiles = 100

  override fun getAnimals(): Flowable<List<Animal>> {
    return cache.getNearbyAnimals()
        .distinctUntilChanged()
        .map { animalList ->
          animalList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
        }
  }

  override fun getAnimal(animalId: Long): Single<AnimalWithDetails> {
    return cache.getAnimal(animalId)
        .flatMap { animal ->
          cache.getOrganization(animal.animal.organizationId)
              .map {
                animal.animal.toDomain(animal.photos, animal.videos, animal.tags, it)
              }
        }
  }

  override suspend fun requestMoreAnimals(
      pageToLoad: Int,
      numberOfItems: Int
  ): PaginatedAnimals {
    val (apiAnimals, apiPagination) = api.getNearbyAnimals(
        pageToLoad,
        numberOfItems,
        postcode,
        maxDistanceMiles
    )

    return PaginatedAnimals(
        apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
        apiPaginationMapper.mapToDomain(apiPagination)
    )
  }

  override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
    // Organizations have a 1-to-many relation with animals, so we need to insert them first in
    // order for Room not to complain about foreign keys being invalid (since we have the
    // organizationId as a foreign key in the animals table)
    val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }

    cache.storeOrganizations(organizations)
    cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
  }

  override suspend fun getAnimalTypes(): List<String> {
    return cache.getAllTypes()
  }

  override fun getAnimalAges(): List<Age> {
    return Age.values().toList()
  }

  override fun searchCachedAnimalsBy(searchParameters: SearchParameters): Flowable<SearchResults> {
    return cache.searchAnimalsBy(
        searchParameters.uppercaseName,
        searchParameters.uppercaseAge,
        searchParameters.uppercaseType
    )
        .map { animalList ->
          animalList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
        }
        .map { SearchResults(it, searchParameters) }
  }

  override suspend fun searchAnimalsRemotely(
      pageToLoad: Int,
      searchParameters: SearchParameters,
      numberOfItems: Int
  ): PaginatedAnimals {

    val (apiAnimals, apiPagination) = api.searchAnimalsBy(
        searchParameters.name,
        searchParameters.age,
        searchParameters.type,
        pageToLoad,
        numberOfItems,
        postcode,
        maxDistanceMiles
    )

    return PaginatedAnimals(
        apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
        apiPaginationMapper.mapToDomain(apiPagination)
    )
  }
}
