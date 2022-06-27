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

package com.realworld.android.petsave.details.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.*
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
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.presentation.model.UIAnimalDetailed
import com.realworld.android.petsave.common.utils.setImage
import com.realworld.android.petsave.common.utils.toEmoji
import com.realworld.android.petsave.databinding.FragmentDetailsBinding
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
      savedInstanceState: Bundle?): View {
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
          displayPetDetails(state.animal, state.adopted)
        }
      }
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun displayPetDetails(animalDetails: UIAnimalDetailed, adopted: Boolean) {
    binding.call.scaleX = 0.6f
    binding.call.scaleY = 0.6f
    binding.call.isVisible = true
    binding.scrollView.isVisible = true
    stopAnimation()
    binding.name.text = animalDetails.name
    binding.description.text = animalDetails.description
    binding.image.setImage(animalDetails.photo)
    binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEmoji()
    binding.specialNeeds.text = animalDetails.specialNeeds.toEmoji()
    binding.declawed.text = animalDetails.declawed.toEmoji()
    binding.shotsCurrent.text = animalDetails.shotsCurrent.toEmoji()
    val doubleTapGestureListener = object: GestureDetector.SimpleOnGestureListener() {
      override fun onDoubleTap(e: MotionEvent): Boolean {
        (binding.heartImage.drawable as Animatable?)?.start()
        return true
      }

      override fun onDown(e: MotionEvent) = true
    }
    val doubleTapGestureDetector = GestureDetector(requireContext(), doubleTapGestureListener)

    binding.image.setOnTouchListener { v, event ->
      doubleTapGestureDetector.onTouchEvent(event)
    }

    callScaleXSpringAnimation.animateToFinalPosition(FLING_SCALE)
    callScaleYSpringAnimation.animateToFinalPosition(FLING_SCALE)

    val flingGestureListener = object: GestureDetector.SimpleOnGestureListener() {
      override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float,
          velocityY: Float): Boolean {
        callFlingXAnimation.setStartVelocity(velocityX).start()
        callFlingYAnimation.setStartVelocity(velocityY).start()
        return true
      }

      override fun onDown(e: MotionEvent) = true
    }
    val flingGestureDetector = GestureDetector(requireContext(), flingGestureListener)

    binding.call.setOnTouchListener { v, event ->
      flingGestureDetector.onTouchEvent(event)
    }

    callFlingYAnimation.addEndListener { _, _, _, _ ->
      if (areViewsOverlapping(binding.call, binding.image)) {
        val action = AnimalDetailsFragmentDirections.actionDetailsToSecret()
        findNavController().navigate(action)
      }
    }

    binding.adoptButton.setOnClickListener {
      binding.adoptButton.startLoading()
      viewModel.handleEvent(AnimalDetailsEvent.AdoptAnimal)
    }

    if (adopted) {
      binding.adoptButton.done()
    }
  }

  private fun displayError() {
    startAnimation(R.raw.lazy_cat)
    binding.scrollView.isVisible = false
    Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
  }

  private fun displayLoading() {
    startAnimation(R.raw.happy_dog)
    binding.scrollView.isVisible = false
  }

  private fun startAnimation(@RawRes animationRes: Int) {
    binding.loader.apply {
      isVisible = true
      setMinFrame(50)
      setMaxFrame(112)
      speed = 1.2f
      setAnimation(animationRes)
      playAnimation()
    }
    binding.loader.addValueCallback(
        KeyPath("icon_circle", "**"),
        LottieProperty.COLOR_FILTER
    ) {
      PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP)
    }
  }

  private fun stopAnimation() {
    binding.loader.apply {
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

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}