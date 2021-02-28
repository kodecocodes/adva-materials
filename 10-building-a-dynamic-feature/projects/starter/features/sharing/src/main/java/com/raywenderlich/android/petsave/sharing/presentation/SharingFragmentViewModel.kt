package com.raywenderlich.android.petsave.sharing.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.common.domain.usecases.GetAnimalDetails
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.sharing.presentation.model.mappers.UiAnimalToShareMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharingFragmentViewModel @ViewModelInject constructor(
    private val getAnimalDetails: GetAnimalDetails,
    private val uiAnimalToShareMapper: UiAnimalToShareMapper,
    private val dispatchersProvider: DispatchersProvider
): ViewModel() {

  val viewState: StateFlow<SharingViewState> get() = _viewState

  private val _viewState = MutableStateFlow(SharingViewState())

  fun onEvent(event: SharingEvent) {
    when (event) {
      is SharingEvent.GetAnimalToShare -> getAnimalToShare(event.animalId)
    }
  }

  private fun getAnimalToShare(animalId: Long) {
    viewModelScope.launch {
      val animal = withContext(dispatchersProvider.io()) { getAnimalDetails(animalId) }

      _viewState.value = viewState.value.copy(
          animalToShare =  uiAnimalToShareMapper.mapToView(animal)
      )
    }
  }
}