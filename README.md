# GSheetSync

A Kotlin Multiplatform library for real-time Google Sheets integration, enabling seamless data synchronization between Android and iOS applications.

## Features

- Real-time cell updates
- Support for both Android and iOS platforms
- MVI architecture for clean state management
- Repository pattern for data access
- Dependency injection with Koin
- Modern UI with Jetpack Compose and SwiftUI

## Setup

1. Add the library to your project:

```kotlin
// In your shared module's build.gradle.kts
dependencies {
    implementation("com.gsheetsync:gsheetsync:1.0.0")
}
```

2. Configure your Google Sheets API credentials:

```kotlin
// In your shared module's Koin module
single { SheetConfig(
    spreadsheetId = "YOUR_SPREADSHEET_ID",
    sheetId = "YOUR_SHEET_ID",
    apiKey = "YOUR_API_KEY"
) }
```

3. Initialize Koin in your application:

```kotlin
// Android
class YourApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this)
            modules(sharedModule + platformModule)
        }
    }
}

// iOS
class YourApp : KoinComponent {
    fun start() {
        startKoin {
            modules(sharedModule + platformModule)
        }
    }
}
```

## Usage

1. Inject the SheetModel:

```kotlin
class YourViewModel : KoinComponent {
    private val sheetModel: SheetModel by inject()
}
```

2. Use the SheetModel to interact with Google Sheets:

```kotlin
// Update a cell
sheetModel.processIntent(SheetIntent.UpdateCell(
    SheetCell(row = 0, column = 0, value = "Hello, World!")
))

// Observe a cell
sheetModel.processIntent(SheetIntent.ObserveCell(row = 0, column = 0))

// Stop observing
sheetModel.processIntent(SheetIntent.StopObserving)
```

3. Collect the state:

```kotlin
val state by sheetModel.state.collectAsState()

// Access the current cell value
state.currentCell?.value
```

## Demo Apps

The project includes demo applications for both Android and iOS platforms:

- Android: Uses Jetpack Compose for the UI
- iOS: Uses SwiftUI for the UI

To run the demo apps:

1. Clone the repository
2. Update the Google Sheets API credentials in `shared/src/commonMain/kotlin/com/gsheetsync/di/SharedModule.kt`
3. Build and run the desired platform's demo app

## Requirements

- Kotlin 1.9.22 or higher
- Android Studio Hedgehog or higher
- Xcode 15.0 or higher (for iOS development)
- Google Sheets API credentials

## License

This project is licensed under the MIT License - see the LICENSE file for details.
