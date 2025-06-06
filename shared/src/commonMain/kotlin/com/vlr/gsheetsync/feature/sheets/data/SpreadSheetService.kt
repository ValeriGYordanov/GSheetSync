package com.vlr.gsheetsync.feature.sheets.data

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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Service for interacting with Google Sheets API.
 * Provides functionality to manage spreadsheets and sheets including creation, retrieval, and deletion.
 *
 * @property client The HTTP client used for API requests
 */
class SpreadSheetService(private val client: HttpClient) {

    // region Constants
    private companion object {
        const val BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets"
    }

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    // endregion

    // region Properties
    private lateinit var token: String
    private lateinit var spreadsheetId: String //default value: "1px0triZ-w_8UH122HDSNjbAHJf1LAMi6s92twPJP2_0"
    private lateinit var sheetName: String //default value: "Initial"
    // endregion

    // region Authentication & Configuration
    /**
     * Sets the access token for Google Sheets API authentication.
     *
     * @param accessToken The OAuth2 access token
     * @throws IllegalArgumentException if the token is empty
     */
    fun setAccessToken(accessToken: String) {
        require(accessToken.isNotEmpty()) { "Access token cannot be empty" }
        token = accessToken
    }

    /**
     * Extracts and sets the spreadsheet ID from a Google Sheets URL.
     *
     * @param url The Google Sheets URL containing the document ID
     * @throws IllegalArgumentException if the URL is invalid
     */
    suspend fun setSpreadsheetId(url: String) {
        val pattern = """/d/([a-zA-Z0-9-_]+)""".toRegex()
        val sheetId = pattern.find(url)?.groupValues?.get(1)
        require(sheetId != null) { "Invalid Google Sheets URL: $url" }
        spreadsheetId = sheetId
        getSpreadsheet()?.sheets?.firstOrNull()?.properties?.let {
            sheetName = it.title?:""
        }
    }
    //endregion

