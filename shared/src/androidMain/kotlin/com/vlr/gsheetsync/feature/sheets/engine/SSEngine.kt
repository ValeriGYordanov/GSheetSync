package com.vlr.gsheetsync.feature.sheets.engine

import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetService
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * High-level wrapper for [SpreadSheetService] that provides safe, coroutine-friendly interaction
 * with Google Sheets API. Handles:
 * - Error propagation via [errorListener]
 * - Loading state management via [loadingListener]
 * - Automatic JSON serialization of responses
 * - Consistent error handling for all operations
 *
 * Designed for use with Android ViewModels following MVVM architecture.
 *
 * @property spreadsheetService The underlying [SpreadSheetService] instance for API operations
 * @see SpreadSheetService for low-level API operations
 */
actual class SSEngine actual constructor(
    private val spreadsheetService: SpreadSheetService
) {
    /**
     * Callback for error notifications with default no-op implementation.
     * Receives user-friendly error messages suitable for direct UI display.
     *
     */
    var errorListener: (String) -> Unit = { }

    /**
     * Callback for loading state changes with default no-op implementation.
     * Triggers on operation start/complete.
     *
     */
    var loadingListener: (Boolean) -> Unit = { }

    /**
     * Sets the OAuth2 access token for API authentication.
     * @see SpreadSheetService.setAccessToken
     *
     * @param token Valid Google API access token
     */
    actual suspend fun setAccessToken(token: String) = safeCall {
        spreadsheetService.setAccessToken(token)
    }

    /**
     * Configures the target spreadsheet using its URL.
     * Extracts and validates the spreadsheet ID automatically.
     * @see SpreadSheetService.setSpreadsheetId
     *
     * @param url Valid Google Sheets URL (format: "https://docs.google.com/spreadsheets/d/{ID}/edit")
     */
    actual suspend fun setSpreadsheetId(url: String) = safeCall {
        spreadsheetService.setSpreadsheetId(url)
    }

    /**
     * Creates a new spreadsheet with the specified title.
     * @see SpreadSheetService.createSpreadsheet
     *
     * @param title Name for the new spreadsheet (1-100 characters)
     * @return Serialized spreadsheet metadata as [JsonElement], or null on failure
     */
    actual suspend fun createSpreadsheet(title: String): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.createSpreadsheet(title))
    }

    /**
     * Retrieves spreadsheet metadata.
     * @see SpreadSheetService.getSpreadsheet
     *
     * @param googleSheetsUrl Optional URL to set spreadsheet ID before fetching
     * @return Serialized spreadsheet data as [JsonElement], or null on failure
     */
    actual suspend fun getSpreadsheet(googleSheetsUrl: String?): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.getSpreadsheet(googleSheetsUrl))
    }

    /**
     * Creates a new sheet in the current spreadsheet.
     * @see SpreadSheetService.createSheet
     *
     * @param sheetTitle Name for the new sheet (1-100 chars, unique per spreadsheet)
     * @return Serialized response as [JsonElement], or null on failure
     */
    actual suspend fun createSheet(sheetTitle: String): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.createSheet(sheetTitle))
    }

    /**
     * Deletes a sheet from the current spreadsheet.
     * @see SpreadSheetService.deleteSheet
     *
     * @param sheetTitle Name of the sheet to delete
     * @return Serialized response as [JsonElement], or null on failure
     * @throws IllegalStateException if sheet doesn't exist
     */
    actual suspend fun deleteSheet(sheetTitle: String): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.deleteSheet(sheetTitle))
    }

    /**
     * Retrieves metadata for a specific sheet.
     * @see SpreadSheetService.getSheet
     *
     * @param sheetTitle Name of the sheet to retrieve
     * @return Serialized sheet data as [JsonElement], or null if not found
     */
    actual suspend fun getSheet(sheetTitle: String): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.getSheet(sheetTitle))
    }

    /**
     * Fetches cell values from a specified range.
     * @see SpreadSheetService.getData
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @return Map of cell references to values, or null on failure
     * @throws IllegalArgumentException for invalid cell references
     */
    actual suspend fun getData(from: String, to: String?): Map<String, String>? = safeCall {
        spreadsheetService.getData(from, to?: from)
    }

    /**
     * Updates multiple cells in batch.
     * @see SpreadSheetService.updateData
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @return Raw API response string, or null on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    actual suspend fun updateData(updates: Map<String, String>): String? = safeCall {
        spreadsheetService.updateData(updates)
    }

    /**
     * Inserts a new row at the specified index in a sheet.
     *
     * @param rowIndex The index where the new row should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertRow(rowIndex: Int): JsonElement? = safeCall {
        spreadsheetService.insertRow(rowIndex)
    }

    /**
     * Deletes a row at the specified index in a sheet.
     *
     * @param rowIndex The index of the row to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteRow(rowIndex: Int): JsonElement? = safeCall {
        spreadsheetService.deleteRow(rowIndex)
    }

    /**
     * Inserts a new column at the specified index in a sheet.
     *
     * @param columnIndex The index where the new column should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertColumn(columnIndex: Int): JsonElement? = safeCall {
        spreadsheetService.insertColumn(columnIndex)
    }

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteColumn(columnIndex: Int): JsonElement? = safeCall {
        spreadsheetService.deleteColumn(columnIndex)
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    actual suspend fun clearCell(cell: String): String? = safeCall {
        spreadsheetService.clearCell(cell)
    }

    /**
     * Protects a sheet in the spreadsheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectSheet(sheetTitle: String?): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectSheet(sheetTitle))
    }


    /**
     * Protects all sheets in the spreadsheet, preventing manual edits.
     *
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectAllSheets(): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectAllSheets())
    }

    /**
     * Removes protection from a sheet by title, allowing manual edits again.
     *
     * @param sheetTitle The title of the sheet to unprotect
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectSheet(sheetTitle: String?): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.unprotectSheet(sheetTitle))
    }

    /**
     * Removes protection from all sheets in the spreadsheet, allowing manual edits again.
     *
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectAllSheets(): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.unprotectAllSheets())
    }

    /**
     * Protects a range of cells in the spreadsheet.
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalArgumentException for invalid cell references
     * @throws IllegalStateException if spreadsheet ID is not set
     *
     */
    actual suspend fun protectCellsInRange(
        from: String,
        to: String
    ): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectCellsInRange(from, to))
    }

    /**
     * Protects all cells in the spreadsheet.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectAllCells(sheetTitle: String?): JsonElement? = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectAllCellsInSheet(sheetTitle))
    }


    /**
     * Unified safe execution wrapper for all operations.
     * Handles:
     * - Loading state transitions
     * - Error conversion to user messages
     * - Serialization exceptions
     *
     * @param block Operation to execute safely
     * @return Result of the operation or null if failed
     */
    private suspend fun <T> safeCall(block: suspend () -> T): T? {
        loadingListener(true)
        return try {
            block()
        } catch (e: IllegalArgumentException) {
            SyncLog.print(e.message ?: "Invalid operation parameters")
            errorListener(e.message ?: "Invalid operation parameters")
            null
        } catch (e: SerializationException) {
            SyncLog.print(e.message ?: "Data format error: ${e.message ?: "Unknown"}")
            errorListener("Data format error: ${e.message ?: "Unknown"}")
            null
        } finally {
            loadingListener(false)
        }
    }
}