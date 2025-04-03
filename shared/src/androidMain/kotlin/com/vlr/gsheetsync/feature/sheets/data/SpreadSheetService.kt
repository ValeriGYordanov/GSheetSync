package com.vlr.gsheetsync.feature.sheets.data

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials


actual class SpreadSheetService actual constructor() {

    companion object {
        private lateinit var service: Sheets
        private lateinit var spreadsheetId: String
        private lateinit var spreadsheet: Spreadsheet
    }

    actual suspend fun initialiseSheets(accessToken: Any): Boolean {
        if (accessToken !is AccessToken) {
            throw Exception("Invalid Android Class is used to grand access for Google Sheets")
        }
        try {
            val credentials = GoogleCredentials.create(accessToken).createScoped(listOf(SheetsScopes.SPREADSHEETS))
            val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(
                credentials
            )
            service = Sheets.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName("SheetSync")
                .build()
            return true
        } catch (e: Exception) {
            return false
        }

//        spreadsheet = Spreadsheet()
//            .setProperties(
//                SpreadsheetProperties()
//                    .setTitle("TEST")
//            )
//        spreadsheet = service.spreadsheets().create(spreadsheet)
//            .setFields("spreadsheetId")
//            .execute()

    }

}