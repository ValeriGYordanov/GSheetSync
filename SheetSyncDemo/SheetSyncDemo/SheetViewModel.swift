import Foundation
import shared
import Combine

class SheetViewModel: ObservableObject {
    private let model: shared.SheetModel
    private var cancellables = Set<AnyCancellable>()
    
    @Published var uiState: shared.SheetUiState = shared.SheetUiState.Initial()
    
    init() {
        let koin = SharedModuleKt.sharedModule
        model = koin.getSheetModel()
        
        // Convert Kotlin StateFlow to Combine publisher using Skie
        let publisher = model.state.toPublisher()
        
        publisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.uiState = if state.isLoading {
                    shared.SheetUiState.Loading()
                } else if state.isConnected {
                    shared.SheetUiState.Connected()
                } else if let error = state.error {
                    shared.SheetUiState.Error(message: error)
                } else {
                    shared.SheetUiState.Initial()
                }
            }
            .store(in: &cancellables)
    }
    
    func processIntent(intent: shared.SheetIntent) {
        model.processIntent(intent: intent)
    }
} 
