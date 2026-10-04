# MyLedger — ማህረቤ

## Version 1.2.0

A local-first Android personal finance manager built with Kotlin, Jetpack Compose and Room.

### Included
- Dashboard
- Income / expense / transfer tracking
- Manual transaction entry
- Automatic vendor categorization
- CBE / Telebirr / M-Pesa SMS parsing framework
- Duplicate SMS protection
- Search
- Category spending insights
- Monthly budgets
- CSV export/share
- Local Room database
- Dark-mode setting foundation
- GitHub Actions APK build
- Unit tests
- Amharic-ready UI

### Build
Open the root folder in Android Studio using JDK 17.

Command line:
`gradle :app:assembleDebug`

APK:
`app/build/outputs/apk/debug/app-debug.apk`

### GitHub
Push to GitHub. The workflow in `.github/workflows/build-apk.yml` builds the debug APK and uploads it as an artifact.

### Important
Provider SMS formats vary. The included parsers are a framework and examples; production deployment should be tested against current, anonymized SMS samples from each supported provider. SMS access is subject to Android permissions and platform/policy requirements.
