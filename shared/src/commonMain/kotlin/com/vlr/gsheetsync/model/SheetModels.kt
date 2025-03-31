package com.vlr.gsheetsync.model

import kotlinx.serialization.Serializable

@Serializable
data class SheetCell(
    val row: Int,
    val column: Int,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SheetRange(
    val sheetId: String,
    val range: String,
    val values: List<List<String>>
)

@Serializable
data class SheetUpdate(
    val sheetId: String,
    val cell: SheetCell,
    val userId: String
)

@Serializable
data class SheetConfig(
    val spreadsheetId: String,
    val sheetId: String,
    val apiKey: String
) 