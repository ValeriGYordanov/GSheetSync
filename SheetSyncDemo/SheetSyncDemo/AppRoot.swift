//
//  AppRoot.swift
//  SheetSyncDemo
//
//  Created by Valeri Yordanov on 1.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

// 1. First, define a root view that will control the switching
struct AppRoot: View {
    @StateObject private var viewModel = SheetViewModelWrapper()
    @State private var showHome = false
    
    var body: some View {
        if showHome {
            HomeScreen()
        } else {
            ContentView(viewModel: viewModel, showHome: $showHome)
        }
    }
}
