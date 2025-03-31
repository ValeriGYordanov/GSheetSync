package com.vlr.gsheetsync.repository

import com.vlr.gsheetsync.model.SheetCell
import com.vlr.gsheetsync.model.SheetConfig
import com.vlr.gsheetsync.model.SheetRange
import com.vlr.gsheetsync.model.SheetUpdate
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

interface SheetRepository {
    suspend fun getCell(config: SheetConfig, row: Int, column: Int): SheetCell
    suspend fun updateCell(config: SheetConfig, cell: SheetCell): Boolean
    suspend fun getRange(config: SheetConfig, range: String): SheetRange
    fun observeCell(config: SheetConfig, row: Int, column: Int): Flow<SheetCell>
}

class GoogleSheetRepository(
    private val httpClient: HttpClient,
    private val json: Json
) : SheetRepository {
    private val baseUrl = "https://sheets.googleapis.com/v4/spreadsheets"

    override suspend fun getCell(config: SheetConfig, row: Int, column: Int): SheetCell {
        val range = "${getColumnName(column)}${row + 1}"
        val response = httpClient.get("$baseUrl/${config.spreadsheetId}/values/$range") {
            url {
                parameters.append("key", config.apiKey)
            }
        }
        
        return parseCellResponse(response, row, column)
    }

    override suspend fun updateCell(config: SheetConfig, cell: SheetCell): Boolean {
        val range = "${getColumnName(cell.column)}${cell.row + 1}"
        val response = httpClient.put("$baseUrl/${config.spreadsheetId}/values/$range") {
            url {
                parameters.append("key", config.apiKey)
                parameters.append("valueInputOption", "RAW")
            }
            setBody(listOf(listOf(cell.value)))
        }
        
        return response.status.isSuccess()
    }

    override suspend fun getRange(config: SheetConfig, range: String): SheetRange {
        val response = httpClient.get("$baseUrl/${config.spreadsheetId}/values/$range") {
            url {
                parameters.append("key", config.apiKey)
            }
        }
        
        return parseRangeResponse(response, config.sheetId)
    }

    override fun observeCell(config: SheetConfig, row: Int, column: Int): Flow<SheetCell> = flow {
        while (true) {
            emit(getCell(config, row, column))
            kotlinx.coroutines.delay(5000) // Poll every 5 seconds
        }
    }

    private fun getColumnName(column: Int): String {
        var result = ""
        var num = column
        while (num >= 0) {
            result = ('A' + (num % 26)).toString() + result
            num = num / 26 - 1
        }
        return result
    }

    private suspend fun parseCellResponse(response: HttpResponse, row: Int, column: Int): SheetCell {
        val jsonResponse = json.decodeFromString<Map<String, Any>>(response.bodyAsText())
        val values = jsonResponse["values"] as? List<List<String>>
        val value = values?.firstOrNull()?.firstOrNull() ?: ""
        return SheetCell(row, column, value)
    }

    private suspend fun parseRangeResponse(response: HttpResponse, sheetId: String): SheetRange {
        val jsonResponse = json.decodeFromString<Map<String, Any>>(response.bodyAsText())
        val values = jsonResponse["values"] as? List<List<String>> ?: emptyList()
        return SheetRange(sheetId, "", values)
    }
} 