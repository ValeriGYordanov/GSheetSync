package com.vlr.gsheetsync.feature.sheets.engine

import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetService
import kotlinx.serialization.json.JsonElement

expect class SSEngine(spreadsheetService: SpreadSheetService) {


    /**
     * Sets the OAuth2 access token for API authentication.
     * @see SpreadSheetService.setAccessToken
     *
     * @param token Valid Google API access token
     */
    suspend fun setAccessToken(token: String): Unit?

    /**
     * Configures the target spreadsheet using its URL.
     * Extracts and validates the spreadsheet ID automatically.
     * @see SpreadSheetService.setSpreadsheetId
     *
     * @param url Valid Google Sheets URL (format: "https://docs.google.com/spreadsheets/d/{ID}/edit")
     */
    suspend fun setSpreadsheetId(url: String): Unit?

    /**
     * Creates a new spreadsheet with the specified title.
     * @see SpreadSheetService.createSpreadsheet
     *
     * @param title Name for the new spreadsheet (1-100 characters)
     * @return Serialized spreadsheet metadata as [JsonElement], or null on failure
     */
    suspend fun createSpreadsheet(title: String): JsonElement?

    /**
     * Retrieves spreadsheet metadata.
     * @see SpreadSheetService.getSpreadsheet
     *
     * @param googleSheetsUrl Optional URL to set spreadsheet ID before fetching
     * @return Serialized spreadsheet data as [JsonElement], or null on failure
     */
    suspend fun getSpreadsheet(googleSheetsUrl: String?): JsonElement?

    /**
     * Creates a new sheet in the current spreadsheet.
     * @see SpreadSheetService.createSheet
     *
     * @param sheetTitle Name for the new sheet (1-100 chars, unique per spreadsheet)
     * @return Serialized response as [JsonElement], or null on failure
     */
    suspend fun createSheet(sheetTitle: String): JsonElement?

    /**
     * Deletes a sheet from the current spreadsheet.
     * @see SpreadSheetService.deleteSheet
     *
     * @param sheetTitle Name of the sheet to delete
     * @return Serialized response as [JsonElement], or null on failure
     * @throws IllegalStateException if sheet doesn't exist
     */
    suspend fun deleteSheet(sheetTitle: String): JsonElement?

    /**
     * Retrieves metadata for a specific sheet.
     * @see SpreadSheetService.getSheet
     *
     * @param sheetTitle Name of the sheet to retrieve
     * @return Serialized sheet data as [JsonElement], or null if not found
     */
    suspend fun getSheet(sheetTitle: String): JsonElement?

    /**
     * Fetches cell values from a specified range.
     * @see SpreadSheetService.getData
     *
     * @param from Starting cell reference (A1 notation, e.g., "B2")
     * @param to Ending cell reference (defaults to [from] for single-cell)
     * @return Map of cell references to values, or null on failure
     * @throws IllegalArgumentException for invalid cell references
     */
    suspend fun getData(
        from: String,
        to: String?
    ): Map<String, String>?

    /**
     * Updates multiple cells in batch.
     * @see SpreadSheetService.updateData
     *
     * @param updates Map of cell references to values (e.g., {"A1" to "Hello"})
     * @return Raw API response string, or null on failure
     * @throws IllegalArgumentException for invalid cell references or blank values
     */
    suspend fun updateData(updates: Map<String, String>): String?
}