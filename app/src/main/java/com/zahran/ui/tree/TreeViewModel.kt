package com.zahran.ui.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zahran.data.model.Person
import com.zahran.data.repository.PersonRepository
import com.zahran.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val repository: PersonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TreeState())
    val state: StateFlow<TreeState> = _state.asStateFlow()

    private var currentLoadJob: Job? = null

    init {
        // Initial state loading for Root "Zahran" (ID = 1)
        val rootZahran = Person(
            id = "1",
            parentId = "root",
            name = "زهران",
            fullName = "زهران عمر",
            gen = 1,
            gender = 1
        )
        _state.update { it.copy(currentParent = rootZahran) }
        handleIntent(TreeIntent.LoadMembers("1"))
    }

    fun handleIntent(intent: TreeIntent) {
        viewModelScope.launch {
            when (intent) {
                is TreeIntent.LoadMembers -> loadMembers(intent.parentId)
                is TreeIntent.NavigateToChild -> {
                    val currentHistory = _state.value.navigationHistory.toMutableList()
                    _state.value.currentParent?.let { currentHistory.add(it) }
                    
                    _state.update { 
                        it.copy(
                            currentParent = intent.person,
                            navigationHistory = currentHistory
                        )
                    }
                    loadMembers(intent.person.id)
                }
                is TreeIntent.NavigateBack -> {
                    val currentHistory = _state.value.navigationHistory.toMutableList()
                    if (currentHistory.isNotEmpty()) {
                        val previousParent = currentHistory.removeAt(currentHistory.size - 1)
                        _state.update {
                            it.copy(
                                currentParent = previousParent,
                                navigationHistory = currentHistory
                            )
                        }
                        loadMembers(previousParent.id)
                    }
                }
            }
        }
    }

    private fun loadMembers(parentId: String) {
        currentLoadJob?.cancel()
        _state.update { it.copy(uiState = UiState.Loading) }
        
        currentLoadJob = repository.getFamilyMembers(parentId)
            .onEach { members ->
                _state.update { state ->
                    if (members.isEmpty()) {
                        state.copy(uiState = UiState.Empty)
                    } else {
                        state.copy(uiState = UiState.Success(members))
                    }
                }
            }
            .catch { error ->
                _state.update { 
                    it.copy(uiState = UiState.Error(error.localizedMessage ?: "Unknown Error"))
                }
            }
            .launchIn(viewModelScope)
    }
}
