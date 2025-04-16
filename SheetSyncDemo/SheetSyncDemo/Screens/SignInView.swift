import SwiftUI
import GoogleSignInSwift
import shared

struct SignInView: View {
    @ObservedObject var viewModel: SheetViewModelWrapper
    @StateObject private var authService = GoogleAuthService()
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                if authService.isSignedIn {
                    VStack {
                        Text("Signed in as:")
                        Text(authService.user?.profile?.email ?? "")
                            .font(.headline)
                        
                        Button("Sign Out") {
                            authService.signOut()
                        }
                        .buttonStyle(.bordered)
                    }.onAppear {
                        // Initialize service when signed in
                        if let idToken = authService.user?.accessToken.tokenString {
                            viewModel.sheetViewModel.setAccessToken(accessToken: idToken)
                        }
                    }
                } else {
                    GoogleSignInButton(action: {
                        Task {
                            await authService.signIn()
                        }
                    })
                    .frame(width: 200, height: 50)
                }
                
                if let error = authService.errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .padding()
                }
            }
            .padding()
        }
    }
}
