import SwiftUI
import shared

struct ContentView: View {
	@StateObject private var viewModel = SheetViewModel()
	@State private var isConnected = false
	
	private var isLoading: Bool {
		if case is SheetUiState.Loading = viewModel.uiState {
			return true
		}
		return false
	}
	
	var body: some View {
		ZStack {
			if !isConnected {
				LaunchScreen(
					uiState: viewModel.uiState,
					onConnect: { apiKey in
						viewModel.processIntent(intent: .Initialize(apiKey: apiKey))
					},
					onConnectionSuccess: {
						isConnected = true
					}
				)
			} else {
				MainScreen(
					onObserve: {
						viewModel.processIntent(intent: .ObserveCell(row: 0, column: 0))
					},
					onUpdate: {
						viewModel.processIntent(intent: .UpdateCell(
							cell: SheetCell(row: 0, column: 0, value: "Updated at \(Date().timeIntervalSince1970)")
						))
					},
					onDelete: {
						viewModel.processIntent(intent: .DeleteCell(row: 0, column: 0))
					},
					isLoading: isLoading
				)
			}
		}
	}
}
