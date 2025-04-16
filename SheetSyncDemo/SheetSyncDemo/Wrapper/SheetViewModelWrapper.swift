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
final class SheetViewModelWrapper: ObservableObject {
    let sheetViewModel: SheetViewModel
    
    // MARK: - Published States
    @Published private(set) var loadingState: GSyncResultLoading?
    @Published private(set) var errorState: GSyncResultError?
    @Published private(set) var setAccessTokenIdState: GSyncResult<AnyObject>?
    @Published private(set) var setSpreadsheetIdState: GSyncResult<AnyObject>?
    @Published private(set) var createSpreadsheetState: GSyncResult<AnyObject>?
    @Published private(set) var getSpreadsheetState: GSyncResult<AnyObject>?
    @Published private(set) var createSheetState: GSyncResult<AnyObject>?
    @Published private(set) var deleteSheetState: GSyncResult<AnyObject>?
    @Published private(set) var getSheetState: GSyncResult<AnyObject>?
    @Published private(set) var getDataState: GSyncResult<NSDictionary>?
    @Published private(set) var updateDataState: GSyncResult<AnyObject>?

    // MARK: - Initialization
    init() {
        sheetViewModel = ViewModelInjector().sheetViewModel
        setupInitialStates()
    }
    
    private func setupInitialStates() {
        loadingState = sheetViewModel.loadingState.value
        errorState = sheetViewModel.errorState.value
        setAccessTokenIdState = sheetViewModel.setAccessTokenState.value
        setSpreadsheetIdState = sheetViewModel.setSpreadsheetIdState.value
        createSpreadsheetState = sheetViewModel.createSpreadsheetState.value
        getSpreadsheetState = sheetViewModel.getSpreadsheetState.value
        createSheetState = sheetViewModel.createSheetState.value
        deleteSheetState = sheetViewModel.deleteSheetState.value
        getSheetState = sheetViewModel.getSheetState.value
        getDataState = sheetViewModel.getDataState.value
        updateDataState = sheetViewModel.updateDataState.value
    }

    // MARK: - Observation Management
    func startObserving() {
        observeLoadingState()
        observeErrorState()
        observeSetAccessTokenState()
        observeSetSpreadsheetIdState()
        observeCreateSpreadsheetState()
        observeGetSpreadsheetState()
        observeCreateSheetState()
        observeDeleteSheetState()
        observeGetSheetState()
        observeGetDataState()
        observeUpdateDataState()
    }
    
    // MARK: - Observation Methods
    
    private func observeLoadingState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.loadingState {
                await MainActor.run {
                    self.loadingState = state
                    print("🔄 Loading state: \(state?.loading ?? false)")
                }
            }
        }
    }
    
    private func observeErrorState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.errorState {
                await MainActor.run {
                    self.errorState = state
                    if let state {
                        print("❌ Error state: \(state.description)")
                    }
                }
            }
        }
    }
    
    private func observeSetAccessTokenState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.setAccessTokenState {
                await MainActor.run {
                    self.setAccessTokenIdState = state
                    if state != nil {
                        print("🔑 Access token state updated")
                    }
                }
            }
        }
    }
    
    private func observeSetSpreadsheetIdState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.setSpreadsheetIdState {
                await MainActor.run {
                    self.setSpreadsheetIdState = state
                    if state != nil {
                        print("📄 Spreadsheet ID state updated")
                    }
                }
            }
        }
    }
    
    private func observeCreateSpreadsheetState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.createSpreadsheetState {
                await MainActor.run {
                    self.createSpreadsheetState = state
                    if state != nil {
                        print("🆕 Create spreadsheet state updated")
                    }
                }
            }
        }
    }
    
    private func observeGetSpreadsheetState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.getSpreadsheetState {
                await MainActor.run {
                    self.getSpreadsheetState = state
                    if state != nil {
                        print("📊 Get spreadsheet state updated")
                    }
                }
            }
        }
    }
    
    private func observeCreateSheetState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.createSheetState {
                await MainActor.run {
                    self.createSheetState = state
                    if state != nil {
                        print("📝 Create sheet state updated")
                    }
                }
            }
        }
    }
    
    private func observeDeleteSheetState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.deleteSheetState {
                await MainActor.run {
                    self.deleteSheetState = state
                    if state != nil {
                        print("🗑️ Delete sheet state updated")
                    }
                }
            }
        }
    }
    
    private func observeGetSheetState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.getSheetState {
                await MainActor.run {
                    self.getSheetState = state
                    if state != nil {
                        print("📋 Get sheet state updated")
                    }
                }
            }
        }
    }
    
    private func observeGetDataState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.getDataState {
                await MainActor.run {
                    self.getDataState = state
                    print("📦 GetData state: \(String(describing: state))")
                    self.objectWillChange.send() // Force UI update
                }
            }
        }
    }
    
    private func observeUpdateDataState() {
        Task { [weak self] in
            guard let self else { return }
            for await state in sheetViewModel.updateDataState {
                await MainActor.run {
                    self.updateDataState = state
                    if state != nil {
                        print("🔄 Update data state updated")
                    }
                }
            }
        }
    }
}
