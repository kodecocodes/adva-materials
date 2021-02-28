package com.raywenderlich.android.petsave.sharing.presentation

sealed class SharingEvent {
  data class GetAnimalToShare(val animalId: Long): SharingEvent()
}
