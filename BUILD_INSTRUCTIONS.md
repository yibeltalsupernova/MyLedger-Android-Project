# Build MyLedger

## Android Studio
Open the `MyLedger` directory as a Gradle project and let Android Studio sync dependencies.

Use JDK 17.

Run:
`:app`

## Command line
If Gradle is installed:

`gradle :app:assembleDebug`

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub
Push the complete repository to GitHub.

Actions -> Build MyLedger APK -> run workflow.

The workflow uploads:
`MyLedger-debug-apk`

## Next production steps
- Add signed release keystore through GitHub Secrets.
- Add more provider-specific SMS templates after collecting real anonymized SMS examples.
- Add encrypted local storage if sensitive local data needs stronger protection.
- Add CSV/PDF export.
- Add recurring budgets.
