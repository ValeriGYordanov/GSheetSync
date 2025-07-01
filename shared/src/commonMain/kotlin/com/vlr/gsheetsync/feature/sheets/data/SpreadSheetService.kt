package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.math.abs
import kotlin.random.Random

/**
 * Service for interacting with Google Sheets API.
 * Provides functionality to manage spreadsheets and sheets including creation, retrieval, and deletion.
 *
 * @property client The HTTP client used for API requests
 */
class SpreadSheetService(private val client: HttpClient) {

    //region Constants
    private companion object {
        const val BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets"
    }

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    //endregion

    //region State
    private lateinit var token: String
    private lateinit var spreadsheetId: String
    private lateinit var sheetName: String
    private lateinit var sheetId: String

    private fun requireConfig(): SheetConfig {
        check(::token.isInitialized) { "Access token is not set." }
        check(::spreadsheetId.isInitialized) { "Spreadsheet ID is not set." }
        check(::sheetName.isInitialized) { "Sheet name is not set." }
        check(::sheetId.isInitialized) { "SheetID is not set." }
        return SheetConfig(token, spreadsheetId, sheetName)
    }

    private data class SheetConfig(
        val token: String,
        val spreadsheetId: String,
        val sheetName: String
    )
    //endregion

    //region Configuration
    /**
     * Sets the access token for authenticating with Google Sheets API.
     *
     * @param accessToken The OAuth2 access token to be used for API requests
     * @throws IllegalArgumentException if the access token is blank
     */
    fun setAccessToken(accessToken: String) {
        require(accessToken.isNotBlank())
        token = accessToken
    }

    /**
     * Sets the spreadsheet ID and default sheet name from a Google Sheets URL.
     *
     * @param url The URL of the Google Sheets document
     * @throws IllegalArgumentException if the URL is invalid
     */
    suspend fun setSpreadsheetId(url: String) {
        val id = "/d/([a-zA-Z0-9-_]+)".toRegex().find(url)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Invalid Google Sheets URL")
        spreadsheetId = id
        val spreadsheetProperties =
            getSpreadsheet()?.jsonObject?.get("sheets")?.jsonArray?.firstOrNull()?.jsonObject?.get("properties")
        sheetId = spreadsheetProperties
            ?.jsonObject?.get("sheetId")
            ?.jsonPrimitive?.int.toString() ?: ""

        sheetName = spreadsheetProperties
            ?.jsonObject?.get("title")
            ?.jsonPrimitive?.content ?: "Initial"
    }

    /**
     * Sets the name of the sheet to be used for operations.
     *
     * @param sheetName The name of the sheet to be used
     * @return The selected sheet with matching title or null if none was found.
     *
     * @throws IllegalArgumentException if the sheet name is blank
     */
    suspend fun setWorkingSheet(sheetName: String): JsonElement? {
        require(sheetName.isNotBlank(), { "Sheet name cannot be blank" })

        val sheets = getSpreadsheet()?.jsonObject?.get("sheets")?.jsonArray
        val workingSheet = sheets?.find {
            it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == sheetName
        }

        if (workingSheet != null) {
            this.sheetName = sheetName
            sheetId =
                workingSheet.jsonObject["properties"]?.jsonObject?.get("sheetId")?.jsonPrimitive?.int.toString()
        }
        return workingSheet
    }
    //endregion

    //region Public API

    /**
     * Searches for a spreadsheet with the given name using the Google Drive API.
     *
     * @param name The name (title) of the spreadsheet to search for.
     * @return JSON representation of the matching spreadsheet metadata or null if not found.
     */
    suspend fun findSpreadsheetByName(name: String): JsonElement? {
        require(token.isNotBlank()) { "Access token cannot be blank" }

        val query = "mimeType='application/vnd.google-apps.spreadsheet' and name='${name}' and trashed=false"
        val url = "https://www.googleapis.com/drive/v3/files?q=${query.encodeURLParameter()}&fields=files(id,name)"

        return safeApiGet<JsonElement>(url) {
            bearerAuth(token)
            accept(ContentType.Application.Json)
        }
    }

