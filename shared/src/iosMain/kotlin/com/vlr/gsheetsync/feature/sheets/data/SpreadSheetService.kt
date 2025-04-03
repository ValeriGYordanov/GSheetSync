package com.vlr.gsheetsync.feature.sheets.data

actual class SpreadSheetService actual constructor(){

    actual suspend fun initialiseSheets(accessToken: Any): Boolean {
        return true
    }

}