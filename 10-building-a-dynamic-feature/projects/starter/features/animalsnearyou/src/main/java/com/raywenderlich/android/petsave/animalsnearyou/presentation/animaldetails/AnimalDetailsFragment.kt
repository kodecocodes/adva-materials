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

package com.raywenderlich.android.petsave.animalsnearyou.presentation.animaldetails

import android.os.Bundle
import android.view.*
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.animalsnearyou.R
import com.raywenderlich.android.petsave.animalsnearyou.databinding.FragmentDetailsBinding
import com.raywenderlich.android.petsave.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import com.raywenderlich.android.petsave.common.utils.setImage
import com.raywenderlich.android.petsave.common.utils.toEnglish
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {

  companion object {
    const val ANIMAL_ID = "id"
  }

  private val binding get() = _binding!!
  private var _binding: FragmentDetailsBinding? = null

  private val viewModel: AnimalDetailsFragmentViewModel by viewModels()

  private var animalId: Long? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    animalId = requireArguments().getLong(ANIMAL_ID)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentDetailsBinding.inflate(inflater, container, false)
    
    setHasOptionsMenu(true)
    return binding.root
  }


  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_share, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == R.id.share) {
      navigateToSharing()
      true
    }
    else {
      super.onOptionsItemSelected(item)
    }
  }

  private fun navigateToSharing() {
    val animalId = requireArguments().getLong(ANIMAL_ID)

    val deepLink = NavDeepLinkRequest.Builder
        .fromUri("petsave://sharing/$animalId".toUri())
        .build()

    findNavController().navigate(deepLink)
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    observeState()
    val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
    viewModel.handleEvent(event)
  }

  private fun observeState() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        is AnimalDetailsViewState.Loading -> { displayLoading() }
        is AnimalDetailsViewState.Failure -> { displayError() }
        is AnimalDetailsViewState.AnimalDetails -> { displayPetDetails(state.animal) }
      }
    }
  }

  private fun displayPetDetails(animalDetails: UIAnimalDetailed) {
    binding.group.isVisible = true
    binding.name.text = animalDetails.name
    binding.description.text = animalDetails.description
    binding.image.setImage(animalDetails.photo)
    binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
    binding.specialNeeds.text = animalDetails.specialNeeds.toEnglish()
  }

  private fun displayError() {
    binding.group.isVisible = false
    Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
  }

  private fun displayLoading() {
    binding.group.isVisible = false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}