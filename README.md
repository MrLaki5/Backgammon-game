## Royal Backgammon

Offline Android backgammon with a polished royal board style, human-vs-human play,
human-vs-bot play, tutorial mode, score tracking, and bilingual UI.

### Features

- Classic backgammon rules
- Human and AI players
- AI difficulty levels: Easy, Medium, Hard, Royal
- Tutorial mode for new players
- Persian and English UI with in-app language switching
- Board themes and bot difficulty selection
- Dice animation, checker movement feedback, haptics, and sound effects
- Score and match result history
- Pause and continue support

### Build

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleRelease
```

For direct APK distribution, build release with a stable private signing key.
Put the signing values in ignored `local.properties` or environment variables,
never in committed files:

```properties
RELEASE_STORE_FILE=battleheim-release-key.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-key-alias
RELEASE_KEY_PASSWORD=your-key-password
```

With signing values configured, the release APK is generated as
`app/build/outputs/apk/release/app-release.apk`. Without them, Gradle produces
`app-release-unsigned.apk`, which should not be shared directly. Verify the
signed APK before sharing:

```powershell
$apksigner = "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat"
& $apksigner verify --verbose --print-certs app\build\outputs\apk\release\app-release.apk
```

MIT license applies to commit `7fc82cd` and commits after it. Earlier commits are
not under the MIT license.