    /**
     * Shares the spreadsheet with anyone who has the link (read/write).
     *
     * @throws IllegalStateException if the spreadsheet ID is not set
     */
    suspend fun shareSpreadsheetPublicly(): JsonElement? {
        require(token.isNotBlank()) { "Access token cannot be blank" }
        require(spreadsheetId.isNotBlank()) { "Spreadsheet ID is not set" }

        val url = "https://www.googleapis.com/drive/v3/files/$spreadsheetId/permissions"

        val payload = buildJsonObject {
            put("role", "writer")
            put("type", "anyone")
        }

        return safeApiPost<JsonObject>(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(payload)
        }
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    suspend fun clearCell(cell: String, sheetName: String? = null): String {
        require(cell.isValidA1())
        val cfg = requireConfig()
        val fullRange = if (sheetName == null) {
            "${cfg.sheetName}!$cell"
        } else {
            "${sheetName}!$cell"
        }

        return safeApiPost<String>("$BASE_URL/${cfg.spreadsheetId}/values/$fullRange:clear") {
            bearerAuth(cfg.token)
            contentType(ContentType.Application.Json)
            setBody("{}")
        } ?: ""
    }

    /**
     * Retrieves spreadsheet metadata for a given URL.
     *
     * @param url The URL of the spreadsheet (optional - uses configured ID if null)
     * @return JSON representation of the spreadsheet
     * @throws IllegalStateException if the request fails
     * @throws IllegalArgumentException if URL is invalid when provided
     */
    suspend fun getSpreadsheet(url: String?): JsonElement {
        val sheetId = extractSheetIdFromUrl(url)
        spreadsheetId = sheetId
        return safeApiGet<JsonElement>("$BASE_URL/$sheetId") {
            bearerAuth(token)
        } ?: throw IllegalStateException("Failed to fetch spreadsheet")
    }

    /**
     * Creates a new spreadsheet with the given title.
     *
     * @param title The title for the new spreadsheet
     * @param sheetsTitle List of titles for the sheets to be included.
     * @param protected If true, all sheet ranges will be protected (full range).
     * @return JSON representation of the created spreadsheet or null if creation failed
     */
    suspend fun createSpreadsheet(
        title: String,
        sheetsTitle: List<String>? = null,
        protected: Boolean? = null
    ): JsonElement? {
        val body = buildJsonObject {
            putJsonObject("properties") { put("title", title) }
            if (sheetsTitle != null) {
                putJsonArray("sheets") {
                    sheetsTitle.forEachIndexed { index, sheetName ->
                        val generatedSheetId = abs(sheetName.hashCode()) + index
                        addJsonObject {
                            putJsonObject("properties") {
                                put("sheetId", generatedSheetId)
                                put("title", sheetName)
                            }
                            if (protected == true) {
                                putJsonArray("protectedRanges") {
                                    addJsonObject {
                                        put("range", buildJsonObject {
                                            put("sheetId", generatedSheetId)
                                            put("startRowIndex", 0)
                                            put("endRowIndex", 1000)
                                            put("startColumnIndex", 0)
                                            put("endColumnIndex", 26)
                                        })
                                        put("description", "Full sheet protection")
                                        put("warningOnly", false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        require(token.isNotBlank(), { "Access token cannot be blank" })
        return safeApiPost<JsonElement>(BASE_URL) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Creates a new sheet within the current spreadsheet.
     *
     * @param sheetTitle The title for the new sheet
     * @return String representation of the API response
     * @throws IllegalStateException if the request fails
     */
    suspend fun createSheet(sheetTitle: String): String {
        val cfg = requireConfig()
        val body = buildJsonObject {
            put("requests", buildJsonArray {
                add(buildJsonObject {
                    put("addSheet", buildJsonObject {
                        put("properties", buildJsonObject {
                            put("title", sheetTitle)
                        })
                    })
                })
            })
        }

        return safeApiPost<JsonElement>("$BASE_URL/${cfg.spreadsheetId}:batchUpdate") {
            bearerAuth(cfg.token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }?.toString() ?: throw IllegalStateException("Failed to create sheet")
    }

    /**
     * Retrieves metadata for the currently configured spreadsheet.
     *
     * @return JSON representation of the spreadsheet or null if request fails
     * @throws IllegalArgumentException if spreadsheet ID or token is not set
     */
    suspend fun getSpreadsheet(): JsonElement? {
        if (!this::spreadsheetId.isInitialized || !this::token.isInitialized) {
            throw IllegalArgumentException("Spreadsheet ID or token is not set.")
        }
        return safeApiGet<JsonElement>("$BASE_URL/${spreadsheetId}") {
            bearerAuth(token)
        }
    }

    /**
     * Retrieves metadata for a specific sheet within the current spreadsheet.
     *
     * @param sheetTitle The title of the sheet to retrieve
     * @return JSON representation of the sheet or null if not found
     */
    suspend fun getSheet(sheetTitle: String): JsonElement? {
        return getSpreadsheet()?.jsonObject?.get("sheets")
            ?.jsonArray?.firstOrNull {
                it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == sheetTitle
            }
    }

    /**
     * Deletes a sheet from the current spreadsheet.
     *
     * @param sheetTitle The title of the sheet to delete
     * @return JSON representation of the API response or null if request fails
     * @throws IllegalStateException if the sheet is not found
     */
    suspend fun deleteSheet(sheetTitle: String): JsonElement? {
        val cfg = requireConfig()
        val sheet = getSheet(sheetTitle)
        val sheetId =
            sheet?.jsonObject?.get("properties")?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
                ?: throw IllegalStateException("Sheet not found")

        val body = buildJsonObject {
            put("requests", buildJsonArray {
                add(buildJsonObject {
                    putJsonObject("deleteSheet") {
                        put("sheetId", sheetId)
                    }
                })
            })
        }
        return safeApiPost<JsonElement>("$BASE_URL/${cfg.spreadsheetId}:batchUpdate") {
            bearerAuth(cfg.token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Protects a sheet in the spreadsheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    suspend fun protectSheet(sheetTitle: String? = sheetName): JsonElement? {
        val cfg = requireConfig()
        val id = cfg.spreadsheetId

        // Get sheet ID from spreadsheet metadata
        val sheetsMetadata = getSpreadsheet()
        val sheetId = sheetsMetadata
            ?.jsonObject?.get("sheets")?.jsonArray
            ?.firstOrNull { it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == sheetTitle }
            ?.jsonObject?.get("properties")?.jsonObject?.get("sheetId")?.jsonPrimitive?.int

        if (sheetId == null) {
            throw IllegalArgumentException("Sheet with title '$sheetTitle' not found")
        }

        val body = buildJsonObject {
            putJsonArray("requests") {
                addJsonObject {
                    putJsonObject("addProtectedRange") {
                        putJsonObject("protectedRange") {
                            put("description", "Protected by API")
                            put("warningOnly", false)
                            put(
                                "editors",
                                buildJsonObject {
                                    put(
                                        "users",
                                        buildJsonArray {})
                                }) // Empty editors = no one can edit
                            putJsonObject("range") {
                                put("sheetId", sheetId)
                            }
                        }
                    }
                }
            }
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Removes protection from a sheet by title, allowing manual edits again.
     *
     * @param sheetTitle The title of the sheet to unprotect
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    suspend fun unprotectSheet(sheetTitle: String? = sheetName): JsonElement? {
        val cfg = requireConfig()
        val id = cfg.spreadsheetId

        val spreadsheet = getSpreadsheet()
        val sheets = spreadsheet?.jsonObject?.get("sheets")?.jsonArray ?: return null

        val sheetId = sheets
            .firstOrNull { it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == sheetTitle }
            ?.jsonObject?.get("properties")?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("Sheet with title '$sheetTitle' not found")

        val protectedRanges = spreadsheet.jsonObject["sheets"]?.jsonArray
            ?.flatMap { sheet ->
                sheet.jsonObject["protectedRanges"]?.jsonArray.orEmpty().mapNotNull { range ->
                    val rangeObj = range.jsonObject
                    val id = rangeObj["protectedRangeId"]?.jsonPrimitive?.int
                    val rangeSheetId =
                        rangeObj["range"]?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
                    if (id != null && rangeSheetId == sheetId) id else null
                }
            } ?: emptyList()

        if (protectedRanges.isEmpty()) return null

        val requests = buildJsonArray {
            for (rangeId in protectedRanges) {
                addJsonObject {
                    putJsonObject("deleteProtectedRange") {
                        put("protectedRangeId", rangeId)
                    }
                }
            }
        }

        val body = buildJsonObject {
            put("requests", requests)
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Protects all sheets in the spreadsheet to prevent manual edits.
     *
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    suspend fun protectAllSheets(): JsonElement? {
        val cfg = requireConfig()
        val id = cfg.spreadsheetId

        // Get all sheets metadata
        val sheetsMetadata = getSpreadsheet()
        val sheets = sheetsMetadata?.jsonObject?.get("sheets")?.jsonArray ?: return null

        // Build protection requests for all sheets
        val protectionRequests = buildJsonArray {
            for (sheet in sheets) {
                val sheetId = sheet.jsonObject["properties"]
                    ?.jsonObject?.get("sheetId")?.jsonPrimitive?.int ?: continue

                addJsonObject {
                    putJsonObject("addProtectedRange") {
                        putJsonObject("protectedRange") {
                            put("description", "Protected by API")
                            put("warningOnly", false)
                            put("editors", buildJsonObject {
                                put("users", buildJsonArray {}) // No users allowed to edit manually
                            })
                            putJsonObject("range") {
                                put("sheetId", sheetId) // Entire sheet
                            }
                        }
                    }
                }
            }
        }

        val body = buildJsonObject {
            put("requests", protectionRequests)
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Removes protection from all sheets in the spreadsheet.
     *
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    suspend fun unprotectAllSheets(): JsonElement? {
        val cfg = requireConfig()
        val id = cfg.spreadsheetId

        val spreadsheet = getSpreadsheet()
        val sheets = spreadsheet?.jsonObject?.get("sheets")?.jsonArray ?: return null

        val protectedRangeIds = sheets.flatMap { sheet ->
            sheet.jsonObject["protectedRanges"]?.jsonArray.orEmpty().mapNotNull { range ->
                range.jsonObject["protectedRangeId"]?.jsonPrimitive?.int
            }
        }

        if (protectedRangeIds.isEmpty()) return null

        val requests = buildJsonArray {
            for (rangeId in protectedRangeIds) {
                addJsonObject {
                    putJsonObject("deleteProtectedRange") {
                        put("protectedRangeId", rangeId)
                    }
                }
            }
        }

        val body = buildJsonObject {
            put("requests", requests)
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Protects a range of cells in a sheet using A1 notation for 'from' and 'to' positions.
     *
     * @param from The starting cell in A1 notation (e.g., "A1")
     * @param to The ending cell in A1 notation (e.g., "C30")
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     * @throws IllegalArgumentException if range inputs are invalid
     */
    suspend fun protectCellsInRange(from: String, to: String, sheetName: String? = null): JsonElement? {
        val cfg = requireConfig()
        val sheetTitle = sheetName ?: cfg.sheetName

        require(from.isValidA1()) { "Invalid 'from' A1 notation: $from" }
        require(to.isValidA1()) { "Invalid 'to' A1 notation: $to" }

        val id = cfg.spreadsheetId

        // Fetch sheetId from sheet name
        val spreadsheet = getSpreadsheet()
        val sheet = spreadsheet?.jsonObject?.get("sheets")?.jsonArray?.firstOrNull {
            it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == sheetTitle
        } ?: throw IllegalArgumentException("Sheet '$sheetTitle' not found")

        val sheetId = sheet.jsonObject["properties"]?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
            ?: throw IllegalStateException("SheetId missing for '$sheetTitle'")

        // Convert A1 range (from & to) into GridRange
        val gridRange = buildGridRangeFromA1(from, to, sheetId)

        // Build protection request
        val body = buildJsonObject {
            putJsonArray("requests") {
                addJsonObject {
                    putJsonObject("addProtectedRange") {
                        putJsonObject("protectedRange") {
                            put("description", "Warning: Protected range $from to $to")
                            put("warningOnly", true) // Enables warning instead of locking
                            put("range", gridRange)
                        }
                    }
                }
            }
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Protects all cells in a sheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    suspend fun protectAllCellsInSheet(sheetTitle: String?): JsonElement? {
        val cfg = requireConfig()
        val id = cfg.spreadsheetId

        // Get sheet ID from title
        val spreadsheet = getSpreadsheet()
        val sheet = spreadsheet?.jsonObject?.get("sheets")?.jsonArray?.firstOrNull {
            it.jsonObject["properties"]?.jsonObject?.get("title")?.jsonPrimitive?.content == (sheetTitle
                ?: sheetName)
        } ?: throw IllegalArgumentException("Sheet '${(sheetTitle ?: sheetName)}' not found")

        val sheetId = sheet.jsonObject["properties"]?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
            ?: throw IllegalStateException("sheetId not found for sheet '${(sheetTitle ?: sheetName)}'")

        // Set a large range (adjustable depending on your needs)
        val body = buildJsonObject {
            putJsonArray("requests") {
                addJsonObject {
                    putJsonObject("addProtectedRange") {
                        putJsonObject("protectedRange") {
                            put("description", "Full cell protection by API")
                            put("warningOnly", true)
                            putJsonObject("range") {
                                put("sheetId", sheetId)
                                put("startRowIndex", 0)
                                put(
                                    "endRowIndex",
                                    1000
                                )       // Adjust based on expected sheet size
                                put("startColumnIndex", 0)
                                put("endColumnIndex", 26)      // A to Z = 26 columns
                            }
                        }
                    }
                }
            }
        }

        return safeApiPost<JsonElement>("$BASE_URL/$id:batchUpdate") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Retrieves data from a range of cells in the current sheet.
     *
     * @param from The starting cell reference (A1 notation)
     * @param to The ending cell reference (A1 notation, defaults to same as 'from')
     * @return Map of cell addresses to their values (only includes non-blank cells)
     */
    suspend fun getData(from: String, to: String = from, sheetName: String? = null): Map<String, String> {
        val cfg = requireConfig()
        val range = if (sheetName == null) {
            "${cfg.sheetName}!$from:$to"
        } else {
            "${sheetName}!$from:$to"
        }
        val result = safeApiGet<JsonElement>("$BASE_URL/${cfg.spreadsheetId}/values/$range") {
            bearerAuth(cfg.token)
        }
        val values = result?.jsonObject?.get("values")?.jsonArray ?: return emptyMap()
        return values.flatMapIndexed { rowIndex, row ->
            row.jsonArray.mapIndexedNotNull { colIndex, cell ->
                val cellAddress = "${('A' + colIndex)}${rowIndex + 1}"
                if (cell.jsonPrimitive.content.isNotBlank()) cellAddress to cell.jsonPrimitive.content else null
            }
        }.toMap()
    }

    /**
     * Updates multiple cells with new values.
     *
     * @param updates Map of cell addresses (A1 notation) to their new values
     * @return String representation of the API response or null if request fails
     */
    suspend fun updateData(updates: Map<String, String>, sheetName: String? = null): String? {
        val cfg = requireConfig()
        val sheetTitle = sheetName ?: cfg.sheetName
        val data = updates.map { (cell, value) ->
            buildJsonObject {
                put("range", "$sheetTitle!$cell")
                put("values", buildJsonArray {
                    add(buildJsonArray { add(value) })
                })
            }
        }
        val body = buildJsonObject {
            put("valueInputOption", "RAW")
            put("data", JsonArray(data))
        }
        return safeApiPost<JsonElement>("$BASE_URL/${cfg.spreadsheetId}/values:batchUpdate") {
            bearerAuth(cfg.token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }?.toString()
    }

    /**
     * Inserts a new row at the specified index in a sheet.
     *
     * @param rowIndex The index where the new row should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    suspend fun insertRow(rowIndex: Int): JsonElement? = modifyDimension("ROWS", rowIndex)

    /**
     * Deletes a row at the specified index in a sheet.
     *
     * @param rowIndex The index of the row to delete
     * @return JSON representation of the API response or null if request fails
     */
    suspend fun deleteRow(rowIndex: Int): JsonElement? = modifyDimension("ROWS", rowIndex, true)

    /**
     * Inserts a new column at the specified index in a sheet.
     *
     * @param columnIndex The index where the new column should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    suspend fun insertColumn(columnIndex: Int): JsonElement? =
        modifyDimension("COLUMNS", columnIndex)

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @return JSON representation of the API response or null if request fails
     */
    suspend fun deleteColumn(columnIndex: Int): JsonElement? =
        modifyDimension("COLUMNS", columnIndex, true)

    private suspend fun modifyDimension(
        dimension: String,
        index: Int,
        delete: Boolean = false
    ): JsonElement? {
        val cfg = requireConfig()
        val key = if (delete) "deleteDimension" else "insertDimension"
        val body = buildJsonObject {
            put("requests", buildJsonArray {
                add(buildJsonObject {
                    putJsonObject(key) {
                        putJsonObject("range") {
                            put("sheetId", sheetId)
                            put("dimension", dimension)
                            put("startIndex", index - 1)
                            put("endIndex", index)
                        }
                        if (!delete) put("inheritFromBefore", false)
                    }
                })
            })
        }
        SyncLog.print("performing $key with body: $body")
        return safeApiPost<JsonElement>("$BASE_URL/${cfg.spreadsheetId}:batchUpdate") {
            bearerAuth(cfg.token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
    //endregion

    //region Helpers

    private fun buildGridRangeFromA1(from: String, to: String, sheetId: Int): JsonObject {
        val regex = Regex("([A-Z]+)(\\d+)")
        val fromMatch =
            regex.matchEntire(from) ?: throw IllegalArgumentException("Invalid 'from': $from")
        val toMatch = regex.matchEntire(to) ?: throw IllegalArgumentException("Invalid 'to': $to")

        val (colStart, rowStart) = fromMatch.destructured
        val (colEnd, rowEnd) = toMatch.destructured

        fun columnToIndex(col: String): Int =
            col.fold(0) { acc, c -> acc * 26 + (c - 'A' + 1) } - 1

        return buildJsonObject {
            put("sheetId", sheetId)
            put("startRowIndex", rowStart.toInt() - 1)
            put("endRowIndex", rowEnd.toInt()) // end-exclusive
            put("startColumnIndex", columnToIndex(colStart))
            put("endColumnIndex", columnToIndex(colEnd) + 1) // end-exclusive
        }
    }

    private suspend inline fun <reified T> safeApiGet(
        url: String,
        noinline builder: HttpRequestBuilder.() -> Unit = {}
    ): T? {
        SyncLog.print("Performing GET with url: $url")
        val response = client.get(url, builder)

        if (!response.status.isSuccess()) {
            // Try to parse error response
            val errorResponse = response.body<ErrorResponse>()
            SyncLog.print("API Error: ${errorResponse.error?.code} - ${errorResponse.error?.message}")
            throw Exception("API Error: ${errorResponse.error?.code} - ${errorResponse.error?.message}")
        }

        val responseBody = response.body<T>()
        SyncLog.print("Response: $responseBody")
        return responseBody
    }

    private suspend inline fun <reified T> safeApiPost(
        url: String,
        noinline builder: HttpRequestBuilder.() -> Unit = {}
    ): T? {
        SyncLog.print("Performing POST with url: $url")
        val response = client.post(url, builder)

        if (!response.status.isSuccess()) {
            // Try to parse error response
            val errorResponse = response.body<ErrorResponse>()
            SyncLog.print("API Error: ${errorResponse.error?.code} - ${errorResponse.error?.message}")
            throw Exception("API Error: ${errorResponse.error?.code} - ${errorResponse.error?.message}")
        }

        val responseBody = response.body<T>()
        SyncLog.print("Response: $responseBody")
        return responseBody
    }

    private fun extractSheetIdFromUrl(url: String?): String {
        val regex = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
        return regex.find(url ?: "INVALID URL")?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Invalid spreadsheet URL")
    }

    //endregion
}

fun String.isValidA1(): Boolean =
    matches(Regex("^[A-Z]+\\d+(?::[A-Z]+\\d+)?$"))

// Define error response data classes
@Serializable
data class ErrorResponse(
    val error: ApiError?
)

@Serializable
data class ApiError(
    val code: Int,
    val message: String
)