//
//  AppRoot.swift
//  SheetSyncDemo
//
//  Created by Valeri Yordanov on 1.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import GoogleSignIn
import shared

@main
struct SheetSyncDemoApp: App {
    @StateObject private var viewModel = SheetViewModelWrapper()
    @State private var showHome = false
    
    init() {
        KoinInitializerKt.doInitKoin()
        // Initialize Google Sign-In
        GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
            if let error = error {
                print("Error restoring previous sign-in: \(error.localizedDescription)")
            }
        }
    }
    
    var body: some Scene {
        WindowGroup {
            if showHome {
                HomeScreen(viewModel: viewModel)
            } else {
                SignInView(viewModel: viewModel, showHome: $showHome)
            }
        }
    }
}
