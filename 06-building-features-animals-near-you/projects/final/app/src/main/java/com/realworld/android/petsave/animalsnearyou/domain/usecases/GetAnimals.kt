package com.realworld.android.petsave.animalsnearyou.domain.usecases

import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimals @Inject constructor(private val animalRepository: AnimalRepository) {

  operator fun invoke() = animalRepository.getAnimals()
      .filter { it.isNotEmpty() }
}
