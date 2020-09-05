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
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_HIGH_BOUNCY
import androidx.dynamicanimation.animation.SpringForce.STIFFNESS_VERY_LOW
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.common.presentation.model.UIAnimalDetailed
import com.raywenderlich.android.petsave.common.utils.setImage
import com.raywenderlich.android.petsave.common.utils.toEnglish
import com.raywenderlich.android.petsave.databinding.FragmentDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {

  companion object {
    const val ANIMAL_ID = "id"
  }

  private val binding get() = _binding!!
  private var _binding: FragmentDetailsBinding? = null

  private val viewModel: AnimalDetailsViewModel by viewModels()

  private var animalId: Long? = null

  private val springForce: SpringForce by lazy {
    SpringForce().apply {
      dampingRatio = DAMPING_RATIO_HIGH_BOUNCY
      stiffness = STIFFNESS_VERY_LOW
    }
  }

  private val callScaleXSpringAnimation: SpringAnimation by lazy {
    SpringAnimation(binding.call, DynamicAnimation.SCALE_X).apply {
      spring = springForce
    }
  }

  private val callScaleYSpringAnimation: SpringAnimation by lazy {
    SpringAnimation(binding.call, DynamicAnimation.SCALE_Y).apply {
      spring = springForce
    }
  }

  private val FLING_FRICTION = 2f

  private val callFlingXAnimation: FlingAnimation by lazy {
    FlingAnimation(binding.call, DynamicAnimation.X).apply {
      friction = FLING_FRICTION
      setMinValue(0f)
      setMaxValue(binding.root.width.toFloat() - binding.call.width.toFloat())
    }
  }

  private val callFlingYAnimation: FlingAnimation by lazy {
    FlingAnimation(binding.call, DynamicAnimation.Y).apply {
      friction = FLING_FRICTION
      setMinValue(0f)
      setMaxValue(binding.root.height.toFloat() - binding.call.width.toFloat())
    }
  }

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
    stopAnimation()
    binding.group.isVisible = true
    binding.name.text = animalDetails.name
    binding.description.text = animalDetails.description
    binding.image.setImage(animalDetails.photo)
    binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
    binding.specialNeeds.text = animalDetails.specialNeeds.toEnglish()

    binding.call.postDelayed({
      val finalScale = 1f
      callScaleXSpringAnimation.animateToFinalPosition(finalScale)
      callScaleYSpringAnimation.animateToFinalPosition(finalScale)
    }, 300L)

    val gestureListener = object: GestureDetector.SimpleOnGestureListener() {
      override fun onDoubleTap(e: MotionEvent?): Boolean {
        (binding.heartImage.drawable as Animatable?)?.start()
        return true
      }

      override fun onDown(e: MotionEvent?): Boolean {
        return true
      }

      override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float,
          velocityY: Float): Boolean {
        callFlingXAnimation.setStartVelocity(velocityX).start()
        callFlingYAnimation.setStartVelocity(velocityY).start()
        return true
      }
    }

    val gestureDetector = GestureDetector(requireContext(), gestureListener)

    binding.image.setOnTouchListener { v, event ->
      gestureDetector.onTouchEvent(event)
    }

    binding.call.setOnTouchListener { v, event ->
      gestureDetector.onTouchEvent(event)
    }

    callFlingYAnimation.addEndListener { _, _, _, _ ->
      if (areViewsOverlapping(binding.call, binding.image)) {
        Toast.makeText(requireContext(), "WIN WIN WIN", Toast.LENGTH_SHORT).show()
      }
    }

  }

  private fun displayError() {
    startAnimation(R.raw.lazy_cat)
    binding.group.isVisible = false
    Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
  }

  private fun displayLoading() {
    startAnimation(R.raw.happy_dog)
    binding.group.isVisible = false
  }

  private fun startAnimation(@RawRes animationRes: Int) {
    binding.lottieView.apply {
      isVisible = true
      setAnimation(animationRes)
      playAnimation()
    }
  }

  private fun stopAnimation() {
    binding.lottieView.apply {
      cancelAnimation()
      isVisible = false
    }
  }

  private fun areViewsOverlapping(view1: View, view2: View): Boolean {
    val firstRect = Rect()
    view1.getHitRect(firstRect)

    val secondRect = Rect()
    view2.getHitRect(secondRect)

    return Rect.intersects(firstRect, secondRect)
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}