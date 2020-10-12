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

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.*
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.core.presentation.model.UIAnimalDetailed
import com.raywenderlich.android.petsave.core.utils.setImage
import com.raywenderlich.android.petsave.core.utils.toEnglish
import com.raywenderlich.android.petsave.databinding.FragmentDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {

  companion object {
    const val ANIMAL_ID = "id"
  }

  private val FLING_SCALE = 1f

  private val binding get() = _binding!!
  private var _binding: FragmentDetailsBinding? = null

  private val viewModel: AnimalDetailsViewModel by viewModels()

  private var animalId: Long? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    animalId = requireArguments().getLong(ANIMAL_ID)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    _binding = FragmentDetailsBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    observerState()
    val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
    viewModel.handleEvent(event)
  }

  private fun observerState() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        is AnimalDetailsViewState.Loading -> {
          displayLoading()
        }
        is AnimalDetailsViewState.Failure -> {
          displayError()
        }
        is AnimalDetailsViewState.AnimalDetails -> {
          displayPetDetails(state.animal)
        }
      }
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun displayPetDetails(animalDetails: UIAnimalDetailed) {
    binding.group.isVisible = true
    stopAnimation()
    binding.name.text = animalDetails.name
    binding.description.text = animalDetails.description
    binding.image.setImage(animalDetails.photo)
    binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
    binding.specialNeeds.text = animalDetails.specialNeeds.toEnglish()

    val doubleTapGestureListener = object: GestureDetector.SimpleOnGestureListener() {
      override fun onDoubleTap(e: MotionEvent): Boolean {
        //TODO: start animation on double tap
        return true
      }

      override fun onDown(e: MotionEvent) = true
    }
    val doubleTapGestureDetector = GestureDetector(requireContext(), doubleTapGestureListener)

    binding.image.setOnTouchListener { v, event ->
      doubleTapGestureDetector.onTouchEvent(event)
    }

    //TODO: start scaling Spring Animation

    //TODO: Create and set fling Gesture Listener

    //TODO: Add end listener for fling animation
  }

  private fun displayError() {
    startAnimation()
    binding.group.isVisible = false
    Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
  }

  private fun displayLoading() {
    startAnimation()
    binding.group.isVisible = false
  }

  //TODO: add method parameter for animation resource
  private fun startAnimation() {
    //TODO: Replace with code to initialize and start Lottie animation
    binding.loader.isVisible = true
  }

  private fun stopAnimation() {
    //TODO: Replace with code to stop Lottie animation
    binding.loader.isVisible = false
  }

  private fun areViewsOverlapping(view1: View, view2: View): Boolean {
    val firstRect = Rect()
    view1.getHitRect(firstRect)

    val secondRect = Rect()
    view2.getHitRect(secondRect)

    return Rect.intersects(firstRect, secondRect)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}