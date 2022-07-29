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

package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.model.animal.details.Age
import com.realworld.android.petsave.search.domain.model.SearchFilters
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.search.domain.model.MenuValueException
import java.util.*
import javax.inject.Inject

class GetSearchFilters @Inject constructor(
  private val animalRepository: AnimalRepository
) {

  companion object {
    private const val DEFAULT_VALUE = "Any"
    private const val DEFAULT_VALUE_LOWERCASE = "any"
  }

  suspend operator fun invoke(): SearchFilters {
    val types = animalRepository.getAnimalTypes()

    val filteringTypes = if (types.any { it.lowercase(Locale.ROOT) == DEFAULT_VALUE_LOWERCASE }) {
      types
    } else {
      listOf(DEFAULT_VALUE) + types
    }

    if (types.isEmpty()) throw MenuValueException("No animal types")

    val ages = animalRepository.getAnimalAges()
        .map { it.name }
        .replace(Age.UNKNOWN.name, DEFAULT_VALUE)
        .map { it.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

    return SearchFilters(ages, filteringTypes)
  }

  private fun List<String>.replace(old: String, new: String): List<String> {
    return map { if (it == old) new else it }
  }
}