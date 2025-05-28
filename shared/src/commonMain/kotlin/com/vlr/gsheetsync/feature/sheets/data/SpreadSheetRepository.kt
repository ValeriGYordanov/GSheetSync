package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels


class SpreadSheetRepository(private val service: SpreadSheetService) {

    fun setAccessToken(accessToken: String) {
        service.setAccessToken(accessToken)
    }

    suspend fun setSpreadsheetId(url: String) {
        service.setSpreadsheetId(url)
    }

    suspend fun createSpreadsheet(title: String): Boolean {
        val spreadsheet = service.createSpreadsheet(title)
        return spreadsheet != null
    }

    suspend fun getSpreadsheet(googleSheetsUrl: String? = null): SheetSyncResponseModels.Spreadsheet? {
        return service.getSpreadsheet(googleSheetsUrl)
    }

    suspend fun createSheet(sheetTitle: String): Boolean {
        val sheet = service.createSheet(sheetTitle)
        return sheet != null
    }

    suspend fun deleteSheet(sheetTitle: String): Boolean {
        val sheet = service.deleteSheet(sheetTitle)
        return sheet != null
    }

    suspend fun getSheet(sheetTitle: String): SheetSyncResponseModels.Sheet? {
        return service.getSheet(sheetTitle)
    }

    suspend fun getData(from: String, to: String = from): Map<String, String> {
        return service.getData(from, to)
    }

    suspend fun updateData(updates: Map<String, String>) {
        //TBD
        service.updateData(updates)
    }

}