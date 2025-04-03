package com.vlr.gsheetsync.feature.sheets.data

import com.vlr.gsheetsync.SyncLog
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

class SpreadSheetService(private val client: HttpClient) {

    private val baseUrl = "https://sheets.googleapis.com/v4/spreadsheets"
    private lateinit var token: String

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
                val responseBody = response.bodyAsText()  // Read response body as string
                SyncLog.print("Response: $responseBody")
                return true
            } else {
                SyncLog.print("Request failed: ${response.status}")
            }
        } catch (e: Exception) {
            SyncLog.print("Request failed with exception: $e")
        } finally {
            client.close()
        }
        return false

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