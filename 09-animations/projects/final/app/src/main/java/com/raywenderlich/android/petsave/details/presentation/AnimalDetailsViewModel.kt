/*
 * Copyright (c) 2020 Razeware LLC
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

package com.raywenderlich.android.petsave.details.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.android.petsave.core.domain.model.animal.AnimalWithDetails
import com.raywenderlich.android.petsave.core.presentation.model.mappers.UiAnimalDetailsMapper
import com.raywenderlich.android.petsave.details.domain.usecases.AnimalDetails
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class AnimalDetailsViewModel @ViewModelInject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val animalDetails: AnimalDetails,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

  val state: LiveData<AnimalDetailsViewState> get() = _state
  private val _state = MutableLiveData<AnimalDetailsViewState>()

  init {
    _state.value = AnimalDetailsViewState.Loading
  }

  fun handleEvent(event: AnimalDetailsEvent) {
    when(event) {
      is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
    }
  }

  private fun subscribeToAnimalDetails(animalId: Long) {
    animalDetails(animalId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { onAnimalsDetails(it) },
            { onFailure(it) }
        )
        .addTo(compositeDisposable)
  }

  private fun onAnimalsDetails(animal: AnimalWithDetails) {
    val animalDetails = uiAnimalDetailsMapper.mapToView(animal)
    _state.value = AnimalDetailsViewState.AnimalDetails(animalDetails)
  }

  private fun onFailure(failure: Throwable) {
    _state.value = AnimalDetailsViewState.Failure
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}