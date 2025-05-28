package com.vlr.gsheetsync.android.di

import com.vlr.gsheetsync.feature.sheets.presentation.SheetViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { SheetViewModel(get()) }
}