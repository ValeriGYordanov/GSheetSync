package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels.Spreadsheet
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels.ValueRange
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels.BatchUpdateValuesRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SpreadSheetServiceGPT(private val client: HttpClient) {

    private val baseUrl = "https://sheets.googleapis.com/v4/spreadsheets"
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private var token: String = ""
    private var spreadsheetId: String? = "1px0triZ-w_8UH122HDSNjbAHJf1LAMi6s92twPJP2_0"
    private var sheetName = "Initial"

    fun setAccessToken(accessToken: String) {
        token = accessToken
    }

    fun setSpreadsheetId(sheetId: String) {
        spreadsheetId = sheetId
    }

    //Chat GPT Methods
    suspend fun createSpreadsheet(title: String): Spreadsheet? {
        val body = mapOf("properties" to mapOf("title" to title))
        return client.post(baseUrl) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun getSpreadsheet(): Spreadsheet? {
        return client.get("$baseUrl/$spreadsheetId") {
            bearerAuth(token)
        }.body<Spreadsheet?>()
    }

    suspend fun createSheet(sheetTitle: String): Spreadsheet? {
        val body = mapOf(
            "requests" to listOf(
                mapOf("addSheet" to mapOf("properties" to mapOf("title" to sheetTitle)))
            )
        )
        return client.post("$baseUrl/$spreadsheetId:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun deleteSheet(sheetTitle: String): Spreadsheet {
        val sheetId = getSheet(sheetTitle)?.properties?.sheetId
        val body = mapOf(
            "requests" to listOf(
                mapOf("deleteSheet" to mapOf("sheetId" to sheetId))
            )
        )
        return client.post("$baseUrl/$spreadsheetId:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun getSheet(sheetTitle: String): SheetSyncResponseModels.Sheet? {
        return getSpreadsheet()?.sheets?.firstOrNull { it.properties?.title == sheetTitle }
    }

    //END


    /**
     * Fetches and processes cell values from a Google Sheet within a specified range.
     * @throws IllegalArgumentException if cell references are invalid
     */
    suspend fun getData(from: String, to: String = from): Map<String, String> {
        require(from.isValidCellReference()) { "Invalid 'from' cell reference: $from" }
        require(to.isValidCellReference()) { "Invalid 'to' cell reference: $to" }

        val range = "$sheetName!$from:$to"
        val response = client.get("$baseUrl/$spreadsheetId/values/$range") {
            bearerAuth(token)
        }.bodyAsText()

        val sheetValues = Json.decodeFromString<ValueRange>(response)
        return extractCellValues(sheetValues, from, to)
    }

    /**
     * Extracts cell values with boundary checks and validation.
     * @throws IllegalArgumentException if ranges are invalid
     */
    private fun extractCellValues(
        valueRange: ValueRange,
        fromCell: String,
        toCell: String
    ): Map<String, String> {
        val (gridStartCol, gridStartRow) = valueRange.range.parseSheetRange()
        val (targetStartCol, targetStartRow) = fromCell.parseCellReference()
        val (targetEndCol, targetEndRow) = toCell.parseCellReference()

        require(targetStartCol <= targetEndCol) { "Start column must be <= end column" }
        require(targetStartRow <= targetEndRow) { "Start row must be <= end row" }

        return valueRange.values.flatMapIndexed { rowIndex, rowValues ->
            val currentRow = gridStartRow + rowIndex
            when {
                currentRow < targetStartRow -> emptyList()
                currentRow > targetEndRow -> emptyList()
                else -> rowValues.mapIndexedNotNull { colIndex, value ->
                    val currentCol = gridStartCol + colIndex
                    when {
                        value.isBlank() -> null
                        currentCol < targetStartCol -> null
                        currentCol > targetEndCol -> null
                        else -> "${currentCol.toColumnName()}$currentRow" to value
                    }
                }
            }
        }.toMap()
    }

    /**
     * Updates multiple cells in a Google Sheet with safety checks and validation.
     *
     * @param updates Map of cell addresses to values (e.g., mapOf("A1" to "Test", "B2" to "42"))
     * @return API response as JSON string
     * @throws IllegalArgumentException if any cell reference is invalid
     */
    suspend fun updateData(updates: Map<String, String>): String {
        require(updates.isNotEmpty()) { "Updates cannot be empty" }
        updates.keys.forEach { cellRef ->
            require(cellRef.isValidCellReference()) {
                "Invalid cell reference: '$cellRef'. Must be in A1 notation (e.g., 'B3')"
            }
        }

        val body = buildValuesFor(updates)
        return client.post("$baseUrl/$spreadsheetId/values:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    /**
     * Builds the JSON request body for batch updates with validation.
     *
     * @param updates Map of cell addresses to values
     * @return JSON string ready for API request
     * @throws IllegalArgumentException if any value is blank
     */
    private fun buildValuesFor(updates: Map<String, String>): String {
        updates.values.forEach { value ->
            require(value.isNotBlank()) { "Cell values cannot be blank" }
        }

        val cellUpdates = updates.map { (range, value) ->
            ValueRange(
                range = "$sheetName!$range",
                values = listOf(listOf(value))
            )
        }

        val request = BatchUpdateValuesRequest(data = cellUpdates)
        return json.encodeToString(request)
    }

}