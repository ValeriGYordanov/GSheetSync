package com.vlr.gsheetsync.android

import android.app.Application
import com.vlr.gsheetsync.android.di.viewModelsModule
import com.vlr.gsheetsync.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SheetSyncApp: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        val modules = sharedModule + viewModelsModule

        startKoin {
            androidContext(this@SheetSyncApp)
            modules(modules)
        }
    }

}