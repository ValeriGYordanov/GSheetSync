package com.vlr.gsheetsync.android.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.sheets.v4.SheetsScopes
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.feature.sheets.domain.model.GSyncResult
import com.vlr.gsheetsync.feature.sheets.presentation.SheetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    sheetViewModel: SheetViewModel = getViewModel()
) {
    var editText1 by remember { mutableStateOf("") }
    var editText2 by remember { mutableStateOf("") }

    val authLauncher = GoogleReAuthLauncher { token ->
        if (token != null) {
            // Save or update token
        } else {
            // Show error
        }
    }

    val loadingState = sheetViewModel.loadingState.collectAsState()
    val dataState = sheetViewModel.getDataState.collectAsState()
    val items = if (dataState.value is GSyncResult.Success && dataState.value != null) {
        (dataState.value as GSyncResult.Success).data
    } else {
        mapOf()
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {

        // Header Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2D2D)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text Fields
                Text(
                    text = "Welcome to SheetSync",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = "Manage your sheets efficiently",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF757575)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Edit Text Fields
                OutlinedTextField(
                    value = editText1,
                    onValueChange = { editText1 = it },
                    label = { Text("Enter Value for A1", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF757575),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color(0xFF757575),
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = editText2,
                    onValueChange = { editText2 = it },
                    label = { Text("Enter Value for B1", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF757575),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color(0xFF757575),
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                Button(
                    onClick = { sheetViewModel.updateData(mapOf(
                        "A1" to editText1,
                        "B1" to editText2
                    )) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sync Now", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        val account = GoogleSignIn.getLastSignedInAccount(context)?.account
                        if (account != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val token = GoogleAuthUtil.getToken(context, account, "oauth2:${SheetsScopes.SPREADSHEETS}")
                                    sheetViewModel.setAccessToken(token)
                                } catch (e: UserRecoverableAuthException) {
                                    authLauncher.launch(e.intent)
                                }
                            }

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2196F3),
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View History", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        sheetViewModel.getData("A1", "G10")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        // List Section
        if (loadingState.value?.loading == true) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items.entries.toList()) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1A1A1A)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = item.key,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        ": ${item.value}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF757575)
                                    )
                                },
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}