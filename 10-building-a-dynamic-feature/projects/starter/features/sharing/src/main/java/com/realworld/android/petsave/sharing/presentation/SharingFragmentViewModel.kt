package com.realworld.android.petsave.sharing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.common.domain.usecases.GetAnimalDetails
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.sharing.presentation.model.mappers.UiAnimalToShareMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharingFragmentViewModel @Inject constructor(
    private val getAnimalDetails: GetAnimalDetails,
    private val uiAnimalToShareMapper: UiAnimalToShareMapper
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
      val animal = getAnimalDetails(animalId)

      _viewState.update { oldState ->
        oldState.copy(animalToShare = uiAnimalToShareMapper.mapToView(animal))
      }
    }
  }
}
