//
//  SheetViewModelWrapper.swift
//  SheetSyncDemo
//
//  Created by Valeri Yordanov on 1.04.25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

@MainActor
class SheetViewModelWrapper: ObservableObject {
    let sheetViewModel: SheetViewModel

    init() {
        sheetViewModel = ViewModelInjector().sheetViewModel
        sheetState = sheetViewModel.state.value // Initialize with the current value
    }

    @Published var sheetState: SyncResult // Change type to store actual state

    func startObserving() {
        Task {
            for await states in sheetViewModel.state { // Iterate over state flow
                self.sheetState = states // Assign the unwrapped value
            }
        }
    }
}
