# Pocket

Pocket is a CS361 Android app for recording personal income and expenses on one device. Everything is stored in a local SQLite database. There is no account, server, or network API.

The launcher name is **Pocket**. The Gradle project is `Cs361v.2`, and the application id is `com.example.cs361v2` (version 1.0, versionCode 1).

## Features

- Pick a day on a calendar and see that day's transactions.
- Add an income or expense with a category and an amount. In English, each row is labeled "Baht" and the summary uses "THB". In Thai, both use "บาท".
- Edit or delete a transaction. Deleting asks for confirmation.
- Filter the rows already shown for the selected day by typing in the search field. The search does not look at other dates.
- Save a spending goal for a specific date, and open a conclusion screen that compares that day's expenses with the goal.
- Summarize income and expenses by month or by year.
- Switch the interface between English and Thai, and set a small, medium, or large font scale.
- Delete every transaction and every daily goal after a confirmation dialog.
- Portrait and landscape layouts for the main screen, home, summary, conclusion, and settings.

Language and font size are applied by updating the activity configuration and recreating the activity. They are not written to `SharedPreferences`, so a fresh process starts from the device locale and the default font scale.

The year list on the summary screen is fixed to 2022, 2023, 2024, and 2025.

## Screens

The app has one activity, `MainActivity`, and a bottom navigation bar. The bar opens the summary list, home, or settings. Home is the screen shown at launch.

| Screen | Class | Layout | What it does |
| --- | --- | --- | --- |
| Home | `HomeFragment` | `fragment_home.xml` | Calendar, date field, search box, daily goal, the day's history, an add button, and a Conclusion button. |
| Add / edit transaction | Dialogs built in `HomeFragment` | Built in code | Income or expense checkbox, category spinner, and amount field. |
| Daily conclusion | `ConclusionFragment` | `fragment_conclusion.xml` | Selected date, expense total, goal, status text, and a happy, neutral, or sad face. Back returns to Home. |
| Income and expense summary | `ListFragment` | `fragment_list.xml` | Monthly or yearly radio buttons, month and year spinners, and a Display Data button. Monthly results are grouped by day. Yearly results are grouped by month. |
| Settings | `settingsFragment` | `fragment_settings.xml` | Choose language (English or Thai), choose font size, or delete all data. |

Landscape variants live under `app/src/main/res/layout-land/`.

### Categories

Income: Salary, Other Income, Transfer In, Interest.

Expenses: Food & Beverages, Bills & Utilities, Shopping, Household Items, Family, Travel, Health & Fitness, Education, Entertainment, Giving & Donations, Insurance, Other Expenses.

New transactions store the category and type labels from the current locale (`values/strings.xml` or `values-th/strings.xml`). The edit dialog writes the type as the English literals `Income` or `Expenses`. Summary totals and row colors recognize `Income` / `รายรับ` and `Expenses` / `รายจ่าย`. The daily conclusion also counts a type stored as `Outcome`.

The conclusion adds up expenses for the selected day only. Income is left out of that total. The total passed to the conclusion screen is an `int`, so a fractional amount is truncated before it is compared with the goal.

### Conclusion images

| Spending vs. goal | Image | Message |
| --- | --- | --- |
| Over the goal | `drawable/sad_face.png` | You have exceeded your spending goal! |
| Equal to the goal | `drawable/neutral_face.png` | You have met your spending goal exactly! |
| Under the goal | `drawable/happy_face.png` | Your spending is below the goal! |

## Data model

`DatabaseHelper` (`SQLiteOpenHelper`) creates `transactions.db`, database version 2. `onUpgrade` drops both tables and creates them again, so raising the version deletes existing rows.

`Transaction` is the in-memory row: `id`, `date`, `type`, `category`, `amount`.

### `transactions`

| Column | Type | Meaning |
| --- | --- | --- |
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Row id |
| `date` | TEXT | `dd/MM/yyyy HH:mm`, using the time of day when the row was added |
| `type` | TEXT | Income or expense label, as described above |
| `category` | TEXT | Category label from the spinner |
| `amount` | REAL | Amount |

Day, month, and year queries slice this string. They do not parse it with a date function:

- A day matches `substr(date, 1, 10)` to `dd/MM/yyyy`.
- A month rebuilds `yyyy-MM` from characters 7–10 (year) and 4–5 (month).
- A year reads characters 7–10.

Updates find a row by the full `date` string, not by `id`. Deletes from the home screen match `date`, `type`, `category`, and `amount`. A delete-by-id method exists on `DatabaseHelper` and is not called by the screens.

### `daily_goals`

| Column | Type | Meaning |
| --- | --- | --- |
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Row id |
| `date` | TEXT | `dd/MM/yyyy` for the selected day |
| `goal_amount` | REAL | Spending goal for that date |

