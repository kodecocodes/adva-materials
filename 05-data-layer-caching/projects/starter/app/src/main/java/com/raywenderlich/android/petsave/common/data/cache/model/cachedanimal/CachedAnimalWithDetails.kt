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

package com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raywenderlich.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization
import com.raywenderlich.android.petsave.common.domain.model.animal.AdoptionStatus
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.animal.Media
import com.raywenderlich.android.petsave.common.domain.model.animal.details.*
import com.raywenderlich.android.petsave.common.utils.DateTimeUtils

@Entity(
    tableName = "animals",
    foreignKeys = [
      ForeignKey(
          entity = CachedOrganization::class,
          parentColumns = ["organizationId"],
          childColumns = ["organizationId"],
          onDelete = CASCADE
      )
    ],
    indices = [Index("organizationId")]
)
data class CachedAnimalWithDetails(
    @PrimaryKey
    val animalId: Long,
    val organizationId: String,
    val name: String,
    val type: String,
    val description: String,
    val age: String,
    val species: String,
    val primaryBreed: String,
    val secondaryBreed: String,
    val primaryColor: String,
    val secondaryColor: String,
    val tertiaryColor: String,
    val gender: String,
    val size: String,
    val coat: String,
    val isSpayedOrNeutered: Boolean,
    val isDeclawed: Boolean,
    val hasSpecialNeeds: Boolean,
    val shotsAreCurrent: Boolean,
    val goodWithChildren: Boolean,
    val goodWithDogs: Boolean,
    val goodWithCats: Boolean,
    val adoptionStatus: String,
    val publishedAt: String
) {
  companion object {
    fun fromDomain(domainModel: AnimalWithDetails): CachedAnimalWithDetails {
      val details = domainModel.details
      val healthDetails = details.healthDetails
      val habitatAdaptation = details.habitatAdaptation

      return CachedAnimalWithDetails(
          animalId = domainModel.id,
          organizationId = details.organization.id,
          name = domainModel.name,
          type = domainModel.type,
          description = details.description,
          age = details.age.toString(),
          species = details.species,
          primaryBreed = details.breed.primary,
          secondaryBreed = details.breed.secondary,
          primaryColor = details.colors.primary,
          secondaryColor = details.colors.secondary,
          tertiaryColor = details.colors.tertiary,
          gender = details.gender.toString(),
          size = details.size.toString(),
          coat = details.coat.toString(),
          isSpayedOrNeutered = healthDetails.isSpayedOrNeutered,
          isDeclawed = healthDetails.isDeclawed,
          hasSpecialNeeds = healthDetails.hasSpecialNeeds,
          shotsAreCurrent = healthDetails.shotsAreCurrent,
          goodWithChildren = habitatAdaptation.goodWithChildren,
          goodWithDogs = habitatAdaptation.goodWithDogs,
          goodWithCats = habitatAdaptation.goodWithCats,
          adoptionStatus = domainModel.adoptionStatus.toString(),
          publishedAt = domainModel.publishedAt.toString()
      )
    }
  }

  fun toDomain(
      photos: List<CachedPhoto>,
      videos: List<CachedVideo>,
      tags: List<CachedTag>,
      organization: CachedOrganization
  ): AnimalWithDetails {
    return AnimalWithDetails(
        id = animalId,
        name = name,
        type = type,
        details = mapDetails(organization),
        media = Media(
            photos = photos.map { it.toDomain() },
            videos = videos.map { it.toDomain() }
        ),
        tags = tags.map { it.tag },
        adoptionStatus = AdoptionStatus.valueOf(adoptionStatus),
        publishedAt = DateTimeUtils.parse(publishedAt)
    )
  }

  fun toAnimalDomain(
      photos: List<CachedPhoto>,
      videos: List<CachedVideo>,
      tags: List<CachedTag>): Animal {
    return Animal(
        id = animalId,
        name = name,
        type = type,
        media = Media(
            photos = photos.map { it.toDomain() },
            videos = videos.map { it.toDomain() }
        ),
        tags = tags.map { it.tag },
        adoptionStatus = AdoptionStatus.valueOf(adoptionStatus),
        publishedAt = DateTimeUtils.parse(publishedAt)
    )
  }

  private fun mapDetails(organization: CachedOrganization): Details {
    return Details(
        description = description,
        age = Age.valueOf(age),
        species = species,
        breed = Breed(primaryBreed, secondaryBreed),
        colors = Colors(primaryColor, secondaryColor, tertiaryColor),
        gender = Gender.valueOf(gender),
        size = Size.valueOf(size),
        coat = Coat.valueOf(coat),
        healthDetails = HealthDetails(isSpayedOrNeutered, isDeclawed,
            hasSpecialNeeds, shotsAreCurrent),
        habitatAdaptation = HabitatAdaptation(goodWithChildren, goodWithDogs,
            goodWithCats),
        organization = organization.toDomain()
    )
  }
}
