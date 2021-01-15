package com.raywenderlich.android.petsave.sharing.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.common.utils.setImage
import com.raywenderlich.android.petsave.di.SharingModuleDependencies
import com.raywenderlich.android.petsave.sharing.databinding.FragmentSharingBinding
import com.raywenderlich.android.petsave.sharing.di.DaggerSharingComponent
import com.raywenderlich.android.petsave.sharing.di.ViewModelFactory
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
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
    observeViewStateUpdates()
  }

  private fun setupUI() {
    val animalId = requireArguments().getLong(ANIMAL_ID)
    viewModel.onEvent(SharingEvent.GetAnimalToShare(animalId))

    binding.shareButton.setOnClickListener {
      Snackbar.make(requireView(), "Shared! Or not :]", Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  private fun observeViewStateUpdates() {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.viewState.collect { render(it) }
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