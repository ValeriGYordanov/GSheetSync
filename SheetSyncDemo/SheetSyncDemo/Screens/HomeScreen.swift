import SwiftUI

struct HomeScreen: View {
    @State private var sheetId: String = ""
    @State private var sheetName: String = ""
    @State private var scrollOffset: CGFloat = 0
    
    // Sample data for the list
    let items = (1...10).map { "Item \($0)" }
    
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
                        TextField("Enter Sheet ID", text: $sheetId)
                            .textFieldStyle(HomeTextFieldStyle())
                        
                        TextField("Enter Sheet Name", text: $sheetName)
                            .textFieldStyle(HomeTextFieldStyle())
                    }
                    .padding(.vertical, 8)
                    
                    // Buttons
                    VStack(spacing: 8) {
                        Button(action: {
                            // TODO: Implement sync action
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
                            // TODO: Implement settings action
                        }) {
                            Text("Settings")
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
                    ForEach(items, id: \.self) { item in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item)
                                    .font(.headline)
                                    .foregroundColor(.white)
                                
                                Text("Supporting text for \(item)")
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
                .padding(16)
            }
        }
        .background(Color(red: 0.1, green: 0.1, blue: 0.1).edgesIgnoringSafeArea(.all))
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
