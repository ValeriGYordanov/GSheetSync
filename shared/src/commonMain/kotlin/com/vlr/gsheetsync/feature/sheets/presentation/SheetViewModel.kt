package com.vlr.gsheetsync.feature.sheets.presentation

import com.vlr.gsheetsync.BaseViewModel
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.domain.SheetsUseCase
import com.vlr.gsheetsync.feature.sheets.domain.model.GBaseError
import com.vlr.gsheetsync.feature.sheets.domain.model.GSyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SheetViewModel(private val useCase: SheetsUseCase): BaseViewModel() {

    private val _setAccessTokenState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val setAccessTokenState: StateFlow<GSyncResult<Any?>?> get() = _setAccessTokenState

    private val _setSpreadsheetIdState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val setSpreadsheetIdState: StateFlow<GSyncResult<Any?>?> get() = _setSpreadsheetIdState

    private val _createSpreadsheetState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val createSpreadsheetState: StateFlow<GSyncResult<Any?>?> get() = _createSpreadsheetState

    private val _getSpreadsheetState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val getSpreadsheetState: StateFlow<GSyncResult<Any?>?> get() = _getSpreadsheetState

    private val _createSheetState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val createSheetState: StateFlow<GSyncResult<Any?>?> get() = _createSheetState

    private val _deleteSheetState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val deleteSheetState: StateFlow<GSyncResult<Any?>?> get() = _deleteSheetState

    private val _getSheetState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val getSheetState: StateFlow<GSyncResult<Any?>?> get() = _getSheetState

    private val _getDataState: MutableStateFlow<GSyncResult<Map<String, String>>?> = MutableStateFlow(null)
    val getDataState: StateFlow<GSyncResult<Map<String, String>>?> get() = _getDataState

    private val _updateDataState: MutableStateFlow<GSyncResult<Any?>?> = MutableStateFlow(null)
    val updateDataState: StateFlow<GSyncResult<Any?>?> get() = _updateDataState

    //Generic Loading and Error States!
    private val _loadingState: MutableStateFlow<GSyncResult.Loading?> = MutableStateFlow(GSyncResult.Loading(true))
    val loadingState: StateFlow<GSyncResult.Loading?> get() = _loadingState

    private val _errorState: MutableStateFlow<GSyncResult.Error?> = MutableStateFlow(null)
    val errorState: StateFlow<GSyncResult.Error?> get() = _errorState

    init {
        SyncLog.print("ViewModel: System created")
    }

    fun setAccessToken(accessToken: String) {
        SyncLog.print("ViewModel: Setting TOKEN: $accessToken")
        scope.launchWithResult(
            delayMillis = 5000,
            dataFlow = _setAccessTokenState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.setAccessToken(accessToken)
        }
    }

    fun setSpreadsheetId(url: String) {
        SyncLog.print("ViewModel: Setting Sheet ID: $url")
        scope.launchWithResult(
            dataFlow = _setSpreadsheetIdState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.setSpreadsheetId(url)
        }
    }

    fun createSpreadsheet(title: String) {
        SyncLog.print("ViewModel: Creating Spreadsheet: $title")
        scope.launchWithResult(
            dataFlow = _createSpreadsheetState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.createSpreadsheet(title)
        }
    }

    fun getSpreadsheet(googleSheetsUrl: String? = null) {
        SyncLog.print("ViewModel: Getting Spreadsheet")
        scope.launchWithResult(
            dataFlow = _getSpreadsheetState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.getSpreadsheet(googleSheetsUrl)
        }
    }

    fun createSheet(sheetTitle: String) {
        SyncLog.print("ViewModel: Creating Sheet: $sheetTitle")
        scope.launchWithResult(
            dataFlow = _createSheetState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.createSheet(sheetTitle)
        }
    }

    fun deleteSheet(sheetTitle: String) {
        SyncLog.print("ViewModel: Deleting Sheet: $sheetTitle")
        scope.launchWithResult(
            dataFlow = _deleteSheetState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.deleteSheet(sheetTitle)
        }
    }

    fun getSheet(sheetTitle: String) {
        SyncLog.print("ViewModel: Getting Sheet: $sheetTitle")
        scope.launchWithResult(
            dataFlow = _getSheetState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.getSheet(sheetTitle)
        }
    }

    fun getData(from: String, to: String = from) {
        SyncLog.print("ViewModel: Getting Data from $from to $to")
        scope.launchWithResult(
            delayMillis = 2000,
            dataFlow = _getDataState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.getData(from, to)
        }
    }

    fun updateData(updates: Map<String, String>) {
        SyncLog.print("ViewModel: Updating Data: $updates")
        scope.launchWithResult(
            dataFlow = _updateDataState,
            errorFlow = _errorState,
            loadingFlow = _loadingState
        ) {
            useCase.updateData(updates)
        }
    }

    private fun <T>CoroutineScope.launchWithResult(
        delayMillis: Long = 0,
        dataFlow: MutableStateFlow<GSyncResult<T>?>,
        errorFlow: MutableStateFlow<GSyncResult.Error?> = _errorState,
        loadingFlow: MutableStateFlow<GSyncResult.Loading?> = _loadingState,
        function: suspend () -> GSyncResult<T>
    ): Job {
        return launch {
            loadingFlow.emit(GSyncResult.Loading(true))
            if (delayMillis > 0) delay(delayMillis)
            when (val result = function()) {
                is GSyncResult.Success -> {
                    SyncLog.print("ViewModel: Success for: $dataFlow")
                    dataFlow.emit(result)
                }

                is GSyncResult.Error -> {
                    SyncLog.print("ViewModel: Error for: $dataFlow")
                    errorFlow.emit(result)
                }

                else -> {
                    SyncLog.print("ViewModel: Error for: $dataFlow")
                    errorFlow.emit(GSyncResult.Error(GBaseError("Unknown Client Error!")))
                }
            }
            loadingFlow.emit(GSyncResult.Loading(false))
        }
    }

}