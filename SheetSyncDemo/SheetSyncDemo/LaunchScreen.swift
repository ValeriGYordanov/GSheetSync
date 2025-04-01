import SwiftUI
import shared

struct LaunchScreen: View {
    @StateObject private var viewModel = SheetViewModel()
    @State private var apiKey: String = ""
    
    var body: some View {
        ZStack {
            Color(red: 0.1, green: 0.1, blue: 0.1)
                .ignoresSafeArea()
            
            VStack(spacing: 20) {
                Text("Sheet Sync")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                
                SecureField("API Key", text: $apiKey)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding(.horizontal)
                
                Button(action: {
                    viewModel.processIntent(intent: shared.SheetIntent.Initialize(apiKey: apiKey))
                }) {
                    HStack {
                        if case .Loading = viewModel.uiState {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Connect")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .disabled(apiKey.isEmpty || case .Loading = viewModel.uiState)
                .padding(.horizontal)
                
                if case .Error(let message) = viewModel.uiState {
                    Text(message)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }
            }
        }
    }
} 
