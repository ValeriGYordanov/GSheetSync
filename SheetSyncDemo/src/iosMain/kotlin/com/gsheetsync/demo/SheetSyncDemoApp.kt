package com.gsheetsync.demo

import com.gsheetsync.di.platformModule
import com.gsheetsync.di.sharedModule
import com.gsheetsync.presentation.SheetModel
import kotlinx.cinterop.ObjCObjectBase
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.value
import platform.Foundation.*
import platform.objc.*
import platform.SwiftUI.*
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SheetSyncDemoApp : KoinComponent {
    private val sheetModel: SheetModel by inject()

    fun start() {
        // Initialize Koin
        startKoin {
            modules(sharedModule + platformModule)
        }

        // Create and run the SwiftUI app
        autoreleasepool {
            val app = NSApplication.sharedApplication()
            if (app != null) {
                app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
                val delegate = AppDelegate(sheetModel)
                app.setDelegate(delegate)
                app.run()
            }
        }
    }
}

private class AppDelegate(
    private val sheetModel: SheetModel
) : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationDidFinishLaunching(notification: NSNotification?) {
        val window = NSWindow(
            contentRect = NSMakeRect(0.0, 0.0, 480.0, 300.0),
            styleMask = NSWindowStyleMaskTitled or
                    NSWindowStyleMaskClosable or
                    NSWindowStyleMaskMiniaturizable or
                    NSWindowStyleMaskResizable,
            backing = NSBackingStoreType.NSBackingStoreBuffered,
            defer = false
        )
        window.setTitle("SheetSync Demo")
        window.center()
        window.makeKeyAndOrderFront(null)
        window.setContentView(SheetDemoView(sheetModel))
    }

    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication?): Boolean = true
} 