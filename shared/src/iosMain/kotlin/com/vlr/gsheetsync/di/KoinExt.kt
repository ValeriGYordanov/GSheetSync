package com.vlr.gsheetsync.di

import com.vlr.gsheetsync.presentation.SheetModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module

object KoinHelper : KoinComponent {
    val sheetModel: SheetModel by inject()
}

// Extension function to be called from Swift
fun Module.getSheetModel(): SheetModel = KoinHelper.sheetModel 