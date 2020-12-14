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

package com.raywenderlich.android.petsave.onboarding.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class OnboardingFragment: Fragment() {


  private val binding get() = _binding!!
  private var _binding: FragmentOnboardingBinding? = null

  private val viewModel by viewModels<OnboardingFragmentViewModel>()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentOnboardingBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupUI()
    observeViewStateUpdates()
    observeViewEffects()
  }

  private fun setupUI() {
    setupPostcodeTextField()
    setupDistanceTextField()
    listenToSubmitButton()
  }

  private fun setupPostcodeTextField() {
    binding.postcodeInputText.doAfterTextChanged {
      viewModel.onEvent(OnboardingEvent.PostcodeChanged(it!!.toString()))
    }
  }

  private fun setupDistanceTextField() {
    binding.maxDistanceInputText.doAfterTextChanged {
      viewModel.onEvent(OnboardingEvent.DistanceChanged(it!!.toString()))
    }
  }

  private fun listenToSubmitButton() {
    binding.onboardingSubmitButton.setOnClickListener {
      viewModel.onEvent(OnboardingEvent.SubmitButtonClicked)
    }
  }

  private fun observeViewStateUpdates() {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.viewState.collect { render(it) }
    }
  }

  private fun render(state: OnboardingViewState) {
    with (binding) {
      postcodeTextInputLayout.error = resources.getString(state.postcodeError)
      maxDistanceTextInputLayout.error = resources.getString(state.distanceError)
      onboardingSubmitButton.isEnabled = state.submitButtonActive
    }
  }

  private fun observeViewEffects() {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.viewEffects.collect { reactTo(it) }
    }
  }

  private fun reactTo(effect: OnboardingViewEffect) {
    when (effect) {
      is OnboardingViewEffect.NavigateToAnimalsNearYou -> navigateToAnimalsNearYou()
    }
  }

  private fun navigateToAnimalsNearYou() {
    findNavController().navigate(R.id.action_onboardingFragment_to_animalsNearYou)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}