package com.vlr.gsheetsync.feature.sheets.presentation.model

data class SyncResult(
    val success: Any? = null,
    val loading: Boolean = false,
    val error: String? = null
)