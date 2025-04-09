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
    }
    
    var body: some Scene {
        WindowGroup {
            Group {
                if showHome {
                    HomeScreen(viewModel: viewModel)
                } else {
                    SignInView(viewModel: viewModel, showHome: $showHome)
                }
            }
            .onAppear {
                // Move the restoration check here
                GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
                    if error == nil && user != nil {
                        DispatchQueue.main.async {
                            print("User already logged in initialising with access token: \(String(describing: user!.accessToken.tokenString))")
                            viewModel.sheetViewModel.initialiseService(accessToken: user!.accessToken.tokenString)
                            showHome = true
                        }
                    } else {
                        print("Error restoring previous sign in: \(String(describing: error))")
                    }
                }
            }
        }
    }
}
