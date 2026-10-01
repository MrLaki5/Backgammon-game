# Theme 4 — Woodland Implementation Prompt

## Overview
Add `THEME_WOODLAND` (id = 4) to the game. Simultaneously expand both theme-picker dialogs from a 2×2 grid to a **2×4 grid** (2 rows × 4 columns) so themes 5–7 can be added later with only drawable + class changes.

**Do NOT touch** `game-core/`.

---

## Asset Prerequisites (do BEFORE coding)

From the Gemini-generated Woodland image:

1. **`preview_theme_woodland.png`**
   - Crop only the BOARD portion of the image (exclude the piece/dice strip at the bottom).
   - Resize to **320 × 180 px** (16:9 thumbnail).
   - Save to:
     - `app/src/main/res/drawable-xxhdpi/preview_theme_woodland.png`
     - `app/src/main/res/drawable-xhdpi/preview_theme_woodland.png` (resize to 213×120)
     - `app/src/main/res/drawable-hdpi/preview_theme_woodland.png` (resize to 160×90)

2. **`board_woodland.png`**
   - Same crop as above (board only, no piece strip), full resolution.
   - Save to:
     - `app/src/main/res/drawable-xxhdpi/board_woodland.png`
     - `app/src/main/res/drawable-xhdpi/board_woodland.png`
     - `app/src/main/res/drawable-hdpi/board_woodland.png`

---

## Phase 1 — `GamePreferences.java`

Add after `THEME_LUXURY = 3;`:

```java
public static final int THEME_WOODLAND = 4;
```

---

## Phase 2 — `WoodlandTheme.java` (NEW FILE)

Create `app/src/main/java/games/mrlaki5/backgammon/GameView/themes/WoodlandTheme.java`:

