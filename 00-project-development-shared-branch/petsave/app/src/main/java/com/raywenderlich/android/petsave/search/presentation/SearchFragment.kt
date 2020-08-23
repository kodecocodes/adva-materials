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
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {

  private val binding get() = _binding!!
  private var _binding: FragmentSearchBinding? = null


  private val viewModel: SearchFragmentViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    _binding = FragmentSearchBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    observeViewStateUpdates()
    prepareForSearch()
  }

  private fun observeViewStateUpdates() {
    viewModel.state.observe(viewLifecycleOwner) {
      updateScreenState(it)
    }
  }

  private fun updateScreenState(newState: SearchViewState) {
    setupMenuValues(newState.ageMenuValues, R.id.age_dropdown)
    setupMenuValues(newState.typeMenuValues, R.id.type_dropdown)
  }

  private fun setupMenuValues(menuValues: List<String>, @IdRes dropdownId: Int) {
    if (menuValues.isEmpty()) return

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

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
