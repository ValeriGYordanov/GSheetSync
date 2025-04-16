import SwiftUI
import shared

struct HomeScreen: View {
    @ObservedObject var viewModel: SheetViewModelWrapper
    @State private var sheetId: String = ""
    @State private var sheetName: String = ""
    @State private var scrollOffset: CGFloat = 0
    
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var items: NSDictionary = [:]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Header Section
                VStack(spacing: 12) {
                    // Title section
                    VStack(spacing: 8) {
                        Text("Welcome to SheetSync")
                            .font(.system(size: 28))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                        
                        Text("Manage your sheets efficiently")
                            .font(.body)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                    }
                    
                    // Input fields
                    VStack(spacing: 12) {
                        TextField("Enter Value For A2", text: $sheetId)
                            .textFieldStyle(HomeTextFieldStyle())
                        
                        TextField("Enter Value For B2", text: $sheetName)
                            .textFieldStyle(HomeTextFieldStyle())
                    }
                    .padding(.vertical, 8)
                    
                    // Buttons
                    VStack(spacing: 8) {
                        Button(action: {
                            viewModel.sheetViewModel.updateData(updates: [
                                "A2": sheetId,
                                "B2": sheetName
                            ])
                        }) {
                            Text("Sync Now")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(12)
                        }
                        
                        Button(action: {
                            // TODO: Implement view history action
                        }) {
                            Text("View History")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.blue, lineWidth: 1)
                                )
                                .foregroundColor(.blue)
                        }
                        
                        Button(action: {
                            viewModel.sheetViewModel.getData(from: "A1", to: "G10")
                        }) {
                            Text("Get Data")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .foregroundColor(.blue)
                        }
                    }
                }
                .padding(20)
                .background(Color(red: 0.18, green: 0.18, blue: 0.18))
                .cornerRadius(16)
                .padding(16)
                
                // List Section
                VStack(spacing: 8) {
                    if isLoading {
                        VStack {
                            Text("Loading data...")
                                .padding(10)
                                .font(.footnote)
                            ProgressView()
                                .scaleEffect(2)
                        }
                    } else if let error = errorMessage {
                        Text("Error: \(error)")
                            .foregroundColor(.red)
                            .padding()
                    } else {
                        ForEach(Array(items.allKeys.compactMap { $0 as? String }), id: \.self) { key in
                            if let value = items[key] {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(key)
                                            .font(.headline)
                                            .foregroundColor(.white)
                                        
                                        Text("\(value)")
                                            .font(.subheadline)
                                            .foregroundColor(.gray)
                                    }
                                    Spacer()
                                }
                                .padding(12)
                                .background(Color(red: 0.18, green: 0.18, blue: 0.18))
                                .cornerRadius(12)
                            }
                        }
                    }
                }
                .padding(16)
            }
        }
        .background(Color(red: 0.1, green: 0.1, blue: 0.1).edgesIgnoringSafeArea(.all))
        .onAppear {
            viewModel.startObserving()
            viewModel.sheetViewModel.getData(from: "A1", to: "G10")
        }
        .onChange(of: viewModel.loadingState) { _, newValue in
            isLoading = newValue?.loading ?? false
            SyncLog().print(message: "NewValue Loading: \(isLoading)")
        }
        .onChange(of: viewModel.errorState) { _, newValue in
            errorMessage = newValue?.description
            SyncLog().print(message: "NewValue Error: \(errorMessage ?? "No Error")")
        }
        .onChange(of: viewModel.getDataState) { _, newValue in
            SyncLog().print(message: "NewValue V: \(newValue?.description ?? "No Value")")
            handleDataState(state: newValue)
        }
    }
    
    private func handleDataState(state: GSyncResult<NSDictionary>?) {
        guard let state = state else { return }
        
        if let success = state as? GSyncResultSuccess<NSDictionary> {
            items = success.data ?? [:]
            errorMessage = nil
            isLoading = false
        }
    }
}

// Custom text field style for dark theme
struct HomeTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(12)
            .background(Color(red: 0.1, green: 0.1, blue: 0.1))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
            )
            .foregroundColor(.white)
            .tint(.blue)
    }
}
