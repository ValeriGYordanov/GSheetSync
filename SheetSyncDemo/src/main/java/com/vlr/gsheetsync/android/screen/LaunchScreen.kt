package com.vlr.gsheetsync.android.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.oauth2.AccessToken
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.domain.model.GSyncResult
import com.vlr.gsheetsync.feature.sheets.presentation.SheetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

var additionalInfoText = "Authorizing Google Sheets..."

@Composable
fun LaunchScreen(
    onConnectionSuccess: () -> Unit,
    sheetViewModel: SheetViewModel = getViewModel()
) {
    val context = LocalContext.current
    val errorState = sheetViewModel.errorState.collectAsState()
    val loadingState = sheetViewModel.loadingState.collectAsState()

    val accessTokenState = sheetViewModel.setAccessTokenState.collectAsState()
    val spreadSheetIdState = sheetViewModel.setSpreadsheetIdState.collectAsState()

    val account = GoogleSignIn.getLastSignedInAccount(LocalContext.current)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (account == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Google Sheets Sync",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                GoogleSignInButton {
                    if (!loadingState.value?.loading!!) {
                        if (it != null) {
                            sheetViewModel.setAccessToken(it.tokenValue)
                        } else {
                            SyncLog.print("FAIL:")
                        }
                    }
                }

                if (errorState.value?.message?.description?.isNotEmpty() == true) {
                    Text(
                        text = errorState.value?.message?.description?:"Unknown Client Side Error",
                        color = Color(0xFFE57373),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${account.givenName}, Welcome to Google Sheets Sync",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Please wait while we fetch your data...",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = additionalInfoText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                CircularProgressIndicator()
            }
        }
    }

    LaunchedEffect(accessTokenState.value) {
        if (accessTokenState.value is GSyncResult.Success) {
            additionalInfoText = "Setting up the Spreadsheet ..."
            sheetViewModel.setSpreadsheetId("https://docs.google.com/spreadsheets/d/1px0triZ-w_8UH122HDSNjbAHJf1LAMi6s92twPJP2_0/edit")
        } else {
            sheetViewModel.scope.launch {
                withContext(Dispatchers.IO) {
                    account?.account?.let { acc ->
                        val accessToken = GoogleAuthUtil.getToken(
                            context,
                            acc,
                            "oauth2:${SheetsScopes.SPREADSHEETS}"
                        )
                        sheetViewModel.setAccessToken(accessToken)
                    }
                }
            }
        }
    }

    LaunchedEffect(spreadSheetIdState.value) {
        if (spreadSheetIdState.value is GSyncResult.Success) {
            onConnectionSuccess()
        }
    }
}

@Composable
fun GoogleSignInButton(onSignInResult: (AccessToken?) -> Unit) {
    val context = LocalContext.current

    val scope = CoroutineScope(Dispatchers.IO)
    val coroutineScope = rememberCoroutineScope()
    val consentLauncher = rememberUserConsentLauncher { consentGranted ->
        if (consentGranted) {
            // Retry accessing the Sheets API
            coroutineScope.launch {
                try {
                    // Your code to access the Sheets API
                } catch (e: UserRecoverableAuthException) {
                    // Handle the exception if needed
                }
            }
        } else {
            // Handle the case where consent was not granted
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("595545246834-hmekg2rbs9jgoc4v1pi7cim7banredo8.apps.googleusercontent.com") // Your Client ID from Google Cloud Console
        .requestEmail()
        .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val authResultLauncher =
        rememberLauncherForActivityResult(AuthResultContract(googleSignInClient)) { task ->
            scope.launch {
                try {
                    val account = task?.getResult(ApiException::class.java)
                    account?.account?.let { acc ->
                        val accessToken = GoogleAuthUtil.getToken(
                            context,
                            acc,
                            "oauth2:${SheetsScopes.SPREADSHEETS}"
                        )
                        onSignInResult(AccessToken(accessToken, null))
                    }
                } catch (e: ApiException) {
                    onSignInResult(null)
                } catch (e: UserRecoverableAuthException) {
                    consentLauncher.launch(e.intent)
                }
            }

        }

    Button(onClick = { authResultLauncher.launch(0) }) {
        Text("Sign in with Google")
    }
}

@Composable
fun rememberUserConsentLauncher(
    onConsentResult: (Boolean) -> Unit
): ActivityResultLauncher<Intent> {
    val currentOnConsentResult = rememberUpdatedState(onConsentResult)
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentOnConsentResult.value(true)
        } else {
            currentOnConsentResult.value(false)
        }
    }
}

class AuthResultContract(private val googleSignInClient: GoogleSignInClient) :
    ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {

    override fun createIntent(context: Context, input: Int): Intent =
        googleSignInClient.signInIntent.putExtra("input", input)

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return when (resultCode) {
            Activity.RESULT_OK -> GoogleSignIn.getSignedInAccountFromIntent(intent)
            else -> null
        }
    }
}