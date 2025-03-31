package com.vlr.gsheetsync.android

import android.app.Application
import com.vlr.gsheetsync.di.platformModule
import com.vlr.gsheetsync.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SheetSyncDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@SheetSyncDemoApp)
            modules(sharedModule + platformModule())
        }
    }
} 