package com.vlr.gsheetsync.feature.sheets.presentation

import com.vlr.gsheetsync.BaseViewModel
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.domain.SheetsUseCase
import com.vlr.gsheetsync.feature.sheets.presentation.model.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
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
        SyncLog.print("Initializing with TOKEN: $accessToken")
        scope.launch {
            //Delay is needed to let google authenticate the AccessToken.
            delay(5000)
            withContext(Dispatchers.IO) {
                _state.emit(SyncResult(loading = true))

                val result = useCase.initialiseSheets(accessToken)
                if (result.equals("success", true)) _state.emit(SyncResult(success = result))
                else _state.emit(SyncResult(error = result))
            }
        }
    }

    fun setAccessToken(accessToken: String) {
        SyncLog.print("Setting TOKEN: $accessToken")
        scope.launch {
            delay(3000)
            useCase.setAccessToken(accessToken)
            _state.emit(SyncResult(success = "Success"))
        }
    }

    fun setSpreadsheetId(sheetId: String) {
        SyncLog.print("Setting SPID: $sheetId")
        useCase.setSpreadsheetId(sheetId)
    }

    fun addText() {
        CoroutineScope(Dispatchers.IO).launch {
            _state.emit(SyncResult(loading = true))
            useCase.addText()
        }
    }
}