package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog

class SpreadSheetRepository(private val service: SpreadSheetServiceGPT) {

    fun setAccessToken(accessToken: String) {
        service.setAccessToken(accessToken)
    }

    fun setSpreadsheetId(sheetId: String) {
        service.setSpreadsheetId(sheetId)
    }

    suspend fun initialiseSheets(accessToken: Any): String {
        service.setAccessToken(accessToken as String)
        return "Success"
    }

    companion object {
        private var counter = 0
    }

    suspend fun addText() {
        SyncLog.print("Performing addText $counter")

        when (counter) {
            0 -> {
                SyncLog.print(service.updateData(
                    mapOf(
                        "A1" to "AAAA",
                        "B2" to "BBBB",
                        "C3" to "CCCC",
                        "D4" to "DDDD",
                        "E5" to "EEEE"
                    )
                ))
            }
            1 -> SyncLog.print("CellData for A2 to B3 : " + service.getData("A1", "E5").toString())
            else -> counter = -1
        }

        counter++
    }




}