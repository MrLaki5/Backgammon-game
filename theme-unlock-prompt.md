# Theme Unlock System — Ad-Gated Themes at Game Start

## Design Overview

Themes leave the coin shop entirely. Instead, premium themes are unlocked **permanently** by watching a rewarded ad — right from the new-game dialog, right when the player taps a locked theme thumbnail. Royal theme stays free forever. Once unlocked, a theme stays unlocked (stored in SharedPreferences).

**Flow:** tap locked theme → rewarded ad plays → `onRewardEarned` → theme permanently unlocked → selected → player proceeds.

---

## Files Modified

1. `GamePreferences.java` — add `isThemeUnlocked()` + `unlockTheme()` statics  
2. `RewardedAdPlacement.java` — add `THEME_UNLOCK` entry  
3. `dialog_single_player.xml` — add lock overlays to themeThumb1/2/3  
4. `pass_and_play_dialog.xml` — add lock overlays to pnpThemeThumb1/2/3  
5. `MenuActivity.java` — theme picker logic with ad gate  
6. `CoinShopActivity.java` — remove THEME items from catalog  
7. `activity_coin_shop.xml` — remove Themes RadioButton tab  
8. `ShopAdapter.java` — remove THEME cases  
9. `strings.xml` — new strings  

**Do NOT touch** `game-core/`.

---

## Phase 1 — `GamePreferences.java`

Add these two static methods inside the class body (after `saveAudioVolumes`):

```java
/** Returns true if the given theme is available to play. Royal (0) is always free. */
public static boolean isThemeUnlocked(Context context, int themeId) {
    if (themeId == THEME_ROYAL) return true;
    return preferences(context).getBoolean("theme_unlocked_" + themeId, false);
}

/** Permanently marks a theme as unlocked. */
public static void unlockTheme(Context context, int themeId) {
    preferences(context).edit()
            .putBoolean("theme_unlocked_" + themeId, true)
            .apply();
}
```

---

## Phase 2 — `RewardedAdPlacement.java`

Add `THEME_UNLOCK` to the enum before the closing semicolon:

```java
THEME_UNLOCK("theme_unlock", 0, 0, 0);   // no cap — permanent one-time unlock per theme
```

Existing last entry (`BONUS_CHEST`) currently ends with `;` — change it to `,` and then add the new entry.

Before:
```java
BONUS_CHEST("bonus_chest", 0, 1, 0);       // 1 per day (1 daily challenge)
```
After:
```java
BONUS_CHEST("bonus_chest", 0, 1, 0),       // 1 per day (1 daily challenge)
THEME_UNLOCK("theme_unlock", 0, 0, 0);     // no cap — permanent unlock
```

---

## Phase 3 — `dialog_single_player.xml`

Inside each of `themeThumb1`, `themeThumb2`, `themeThumb3` (NOT themeThumb0 — Royal is free), add a lock overlay as the **second child** of the FrameLayout, right after the existing `<ImageView>`:

### themeThumb1 → add child:
```xml
<LinearLayout
    android:id="@+id/lockOverlay1"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#CC000000"
    android:gravity="center"
    android:orientation="vertical"
    android:visibility="gone">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="🔒"
        android:textSize="16sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/watch_ad_unlock"
        android:textColor="#F4B044"
        android:textSize="8sp"
        android:textStyle="bold"
        android:gravity="center" />
</LinearLayout>
```

### themeThumb2 → identical child with `android:id="@+id/lockOverlay2"`
### themeThumb3 → identical child with `android:id="@+id/lockOverlay3"`

---

## Phase 4 — `pass_and_play_dialog.xml`

Same as Phase 3 but for `pnpThemeThumb1`, `pnpThemeThumb2`, `pnpThemeThumb3`:

- `pnpThemeThumb1` → add overlay with `android:id="@+id/pnpLockOverlay1"`
- `pnpThemeThumb2` → add overlay with `android:id="@+id/pnpLockOverlay2"`
- `pnpThemeThumb3` → add overlay with `android:id="@+id/pnpLockOverlay3"`

Same `LinearLayout` structure, same `watch_ad_unlock` string, only the IDs differ.

---

## Phase 5 — `MenuActivity.java`

### 5a — Add private helper methods (add anywhere before `onDestroy`):

