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
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var info: String = "Gathering data..."
    
    init() {
        KoinInitializerKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            Group {
                if isLoading && !showHome {
                    VStack {
                        Text("Welcome to GSheet")
                            .padding(10)
                            .font(.title)
                        Text(info)
                            .padding(10)
                            .font(.caption)
                        ProgressView()
                            .scaleEffect(2)
                    }
                    
                } else if let error = errorMessage {
                    VStack {
                        Text("Error: \(error)")
                            .foregroundColor(.red)
                            .padding()
                        Button("Retry") {
                            errorMessage = nil
                            checkPreviousLogin()
                        }
                    }
                } else if showHome {
                    HomeScreen(viewModel: viewModel)
                } else {
                    SignInView(viewModel: viewModel)
                }
            }
            .onAppear {
                checkPreviousLogin()
            }
            .task {
                // Observe loading state
                for await state in viewModel.sheetViewModel.loadingState {
                    await MainActor.run {
                        isLoading = state?.loading ?? false
                    }
                }
            }
            .task {
                // Observe error state
                for await state in viewModel.sheetViewModel.errorState {
                    await MainActor.run {
                        if let error = state {
                            errorMessage = error.description
                        }
                    }
                }
            }
        }
    }
    
    private func checkPreviousLogin() {
        isLoading = true
        errorMessage = nil
        
        GIDSignIn.sharedInstance.restorePreviousSignIn { [weak viewModel] user, error in
            guard let viewModel = viewModel else { return }
            
            DispatchQueue.main.async {
                if let error = error {
                    isLoading = false
                    errorMessage = "Sign-in restoration failed: \(error.localizedDescription)"
                    return
                }
                
                guard let user = user else {
                    // No previous user, show sign-in view
                    isLoading = false
                    return
                }
                
                print("User already logged in initialising with access token: \(user.accessToken.tokenString)")
                info = "\(user.profile?.name ?? ""), please wait while we load your spreadsheet..."
                
                // 1. Set the access token first
                viewModel.sheetViewModel.setAccessToken(accessToken: user.accessToken.tokenString)
                
                // 2. Observe for access token state change
                Task { @MainActor in
                    for await state in viewModel.sheetViewModel.setAccessTokenState {
                        if state != nil {
                            // 3. Access token set, now set spreadsheet ID
                            info = "Almost there..."
                            viewModel.sheetViewModel.setSpreadsheetId(
                                url: "https://docs.google.com/spreadsheets/d/1px0triZ-w_8UH122HDSNjbAHJf1LAMi6s92twPJP2_0/edit"
                            )
                            
                            // 4. Observe for spreadsheet ID state change
                            for await spreadsheetState in viewModel.sheetViewModel.setSpreadsheetIdState {
                                if spreadsheetState != nil {
                                    // 5. Both are set, show home screen
                                    showHome = true
                                    isLoading = false
                                    break // Stop observing once we get our result
                                }
                            }
                            break // Stop observing once we get our result
                        }
                    }
                }
            }
        }
    }
}