```java
package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;

/**
 * Woodland theme: smooth alabaster-white marble vs dark polished walnut checkers,
 * copper-bronze rims, forest-green accent rings.
 */
public class WoodlandTheme implements BoardTheme {

    // Player 1 (user/light): warm alabaster white, ivory, copper rim, green accent
    private static final int W_P1_BASE      = Color.rgb(240, 234, 218);
    private static final int W_P1_LIGHT     = Color.rgb(255, 252, 245);
    private static final int W_P1_DARK      = Color.rgb(195, 182, 155);
    private static final int W_P1_RIM       = Color.rgb(175, 115, 52);   // copper
    private static final int W_P1_OUTLINE   = Color.rgb(80, 55, 25);
    private static final int W_P1_HIGHLIGHT = Color.rgb(255, 255, 248);
    private static final int W_P1_ACCENT    = Color.rgb(43, 82, 35);     // forest green

    // Player 2 (opponent/dark): dark espresso walnut, mahogany, copper rim, green accent
    private static final int W_P2_BASE      = Color.rgb(58, 31, 10);
    private static final int W_P2_LIGHT     = Color.rgb(100, 58, 28);
    private static final int W_P2_DARK      = Color.rgb(25, 12, 4);
    private static final int W_P2_RIM       = Color.rgb(160, 105, 42);   // copper
    private static final int W_P2_OUTLINE   = Color.rgb(14, 7, 2);
    private static final int W_P2_HIGHLIGHT = Color.rgb(130, 82, 38);
    private static final int W_P2_ACCENT    = Color.rgb(43, 82, 35);     // forest green

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_WOODLAND;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return R.drawable.board_woodland;
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint, Paint contactShadowPaint,
                         RectF shadowRect, RectF specRect) {
        int baseColor      = player == 1 ? W_P1_BASE      : W_P2_BASE;
        int lightColor     = player == 1 ? W_P1_LIGHT     : W_P2_LIGHT;
        int darkColor      = player == 1 ? W_P1_DARK      : W_P2_DARK;
        int rimColor       = player == 1 ? W_P1_RIM       : W_P2_RIM;
        int outlineColor   = player == 1 ? W_P1_OUTLINE   : W_P2_OUTLINE;
        int highlightColor = player == 1 ? W_P1_HIGHLIGHT : W_P2_HIGHLIGHT;
        int accentColor    = W_P1_ACCENT; // same green for both

        float cx = rect.centerX();
        float cy = rect.centerY();
        float chipRadius = Math.max(rect.width(), rect.height()) / 2F;

        // 1. Contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.07F);
        contactShadowPaint.setColor(Color.argb(90, 20, 10, 3));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(chipRadius * 0.16F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // 2. Main body radial gradient
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.14F,
                cy - rect.height() * 0.17F,
                chipRadius * 1.15F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.50F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // 3. Outline
        rimPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.055F));
        rimPaint.setColor(outlineColor);
        canvas.drawOval(rect, rimPaint);

        // 4. Copper rim band
        RectF rimRing = new RectF(rect);
        rimRing.inset(rect.width() * 0.065F, rect.width() * 0.065F);
        highlightPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.035F));
        highlightPaint.setColor(rimColor);
        canvas.drawOval(rimRing, highlightPaint);

        // 5. Forest-green accent ring
        RectF accentRing = new RectF(rect);
        accentRing.inset(rect.width() * 0.20F, rect.width() * 0.20F);
        accentPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.022F));
        accentPaint.setColor(Color.argb(90, Color.red(accentColor),
                Color.green(accentColor), Color.blue(accentColor)));
        canvas.drawOval(accentRing, accentPaint);

        // 6. Inner dots (4-point natural pattern)
        dotPaint.setColor(Color.argb(55, Color.red(accentColor),
                Color.green(accentColor), Color.blue(accentColor)));
        float dotR    = rect.width() * 0.020F;
        float dotDist = rect.width() * 0.120F;
        canvas.drawCircle(cx,           cy - dotDist, dotR, dotPaint);
        canvas.drawCircle(cx,           cy + dotDist, dotR, dotPaint);
        canvas.drawCircle(cx - dotDist, cy,           dotR, dotPaint);
        canvas.drawCircle(cx + dotDist, cy,           dotR, dotPaint);

        // 7. Specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30F, rect.height() * 0.32F);
        specRect.offset(-rect.width() * 0.10F, -rect.height() * 0.12F);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55F,
                new int[]{Color.argb(70, Color.red(highlightColor),
                        Color.green(highlightColor), Color.blue(highlightColor)),
                        Color.argb(0, 255, 255, 255)},
                new float[]{0F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(specRect, specPaint);
    }

    @Override
    public void drawEndChip(Canvas canvas, RectF rect, int player,
                            Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                            Paint highlightPaint, Paint glowPaint, Paint contactShadowPaint,
                            RectF shadowRect) {
        int baseColor    = player == 1 ? W_P1_BASE    : W_P2_BASE;
        int lightColor   = player == 1 ? W_P1_LIGHT   : W_P2_LIGHT;
        int darkColor    = player == 1 ? W_P1_DARK    : W_P2_DARK;
        int rimColor     = player == 1 ? W_P1_RIM     : W_P2_RIM;
        int outlineColor = player == 1 ? W_P1_OUTLINE : W_P2_OUTLINE;

        float cornerR = rect.width() * 0.12F;

        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.12F);
        contactShadowPaint.setColor(Color.argb(75, 20, 10, 3));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.10F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, contactShadowPaint);

        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.10F,
                rect.centerY() - rect.height() * 0.14F,
                Math.max(rect.width(), rect.height()) * 0.70F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.50F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerR, cornerR, fillPaint);
        fillPaint.setShader(null);

        rimPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.05F));
        rimPaint.setColor(outlineColor);
        canvas.drawRoundRect(rect, cornerR, cornerR, rimPaint);

        RectF innerRect = new RectF(rect);
        innerRect.inset(rect.width() * 0.06F, rect.height() * 0.06F);
        highlightPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.03F));
        highlightPaint.setColor(rimColor);
        canvas.drawRoundRect(innerRect, cornerR * 0.8F, cornerR * 0.8F, highlightPaint);
    }
}
```

---

