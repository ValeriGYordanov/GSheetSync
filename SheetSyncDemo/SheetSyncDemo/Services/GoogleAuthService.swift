import Foundation
import GoogleSignIn
import GoogleSignInSwift
import shared

@MainActor
class GoogleAuthService: ObservableObject {
    @Published var isSignedIn = false
    @Published var user: GIDGoogleUser?
    @Published var errorMessage: String?
    
    private let clientID = "595545246834-kphlnvqt8u8ggautffjoobbo8aimh069.apps.googleusercontent.com"
    private let sheetsScopes = [
        "https://www.googleapis.com/auth/spreadsheets",         // Read/write access to Sheets
        "https://www.googleapis.com/auth/drive.file"            // Access to files created by the app
    ]
    
    init() {
        setupGoogleSignIn()
    }
    
    private func setupGoogleSignIn() {
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
    }
    
    func signIn() async {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first,
              let rootViewController = window.rootViewController else {
            errorMessage = "No root view controller found"
            return
        }
        
        do {
            
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController, hint: nil, additionalScopes: sheetsScopes)
            self.user = result.user
            self.isSignedIn = true
            self.errorMessage = nil
            
            // Store the access token for later use with Google Sheets API
            SyncLog().print(message: "ACCESS TOKEN: \(String(describing: result.user.accessToken.tokenString))")
            let accessToken = result.user.accessToken.tokenString
            if accessToken.isEmpty == false {
                UserDefaults.standard.set(accessToken, forKey: "google_access_token")
            }
        } catch {
            self.errorMessage = error.localizedDescription
            self.isSignedIn = false
        }
    }
    
    func signOut() {
        GIDSignIn.sharedInstance.signOut()
        self.user = nil
        self.isSignedIn = false
        UserDefaults.standard.removeObject(forKey: "google_access_token")
    }
    
    func getAccessToken() -> String? {
        return UserDefaults.standard.string(forKey: "google_access_token")
    }
} 
