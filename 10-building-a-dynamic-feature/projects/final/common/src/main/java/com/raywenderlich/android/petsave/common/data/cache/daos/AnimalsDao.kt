package com.raywenderlich.android.petsave.common.data.cache.daos

import androidx.room.*
import com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal.*
import io.reactivex.Flowable

@Dao
abstract class AnimalsDao {

  @Transaction
  @Query("SELECT * FROM animals ORDER BY animalId DESC")
  abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

  @Transaction
  @Query("SELECT * FROM animals WHERE animalId IS :animalId")
  abstract suspend fun getAnimal(animalId: Long): CachedAnimalAggregate

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertAnimalAggregate(
      animal: CachedAnimalWithDetails,
      photos: List<CachedPhoto>,
      videos: List<CachedVideo>,
      tags: List<CachedTag>
  )

  suspend fun insertAnimalsWithDetails(animalAggregates: List<CachedAnimalAggregate>) {
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
      WHERE name LIKE '%' || :name || '%' AND
      AGE LIKE '%' || :age || '%'
      AND type LIKE '%' || :type || '%'
  """)
  abstract fun searchAnimalsBy(
      name: String,
      age: String,
      type: String
  ): Flowable<List<CachedAnimalAggregate>>
}
