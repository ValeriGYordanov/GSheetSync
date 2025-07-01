package com.vlr.gsheetsync.android.screen

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.oauth2.AccessToken

object GoogleAuthManager {

    var onTokenReceived: ((AccessToken?) -> Unit)? = null

    fun getAuthIntent(context: Context, account: Account): Intent? {
        val scope = "oauth2:${SheetsScopes.SPREADSHEETS} ${DriveScopes.DRIVE_METADATA_READONLY}"
        return GoogleAuthUtil.getToken(context, account, scope) // will throw UserRecoverableAuthException
            .let { null } // placeholder
    }
}