import SwiftUI
import shared

struct MainScreen: View {
    let onObserve: () -> Void
    let onUpdate: () -> Void
    let onDelete: () -> Void
    let isLoading: Bool
    
    var body: some View {
        ZStack {
            Color(red: 0.1, green: 0.1, blue: 0.1)
                .ignoresSafeArea()
            
            VStack(spacing: 20) {
                Text("Sheet Operations")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                
                Button(action: onObserve) {
                    Text("Observe Sheet")
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .disabled(isLoading)
                
                Button(action: onUpdate) {
                    Text("Update Cell")
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .disabled(isLoading)
                
                Button(action: onDelete) {
                    Text("Delete Cell")
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .disabled(isLoading)
            }
            .padding()
            
            if isLoading {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
            }
        }
    }
} 