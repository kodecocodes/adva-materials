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

package com.raywenderlich.android.petsave.common.data.api.model.mappers

import com.raywenderlich.android.petsave.common.data.api.model.ApiAnimal
import com.raywenderlich.android.petsave.common.domain.model.animal.AdoptionStatus
import com.raywenderlich.android.petsave.common.domain.model.animal.Media
import com.raywenderlich.android.petsave.common.domain.model.animal.details.*
import com.raywenderlich.android.petsave.common.domain.model.organization.Organization
import com.raywenderlich.android.petsave.common.utils.DateTimeUtils
import java.util.*
import javax.inject.Inject

class ApiAnimalMapper @Inject constructor(
    private val apiBreedsMapper: ApiBreedsMapper,
    private val apiColorsMapper: ApiColorsMapper,
    private val apiHealthDetailsMapper: ApiHealthDetailsMapper,
    private val apiHabitatAdaptationMapper: ApiHabitatAdaptationMapper,
    private val apiPhotoMapper: ApiPhotoMapper,
    private val apiVideoMapper: ApiVideoMapper,
    private val apiContactMapper: ApiContactMapper
): ApiMapper<ApiAnimal, AnimalWithDetails> {

  override fun mapToDomain(apiEntity: ApiAnimal): AnimalWithDetails {
    return AnimalWithDetails(
        id = apiEntity.id ?: throw MappingException("Animal ID cannot be null"),
        name = apiEntity.name.orEmpty(),
        type = apiEntity.type.orEmpty(),
        details = parseAnimalDetails(apiEntity),
        media = mapMedia(apiEntity),
        tags = apiEntity.tags.orEmpty().map { it.orEmpty() },
        adoptionStatus = parseAdoptionStatus(apiEntity.status),
        publishedAt = DateTimeUtils.parse(apiEntity.publishedAt.orEmpty()) // throws exception if empty
    )
  }

  private fun parseAnimalDetails(apiAnimal: ApiAnimal): Details {
    return Details(
        description = apiAnimal.description.orEmpty(),
        age = parseAge(apiAnimal.age),
        species = apiAnimal.species.orEmpty(),
        breed = apiBreedsMapper.mapToDomain(apiAnimal.breeds),
        colors = apiColorsMapper.mapToDomain(apiAnimal.colors),
        gender = parserGender(apiAnimal.gender),
        size = parseSize(apiAnimal.size),
        coat = parseCoat(apiAnimal.coat),
        healthDetails = apiHealthDetailsMapper.mapToDomain(apiAnimal.attributes),
        habitatAdaptation = apiHabitatAdaptationMapper.mapToDomain(apiAnimal.environment),
        organization = mapOrganization(apiAnimal)
    )
  }

  private fun parseAge(age: String?): Age {
    if (age.isNullOrEmpty()) return Age.UNKNOWN

    // will throw IllegalStateException if the string does not match any enum value
    return Age.valueOf(age.toUpperCase(Locale.ROOT))
  }

  private fun parserGender(gender: String?): Gender {
    if (gender.isNullOrEmpty()) return Gender.UNKNOWN

    return Gender.valueOf(gender.toUpperCase(Locale.ROOT))
  }

  private fun parseSize(size: String?): Size {
    if (size.isNullOrEmpty()) return Size.UNKNOWN

    return Size.valueOf(
        size.replace(' ', '_').toUpperCase(Locale.ROOT)
    )
  }

  private fun parseCoat(coat: String?): Coat {
    if (coat.isNullOrEmpty()) return Coat.UNKNOWN

    return Coat.valueOf(coat.toUpperCase(Locale.ROOT))
  }

  private fun mapMedia(apiAnimal: ApiAnimal): Media {
    return Media(
        photos = apiAnimal.photos?.map { apiPhotoMapper.mapToDomain(it) }.orEmpty(),
        videos = apiAnimal.videos?.map { apiVideoMapper.mapToDomain(it) }.orEmpty()
    )
  }

  private fun parseAdoptionStatus(status: String?): AdoptionStatus {
    if (status.isNullOrEmpty()) return AdoptionStatus.UNKNOWN

    return AdoptionStatus.valueOf(status.toUpperCase(Locale.ROOT))
  }

  private fun mapOrganization(apiAnimal: ApiAnimal): Organization {
    return Organization(
        id = apiAnimal.organizationId ?: throw MappingException("Organization ID cannot be null"),
        contact = apiContactMapper.mapToDomain(apiAnimal.contact),
        distance = apiAnimal.distance ?: -1f
    )
  }
}
