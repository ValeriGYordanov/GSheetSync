package com.vlr.gsheetsync.feature.sheets.data

expect class SpreadSheetService() {
    suspend fun initialiseSheets(accessToken: Any): Boolean
}