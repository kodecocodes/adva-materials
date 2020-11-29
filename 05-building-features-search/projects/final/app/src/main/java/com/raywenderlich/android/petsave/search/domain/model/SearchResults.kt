package com.raywenderlich.android.petsave.search.domain.model

import com.raywenderlich.android.petsave.common.domain.model.animal.Animal

data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)