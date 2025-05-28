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
// Setup your access token using ViewModel
sheetViewModel: SheetViewModel = getViewModel()
val accessToken = GoogleAuthUtil.getToken(
                            context,
                            USER_GOOGLE_ACCOUNT,
                            "oauth2:${SheetsScopes.SPREADSHEETS}"
                        )
                        sheetViewModel.setAccessToken(accessToken)
// Setup your SpreadSheetID
sheetViewModel.setSpreadsheetId("https://docs.google.com/spreadsheets/d/EXAMPLE_SHEET_ID_HERE/edit")
```

3. Initialize Koin in your application:

```kotlin
// Android
class YourApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
    
    private fun initKoin() {
        val modules = sharedModule + viewModelsModule
        startKoin {
            androidContext(this@SheetSyncApp)
        modules(modules)
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
//Using ViewModels
sheetViewModel: SheetViewModel = org.koin.androidx.compose.getViewModel()
//Using Pure SSEngine
ssEngine: SSEngine = org.koin.compose.getKoin().get()
```

2. Get Data

```kotlin
//Using ViewModels and States
val loadingState = sheetViewModel.loadingState.collectAsState()
val dataState = sheetViewModel.getDataState.collectAsState()
val items = if (dataState.value is GSyncResult.Success && dataState.value != null) {
    (dataState.value as GSyncResult.Success).data
} else {
    mapOf()
}

//Using Pure SSEngine
coroutineScope {
    val items: Map<String, String>? = ssEngine.getData(FROM_CELL, TO_CELL)
}
```

3. Update Data
//Using ViewModels and States
sheetViewModel.updateData(mapOf(
         "A1" to editText1,
         "B1" to editText2))

//Using Pure SSEngine
coroutineScope { 
    ssEngine.updateData(mapOf(
        "A1" to "VALUE IN CELL A1", 
        "B1" to "VALUE IN CELL B1"))
}

## Demo Apps

The project includes demo applications for both Android and iOS platforms:

- Android: Uses Jetpack Compose for the UI
- iOS: Uses SwiftUI for the UI

To run the demo apps:

1. Clone the repository
2. Update the Google Sheets API credentials
3. Build and run the desired platform's demo app

## Requirements

- Kotlin 1.9.22 or higher
- Android Studio Hedgehog or higher
- Xcode 15.0 or higher (for iOS development)
- Google Sheets API credentials

## License

This project is licensed under the MIT License - see the LICENSE file for details.
