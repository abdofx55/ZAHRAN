package com.zahran.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahran.data.repository.PersonRepository
import com.zahran.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val repository: PersonRepository
) : ViewModel() {

    val memberCountState: StateFlow<UiState<Int>> = repository.getFamilyCount()
        .map { count ->
            if (count > 0) UiState.Success(count) else UiState.Empty
        }
        .catch { error ->
            emit(UiState.Error(error.localizedMessage ?: "Unknown Error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
}
