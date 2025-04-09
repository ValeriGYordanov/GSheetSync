package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels
import com.vlr.gsheetsync.feature.sheets.data.model.SheetSyncResponseModels.Spreadsheet
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SpreadSheetService(private val client: HttpClient) {

    suspend fun initialiseSheets(accessToken: Any): Boolean {
        token = accessToken as String
        try {
            val request = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken as String)
                setBody(SpreadsheetRequest(SpreadsheetRequest.Properties("NEW_SHEET")))
            }

            SyncLog.print(request.headers.toString())

            val response: HttpResponse = request.call.response

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                // Read response body as string
                spreadsheetId = Json.parseToJsonElement(responseBody).jsonObject["spreadsheetId"]?.toString()?.replace("\"", "")
                SyncLog.print("Response: $responseBody")
                return true
            } else {
                SyncLog.print("Request failed: ${response.status}")
            }
        } catch (e: Exception) {
            SyncLog.print("Request failed with exception: $e")
        }

        return false

    }

    //Sheet: https://sheets.googleapis.com/v4/spreadsheets1F60jSWHyg4CTrLbKAOtNpLi4RndgoaDm2A66i_b7rLY/values:batchUpdate
    private val baseUrl = "https://sheets.googleapis.com/v4/spreadsheets"
    private lateinit var token: String
    private var spreadsheetId: String? = null

    suspend fun addText() {
        try {
            val jsonBody = """
        {
            "valueInputOption": "RAW",
            "data": [
                {
                    "range": "Лист1",
                    "majorDimension": "ROWS",
                    "values": [
                        ["Test", "Add", "Some", "Text"]
                    ]
                }
            ],
            "includeValuesInResponse": true,
            "responseValueRenderOption": "UNFORMATTED_VALUE",
            "responseDateTimeRenderOption": "FORMATTED_STRING"
        }
        """.trimIndent()



            val url = "$baseUrl/$spreadsheetId/values:batchUpdate"

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(jsonBody)
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val jsonElement = Json.parseToJsonElement(responseBody)
                val updatedSpreadsheetId = jsonElement.jsonObject["spreadsheetId"]?.toString()
                SyncLog.print("Spreadsheet updated: $updatedSpreadsheetId")
                SyncLog.print("Response: $responseBody")
            } else {
                SyncLog.print("Request failed: ${response.status}")
            }
        } catch (e: Exception) {
            SyncLog.print("Request failed with exception: $e")
        }
    }

    //GPT Version

    suspend fun initialiseSheetsss(accessToken: Any): String {
        token = accessToken as String
        return try {
            val response: HttpResponse = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(
                    Spreadsheet(
                        properties = SheetSyncResponseModels.SpreadsheetProperties(title = "NEW_SHEET_V2"),
                        sheets = listOf(
                            SheetSyncResponseModels.Sheet(
                                properties = SheetSyncResponseModels.SheetProperties(
                                    title = "sheetName"
                                )
                            )
                        )
                    )
                )
            }

            if (response.status.isSuccess()) {
                val body = response.bodyAsText()
                spreadsheetId = Json.parseToJsonElement(body).jsonObject["spreadsheetId"]
                    ?.jsonPrimitive?.content
                SyncLog.print("Spreadsheet created: $spreadsheetId")
                SyncLog.print("Using TOKEN created: $token")
                "Success"
            } else {
                SyncLog.print("Create failed: ${response.status}")
                response.bodyAsText()
            }
        } catch (e: Exception) {
            SyncLog.print("Create exception: $e")
            e.message ?: "Unknown error"
        }
    }
}


@Serializable
data class SpreadsheetRequest(
    val properties: Properties
) {
    @Serializable
    data class Properties(
        val title: String
    )
}