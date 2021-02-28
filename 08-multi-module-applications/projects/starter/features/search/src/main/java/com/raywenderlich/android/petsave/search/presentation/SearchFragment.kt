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

package com.raywenderlich.android.petsave.search.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.search.R
import com.raywenderlich.android.petsave.common.presentation.AnimalsAdapter
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.search.databinding.FragmentSearchBinding
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

  companion object {
    private const val ITEMS_PER_ROW = 2
  }

  private val binding get() = _binding!!
  private var _binding: FragmentSearchBinding? = null

  private val viewModel: SearchFragmentViewModel by viewModels()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
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
    return AnimalsAdapter()
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

  private fun prepareForSearch() {
    setupFilterListeners()
    setupSearchViewListener()
    viewModel.onEvent(SearchEvent.PrepareForSearch)
  }

  private fun setupSearchViewListener() {
    val searchView = binding.searchWidget.search
    searchView.setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean {
            viewModel.onEvent(SearchEvent.QueryInput(query.orEmpty()))
            searchView.clearFocus()
            return true
          }

          override fun onQueryTextChange(newText: String?): Boolean {
            viewModel.onEvent(SearchEvent.QueryInput(newText.orEmpty()))
            return true
          }
        }
    )
  }

  private fun setupFilterListeners() {
    with (binding.searchWidget) {
      setupFilterListenerFor(age) { item ->
        viewModel.onEvent(SearchEvent.AgeValueSelected(item))
      }

      setupFilterListenerFor(type) { item ->
        viewModel.onEvent(SearchEvent.TypeValueSelected(item))
      }
    }
  }

  private fun setupFilterListenerFor(
      filter: AutoCompleteTextView,
      block: (item: String) -> Unit
  ) {
    filter.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
      parent?.let { block(it.adapter.getItem(position) as String) }
    }
  }

  private fun updateScreenState(newState: SearchViewState, searchAdapter: AnimalsAdapter) {
    val (
        inInitialState,
        searchResults,
        ageFilterValues,
        typeFilterValues,
        searchingRemotely,
        noResultsState,
        failure
    ) = newState

    updateInitialStateViews(inInitialState)
    searchAdapter.submitList(searchResults)

    with (binding.searchWidget) {
      setupFilterValues(age, ageFilterValues.getContentIfNotHandled())
      setupFilterValues(type, typeFilterValues.getContentIfNotHandled())
    }

    updateRemoteSearchViews(searchingRemotely)
    updateNoResultsViews(noResultsState)
    handleFailures(failure)
  }

  private fun updateInitialStateViews(inInitialState: Boolean) {
    binding.initialSearchImageView.isVisible = inInitialState
    binding.initialSearchText.isVisible = inInitialState
  }

  private fun setupFilterValues(filter: AutoCompleteTextView, filterValues: List<String>?) {
    if (filterValues == null || filterValues.isEmpty()) return

    filter.setAdapter(createFilterAdapter(filterValues))
    filter.setText(GetSearchFilters.NO_FILTER_SELECTED, false)
  }

  private fun createFilterAdapter(adapterValues: List<String>): ArrayAdapter<String> {
    return ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, adapterValues)
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

    val fallbackMessage = getString(R.string.an_error_occurred)
    val snackbarMessage = if (unhandledFailure.message.isNullOrEmpty()) {
      fallbackMessage
    }
    else {
      unhandledFailure.message!!
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
