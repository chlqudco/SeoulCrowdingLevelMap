# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android application. Root Gradle configuration lives in `settings.gradle.kts`, `build.gradle.kts`, and `gradle/libs.versions.toml`; application configuration is in `app/build.gradle.kts`. Kotlin production code is under `app/src/main/java/com/chlqudco/seoulcrowdinglevelmap`, grouped into `data`, `model`, `ui`, and `ui/theme`. Android resources and the manifest are in `app/src/main/res` and `app/src/main/AndroidManifest.xml`. Put local JVM tests in `app/src/test` and device or emulator tests in `app/src/androidTest`.

## Build, Test, and Development Commands

Run the checked-in Gradle wrapper from the repository root:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug
```

These commands build the debug APK, run local unit tests, run instrumented tests on a connected device or emulator, and execute Android lint, respectively. On macOS or Linux, use `./gradlew` with the same tasks. The debug APK is written to `app/build/outputs/apk/debug/`.

## Coding Style & Naming Conventions

Use the official Kotlin style configured in `gradle.properties` and four-space indentation. Keep packages lowercase, types and Compose functions in `PascalCase`, functions and properties in `camelCase`, and constants in `UPPER_SNAKE_CASE`. Follow existing suffixes such as `HomeScreen`, `CrowdViewModel`, and `CrowdRepository`. No dedicated formatter is configured; use Android Studio's Kotlin reformatter and run lint before submitting changes. Do not add code comments or KDoc unless explicitly requested. Do not expand existing comments, and edit them only when a code change requires it.

## Testing Guidelines

Local tests use JUnit 4; instrumented tests use AndroidX JUnit, Espresso, and Compose UI testing dependencies. Name test classes `*Test` and use behavior-focused method names, for example `placesAreRankedByLevelThenPopulation`. Add fast logic tests under `src/test`; reserve `src/androidTest` for Android framework, map, or UI behavior. No coverage threshold is configured, so cover changed behavior and regressions directly.

## Commit & Pull Request Guidelines

History uses short Korean milestone summaries such as `2차 구현 : 네이버 지도 추가`. Keep each commit focused and use a concise Korean or English subject that states the change. Pull requests should explain user-visible behavior, list verification commands, link related issues, and include screenshots or recordings for Compose UI or map changes.

## Security & Configuration

Store `SEOUL_API_KEY` and `NAVER_MAP_KEY_ID` only in the ignored `local.properties` file. Never commit credentials, generated APKs, or build output.