```java
// ── Theme Unlock Helpers ──────────────────────────────────────────────────

/**
 * Sets up the 2x2 theme picker in a new-game dialog.
 *
 * @param root         The dialog's root View
 * @param thumbIds     Array of 4 FrameLayout IDs (index = theme id)
 * @param overlayIds   Array of 4 lock-overlay IDs; 0 for slots with no overlay (Royal)
 * @param selectedRef  Single-element int[] holding the currently selected theme index
 */
private void setupThemePicker(View root,
                               int[] thumbIds, int[] overlayIds,
                               int[] selectedRef) {
    FrameLayout[] thumbs      = new FrameLayout[thumbIds.length];
    View[]        lockViews   = new View[overlayIds.length];

    for (int i = 0; i < thumbIds.length; i++) {
        thumbs[i] = root.findViewById(thumbIds[i]);
    }
    for (int i = 0; i < overlayIds.length; i++) {
        if (overlayIds[i] != 0) {
            lockViews[i] = root.findViewById(overlayIds[i]);
        }
    }

    refreshThemeLocks(lockViews);
    updateThemeSelection(thumbs, selectedRef[0]);

    for (int i = 0; i < thumbs.length; i++) {
        final int idx = i;
        thumbs[i].setOnClickListener(v -> {
            if (GamePreferences.isThemeUnlocked(MenuActivity.this, idx)) {
                selectedRef[0] = idx;
                updateThemeSelection(thumbs, idx);
            } else {
                showThemeUnlockAd(idx, () -> {
                    GamePreferences.unlockTheme(MenuActivity.this, idx);
                    refreshThemeLocks(lockViews);
                    selectedRef[0] = idx;
                    updateThemeSelection(thumbs, idx);
                    android.widget.Toast.makeText(this,
                            R.string.theme_unlocked_toast,
                            android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

private void refreshThemeLocks(View[] lockViews) {
    for (int i = 0; i < lockViews.length; i++) {
        if (lockViews[i] != null) {
            lockViews[i].setVisibility(
                    GamePreferences.isThemeUnlocked(this, i) ? View.GONE : View.VISIBLE);
        }
    }
}

private void showThemeUnlockAd(int themeIdx, Runnable onUnlocked) {
    if (adManager == null || !adManager.isRewardedAdReady()) {
        adManager.preloadAds();
        android.widget.Toast.makeText(this,
                R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show();
        return;
    }
    adManager.showRewardedAd(this, RewardedAdPlacement.THEME_UNLOCK,
            new games.mrlaki5.backgammon.Monetization.ads.AdCallback() {
                @Override public void onAdLoaded() {}
                @Override public void onAdFailedToLoad(String error) {
                    runOnUiThread(() -> android.widget.Toast.makeText(MenuActivity.this,
                            R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show());
                }
                @Override public void onAdShown() {}
                @Override public void onAdDismissed() {}
                @Override public void onAdClicked() {}
                @Override public void onRewardEarned() {
                    runOnUiThread(onUnlocked);
                }
            });
}
```

### 5b — Replace the theme-selection block inside `showNewGameDialog()`

Find and replace the existing theme-selection comment block and the code that follows it:

**Remove these lines** (currently around lines 429–445 in original MenuActivity):
```java
// --- Theme selection (thumbnails) ---
final int[] selectedTheme = {GamePreferences.getBoardTheme(this)};
final FrameLayout[] thumbs = {
        dialogView.findViewById(R.id.themeThumb0),
        dialogView.findViewById(R.id.themeThumb1),
        dialogView.findViewById(R.id.themeThumb2),
        dialogView.findViewById(R.id.themeThumb3)
};
// Set initial selection
updateThemeSelection(thumbs, selectedTheme[0]);
// Click listeners
for (int i = 0; i < thumbs.length; i++) {
    final int idx = i;
    thumbs[i].setOnClickListener(v -> {
        selectedTheme[0] = idx;
        updateThemeSelection(thumbs, idx);
    });
}
```

**Replace with:**
```java
// --- Theme selection (thumbnails with ad-gate for locked themes) ---
final int[] selectedTheme = {GamePreferences.getBoardTheme(this)};
setupThemePicker(dialogView,
        new int[]{R.id.themeThumb0, R.id.themeThumb1, R.id.themeThumb2, R.id.themeThumb3},
        new int[]{0, R.id.lockOverlay1, R.id.lockOverlay2, R.id.lockOverlay3},
        selectedTheme);
```

### 5c — Replace the theme-selection block inside `showPassAndPlayDialog()`

