package com.vlr.gsheetsync.feature.sheets.engine

import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetService
import com.vlr.gsheetsync.feature.sheets.engine.data.SSResult
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
     * @param sheetTitles Optional list of sheet titles to create in the new spreadsheet
     * @param protected Whether the new spreadsheet should be protected (default: false)
     *
     * @return Serialized spreadsheet metadata as [JsonElement], or null on failure
     */
    actual suspend fun createSpreadsheet(title: String, sheetTitles: List<String>?, protected: Boolean?): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.createSpreadsheet(title, sheetTitles, protected))
    }

    /**
     * Retrieves spreadsheet metadata.
     * @see SpreadSheetService.getSpreadsheet
     *
     * @param googleSheetsUrl Optional URL to set spreadsheet ID before fetching
     * @return Serialized spreadsheet data as [JsonElement], or null on failure
     */
    actual suspend fun getSpreadsheet(googleSheetsUrl: String?): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.getSpreadsheet(googleSheetsUrl))
    }

    /**
     * Sets the name of the sheet to be used for operations.
     *
     * @param sheetTitle The title of the sheet to be used
     * @return Serialized sheet metadata as [JsonElement], or null on failure
     *
     * @throws IllegalArgumentException if the sheet name is blank
     */
    actual suspend fun setWorkingSheet(sheetTitle: String): SSResult<JsonElement?> = safeCall {
        spreadsheetService.setWorkingSheet(sheetTitle)
    }

    /**
     * Searches for a spreadsheet with the given name.
     * @see SpreadSheetService.findSpreadsheetByName
     *
     * @param name Name of the spreadsheet to search for
     * @return Serialized spreadsheet metadata as [JsonElement], or null on failure
     * @throws IllegalArgumentException if the sheet name is blank
     * @throws IllegalArgumentException if token is not set
     */
    actual suspend fun findSpreadsheetByName(name: String): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.findSpreadsheetByName(name))
    }

    /**
     * Shares the spreadsheet with anyone who has the link (read/write).
     * @see SpreadSheetService.shareSpreadsheetPublicly
     *
     * @return Serialized response as [JsonElement], or null on failure
     * @throws IllegalStateException if spreadsheet ID is not set
     * @throws IllegalArgumentException if token is not set
     */
    actual suspend fun shareSpreadsheetPublicly(): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.shareSpreadsheetPublicly())
    }

    /**
     * Creates a new sheet in the current spreadsheet.
     * @see SpreadSheetService.createSheet
     *
     * @param sheetTitle Name for the new sheet (1-100 chars, unique per spreadsheet)
     * @return Serialized response as [JsonElement], or null on failure
     */
    actual suspend fun createSheet(sheetTitle: String): SSResult<JsonElement?> = safeCall {
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
    actual suspend fun deleteSheet(sheetTitle: String): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.deleteSheet(sheetTitle))
    }

    /**
     * Retrieves metadata for a specific sheet.
     * @see SpreadSheetService.getSheet
     *
     * @param sheetTitle Name of the sheet to retrieve
     * @return Serialized sheet data as [JsonElement], or null if not found
     */
    actual suspend fun getSheet(sheetTitle: String): SSResult<JsonElement?> = safeCall {
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
    actual suspend fun getData(from: String, to: String?): SSResult<Map<String, String>?> = safeCall {
        spreadsheetService.getData(from, to?: from)
    }

    /**
     * Fetches cell values from a specified range.
     * @see SpreadSheetService.getData
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @param sheetName The name of the sheet to fetch from
     *
     * @return Map of cell references to values, or null on failure
     * @throws IllegalArgumentException for invalid cell references
     */
    actual suspend fun getData(from: String, to: String?, sheetName: String): SSResult<Map<String, String>?> = safeCall {
        spreadsheetService.getData(from, to?: from, sheetName)
    }

    /**
     * Updates multiple cells in batch.
     * @see SpreadSheetService.updateData
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @return Raw API response string, or null on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    actual suspend fun updateData(updates: Map<String, String>): SSResult<String?> = safeCall {
        spreadsheetService.updateData(updates)
    }

    /**
     * Updates multiple cells in batch.
     * @see SpreadSheetService.updateData
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @param sheetName The name of the sheet to update
     *
     * @return Raw API response string, or null on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    actual suspend fun updateData(updates: Map<String, String>, sheetName: String): SSResult<String?> = safeCall {
        spreadsheetService.updateData(updates, sheetName)
    }

    /**
     * Inserts a new row at the specified index in a sheet.
     *
     * @param rowIndex The index where the new row should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertRow(rowIndex: Int): SSResult<JsonElement?> = safeCall {
        spreadsheetService.insertRow(rowIndex)
    }

    /**
     * Deletes a row at the specified index in a sheet.
     *
     * @param rowIndex The index of the row to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteRow(rowIndex: Int): SSResult<JsonElement?> = safeCall {
        spreadsheetService.deleteRow(rowIndex)
    }

    /**
     * Inserts a new column at the specified index in a sheet.
     *
     * @param columnIndex The index where the new column should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertColumn(columnIndex: Int): SSResult<JsonElement?> = safeCall {
        spreadsheetService.insertColumn(columnIndex)
    }

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteColumn(columnIndex: Int): SSResult<JsonElement?> = safeCall {
        spreadsheetService.deleteColumn(columnIndex)
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    actual suspend fun clearCell(cell: String): SSResult<String?> = safeCall {
        spreadsheetService.clearCell(cell)
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @param sheetName The name of the sheet to clear in
     *
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    actual suspend fun clearCell(cell: String, sheetName: String): SSResult<String?> = safeCall {
        spreadsheetService.clearCell(cell, sheetName)
    }

    /**
     * Protects a sheet in the spreadsheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectSheet(sheetTitle: String?): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectSheet(sheetTitle))
    }


    /**
     * Protects all sheets in the spreadsheet, preventing manual edits.
     *
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectAllSheets(): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectAllSheets())
    }

    /**
     * Removes protection from a sheet by title, allowing manual edits again.
     *
     * @param sheetTitle The title of the sheet to unprotect
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectSheet(sheetTitle: String?): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.unprotectSheet(sheetTitle))
    }

    /**
     * Removes protection from all sheets in the spreadsheet, allowing manual edits again.
     *
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectAllSheets(): SSResult<JsonElement?> = safeCall {
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
    ): SSResult<JsonElement?> = safeCall {
        Json.encodeToJsonElement(spreadsheetService.protectCellsInRange(from, to))
    }

    /**
     * Protects a range of cells in the spreadsheet.
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @param sheetName The name of the sheet to protect
     *
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalArgumentException for invalid cell references
     * @throws IllegalStateException if spreadsheet ID is not set
     *
     */
    actual suspend fun protectCellsInRange(
        from: String,
        to: String,
        sheetName: String
    ): SSResult<JsonElement?> = safeCall {
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
    actual suspend fun protectAllCells(sheetTitle: String?): SSResult<JsonElement?> = safeCall {
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
    private suspend fun <T> safeCall(block: suspend () -> T): SSResult<T?> {
        return try {
            SSResult.success(block())
        } catch (e: IllegalArgumentException) {
            SyncLog.print(e.message ?: "Invalid operation parameters")
            SSResult.error(e)
        } catch (e: SerializationException) {
            SyncLog.print(e.message ?: "Data format error: ${e.message ?: "Unknown"}")
            SSResult.error(e)
        } catch (e: Exception) {
            SyncLog.print(e.message ?: "Unknown error:")
            SSResult.error(e)
        } finally {

        }
    }
}