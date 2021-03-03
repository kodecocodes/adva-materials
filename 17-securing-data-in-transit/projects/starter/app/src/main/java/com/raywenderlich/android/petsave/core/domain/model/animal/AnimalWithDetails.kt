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

package com.raywenderlich.android.petsave.core.domain.model.animal

import com.raywenderlich.android.petsave.core.domain.model.organization.Organization
import org.threeten.bp.LocalDateTime

data class AnimalWithDetails(
    val id: Long,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
) {

  fun withNoDetails(): Animal {
    return Animal(id, name, type, media, tags, adoptionStatus, publishedAt)
  }

  data class Details(
      val description: String,
      val age: Age,
      val species: String,
      val breed: Breed,
      val colors: Colors,
      val gender: Gender,
      val size: Size,
      val coat: Coat,
      val healthDetails: HealthDetails,
      val habitatAdaptation: HabitatAdaptation,
      val organization: Organization
  ) {
    enum class Age {
      UNKNOWN,
      BABY,
      YOUNG,
      ADULT,
      SENIOR
    }

    data class Breed(
        val primary: String,
        val secondary: String
    ) {
      val mixed: Boolean
        get() = primary.isNotEmpty() && secondary.isNotEmpty()

      val unknown: Boolean
        get() = primary.isEmpty() && secondary.isEmpty()
    }

    data class Colors(
        val primary: String,
        val secondary: String,
        val tertiary: String
    )

    enum class Gender {
      UNKNOWN,
      FEMALE,
      MALE
    }

    enum class Size {
      UNKNOWN,
      SMALL,
      MEDIUM,
      LARGE,
      EXTRA_LARGE
    }

    enum class Coat {
      UNKNOWN,
      SHORT,
      MEDIUM,
      LONG,
      WIRE,
      HAIRLESS,
      CURLY
    }

    data class HealthDetails(
        val isSpayedOrNeutered: Boolean,
        val isDeclawed: Boolean,
        val hasSpecialNeeds: Boolean,
        val shotsAreCurrent: Boolean
    )

    data class HabitatAdaptation(
        val goodWithChildren: Boolean,
        val goodWithDogs: Boolean,
        val goodWithCats: Boolean
    )
  }
}