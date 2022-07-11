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

package com.realworld.android.petsave.search.presentation

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.presentation.AnimalClickListener
import com.realworld.android.petsave.common.presentation.AnimalsAdapter
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import okio.IOException
import retrofit2.HttpException

@AndroidEntryPoint
class SearchFragment: Fragment() {

  private val binding get() = _binding!!
  private var _binding: FragmentSearchBinding? = null

  companion object {
    private const val ITEMS_PER_ROW = 2
  }

  private val viewModel: SearchFragmentViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View {
    _binding = FragmentSearchBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupUI()
    prepareForSearch()
  }

  private fun setupUI() {
    val adapter = createAdapter()
    setupRecyclerView(adapter)
    observeViewStateUpdates(adapter)
  }

  private fun createAdapter(): AnimalsAdapter {
    return AnimalsAdapter().apply {
      setOnAnimalClickListener(object: AnimalClickListener {
        override fun onClick(animalId: Long) {
          val action = SearchFragmentDirections.actionSearchToDetails(animalId)
          findNavController().navigate(action)
        }
      })
    }
  }

  private fun setupRecyclerView(searchAdapter: AnimalsAdapter) {
    binding.searchRecyclerView.apply {
      adapter = searchAdapter
      layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
      setHasFixedSize(true)
    }
  }

  private fun observeViewStateUpdates(searchAdapter: AnimalsAdapter) {
    viewModel.state.observe(viewLifecycleOwner) {
      updateScreenState(it, searchAdapter)
    }
  }

  private fun updateScreenState(newState: SearchViewState, searchAdapter: AnimalsAdapter) {
    val (
        inInitialState,
        searchResults,
        ageMenuValues,
        typeMenuValues,
        searchingRemotely,
        noResultsState,
        failure
    ) = newState

    updateInitialStateViews(inInitialState)
    searchAdapter.submitList(searchResults)
    setupMenuValues(ageMenuValues.getContentIfNotHandled(), R.id.age_dropdown)
    setupMenuValues(typeMenuValues.getContentIfNotHandled(), R.id.type_dropdown)
    updateRemoteSearchViews(searchingRemotely)
    updateNoResultsViews(noResultsState)
    handleFailures(failure)
  }

  private fun updateInitialStateViews(inInitialState: Boolean) {
    binding.initialSearchImageView.isVisible = inInitialState
    binding.initialSearchText.isVisible = inInitialState
  }

  private fun setupMenuValues(menuValues: List<String>?, @IdRes dropdownId: Int) {
    if (menuValues == null || menuValues.isEmpty()) return

    val dropdown: AutoCompleteTextView =
        binding.collapsibleSearchParamsContainer.findViewById(dropdownId)

    setupValuesFor(dropdown, menuValues)
  }

  private fun setupValuesFor(dropdown: AutoCompleteTextView, dropdownValues: List<String>) {
    dropdown.setAdapter(createMenuAdapter(dropdownValues))
    dropdown.setText(dropdownValues.first(), false)
  }

  private fun createMenuAdapter(adapterValues: List<String>): ArrayAdapter<String> {
    return ArrayAdapter(
        requireContext(),
        R.layout.dropdown_menu_popup_item,
        adapterValues
    )
  }

  private fun prepareForSearch() {
    setupDropdownMenuListeners()
    setupSearchViewListener()
    viewModel.handleEvents(SearchEvent.PrepareForSearch)
  }

  private fun setupSearchViewListener() {
    val searchView: SearchView = binding.collapsibleSearchParamsContainer.findViewById(R.id.search)

    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.handleEvents(SearchEvent.QueryInput(query.orEmpty()))
        searchView.clearFocus()

        return true
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.handleEvents(SearchEvent.QueryInput(newText.orEmpty()))

        return true
      }
    })
  }

  private fun setupDropdownMenuListeners() {
    setupDropdownMenuListenerFor(R.id.age_dropdown) { item ->
      viewModel.handleEvents(SearchEvent.AgeValueSelected(item))
    }

    setupDropdownMenuListenerFor(R.id.type_dropdown) { item ->
      viewModel.handleEvents(SearchEvent.TypeValueSelected(item))
    }
  }

  private fun setupDropdownMenuListenerFor(
      @IdRes dropdownMenu: Int,
      block: (item: String) -> Unit
  ) {
    val dropdown: AutoCompleteTextView =
        binding.collapsibleSearchParamsContainer.findViewById(dropdownMenu)

    dropdown.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
      parent?.let {
        block(it.adapter.getItem(position) as String)
      }
    }
  }

  private fun updateRemoteSearchViews(searchingRemotely: Boolean) {
    binding.searchRemotelyProgressBar.isVisible = searchingRemotely
    binding.searchRemotelyText.isVisible = searchingRemotely
  }

  private fun updateNoResultsViews(noResultsState: Boolean) {
    binding.noSearchResultsImageView.isVisible = noResultsState
    binding.noSearchResultsText.isVisible = noResultsState
  }

  private fun handleFailures(failure: Event<Throwable>?) {
    val unhandledFailure = failure?.getContentIfNotHandled() ?: return

    handleThrowable(unhandledFailure)
  }

  private fun handleThrowable(exception: Throwable) {
    val fallbackMessage = getString(R.string.an_error_occurred)
    val snackbarMessage = when (exception) {
      is NoMoreAnimalsException -> exception.message ?: fallbackMessage
      is IOException, is HttpException -> fallbackMessage
      else -> ""
    }

    if (snackbarMessage.isNotEmpty()) {
      Snackbar.make(requireView(), snackbarMessage, Snackbar.LENGTH_SHORT).show()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
