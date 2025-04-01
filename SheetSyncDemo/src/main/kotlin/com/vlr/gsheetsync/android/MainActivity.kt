package com.vlr.gsheetsync.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vlr.gsheetsync.android.ui.screens.LaunchScreen
import com.vlr.gsheetsync.android.ui.screens.MainScreen
import com.vlr.gsheetsync.presentation.SheetIntent
import com.vlr.gsheetsync.presentation.SheetModel
import com.vlr.gsheetsync.presentation.SheetState
import com.vlr.gsheetsync.presentation.ui.SheetUiState
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val sheetModel: SheetModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by sheetModel.state.collectAsState()
                val uiState = when {
                    state.isLoading -> SheetUiState.Loading
                    state.isConnected -> SheetUiState.Connected
                    state.error != null -> SheetUiState.Error(state.error!!)
                    else -> SheetUiState.Initial
                }

                when {
                    !state.isConnected -> {
                        LaunchScreen(
                            uiState = uiState,
                            onConnect = { apiKey ->
                                sheetModel.processIntent(SheetIntent.Initialize(apiKey))
                            },
                            onConnectionSuccess = {
                                // Connection success is handled by the state change
                            }
                        )
                    }
                    else -> {
                        MainScreen(
                            onObserve = {
                                sheetModel.processIntent(SheetIntent.ObserveCell(0, 0))
                            },
                            onUpdate = {
                                sheetModel.processIntent(
                                    SheetIntent.UpdateCell(
                                        com.vlr.gsheetsync.model.SheetCell(0, 0, "Updated at ${System.currentTimeMillis()}")
                                    )
                                )
                            },
                            onDelete = {
                                sheetModel.processIntent(SheetIntent.DeleteCell(0, 0))
                            },
                            isLoading = state.isLoading
                        )
                    }
                }
            }
        }
    }
}