## Phase 3 — `BoardThemeFactory.java`

Add the singleton field after `LUXURY`:
```java
private static final BoardTheme WOODLAND = new WoodlandTheme();
```

In `themeIdFromString()` — add case before `default`:
```java
case "theme_woodland": return GamePreferences.THEME_WOODLAND;
```

In `themeStringFromId()` — add case before `default`:
```java
case GamePreferences.THEME_WOODLAND: return "theme_woodland";
```

In `getTheme(int)` — add case before `default`:
```java
case GamePreferences.THEME_WOODLAND:
    return WOODLAND;
```

---

## Phase 4 — `dialog_single_player.xml`

### Expand the theme picker from 2×2 to 2×4 (2 rows, 4 columns per row)

**Find** the entire `<!-- ===== LEFT COLUMN: Theme Thumbnails (2x2) ===== -->` LinearLayout block (from `android:layout_weight="1"` through its closing `</LinearLayout>`) and **replace it** with:

```xml
<!-- ===== LEFT COLUMN: Theme Thumbnails (2×4) ===== -->
<LinearLayout
    android:layout_width="0dp"
    android:layout_height="match_parent"
    android:layout_weight="1"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="6dp">

    <!-- Row 1: themes 0–3 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <FrameLayout
            android:id="@+id/themeThumb0"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_selected"
            android:padding="3dp">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@drawable/preview_theme_royal" />
        </FrameLayout>

        <FrameLayout
            android:id="@+id/themeThumb1"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@drawable/preview_theme_pop_art" />
            <LinearLayout
                android:id="@+id/lockOverlay1"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#CC000000"
                android:gravity="center"
                android:orientation="vertical"
                android:visibility="gone">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🔒" android:textSize="14sp" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/watch_ad_unlock" android:textColor="#F4B044" android:textSize="7sp" android:textStyle="bold" android:gravity="center" />
            </LinearLayout>
        </FrameLayout>

        <FrameLayout
            android:id="@+id/themeThumb2"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@drawable/preview_theme_cyberpunk" />
            <LinearLayout
                android:id="@+id/lockOverlay2"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#CC000000"
                android:gravity="center"
                android:orientation="vertical"
                android:visibility="gone">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🔒" android:textSize="14sp" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/watch_ad_unlock" android:textColor="#F4B044" android:textSize="7sp" android:textStyle="bold" android:gravity="center" />
            </LinearLayout>
        </FrameLayout>

        <FrameLayout
            android:id="@+id/themeThumb3"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@drawable/preview_theme_luxury" />
            <LinearLayout
                android:id="@+id/lockOverlay3"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#CC000000"
                android:gravity="center"
                android:orientation="vertical"
                android:visibility="gone">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🔒" android:textSize="14sp" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/watch_ad_unlock" android:textColor="#F4B044" android:textSize="7sp" android:textStyle="bold" android:gravity="center" />
            </LinearLayout>
        </FrameLayout>

    </LinearLayout>

    <!-- Row 2: themes 4–7 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <FrameLayout
            android:id="@+id/themeThumb4"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@drawable/preview_theme_woodland" />
            <LinearLayout
                android:id="@+id/lockOverlay4"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#CC000000"
                android:gravity="center"
                android:orientation="vertical"
                android:visibility="gone">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="🔒" android:textSize="14sp" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/watch_ad_unlock" android:textColor="#F4B044" android:textSize="7sp" android:textStyle="bold" android:gravity="center" />
            </LinearLayout>
        </FrameLayout>

        <!-- Placeholders for themes 5, 6, 7 — add preview drawables when ready -->
        <FrameLayout
            android:id="@+id/themeThumb5"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp"
            android:visibility="gone" />

        <FrameLayout
            android:id="@+id/themeThumb6"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp"
            android:visibility="gone" />

        <FrameLayout
            android:id="@+id/themeThumb7"
            android:layout_width="0dp"
            android:layout_height="54dp"
            android:layout_margin="4dp"
            android:layout_weight="1"
            android:background="@drawable/bg_theme_thumb_normal"
            android:padding="3dp"
            android:visibility="gone" />

    </LinearLayout>
</LinearLayout>
```

