package com.realworld.android.petsave.common.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.api.utils.FakeServer
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import com.realworld.android.petsave.common.data.cache.RoomCache
import com.realworld.android.petsave.common.data.di.CacheModule
import com.realworld.android.petsave.common.data.di.PreferencesModule
import com.realworld.android.petsave.common.data.preferences.FakePreferences
import com.realworld.android.petsave.common.data.preferences.Preferences
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
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
  val hiltRule = HiltAndroidRule(this)

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var database: PetSaveDatabase

  @Inject
  lateinit var retrofitBuilder: Retrofit.Builder

  @Inject
  lateinit var apiAnimalMapper: ApiAnimalMapper

  @Inject
  lateinit var apiPaginationMapper: ApiPaginationMapper

  @BindValue
  val preferences: Preferences = FakePreferences()

  @Module
  @InstallIn(SingletonComponent::class)
  object TestCacheModule {
    @Provides
    fun provideRoomDatabase(): PetSaveDatabase {
      return Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().context,
        PetSaveDatabase::class.java
      )
        .allowMainThreadQueries()
        .build() }
  }

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
    val paginatedAnimals = repository.requestMoreAnimals(pageToLoad = 1, numberOfItems = 100)

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

      val paginatedAnimals = repository.requestMoreAnimals(pageToLoad = 1, numberOfItems = 100)
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
