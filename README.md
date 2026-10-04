# MyLedger — ማህረቤ

A local-first Android expense tracker built with Kotlin, Jetpack Compose and Room.

## Features

- Offline/local Room database
- CBE, Telebirr and M-Pesa SMS parser framework
- Automatic expense categorization
- Duplicate SMS protection
- Manual transaction entry
- Dashboard with income, expense and balance
- GitHub Actions APK build
- Amharic-ready UI foundation

## Open in Android Studio

1. Download/extract this repository.
2. Open the `MyLedger` folder in Android Studio.
3. Allow Gradle to sync.
4. Connect an Android device or start an emulator.
5. Run the `app` configuration.

## Build APK locally

From the project root:

```bash
gradle :app:assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build APK on GitHub

Create a GitHub repository, upload these files, and push to `main`.
GitHub Actions will build the debug APK automatically.

Go to:

Actions → Build MyLedger APK → Artifacts → MyLedger-debug-apk

## SMS permissions

Android requires runtime SMS permissions. The application asks for RECEIVE_SMS and READ_SMS.

SMS formats vary by provider and can change. The parser is intentionally modular so additional provider-specific regular expressions can be added without changing the Room database.

## Architecture

SMS Receiver → Parser Manager → Provider Parser → Category Engine → Room → Repository → ViewModel → Compose UI