> **NOTE:** `themeThumb5`, `themeThumb6`, `themeThumb7` are `visibility="gone"` — they take no space until activated by future theme additions. `themeThumb1/2/3` lock overlays are already included here (replacing the previous separate addition from the theme-unlock prompt — no double overlays).

---

## Phase 5 — `pass_and_play_dialog.xml`

Apply the same 2×4 grid expansion. Replace the `pnpThemeThumb0..3` block with a 2-row layout:

**Row 1:** `pnpThemeThumb0`, `pnpThemeThumb1`, `pnpThemeThumb2`, `pnpThemeThumb3`  
— identical to the structure above, IDs: `pnpThemeThumb0..3`, lock overlays: `pnpLockOverlay1..3`

**Row 2:** `pnpThemeThumb4`, `pnpThemeThumb5`, `pnpThemeThumb6`, `pnpThemeThumb7`  
— `pnpThemeThumb4` uses `@drawable/preview_theme_woodland`, lock overlay id `pnpLockOverlay4`, visibility visible  
— `pnpThemeThumb5/6/7` visibility="gone"

Same structure as Phase 4 but with `pnp` prefix on all IDs.

---

## Phase 6 — `MenuActivity.java`

### 6a — Update `showNewGameDialog()`

**Find:**
```java
setupThemePicker(dialogView,
        new int[]{R.id.themeThumb0, R.id.themeThumb1, R.id.themeThumb2, R.id.themeThumb3},
        new int[]{0, R.id.lockOverlay1, R.id.lockOverlay2, R.id.lockOverlay3},
        selectedTheme);
```

**Replace with:**
```java
setupThemePicker(dialogView,
        new int[]{R.id.themeThumb0, R.id.themeThumb1, R.id.themeThumb2, R.id.themeThumb3, R.id.themeThumb4},
        new int[]{0, R.id.lockOverlay1, R.id.lockOverlay2, R.id.lockOverlay3, R.id.lockOverlay4},
        selectedTheme);
```

### 6b — Update `showPassAndPlayDialog()`

**Find:**
```java
setupThemePicker(dialogView,
        new int[]{R.id.pnpThemeThumb0, R.id.pnpThemeThumb1, R.id.pnpThemeThumb2, R.id.pnpThemeThumb3},
        new int[]{0, R.id.pnpLockOverlay1, R.id.pnpLockOverlay2, R.id.pnpLockOverlay3},
        selectedTheme);
```

**Replace with:**
```java
setupThemePicker(dialogView,
        new int[]{R.id.pnpThemeThumb0, R.id.pnpThemeThumb1, R.id.pnpThemeThumb2, R.id.pnpThemeThumb3, R.id.pnpThemeThumb4},
        new int[]{0, R.id.pnpLockOverlay1, R.id.pnpLockOverlay2, R.id.pnpLockOverlay3, R.id.pnpLockOverlay4},
        selectedTheme);
```

---

## Phase 7 — `strings.xml`

Add inside `<resources>`:

```xml
<string name="theme_woodland_name">Woodland</string>
```

Also add to `values-fa/strings.xml`:
```xml
<string name="theme_woodland_name">جنگل</string>
```

---

## Phase 8 — Verification

```
.\gradlew.bat compileFossDebugJavaWithJavac
```

Confirm:
- `R.id.themeThumb4` and `R.id.lockOverlay4` resolve
- `R.id.pnpThemeThumb4` and `R.id.pnpLockOverlay4` resolve
- `GamePreferences.THEME_WOODLAND` == 4
- `BoardThemeFactory.getTheme(4)` returns `WoodlandTheme` instance
- `R.drawable.board_woodland` and `R.drawable.preview_theme_woodland` resolve
- Theme 4 tappable in both new-game dialogs, ad gate works, persists across sessions

Commit: `feat: add Woodland theme (#4) and expand picker to 2×4 grid for 8-theme support`