**Remove:**
```java
// --- Theme selection (thumbnails) ---
final int[] selectedTheme = {GamePreferences.getBoardTheme(this)};
final FrameLayout[] thumbs = {
        dialogView.findViewById(R.id.pnpThemeThumb0),
        dialogView.findViewById(R.id.pnpThemeThumb1),
        dialogView.findViewById(R.id.pnpThemeThumb2),
        dialogView.findViewById(R.id.pnpThemeThumb3)
};
updateThemeSelection(thumbs, selectedTheme[0]);
for (int i = 0; i < thumbs.length; i++) {
    final int idx = i;
    thumbs[i].setOnClickListener(v -> {
        selectedTheme[0] = idx;
        updateThemeSelection(thumbs, idx);
    });
}
```

**Replace with:**
```java
// --- Theme selection (thumbnails with ad-gate for locked themes) ---
final int[] selectedTheme = {GamePreferences.getBoardTheme(this)};
setupThemePicker(dialogView,
        new int[]{R.id.pnpThemeThumb0, R.id.pnpThemeThumb1, R.id.pnpThemeThumb2, R.id.pnpThemeThumb3},
        new int[]{0, R.id.pnpLockOverlay1, R.id.pnpLockOverlay2, R.id.pnpLockOverlay3},
        selectedTheme);
```

> The `selectedTheme` variable is still referenced by the Play button's `setOnClickListener` below — keep that part unchanged.

---

## Phase 6 — `CoinShopActivity.java`

In `buildShopCatalog()`, **delete the entire `// 4. Themes` block**:

```java
// 4. Themes
allItems.add(new ShopItem("theme_pop_art",  "Pop Art Board",    "Comic-book style board & chips", 350,  ShopItem.Category.THEME, "🎨"));
allItems.add(new ShopItem("theme_cyberpunk","Cyberpunk Board",  "Neon futuristic board & glow",   500,  ShopItem.Category.THEME, "⚡"));
allItems.add(new ShopItem("theme_luxury",   "Luxury Persian",   "Ivory & gold satin board",       750,  ShopItem.Category.THEME, "🏛️"));
```

In `setupCategoryTabs()`, remove the branch that handles `rbCategoryThemes`:
```java
} else if (checkedId == R.id.rbCategoryThemes) {
    filterItems(ShopItem.Category.THEME);
}
```

---

## Phase 7 — `activity_coin_shop.xml`

Remove the Themes RadioButton entirely. Find and delete:

```xml
<RadioButton
    android:id="@+id/rbCategoryThemes"
    ...
    android:text="@string/board_theme" />
```

(The exact attributes may vary — delete the entire `<RadioButton>` element whose id is `rbCategoryThemes`.)

---

## Phase 8 — `ShopAdapter.java`

In `isItemEquipped()`, remove the THEME case:
```java
case THEME:
    return item.getId().equals(profileManager.getActiveTheme());
```

In `equipItem()`, remove the THEME case:
```java
case THEME:
    profileManager.setActiveTheme(item.getId());
    int themeId = BoardThemeFactory.themeIdFromString(item.getId());
    int difficulty = GamePreferences.getBotDifficulty(context);
    GamePreferences.saveSelections(context, difficulty, themeId);
    break;
```

After removing, the imports `BoardThemeFactory` and `GamePreferences` may become unused in ShopAdapter — remove them if the compiler warns.

---

## Phase 9 — `strings.xml`

Add inside `<resources>`:

```xml
<!-- Theme unlock via ad -->
<string name="watch_ad_unlock">📺 Ad</string>
<string name="theme_unlocked_toast">Theme unlocked!</string>
<string name="ad_not_ready">Ad not ready — try again shortly</string>
```

---

## Phase 10 — Verification

```
.\gradlew.bat compileFossDebugJavaWithJavac
```

Confirm:
- `R.id.lockOverlay1`, `R.id.lockOverlay2`, `R.id.lockOverlay3` resolve (dialog_single_player)
- `R.id.pnpLockOverlay1`, `R.id.pnpLockOverlay2`, `R.id.pnpLockOverlay3` resolve (pass_and_play_dialog)
- `GamePreferences.isThemeUnlocked()` and `GamePreferences.unlockTheme()` compile
- `RewardedAdPlacement.THEME_UNLOCK` resolves
- `R.string.watch_ad_unlock`, `R.string.theme_unlocked_toast`, `R.string.ad_not_ready` resolve
- No reference to `ShopItem.Category.THEME` anywhere in `ShopAdapter`
- No `rbCategoryThemes` in `activity_coin_shop.xml`
- No theme `ShopItem` entries in `CoinShopActivity.buildShopCatalog()`

Commit: `feat: move theme unlock from shop to ad-gated picker in new-game dialog`
