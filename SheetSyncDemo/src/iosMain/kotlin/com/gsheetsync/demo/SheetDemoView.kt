package com.gsheetsync.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gsheetsync.presentation.SheetIntent
import com.gsheetsync.presentation.SheetModel
import com.gsheetsync.presentation.SheetState
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.SwiftUI.*

@Composable
fun SheetDemoView(sheetModel: SheetModel) {
    val state by sheetModel.state.collectAsState()

    VStack(spacing = 16.0) {
        Text("Google Sheets Sync Demo")
            .font(.title)
            .padding()

        if (state.isLoading) {
            ProgressView()
        }

        state.error?.let { error ->
            Text(error)
                .foregroundColor(.red)
                .padding()
        }

        state.currentCell?.let { cell ->
            Text("Current Cell Value: ${cell.value}")
                .font(.body)
                .padding()
        }

        HStack(spacing = 16.0) {
            Button("Start Observing") {
                sheetModel.processIntent(SheetIntent.ObserveCell(0, 0))
            }
            .disabled(state.isObserving)

            Button("Stop Observing") {
                sheetModel.processIntent(SheetIntent.StopObserving)
            }
            .disabled(!state.isObserving)

            Button("Update Cell") {
                sheetModel.processIntent(
                    SheetIntent.UpdateCell(
                        com.gsheetsync.model.SheetCell(
                            0,
                            0,
                            "Updated at ${NSDate().timeIntervalSince1970}"
                        )
                    )
                )
            }
        }
        .padding()
    }
    .frame(maxWidth = .infinity, maxHeight = .infinity)
} 