package com.vlr.gsheetsync.android.screen

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.oauth2.AccessToken
import kotlinx.coroutines.launch

@Composable
fun GoogleReAuthLauncher(onSignInResult: (AccessToken?) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        scope.launch {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            try {
                account?.account?.let { acc ->
                    val token = GoogleAuthUtil.getToken(context, acc, "oauth2:${SheetsScopes.SPREADSHEETS} ${DriveScopes.DRIVE_METADATA_READONLY}")
                    onSignInResult(AccessToken(token, null))
                }
            } catch (e: ApiException) {
                onSignInResult(null)
            } catch (e: UserRecoverableAuthException) {
                this.launch { e.intent }
            } catch (e: Exception) {
                onSignInResult(null)
            }
        }
    }

    // Register callback
    LaunchedEffect(Unit) {
        GoogleAuthManager.onTokenReceived = onSignInResult
    }

    return launcher
}