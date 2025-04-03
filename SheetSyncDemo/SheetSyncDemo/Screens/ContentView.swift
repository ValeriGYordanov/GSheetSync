import SwiftUI
import shared


struct ContentView: View {
    @ObservedObject var viewModel: SheetViewModelWrapper
    @Binding var showHome: Bool
    @State private var apiKey: String = ""
    
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
                    
                    Button(action: {
                        viewModel.sheetViewModel.initialiseService(accessToken: apiKey)
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
            .onAppear {
                self.viewModel.startObserving()
            }
            .navigationDestination(isPresented: $showHome) {
                HomeScreen()
            }
            .onChange(of: viewModel.sheetState) { oldState, newState in
                SyncLog().print(message: "State changed from \(oldState) to \(newState)")
                if newState.success != nil {
                    SyncLog().print(message: "Success detected - navigating to Home")
                    showHome = true
                }
            }
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
