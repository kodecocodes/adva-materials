package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.model.animal.details.Age
import com.realworld.android.petsave.common.domain.model.search.SearchFilters
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.DispatchersProvider
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class GetSearchFilters @Inject constructor(
  private val animalRepository: AnimalRepository,
  private val dispatchersProvider: DispatchersProvider
) {

  companion object {
    const val NO_FILTER_SELECTED = "Any"
  }

  suspend operator fun invoke(): SearchFilters {
    return withContext(dispatchersProvider.io()) {
      val unknown = Age.UNKNOWN.name
      val types = listOf(NO_FILTER_SELECTED) + animalRepository.getAnimalTypes()

      val ages = animalRepository.getAnimalAges()
        .map { age ->
          if (age.name == unknown) {
            NO_FILTER_SELECTED
          } else {
            age.name
              .uppercase()
              .replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) {
                  firstChar.titlecase(Locale.ROOT)
                } else {
                  firstChar.toString()
                }
              }
          }
        }

      return@withContext SearchFilters(ages, types)
    }
  }
}
