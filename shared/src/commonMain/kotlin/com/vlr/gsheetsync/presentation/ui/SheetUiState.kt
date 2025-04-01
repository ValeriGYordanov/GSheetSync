package com.vlr.gsheetsync.presentation.ui

sealed class SheetUiState {
    object Initial : SheetUiState()
    object Loading : SheetUiState()
    object Connected : SheetUiState()
    data class Error(val message: String) : SheetUiState()
} 