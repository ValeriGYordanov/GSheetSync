import SwiftUI
import shared

extension ContentView {
    
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
}

struct ContentView: View {
    @ObservedObject private(set) var viewModel: SheetViewModelWrapper
    @State private var apiKey: String = ""
    @State private var showHome: Bool = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 0.102, green: 0.102, blue: 0.102) // #1A1A1A
                    .ignoresSafeArea()
                
                VStack(spacing: 16) {
                    Text("Google Sheets Sync")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                    
                    SecureField("API Key", text: $apiKey)
                        .textFieldStyle(CustomTextFieldStyle())
                        .padding(.horizontal)
                        .foregroundColor(.white)
                        .tint(.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(apiKey.isEmpty ? Color(red: 0.459, green: 0.459, blue: 0.459) : Color(red: 0.129, green: 0.588, blue: 0.953), lineWidth: 1)
                        )
                    
                    Button(action: {
                        viewModel.sheetViewModel.initialiseService(apiKey: apiKey)
                    }) {
                        if viewModel.sheetState.loading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Connect")
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                        }
                    }
                    .background(Color(red: 0.129, green: 0.588, blue: 0.953)) // #2196F3
                    .cornerRadius(8)
                    .disabled(apiKey.isEmpty || viewModel.sheetState.loading)
                    
                    if let error = viewModel.sheetState.error {
                        Text(error)
                            .foregroundColor(Color(red: 0.898, green: 0.451, blue: 0.451)) // #E57373
                            .font(.subheadline)
                    }
                }
                .padding()
            }
            .navigationDestination(isPresented: $showHome) {
                HomeView()
            }
            .onChange(of: viewModel.sheetState.success) { success in
                if success != nil {
                    showHome = true
                }
            }
        }
        .onAppear {
            self.viewModel.startObserving()
        }
    }
}

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color(red: 0.176, green: 0.176, blue: 0.176)) // #2D2D2D
            .cornerRadius(8)
    }
}

struct ErrorMessage: View {
    var message: String
    
    var body: some View {
        Text(message)
            .font(.title)
    }
} 