    // region Spreadsheet Operations
    /**
     * Creates a new spreadsheet with the specified title.
     *
     * @param title The title of the new spreadsheet
     * @return The created spreadsheet or null if the request fails
     */
    suspend fun createSpreadsheet(title: String): Spreadsheet? {
        require(title.isNotEmpty()) { "Title cannot be empty" }
        val body = mapOf("properties" to mapOf("title" to title))
        return client.post(BASE_URL) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    /**
     * Retrieves spreadsheet information.
     *
     * @param googleSheetsUrl Optional URL to set the spreadsheet ID before retrieval
     * @return The spreadsheet information or null if not found
     */
    suspend fun getSpreadsheet(googleSheetsUrl: String? = null): Spreadsheet? {
        googleSheetsUrl?.let { setSpreadsheetId(it) }
        return client.get("$BASE_URL/$spreadsheetId") {
            bearerAuth(token)
        }.body<Spreadsheet?>()
    }
    // endregion

    // region Sheet Operations
    /**
     * Creates a new sheet in the current spreadsheet.
     *
     * @param sheetTitle The title of the new sheet
     * @return The updated spreadsheet information or null if the request fails
     */
    suspend fun createSheet(sheetTitle: String): Spreadsheet? {
        require(sheetTitle.isNotEmpty()) { "Title cannot be empty" }
        val body = mapOf(
            "requests" to listOf(
                mapOf("addSheet" to mapOf("properties" to mapOf("title" to sheetTitle)))
            )
        )
        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    /**
     * Deletes a sheet from the current spreadsheet.
     *
     * @param sheetTitle The title of the sheet to delete
     * @return The updated spreadsheet information
     * @throws IllegalStateException if the sheet cannot be found
     */
    suspend fun deleteSheet(sheetTitle: String): Spreadsheet? {
        require(sheetTitle.isNotEmpty()) { "Title cannot be empty" }
        val sheetId = getSheet(sheetTitle)?.properties?.sheetId
            ?: throw IllegalStateException("Sheet '$sheetTitle' not found")

        val body = mapOf(
            "requests" to listOf(
                mapOf("deleteSheet" to mapOf("sheetId" to sheetId))
            )
        )
        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    /**
     * Retrieves information about a specific sheet.
     *
     * @param sheetTitle The title of the sheet to retrieve
     * @return The sheet information or null if not found
     */
    suspend fun getSheet(sheetTitle: String): SheetSyncResponseModels.Sheet? {
        return getSpreadsheet()?.sheets?.firstOrNull { it.properties?.title == sheetTitle }
    }
    // endregion

    //region Cell Operations
    /**
     * Fetches and processes cell values from a Google Sheet within a specified range.
     * @throws IllegalArgumentException if cell references are invalid
     */
    suspend fun getData(from: String, to: String = from): Map<String, String> {
        require(from.isValidCellReference()) { "Invalid 'from' cell reference: $from" }
        require(to.isValidCellReference()) { "Invalid 'to' cell reference: $to" }

        val range = "$sheetName!$from:$to"
        val response = client.get("$BASE_URL/$spreadsheetId/values/$range") {
            bearerAuth(token)
        }.bodyAsText()

        val sheetValues = Json.decodeFromString<ValueRange>(response)
        return extractCellValues(sheetValues, from, to)
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
        return client.post("$BASE_URL/$spreadsheetId/values:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    suspend fun clearCell(cell: String): String {
        require(cell.isValidCellReference()) {
            "Invalid cell reference: '$cell'. Must be in A1 notation (e.g., 'B3')"
        }

        val range = "$sheetName!$cell"
        return client.post("$BASE_URL/$spreadsheetId/values/$range:clear") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("{}")
        }.bodyAsText()
    }

    suspend fun clearCells(range: String): String {
        require(range.matches(Regex("""^[A-Z]+\d+(:[A-Z]+\d+)?$"""))) {
            "Invalid range: '$range'. Must be in A1 notation (e.g., 'A1:A10' or 'B2:D4')"
        }

        val fullRange = "$sheetName!$range"
        return client.post("$BASE_URL/$spreadsheetId/values/$fullRange:clear") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("{}")
        }.bodyAsText()
    }

    //endregion

    //region Row & Column Operations

    suspend fun insertRow(sheetId: Int, rowIndex: Int): String {
        require(rowIndex >= 0) { "Row index must be non-negative" }

        val body = mapOf(
            "requests" to listOf(
                mapOf(
                    "insertDimension" to mapOf(
                        "range" to mapOf(
                            "sheetId" to sheetId,
                            "dimension" to "ROWS",
                            "startIndex" to rowIndex,
                            "endIndex" to rowIndex + 1
                        ),
                        "inheritFromBefore" to false
                    )
                )
            )
        )

        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    suspend fun deleteRow(rowIndex: Int): String {
        require(rowIndex >= 0) { "Row index must be non-negative" }

        val body = mapOf(
            "requests" to listOf(
                mapOf(
                    "deleteDimension" to mapOf(
                        "range" to mapOf(
                            "sheetId" to spreadsheetId,
                            "dimension" to "ROWS",
                            "startIndex" to rowIndex,
                            "endIndex" to rowIndex + 1
                        )
                    )
                )
            )
        )

        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    suspend fun insertColumn(sheetId: Int, columnIndex: Int): String {
        require(columnIndex >= 0) { "Column index must be non-negative" }

        val body = mapOf(
            "requests" to listOf(
                mapOf(
                    "insertDimension" to mapOf(
                        "range" to mapOf(
                            "sheetId" to sheetId,
                            "dimension" to "COLUMNS",
                            "startIndex" to columnIndex,
                            "endIndex" to columnIndex + 1
                        ),
                        "inheritFromBefore" to false
                    )
                )
            )
        )

        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    suspend fun deleteColumn(sheetId: Int, columnIndex: Int): String {
        require(columnIndex >= 0) { "Column index must be non-negative" }

        val body = mapOf(
            "requests" to listOf(
                mapOf(
                    "deleteDimension" to mapOf(
                        "range" to mapOf(
                            "sheetId" to sheetId,
                            "dimension" to "COLUMNS",
                            "startIndex" to columnIndex,
                            "endIndex" to columnIndex + 1
                        )
                    )
                )
            )
        )

        return client.post("$BASE_URL/$spreadsheetId:batchUpdate") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(body)
        }.bodyAsText()
    }

    //endregion

    //region Private Helper Functions
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

    //endregion

}