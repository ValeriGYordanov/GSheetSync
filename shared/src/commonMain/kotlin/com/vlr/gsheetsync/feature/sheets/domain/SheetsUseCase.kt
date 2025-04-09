package com.vlr.gsheetsync.feature.sheets.domain

import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetRepository

class SheetsUseCase(private val repository: SpreadSheetRepository) {

    fun setAccessToken(accessToken: String) {
        repository.setAccessToken(accessToken)
    }

    fun setSpreadsheetId(sheetId: String) {
        repository.setSpreadsheetId(sheetId)
    }

    suspend fun initialiseSheets(accessToken: Any): String {
        return repository.initialiseSheets(accessToken)
    }

    suspend fun addText() {
        return repository.addText()
    }

}