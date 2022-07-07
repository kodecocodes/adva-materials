package com.realworld.android.petsave.animalsnearyou.domain.usecases

import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.DispatchersProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RequestNextPageOfAnimals @Inject constructor(
  private val animalRepository: AnimalRepository,
  private val dispatchersProvider: DispatchersProvider
) {
  suspend operator fun invoke(
    pageToLoad: Int,
    pageSize: Int = Pagination.DEFAULT_PAGE_SIZE
  ): Pagination {
    return withContext(dispatchersProvider.io()) {
      val (animals, pagination) = animalRepository.requestMoreAnimals(pageToLoad, pageSize)

      if (animals.isEmpty()) {
        throw NoMoreAnimalsException("No animals nearby :(")
      }

      animalRepository.storeAnimals(animals)

      return@withContext pagination
    }
  }
}
