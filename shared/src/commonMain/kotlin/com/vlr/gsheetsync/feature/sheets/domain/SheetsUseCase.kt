package com.vlr.gsheetsync.feature.sheets.domain

import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetRepository

class SheetsUseCase(private val repository: SpreadSheetRepository) {

    suspend fun initialiseSheets(accessToken: Any): Boolean {
        return repository.initialiseSheets(accessToken)
    }

}