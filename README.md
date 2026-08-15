# Smart Pantry Manager

Smart Pantry Manager is a Java Android application for reducing household food waste by recording pantry ingredients and suggesting recipes only when every required ingredient is available in the required quantity.

## Technology
- Java
- Android Studio
- SQLite using SQLiteOpenHelper
- RecyclerView with custom adapters
- Android Activities and Intents

## Database
The app uses a local SQLite database because pantry data should persist on the device and the assignment explicitly permits SQLite. The database contains pantry items, recipes and recipe ingredients.

## Main features
- Create, read, update and delete pantry items
- Optional expiry date
- 20 seeded recipes
- Strict recipe matching
- Robust ingredient normalisation for common singular/plural forms
- Basic unit normalisation for mass and volume
- Recipe detail screen
- Settings screen
- Data persistence between app sessions

## Run
1. Open the project in Android Studio.
2. Allow Gradle to sync.
3. Create/start an Android emulator or connect an Android device.
4. Run the `app` configuration.

## Important
The repository should be developed with meaningful incremental Git commits. Do not replace the development history with a single final commit.
