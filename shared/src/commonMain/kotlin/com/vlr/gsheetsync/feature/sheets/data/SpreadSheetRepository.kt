package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog

class SpreadSheetRepository(private val service: SpreadSheetService) {

    suspend fun initialiseSheets(accessToken: Any): Boolean {
        return service.initialiseSheets(accessToken)
    }

}