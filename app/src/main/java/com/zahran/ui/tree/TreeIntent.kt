package com.zahran.ui.tree

import com.zahran.data.model.Person

sealed interface TreeIntent {
    data class LoadMembers(val parentId: String) : TreeIntent
    data class NavigateToChild(val person: Person) : TreeIntent
    data object NavigateBack : TreeIntent
}
