package com.vlr.gsheetsync.di

import com.vlr.gsheetsync.model.SheetConfig
import com.vlr.gsheetsync.presentation.SheetModel
import com.vlr.gsheetsync.repository.GoogleSheetRepository
import com.vlr.gsheetsync.repository.SheetRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformModule(): Module

val sharedModule = module {
    single { Json { prettyPrint = true } }
    
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
        }
    }
    
    single<SheetRepository> {
        GoogleSheetRepository(get(), get())
    }
    
    single { SheetConfig(
        spreadsheetId = "YOUR_SPREADSHEET_ID",
        sheetId = "YOUR_SHEET_ID",
        apiKey = "YOUR_API_KEY"
    ) }
    
    single { SheetModel(get(), get()) }
} 