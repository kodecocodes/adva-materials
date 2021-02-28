package com.raywenderlich.android.petsave.di

import com.raywenderlich.android.petsave.common.data.api.PetFinderApi
import com.raywenderlich.android.petsave.common.data.cache.Cache
import com.raywenderlich.android.petsave.common.data.preferences.Preferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharingModuleDependencies {
  fun petFinderApi(): PetFinderApi
  fun cache(): Cache
  fun preferences(): Preferences
}