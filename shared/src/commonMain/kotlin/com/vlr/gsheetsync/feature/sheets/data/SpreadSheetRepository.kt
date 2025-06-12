package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


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
        val result = service.getSpreadsheet(googleSheetsUrl)
        return Json.decodeFromString<SheetSyncResponseModels.Spreadsheet>(result.toString())
    }

    suspend fun setWorkingSheet(sheetTitle: String): Boolean {
        val sheet = service.setWorkingSheet(sheetTitle)
        return sheet != null
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
        val result = service.getSheet(sheetTitle)
        return Json.decodeFromString<SheetSyncResponseModels.Sheet>(result.toString())
    }

    suspend fun getData(from: String, to: String = from): Map<String, String> {
        return service.getData(from, to)
    }

    suspend fun clearCell(cell: String): Boolean {
        val result = service.clearCell(cell)
        return result.isEmpty()
    }

    suspend fun protectSheet(sheetTitle: String? = null): Boolean {
        val result = service.protectSheet(sheetTitle)
        return result != null
    }

    suspend fun protectAllSheets(): Boolean {
        val result = service.protectAllSheets()
        return result != null
    }

    suspend fun insertRow(index: Int): JsonElement? {
        return service.insertRow(index)
    }

    suspend fun deleteRow(index: Int): JsonElement? {
        return service.deleteRow(index)
    }

    suspend fun insertColumn(index: Int): JsonElement? {
        return service.insertColumn(index)
    }

    suspend fun deleteColumn(index: Int): JsonElement? {
        return service.deleteColumn(index)
    }

    suspend fun updateData(updates: Map<String, String>) {
        //TBD
        service.updateData(updates)
    }

}