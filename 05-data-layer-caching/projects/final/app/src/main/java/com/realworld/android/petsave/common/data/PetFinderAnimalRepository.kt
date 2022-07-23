package com.realworld.android.petsave.common.data

import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.realworld.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.model.pagination.PaginatedAnimals
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import io.reactivex.Flowable
import javax.inject.Inject

class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
) : AnimalRepository {

    private val postcode = "07097"
    private val maxDistanceMiles = 100

    override fun getAnimals(): Flowable<List<Animal>> {
      return cache.getNearbyAnimals()
          .distinctUntilChanged()
          .map { animalList ->
            animalList.map {
              it.animal.toAnimalDomain(
                  it.photos,
                  it.videos,
                  it.tags
              )
            }
          }
    }

    override suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals {
      val (apiAnimals, apiPagination) = api.getNearbyAnimals(
          pageToLoad,
          numberOfItems,
          postcode,
          maxDistanceMiles
      )

      return PaginatedAnimals( // 2
          apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
          apiPaginationMapper.mapToDomain(apiPagination)
      )
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
      val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }

      cache.storeOrganizations(organizations)
      cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }
}
