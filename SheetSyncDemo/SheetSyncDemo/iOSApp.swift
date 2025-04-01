import SwiftUI
import shared

@main
struct iOSApp: App {
	init() {
		// Initialize Koin
		_ = SharedModuleKt.sharedModule
	}
	
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}