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

package com.realworld.android.petsave.common.data.cache.daos

import androidx.room.*
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.*
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
abstract class AnimalsDao {

  @Transaction
  @Query("SELECT * FROM animals ORDER BY animalId DESC")
  abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

  @Transaction
  @Query("SELECT * FROM animals WHERE animalId IS :animalId")
  abstract fun getAnimal(
      animalId: Long
  ): Single<CachedAnimalAggregate>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  abstract fun insertAnimalAggregate(
      animal: CachedAnimalWithDetails,
      photos: List<CachedPhoto>,
      videos: List<CachedVideo>,
      tags: List<CachedTag>
  )

  fun insertAnimalsWithDetails(animalAggregates: List<CachedAnimalAggregate>) {
    for (animalAggregate in animalAggregates) {
      insertAnimalAggregate(
          animalAggregate.animal,
          animalAggregate.photos,
          animalAggregate.videos,
          animalAggregate.tags
      )
    }
  }

  @Query("SELECT DISTINCT type FROM animals")
  abstract suspend fun getAllTypes(): List<String>

  @Transaction
  @Query("""
      SELECT * FROM animals 
        WHERE upper(name) LIKE '%' || :name || '%' AND 
        AGE LIKE '%' || :age || '%' 
        AND type LIKE '%' || :type || '%'
  """)
  abstract fun searchAnimalsBy(
      name: String,
      age: String,
      type: String
  ): Flowable<List<CachedAnimalAggregate>>
}
