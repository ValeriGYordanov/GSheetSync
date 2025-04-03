package com.vlr.gsheetsync.feature.sheets.presentation

import com.vlr.gsheetsync.BaseViewModel
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.domain.SheetsUseCase
import com.vlr.gsheetsync.feature.sheets.presentation.model.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SheetViewModel(private val useCase: SheetsUseCase): BaseViewModel() {

    private val _state: MutableStateFlow<SyncResult> = MutableStateFlow(SyncResult())
    val state: StateFlow<SyncResult> get() = _state

    init {
        SyncLog.print("System created")
    }

    fun initialiseService(accessToken: Any) {
        scope.launch {
            withContext(Dispatchers.IO) {
                _state.emit(SyncResult(loading = true))

                val result = useCase.initialiseSheets(accessToken)
                if (result) _state.emit(SyncResult(success = result))
                else _state.emit(SyncResult(error = "Something went wrong"))
            }
        }
    }
}