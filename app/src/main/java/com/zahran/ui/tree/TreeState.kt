package com.zahran.ui.tree

import com.zahran.data.model.Person
import com.zahran.ui.UiState

data class TreeState(
    val uiState: UiState<List<Person>> = UiState.Loading,
    val currentParent: Person? = null,
    val navigationHistory: List<Person> = emptyList()
)
