package com.vlr.gsheetsync.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vlr.gsheetsync.presentation.ui.SheetUiState

@Composable
fun LaunchScreen(
    onConnectionSuccess: () -> Unit,
    onConnect: (String) -> Unit,
    uiState: SheetUiState
) {
    var apiKey by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
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

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFF757575),
                    focusedLabelColor = Color(0xFF2196F3),
                    unfocusedLabelColor = Color(0xFF757575),
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = { onConnect(apiKey) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = apiKey.isNotBlank() && uiState !is SheetUiState.Loading
            ) {
                if (uiState is SheetUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Connect")
                }
            }

            if (uiState is SheetUiState.Error) {
                Text(
                    text = uiState.message,
                    color = Color(0xFFE57373),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is SheetUiState.Connected) {
            onConnectionSuccess()
        }
    }
} 