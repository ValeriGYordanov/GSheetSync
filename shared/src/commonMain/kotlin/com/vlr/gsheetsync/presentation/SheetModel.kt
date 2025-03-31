package com.vlr.gsheetsync.presentation

import com.vlr.gsheetsync.model.SheetCell
import com.vlr.gsheetsync.model.SheetConfig
import com.vlr.gsheetsync.repository.SheetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SheetIntent {
    data class UpdateCell(val cell: SheetCell) : SheetIntent()
    data class ObserveCell(val row: Int, val column: Int) : SheetIntent()
    data class LoadRange(val range: String) : SheetIntent()
    object StopObserving : SheetIntent()
}

data class SheetState(
    val currentCell: SheetCell? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isObserving: Boolean = false
)

class SheetModel(
    private val repository: SheetRepository,
    private val config: SheetConfig,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _state = MutableStateFlow(SheetState())
    val state: StateFlow<SheetState> = _state.asStateFlow()

    fun processIntent(intent: SheetIntent) {
        when (intent) {
            is SheetIntent.UpdateCell -> updateCell(intent.cell)
            is SheetIntent.ObserveCell -> startObserving(intent.row, intent.column)
            is SheetIntent.LoadRange -> loadRange(intent.range)
            is SheetIntent.StopObserving -> stopObserving()
        }
    }

    private fun updateCell(cell: SheetCell) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val success = repository.updateCell(config, cell)
                if (success) {
                    _state.value = _state.value.copy(currentCell = cell)
                } else {
                    _state.value = _state.value.copy(error = "Failed to update cell")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun startObserving(row: Int, column: Int) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isObserving = true)
            repository.observeCell(config, row, column).collect { cell ->
                _state.value = _state.value.copy(currentCell = cell)
            }
        }
    }

    private fun stopObserving() {
        _state.value = _state.value.copy(isObserving = false)
    }

    private fun loadRange(range: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val sheetRange = repository.getRange(config, range)
                // Handle range data as needed
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }
} 