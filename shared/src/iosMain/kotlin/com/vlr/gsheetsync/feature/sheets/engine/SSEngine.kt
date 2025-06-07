package com.vlr.gsheetsync.feature.sheets.engine

import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * iOS-optimized wrapper for [SpreadSheetService] that provides safe, completion handler-based interaction
 * with Google Sheets API. Designed for use with iOS applications following Apple's design patterns.
 *
 * Key features:
 * - Completion handler-based async operations
 * - Configurable coroutine dispatchers
 * - Automatic JSON serialization of responses
 * - Consistent error handling for all operations
 *
 * @property spreadsheetService The underlying [SpreadSheetService] instance for API operations
 * @see SpreadSheetService for low-level API operations
 */
actual class SSEngine actual constructor(
    private val spreadsheetService: SpreadSheetService
) {
    /**
     * Options for controlling the coroutine execution context.
     *
     * - DEFAULT: Standard coroutine dispatcher for CPU-intensive work
     * - IO: Optimized for disk and network I/O operations
     * - UNCONFINED: Not confined to any specific thread
     */
    enum class DispatcherOption {
        DEFAULT, IO, UNCONFINED
    }

    //--------------------------------------------------
    // Core API with completion handlers
    //--------------------------------------------------

    /**
     * Sets the OAuth2 access token for API authentication.
     *
     * @param token Valid Google API access token
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with optional error message (nil on success)
     */
    fun setAccessToken(
        token: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            setAccessToken(token)
            null
        }, completion)
    }

    /**
     * Configures the target spreadsheet using its URL.
     * Extracts and validates the spreadsheet ID automatically.
     *
     * @param url Valid Google Sheets URL (format: "https://docs.google.com/spreadsheets/d/{ID}/edit")
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with optional error message (nil on success)
     */
    fun setSpreadsheetId(
        url: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            setSpreadsheetId(url)
            null
        }, completion)
    }

    /**
     * Creates a new spreadsheet with the specified title.
     *
     * @param title Name for the new spreadsheet (1-100 characters)
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Serialized spreadsheet metadata as [JsonElement] on success
     *   - Second parameter: Error message string on failure
     */
    fun createSpreadsheet(
        title: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            createSpreadsheet(title)
        }, completion)
    }

    /**
     * Retrieves spreadsheet metadata.
     *
     * @param googleSheetsUrl Optional URL to set spreadsheet ID before fetching
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Serialized spreadsheet data as [JsonElement] on success
     *   - Second parameter: Error message string on failure
     */
    fun getSpreadsheet(
        googleSheetsUrl: String? = null,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            getSpreadsheet(googleSheetsUrl)
        }, completion)
    }

    /**
     * Creates a new sheet in the current spreadsheet.
     *
     * @param sheetTitle Name for the new sheet (1-100 chars, unique per spreadsheet)
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Serialized response as [JsonElement] on success
     *   - Second parameter: Error message string on failure
     */
    fun createSheet(
        sheetTitle: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            createSheet(sheetTitle)
        }, completion)
    }

    /**
     * Deletes a sheet from the current spreadsheet.
     *
     * @param sheetTitle Name of the sheet to delete
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Serialized response as [JsonElement] on success
     *   - Second parameter: Error message string on failure
     * @note Fails silently if sheet doesn't exist
     */
    fun deleteSheet(
        sheetTitle: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            deleteSheet(sheetTitle)
        }, completion)
    }

    /**
     * Retrieves metadata for a specific sheet.
     *
     * @param sheetTitle Name of the sheet to retrieve
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Serialized sheet data as [JsonElement] on success
     *   - Second parameter: Error message string on failure
     */
    fun getSheet(
        sheetTitle: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            getSheet(sheetTitle)
        }, completion)
    }

    /**
     * Fetches cell values from a specified range.
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Map of cell references to values on success
     *   - Second parameter: Error message string on failure
     * @throws IllegalArgumentException for invalid cell references
     */
    fun getData(
        from: String,
        to: String = from,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (Map<String, String>?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            getData(from, to)
        }, completion)
    }

    /**
     * Updates multiple cells in batch.
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Raw API response string on success
     *   - Second parameter: Error message string on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    fun updateData(
        updates: Map<String, String>,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (String?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            updateData(updates)
        }, completion)
    }

    /**
     * Inserts a new row at the specified index in a sheet.
     *
     * @param rowIndex The index where the new row should be inserted
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the API response or null if request fails
     *   - Second parameter: Error message string on failure
     * @note Fails silently if sheet doesn't exist
     */
    fun insertRow(
        rowIndex: Int,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            insertRow(rowIndex)
        }, completion)
    }

    /**
     * Deletes a row at the specified index in a sheet.
     *
     * @param rowIndex The index of the row to delete
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the API response or null if request fails
     *   - Second parameter: Error message string on failure
     *
     * @note Fails silently if sheet doesn't exist
     */
    fun deleteRow(
        rowIndex: Int,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            deleteRow(rowIndex)
        }, completion)
    }

    /**
     * Inserts a new column at the specified index in a sheet.
     *
     * @param columnIndex The index where the new column should be inserted
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the API response or null if request fails
     *   - Second parameter: Error message string on failure
     *
     * @note Fails silently if sheet doesn't exist
     */
    fun insertColumn(
        columnIndex: Int,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            insertColumn(columnIndex)
        }, completion)
    }

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the API response or null if request fails
     *   - Second parameter: Error message string on failure
     *
     * @note Fails silently if sheet doesn't exist
     */
    fun deleteColumn(
        columnIndex: Int,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            deleteColumn(columnIndex)
        }, completion)
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: Empty string on success
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalArgumentException if cell reference is invalid
     */
    fun clearCell(
        cell: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (String?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            clearCell(cell)
        }, completion)
    }

    /**
     * Protects a sheet in the spreadsheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the protection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun protectSheet(
        sheetTitle: String? = null,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            protectSheet(sheetTitle)
        }, completion)
    }


    /**
     * Protects all sheets in the spreadsheet, preventing manual edits.
     *
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the protection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun protectAllSheets(
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            protectAllSheets()
        }, completion)
    }

    /**
     * Removes protection from a sheet by title, allowing manual edits again.
     *
     * @param sheetTitle The title of the sheet to unprotect
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the unprotection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun unprotectSheet(
        sheetTitle: String? = null,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            unprotectSheet(sheetTitle)
        }, completion)
    }

    /**
     * Removes protection from all sheets in the spreadsheet, allowing manual edits again.
     *
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the unprotection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun unprotectAllSheets(
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            unprotectAllSheets()
        }, completion)
    }

    /**
     * Protects a range of cells in the spreadsheet.
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the protection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalArgumentException for invalid cell references
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun protectCellsInRange(
        from: String,
        to: String,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            protectCellsInRange(from, to)
        }, completion)
    }

    /**
     * Protects all cells in the spreadsheet.
     *
     * @param sheetTitle The title of the sheet to protect
     * @param dispatcher The coroutine context to execute in (default: DEFAULT)
     * @param completion Callback with:
     *   - First parameter: JSON representation of the protection update result, or null if the operation failed
     *   - Second parameter: Error message string on failure
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    fun protectAllCells(
        sheetTitle: String? = null,
        dispatcher: DispatcherOption = DispatcherOption.DEFAULT,
        completion: (JsonElement?, String?) -> Unit
    ) {
        safeCall(dispatcher, {
            protectAllCells(sheetTitle)
        }, completion)
    }


    //--------------------------------------------------
    // Actual methods
    //--------------------------------------------------

    /**
     * Sets the OAuth2 access token for API authentication.
     * @see SpreadSheetService.setAccessToken
     *
     * @param token Valid Google API access token
     */
    actual suspend fun setAccessToken(token: String): Unit? {
        return spreadsheetService.setAccessToken(token)
    }

    /**
     * Configures the target spreadsheet using its URL.
     * Extracts and validates the spreadsheet ID automatically.
     * @see SpreadSheetService.setSpreadsheetId
     *
     * @param url Valid Google Sheets URL (format: "https://docs.google.com/spreadsheets/d/{ID}/edit")
     */
    actual suspend fun setSpreadsheetId(url: String): Unit? {
        return spreadsheetService.setSpreadsheetId(url)
    }

    /**
     * Creates a new spreadsheet with the specified title.
     * @see SpreadSheetService.createSpreadsheet
     *
     * @param title Name for the new spreadsheet (1-100 characters)
     * @return Serialized spreadsheet metadata as [JsonElement], or null on failure
     */
    actual suspend fun createSpreadsheet(title: String): JsonElement? {
        return Json.encodeToJsonElement(spreadsheetService.createSpreadsheet(title))
    }

    /**
     * Retrieves spreadsheet metadata.
     * @see SpreadSheetService.getSpreadsheet
     *
     * @param googleSheetsUrl Optional URL to set spreadsheet ID before fetching
     * @return Serialized spreadsheet data as [JsonElement], or null on failure
     */
    actual suspend fun getSpreadsheet(googleSheetsUrl: String?): JsonElement? {
        return Json.encodeToJsonElement(spreadsheetService.getSpreadsheet(googleSheetsUrl))
    }

    /**
     * Creates a new sheet in the current spreadsheet.
     * @see SpreadSheetService.createSheet
     *
     * @param sheetTitle Name for the new sheet (1-100 chars, unique per spreadsheet)
     * @return Serialized response as [JsonElement], or null on failure
     */
    actual suspend fun createSheet(sheetTitle: String): JsonElement? {
        return Json.encodeToJsonElement(spreadsheetService.createSheet(sheetTitle))
    }

    /**
     * Deletes a sheet from the current spreadsheet.
     * @see SpreadSheetService.deleteSheet
     *
     * @param sheetTitle Name of the sheet to delete
     * @return Serialized response as [JsonElement], or null on failure
     * @throws IllegalStateException if sheet doesn't exist
     */
    actual suspend fun deleteSheet(sheetTitle: String): JsonElement? {
        return Json.encodeToJsonElement(spreadsheetService.deleteSheet(sheetTitle))
    }

    /**
     * Retrieves metadata for a specific sheet.
     * @see SpreadSheetService.getSheet
     *
     * @param sheetTitle Name of the sheet to retrieve
     * @return Serialized sheet data as [JsonElement], or null if not found
     */
    actual suspend fun getSheet(sheetTitle: String): JsonElement? {
        return Json.encodeToJsonElement(spreadsheetService.getSheet(sheetTitle))
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
    actual suspend fun getData(
        from: String,
        to: String?
    ): Map<String, String>? {
        return spreadsheetService.getData(from, to ?: from)
    }

    /**
     * Updates multiple cells in batch.
     * @see SpreadSheetService.updateData
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @return Raw API response string, or null on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    actual suspend fun updateData(updates: Map<String, String>): String? {
        return spreadsheetService.updateData(updates)
    }


    /**
     * Inserts a new row at the specified index in a sheet.
     *
     * @param rowIndex The index where the new row should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertRow(rowIndex: Int): JsonElement? {
        return spreadsheetService.insertRow(rowIndex)
    }

    /**
     * Deletes a row at the specified index in a sheet.
     *
     * @param rowIndex The index of the row to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteRow(rowIndex: Int): JsonElement? {
        return spreadsheetService.deleteRow(rowIndex)
    }

    /**
     * Inserts a new column at the specified index in a sheet.
     *
     * @param columnIndex The index where the new column should be inserted
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun insertColumn(columnIndex: Int): JsonElement? {
        return spreadsheetService.insertColumn(columnIndex)
    }

    /**
     * Deletes a column at the specified index in a sheet.
     *
     * @param columnIndex The index of the column to delete
     * @return JSON representation of the API response or null if request fails
     */
    actual suspend fun deleteColumn(columnIndex: Int): JsonElement? {
        return spreadsheetService.deleteColumn(columnIndex)
    }

    /**
     * Clears the content of a specific cell in the configured sheet.
     *
     * @param cell The cell reference in A1 notation (e.g., "A1")
     * @return Empty string on success
     * @throws IllegalArgumentException if cell reference is invalid
     */
    actual suspend fun clearCell(cell: String): String? {
        return spreadsheetService.clearCell(cell)
    }

    /**
     * Protects a sheet in the spreadsheet by title, preventing manual edits.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectSheet(sheetTitle: String?): JsonElement? {
        return spreadsheetService.protectSheet(sheetTitle)
    }

    /**
     * Protects all sheets in the spreadsheet, preventing manual edits.
     *
     * @return JSON representation of the protection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectAllSheets(): JsonElement? {
        return spreadsheetService.protectAllSheets()
    }

    /**
     * Removes protection from a sheet by title, allowing manual edits again.
     *
     * @param sheetTitle The title of the sheet to unprotect
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectSheet(sheetTitle: String?): JsonElement? {
        return spreadsheetService.unprotectSheet(sheetTitle)
    }

    /**
     * Removes protection from all sheets in the spreadsheet, allowing manual edits again.
     *
     * @return JSON representation of the unprotection update result, or null if the operation failed
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun unprotectAllSheets(): JsonElement? {
        return spreadsheetService.unprotectAllSheets()
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
    ): JsonElement? {
        return spreadsheetService.protectCellsInRange(from, to)
    }

    /**
     * Protects all cells in the spreadsheet.
     *
     * @param sheetTitle The title of the sheet to protect
     * @return JSON representation of the protection update result, or null if the operation failed
     *
     * @throws IllegalStateException if spreadsheet ID is not set
     */
    actual suspend fun protectAllCells(sheetTitle: String?): JsonElement? {
        return spreadsheetService.protectAllCellsInSheet(sheetTitle)
    }


    //--------------------------------------------------
    // Private helper methods
    //--------------------------------------------------

    /**
     * Unified safe execution wrapper for all operations.
     * Handles:
     * - Dispatcher selection
     * - Error conversion to user messages
     * - Serialization exceptions
     * - Completion handler invocation
     *
     * @param dispatcherOption Determines the coroutine execution context
     * @param block The operation to execute safely
     * @param completion Callback to receive results or errors
     * @param T The return type of the operation
     */
    private fun <T> safeCall(
        dispatcherOption: DispatcherOption,
        block: suspend () -> T,
        completion: (T?, String?) -> Unit
    ) {
        val dispatcher = when (dispatcherOption) {
            DispatcherOption.DEFAULT -> Dispatchers.Default
            DispatcherOption.IO -> Dispatchers.IO
            DispatcherOption.UNCONFINED -> Dispatchers.Unconfined
        }

        CoroutineScope(dispatcher).launch {
            try {
                val result = block()
                completion(result, null)
            } catch (e: IllegalArgumentException) {
                completion(null, e.message ?: "Invalid operation parameters")
            } catch (e: SerializationException) {
                completion(null, "Data format error: ${e.message ?: "Unknown"}")
            } catch (e: Exception) {
                completion(null, e.message ?: "Unknown error occurred")
            }
        }
    }
}