package com.vlr.gsheetsync.feature.sheets.domain

import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetRepository
import com.vlr.gsheetsync.feature.sheets.domain.model.UseCaseError
import com.vlr.gsheetsync.feature.sheets.domain.model.GSyncResult

class SheetsUseCase(private val repository: SpreadSheetRepository) {

    suspend fun setAccessToken(accessToken: String): GSyncResult<Unit> {
        return callWithErrorHandling { repository.setAccessToken(accessToken) }
    }

    suspend fun setSpreadsheetId(url: String): GSyncResult<Unit> {
        return callWithErrorHandling {
            repository.setSpreadsheetId(url)
        }
    }

    suspend fun createSpreadsheet(title: String): GSyncResult<Boolean> {
        return callWithErrorHandling { repository.createSpreadsheet(title) }
    }

    suspend fun getSpreadsheet(googleSheetsUrl: String? = null): GSyncResult<Boolean> {
        return callWithErrorHandling {
            //TO BE IMPLEMENTED
            val result = repository.getSpreadsheet(googleSheetsUrl)
            result != null
        }
    }

    suspend fun createSheet(sheetTitle: String): GSyncResult<Boolean> {
        return callWithErrorHandling { repository.createSheet(sheetTitle) }
    }

    suspend fun deleteSheet(sheetTitle: String): GSyncResult<Boolean> {
        return callWithErrorHandling { repository.deleteSheet(sheetTitle) }
    }

    suspend fun getSheet(sheetTitle: String): GSyncResult<Boolean> {
        return callWithErrorHandling {
            //TO BE IMPLEMENTED
            val result = repository.getSheet(sheetTitle)
            result != null
        }
    }

    suspend fun getData(from: String, to: String = from): GSyncResult<Map<String, String>> {
        return callWithErrorHandling { repository.getData(from, to) }
    }

    suspend fun updateData(updates: Map<String, String>): GSyncResult<Unit> {
        return callWithErrorHandling { repository.updateData(updates) }
    }

    private suspend fun <T>callWithErrorHandling(function: suspend () -> T): GSyncResult<T> {
        return try {
            val result = function.invoke()
            SyncLog.print("Success for: $function")
            SyncLog.print("Result: " + result.toString())
            GSyncResult.Success(result, "Success")
        } catch (e: Exception) {
            SyncLog.print("Issue: " + e.stackTraceToString())
            GSyncResult.Error(UseCaseError())
        }
    }
}