package com.raywenderlich.android.petsave.sharing.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    var creator: Provider<out ViewModel>? = viewModels[modelClass]

    if (creator == null) {
      for ((key, value) in viewModels) {
        if (modelClass.isAssignableFrom(key)) {
          creator = value
          break
        }
      }
    }

    if (creator == null) {
      throw IllegalArgumentException("Unknown viewModel class $modelClass")
    }

    try {
      @Suppress("UNCHECKED_CAST")
      return creator.get() as T
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
}