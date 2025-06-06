package com.vlr.gsheetsync.feature.sheets.data.model

data class SheetConfig(
    val token: String,
    val spreadsheetId: String,
    val sheetName: String
)