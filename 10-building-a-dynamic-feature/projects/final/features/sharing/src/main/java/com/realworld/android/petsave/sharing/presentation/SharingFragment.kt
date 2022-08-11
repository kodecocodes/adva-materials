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

package com.realworld.android.petsave.sharing.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.common.utils.setImage
import com.realworld.android.petsave.di.SharingModuleDependencies
import com.realworld.android.petsave.sharing.databinding.FragmentSharingBinding
import com.realworld.android.petsave.sharing.di.DaggerSharingComponent
import com.realworld.android.petsave.sharing.di.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import javax.inject.Inject

class SharingFragment : Fragment() {

  companion object {
    const val ANIMAL_ID = "id"
  }

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val binding get() = _binding!!
  private var _binding: FragmentSharingBinding? = null

  private val viewModel by viewModels<SharingFragmentViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    DaggerSharingComponent.builder()
      .context(requireActivity())
      .moduleDependencies(
        EntryPointAccessors.fromApplication(
          requireActivity().applicationContext,
          SharingModuleDependencies::class.java
        )
      )
      .build()
      .inject(this)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentSharingBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupUI()
    subscribeToViewStateUpdates()
  }

  private fun setupUI() {
    val animalId = requireArguments().getLong(ANIMAL_ID)
    viewModel.onEvent(SharingEvent.GetAnimalToShare(animalId))

    binding.shareButton.setOnClickListener {
      Snackbar.make(requireView(), "Shared! Or not :]", Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  private fun subscribeToViewStateUpdates() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.viewState.collect { render(it) }
      }
    }
  }

  private fun render(viewState: SharingViewState) {
    val (image, message) = viewState.animalToShare

    binding.image.setImage(image)
    binding.messageToShareEditText.setText(message)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
