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

package com.realworld.android.petsave.common.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.RxImmediateSchedulerRule
import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.di.PreferencesModule
import com.realworld.android.petsave.common.data.preferences.FakePreferences
import com.realworld.android.petsave.common.data.preferences.Preferences
import com.realworld.android.petsave.common.data.api.utils.FakeServer
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import com.realworld.android.petsave.common.data.cache.RoomCache
import com.realworld.android.petsave.common.data.di.CacheModule
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import dagger.hilt.android.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(PreferencesModule::class, CacheModule::class)
class PetFinderAnimalRepositoryTest {

  private val fakeServer = FakeServer()
  private lateinit var repository: AnimalRepository
  private lateinit var api: PetFinderApi
  private lateinit var cache: Cache

  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @get:Rule
  val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var database: PetSaveDatabase

  @Inject
  lateinit var retrofitBuilder: Retrofit.Builder

  @Inject
  lateinit var apiAnimalMapper: ApiAnimalMapper

  @Inject
  lateinit var apiPaginationMapper: ApiPaginationMapper

  @BindValue
  @JvmField
  val preferences: Preferences = FakePreferences()

  @Before
  fun setup() {
    fakeServer.start()

    preferences.deleteTokenInfo()
    preferences.putToken("validToken")
    preferences.putTokenExpirationTime(Instant.now().plusSeconds(3600).epochSecond)
    preferences.putTokenType("Bearer")

    hiltRule.inject()

    api = retrofitBuilder
        .baseUrl(fakeServer.baseEndpoint)
        .build()
        .create(PetFinderApi::class.java)

    cache = RoomCache(database.animalsDao(), database.organizationsDao())

    repository = PetFinderAnimalRepository(
        api,
        cache,
        apiAnimalMapper,
        apiPaginationMapper
    )
  }

  @After
  fun teardown() {
    fakeServer.shutdown()
  }

  @Test
  fun requestMoreAnimals_success() = runBlocking {
    // Given
    val expectedAnimalId = 124L
    fakeServer.setHappyPathDispatcher()

    // When
    val paginatedAnimals = repository.requestMoreAnimals(1, 100)

    // Then
    val animal = paginatedAnimals.animals.first()
    assertThat(animal.id).isEqualTo(expectedAnimalId)
  }

  @Test
  fun insertAnimals_success() {
    // Given
    val expectedAnimalId = 124L

    runBlocking {
      fakeServer.setHappyPathDispatcher()

      val paginatedAnimals = repository.requestMoreAnimals(1, 100)
      val animal = paginatedAnimals.animals.first()

      // When
      repository.storeAnimals(listOf(animal))
    }

    // Then
    val testObserver = repository.getAnimals().test()

    testObserver.assertNoErrors()
    testObserver.assertNotComplete()
    testObserver.assertValue { it.first().id == expectedAnimalId }
  }
}
