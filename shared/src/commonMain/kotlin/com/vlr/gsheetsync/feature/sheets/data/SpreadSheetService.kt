package com.vlr.gsheetsync.feature.sheets.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

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
        val spreadsheetProperties = getSpreadsheet()?.jsonObject?.get("sheets")?.jsonArray?.firstOrNull()?.jsonObject?.get("properties")
        sheetId = spreadsheetProperties
            ?.jsonObject?.get("sheetId")
            ?.jsonPrimitive?.int.toString() ?: ""

        sheetName = spreadsheetProperties
            ?.jsonObject?.get("title")
            ?.jsonPrimitive?.content ?: "Initial"
    }
    //endregion

    //region Public API

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    suspend fun clearCell(cell: String): String {
        require(cell.isValidA1())
        val cfg = requireConfig()
        val fullRange = "${cfg.sheetName}!$cell"
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
     * @return JSON representation of the created spreadsheet or null if creation failed
     */
    suspend fun createSpreadsheet(title: String): JsonElement? {
        val body = buildJsonObject {
            putJsonObject("properties") { put("title", title) }
        }
        return safeApiPost<JsonElement>(BASE_URL) {
            bearerAuth(requireConfig().token)
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
        val sheetId = sheet?.jsonObject?.get("properties")?.jsonObject?.get("sheetId")?.jsonPrimitive?.int
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
     * Retrieves data from a range of cells in the current sheet.
     *
     * @param from The starting cell reference (A1 notation)
     * @param to The ending cell reference (A1 notation, defaults to same as 'from')
     * @return Map of cell addresses to their values (only includes non-blank cells)
     */
    suspend fun getData(from: String, to: String = from): Map<String, String> {
        val cfg = requireConfig()
        val range = "${cfg.sheetName}!$from:$to"
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
    suspend fun updateData(updates: Map<String, String>): String? {
        val cfg = requireConfig()
        val data = updates.map { (cell, value) ->
            buildJsonObject {
                put("range", "${cfg.sheetName}!$cell")
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
    suspend fun insertColumn(columnIndex: Int): JsonElement? = modifyDimension("COLUMNS", columnIndex)

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @return JSON representation of the API response or null if request fails
     */
    suspend fun deleteColumn(columnIndex: Int): JsonElement? = modifyDimension("COLUMNS", columnIndex, true)

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
                            put("startIndex", index)
                            put("endIndex", index + 1)
                        }
                        if (!delete) put("inheritFromBefore", false)
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
    //endregion

    //region Helpers

    private suspend inline fun <reified T> safeApiGet(
        url: String,
        noinline builder: HttpRequestBuilder.() -> Unit = {}
    ): T? = try {
        val response = client.get(url, builder).body<T>()
        response
    } catch (e: Exception) {
        null
    }

    private suspend inline fun <reified T> safeApiPost(
        url: String,
        noinline builder: HttpRequestBuilder.() -> Unit = {}
    ): T? = try {
        val response = client.post(url, builder).body<T>()
        response
    } catch (e: Exception) {
        null
    }

    private fun extractSheetIdFromUrl(url: String?): String {
        val regex = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
        return regex.find(url?:"INVALID URL")?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Invalid spreadsheet URL")
    }

    //endregion
}

fun String.isValidA1(): Boolean =
    matches(Regex("^[A-Z]+\\d+(?::[A-Z]+\\d+)?$"))