Saving a goal inserts a row when that date has none, and updates `goal_amount` when it does. "Delete all data" runs `DELETE` on both tables and leaves the schema in place.

The database file is the app's private SQLite file. The manifest sets `android:allowBackup="true"`. `backup_rules.xml` and `data_extraction_rules.xml` are the stock templates and do not exclude the database.

## Tech stack

| Piece | Version / choice |
| --- | --- |
| Language | Java (source and target compatibility 1.8) |
| UI | AndroidX AppCompat, Material Components, one activity, fragments, `BottomNavigationView` |
| Persistence | `android.database.sqlite` (`SQLiteOpenHelper`), not Room |
| Build | Android Gradle Plugin 8.5.2, Gradle 8.7, version catalog in `gradle/libs.versions.toml` |
| SDK | `compileSdk` 34, `targetSdk` 34, `minSdk` 24 |
| Tests | JUnit 4.13.2 on the host, AndroidX JUnit 1.2.1 and Espresso 3.6.1 on a device |

`app/build.gradle` also depends on AndroidX Lifecycle ViewModel and LiveData 2.6.2. The screens do not use a `ViewModel`. Each fragment opens `DatabaseHelper` itself.

`TransactionAdapter` can render a `Transaction` in a two-line list row. `ListFragment` does not use it. The summary list is an `ArrayAdapter` of formatted strings.

The checked-in tests are the Android Studio samples: `ExampleUnitTest` asserts `2 + 2`, and `ExampleInstrumentedTest` asserts the package name `com.example.cs361v2`.

The manifest declares no permissions. The theme is `Theme.AppCompat.Light.NoActionBar`.

## Project structure

```
.
├── build.gradle                 # root plugins
├── settings.gradle              # root project name Cs361v.2, module :app
├── gradle.properties            # AndroidX, non-transitive R, JVM args
├── gradle/libs.versions.toml    # AGP and library versions
├── gradle/wrapper/              # Gradle 8.7 wrapper
└── app/
    ├── build.gradle
    ├── proguard-rules.pro       # release minify is off
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/cs361v2/
        │   │   ├── MainActivity.java
        │   │   ├── HomeFragment.java
        │   │   ├── ListFragment.java
        │   │   ├── ConclusionFragment.java
        │   │   ├── settingsFragment.java
        │   │   ├── DatabaseHelper.java
        │   │   ├── Transaction.java
        │   │   └── TransactionAdapter.java
        │   └── res/
        │       ├── layout/          # portrait
        │       ├── layout-land/     # landscape
        │       ├── menu/bottom_navigation.xml
        │       ├── values/          # English strings, colors, theme
        │       ├── values-th/       # Thai strings
        │       ├── values-night/
        │       ├── drawable/
        │       └── xml/             # backup rule templates
        ├── test/                    # local unit test
        └── androidTest/             # instrumented test
```

## Build and run

You need:

- Android Studio Koala (2024.1.2) or newer, so it can load Android Gradle Plugin 8.5.
- Android SDK Platform 34. Studio can install it during sync.
- A phone, tablet, or emulator running Android 7.0 (API 24) or higher.
- JDK 17 or newer to run Gradle. Android Studio's embedded JDK is enough. The app itself is compiled to Java 8 bytecode.

Open the project:

1. Clone this repository and open the directory that contains `settings.gradle` in Android Studio.
2. Let Gradle sync. Accept the prompt to install any missing SDK components.
3. Choose a device and use **Run 'app'**.

From a terminal, with the Android SDK installed:

```bash
# local.properties is not committed. Point it at your SDK.
echo "sdk.dir=$ANDROID_HOME" > local.properties

./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`. Install it with `adb install -r` on a connected device.

Release builds use `./gradlew :app:assembleRelease`. Minification is disabled, and the project does not include a signing config, so a release APK is unsigned until you add one.

### Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest   # needs a running device or emulator
```

## Screenshots

**Placeholder. These screenshots were not captured.**

This copy of the project was documented on a machine with no Android SDK and no emulator, so the app was not built or launched and no images were taken. After you run Pocket, replace this list with real captures. Suggested files:

- `docs/screenshots/home.png` — Home, with the calendar, daily goal, and that day's transactions
- `docs/screenshots/add-transaction.png` — Add-transaction dialog
- `docs/screenshots/conclusion.png` — Daily conclusion with the status image
- `docs/screenshots/summary.png` — Monthly or yearly income and expense totals
- `docs/screenshots/settings.png` — Language, font size, and delete-all-data

Do not commit generated stand-ins. Add a file here only after it is a screenshot of this app.
