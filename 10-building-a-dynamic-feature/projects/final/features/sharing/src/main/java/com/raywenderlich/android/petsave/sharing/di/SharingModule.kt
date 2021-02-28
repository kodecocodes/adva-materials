package com.raywenderlich.android.petsave.sharing.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.raywenderlich.android.petsave.common.data.PetFinderAnimalRepository
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.common.utils.CoroutineDispatchersProvider
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.sharing.presentation.SharingFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap

@Module
@DisableInstallInCheck
abstract class SharingModule {

  // These two are not scoped to SingletonComponent, so they can't be directly provided through
  // methods in SharingModuleDependencies.
  @Binds
  abstract fun bindDispatchersProvider(
      dispatchersProvider: CoroutineDispatchersProvider
  ): DispatchersProvider

  @Binds
  abstract fun bindRepository(repository: PetFinderAnimalRepository): AnimalRepository

  @Binds
  @IntoMap
  @ViewModelKey(SharingFragmentViewModel::class)
  abstract fun bindSharingFragmentViewModel(
      sharingFragmentViewModel: SharingFragmentViewModel
  ): ViewModel

  @Binds
  @Reusable
  abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}