# Dual-Publish Checklist: Cafe Bazaar + Myket

## Build Differences

| Property | Bazaar Build | Myket Build |
|----------|-------------|-------------|
| Application ID | `com.royalbackgammon.offline.bazaar` | `com.royalbackgammon.offline.myket` |
| Build flavor | `bazaar` | `myket` |
| IAP SDK | Cafe Bazaar IAB SDK | Myket IAP SDK |
| Ad SDK | (Same — Tapsell/chosen network) | (Same) |
| Signing key | Can be same or different | Can be same or different |
| Build command | `./gradlew assembleBazaarRelease` | `./gradlew assembleMyketRelease` |

## Pre-Release Checklist

### Common (both stores):
- [ ] Version bumped in `app/build.gradle` (versionCode + versionName)
- [ ] All features tested on physical device
- [ ] ProGuard rules verified (no crash in release build)
- [ ] Release signing configured in `local.properties`
- [ ] All string resources complete (FA + EN)
- [ ] No debug logs or test URLs in release code
- [ ] `StubAdProvider` / `StubBillingProvider` replaced with real implementations

### Cafe Bazaar specific:
- [ ] Bazaar IAB SDK integrated in `bazaar` flavor source set
- [ ] Bazaar developer console: app registered
- [ ] IAP products created in Bazaar console (SKUs match `Product.java`)
- [ ] Screenshots uploaded (minimum 5, Persian text overlays recommended)
- [ ] Store description from `store-listing/fa.md` copied to console
- [ ] Privacy policy URL set (if required)
- [ ] Category: Board Games (فکری و تخته‌ای)
- [ ] Content rating: 3+
- [ ] APK/AAB uploaded and submitted for review

### Myket specific:
- [ ] Myket IAP SDK integrated in `myket` flavor source set
- [ ] Myket developer console: app registered
- [ ] IAP products created in Myket console (SKUs match `Product.java`)
- [ ] Screenshots uploaded
- [ ] Store description from `store-listing/fa.md` copied to console
- [ ] APK uploaded and submitted for review

## Flavor Source Sets

To add store-specific code (billing SDK), create these directories:

```
app/src/bazaar/java/games/mrlaki5/backgammon/Monetization/iap/
    └── BazaarBillingProviderImpl.java

app/src/myket/java/games/mrlaki5/backgammon/Monetization/iap/
    └── MyketBillingProviderImpl.java
```

Each implements `BillingProvider` using the respective store's SDK.

## Signing

Both stores accept the same signing key, but you MAY use different keys if you want complete separation. Configure in `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=***
RELEASE_KEY_PASSWORD=***
```

## Version Strategy

Keep versionCode synchronized between both builds. Increment once per release cycle:
- v1.0.3 (versionCode 4) — current
- v2.0.0 (versionCode 5) — this release (all new features)

## Post-Release

- [ ] Monitor crash reports (Firebase Crashlytics — console.firebase.google.com)
- [ ] Check analytics for first-day metrics (Firebase Analytics dashboard)
- [ ] Respond to first user reviews within 24h
- [ ] Monitor IAP revenue in both store consoles
