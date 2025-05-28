package com.vlr.gsheetsync.di

import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetRepository
import com.vlr.gsheetsync.feature.sheets.data.SpreadSheetService
import com.vlr.gsheetsync.feature.sheets.domain.SheetsUseCase
import com.vlr.gsheetsync.feature.sheets.engine.SSEngine
import com.vlr.gsheetsync.feature.sheets.presentation.SheetViewModel
import org.koin.dsl.module

val sheetsModule = module {
    single<SpreadSheetService> { SpreadSheetService(get()) }
    single<SpreadSheetRepository> { SpreadSheetRepository(get()) }
    single<SheetsUseCase> { SheetsUseCase(get()) }
    single<SheetViewModel> { SheetViewModel(get()) }
    single<SSEngine> { SSEngine(get()) }
}