package com.vlr.gsheetsync.di

import com.vlr.gsheetsync.feature.sheets.presentation.SheetViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun initKoin() {
    val modules = sharedModule
    startKoin {
        modules(modules)
    }
}

class ViewModelInjector: KoinComponent {
    val sheetViewModel: SheetViewModel by inject()
}