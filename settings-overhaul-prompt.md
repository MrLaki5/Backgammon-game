# Settings Screen UI/UX Overhaul — Implementation Prompt

Refactor the Game Settings screen from a flat, unstyled list into a structured, premium settings UI with section cards, SwitchCompat toggles, and clean slider rows. All changes are confined to:
- `app/src/main/res/layout/activity_settings.xml`
- `app/src/main/res/drawable/settings_section_card.xml` ← NEW FILE
- `app/src/main/java/games/mrlaki5/backgammon/Menus/SettingsActivity.java`
- `app/src/main/res/values/strings.xml` (additions only)

**Do NOT touch** `game-core/` or any file not listed above.

---

## Phase 1 — New Drawable: `settings_section_card.xml`

Create `app/src/main/res/drawable/settings_section_card.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:angle="270"
        android:startColor="#F0183242"
        android:endColor="#F0112532" />
    <corners android:radius="16dp" />
    <stroke android:width="1dp" android:color="#55F4B044" />
</shape>
```

---

## Phase 2 — `activity_settings.xml` — Complete Replacement

Replace the entire file content with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/meny_background"
    android:layoutDirection="locale"
    tools:context=".Menus.SettingsActivity">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:scrollbars="none">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="12dp"
            android:paddingBottom="24dp">

            <!-- ── Header Row ── -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="8dp">

                <Button
                    android:id="@+id/btnSettingsBack"
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:text="←"
                    android:textSize="20sp"
                    android:textColor="@color/accent_gold"
                    android:background="@drawable/royal_secondary_button"
                    android:padding="0dp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="@string/settings_title"
                    android:textColor="@color/accent_gold"
                    android:textSize="24sp"
                    android:textStyle="bold"
                    android:gravity="center" />

                <!-- Balance spacer -->
                <View
                    android:layout_width="48dp"
                    android:layout_height="48dp" />

            </LinearLayout>

            <!-- Gold divider -->
            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="@color/accent_gold"
                android:layout_marginBottom="16dp" />


            <!-- ════════════════════════════════
                 SECTION 1 — SOUND
                 ════════════════════════════════ -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:background="@drawable/settings_section_card"
                android:padding="16dp"
                android:layout_marginBottom="12dp">

                <!-- Section header -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="10dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="🔊"
                        android:textSize="18sp"
                        android:layout_marginEnd="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/settings_section_audio"
                        android:textColor="@color/accent_gold"
                        android:textSize="16sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="#44F4B044"
                    android:layout_marginBottom="14dp" />

                <!-- Sound toggle -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="48dp"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/sound_enabled"
                        android:textColor="@color/text_primary"
                        android:textSize="15sp" />

                    <androidx.appcompat.widget.SwitchCompat
                        android:id="@+id/switchSound"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        app:thumbTint="@color/accent_gold"
                        app:trackTint="@color/accent_gold_support" />
                </LinearLayout>

                <!-- Effects toggle -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="48dp"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="14dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/effects_enabled"
                        android:textColor="@color/text_primary"
                        android:textSize="15sp" />

                    <androidx.appcompat.widget.SwitchCompat
                        android:id="@+id/switchEffects"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        app:thumbTint="@color/accent_gold"
                        app:trackTint="@color/accent_gold_support" />
                </LinearLayout>

                <!-- SFX Volume -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/sound"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvSfxVolumeValue"
                        android:layout_width="56dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderSfxVolume"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="100"
                    android:progress="80"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold"
                    android:layout_marginBottom="12dp" />

                <!-- Music Volume -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/music_volume"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvMusicVolumeValue"
                        android:layout_width="56dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderMusicVolume"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="100"
                    android:progress="55"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold" />

            </LinearLayout>


            <!-- ════════════════════════════════
                 SECTION 2 — GAMEPLAY
                 ════════════════════════════════ -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:background="@drawable/settings_section_card"
                android:padding="16dp"
                android:layout_marginBottom="12dp">

                <!-- Section header -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="10dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="🎮"
                        android:textSize="18sp"
                        android:layout_marginEnd="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/settings_section_gameplay"
                        android:textColor="@color/accent_gold"
                        android:textSize="16sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="#44F4B044"
                    android:layout_marginBottom="14dp" />

                <!-- Turn Speed -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/turn_delay"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvTurnSpeedValue"
                        android:layout_width="56dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderTurnSpeed"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="5"
                    android:progress="1"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold" />

            </LinearLayout>


            <!-- ════════════════════════════════
                 SECTION 3 — SHAKE DICE
                 ════════════════════════════════ -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:background="@drawable/settings_section_card"
                android:padding="16dp"
                android:layout_marginBottom="20dp">

                <!-- Section header -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="10dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="📳"
                        android:textSize="18sp"
                        android:layout_marginEnd="8dp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/settings_section_shake"
                        android:textColor="@color/accent_gold"
                        android:textSize="16sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <View
                    android:layout_width="match_parent"
                    android:layout_height="1dp"
                    android:background="#44F4B044"
                    android:layout_marginBottom="14dp" />

                <!-- Shake Sensitivity -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/shake_sensitivity"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvShakeThresholdValue"
                        android:layout_width="64dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderShakeThreshold"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="1000"
                    android:progress="550"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold"
                    android:layout_marginBottom="12dp" />

                <!-- Shake Precision -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/shake_duration"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvShakePrecisionValue"
                        android:layout_width="64dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderShakePrecision"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="200"
                    android:progress="70"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold"
                    android:layout_marginBottom="12dp" />

                <!-- Shake Delay -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:layout_marginBottom="4dp">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/settings_shake_delay"
                        android:textColor="@color/text_muted"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvShakeDelayValue"
                        android:layout_width="64dp"
                        android:layout_height="wrap_content"
                        android:gravity="end"
                        android:textColor="@color/accent_gold"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <SeekBar
                    android:id="@+id/sliderShakeDelay"
                    android:layout_width="match_parent"
                    android:layout_height="36dp"
                    android:max="15"
                    android:progress="4"
                    android:progressTint="@color/accent_gold"
                    android:thumbTint="@color/accent_gold" />

            </LinearLayout>


            <!-- Restore Defaults button -->
            <Button
                android:id="@+id/btnRestoreDefaults"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                android:background="@drawable/royal_secondary_button"
                android:text="@string/restore_defaults"
                android:textAllCaps="false"
                android:textColor="@color/text_primary"
                android:textSize="15sp"
                android:textStyle="bold" />

        </LinearLayout>
    </ScrollView>

</FrameLayout>
```

---

## Phase 3 — `strings.xml` — Additions Only

Add these strings inside `<resources>` (before the closing tag):

```xml
<!-- Settings screen section headers -->
<string name="settings_section_audio">Sound</string>
<string name="settings_section_gameplay">Gameplay</string>
<string name="settings_section_shake">Shake Dice</string>

<!-- Settings value labels -->
<string name="settings_sfx_value">SFX: %1$d%%</string>
<string name="settings_music_value">Music: %1$d%%</string>
<string name="settings_turn_value">Speed: %1$d</string>
<string name="settings_threshold_value">%1$d</string>
<string name="settings_precision_value">%1$d ms</string>
<string name="settings_shake_delay">Detection Delay</string>
<string name="settings_delay_value">%1$d</string>
```

---

## Phase 4 — `SettingsActivity.java` — Complete Replacement

Replace the entire file content with:

```java
package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.R;

public class SettingsActivity extends AppCompatActivity {

    // ── Preference Keys ─────────────────────────────────────────────────────
    public static final String KEY_SOUND_ENABLED       = "soundEnabled";
    public static final String KEY_EFFECTS_ENABLED     = "effectsEnabled";
    public static final String KEY_SOUND_VOLUME        = "volume";           // legacy SFX key
    public static final String KEY_DICE_TRESHOLD       = "sensor_sensibility";
    public static final String KEY_TIME_SAMPLE         = "sample_time";
    public static final String KEY_DICE_SHAKE_DELAY    = "delay";
    public static final String KEY_TIME_BETWEEN_TURNS  = "turnBTime";

    // ── Defaults ─────────────────────────────────────────────────────────────
    public static final int     DEF_DICE_TRAESHOLD      = 550;
    public static final int     DEF_TIME_SAMPLE         = 70;
    public static final int     DEF_SOUND_VOLUME        = 80;
    public static final int     DEF_DICE_SHAKE_DELAY    = 4;
    public static final int     DEF_TIME_BETWEEN_TURNS  = 1;
    public static final boolean DEF_SOUND_ENABLED       = true;
    public static final boolean DEF_EFFECTS_ENABLED     = true;

    // ── Max values ───────────────────────────────────────────────────────────
    public static final int MAX_SOUND_VOLUME       = 100;
    public static final int MAX_DICE_TRESHOLD      = 1000;
    public static final int MAX_TIME_SAMPLE        = 200;
    public static final int MAX_DICE_SHAKE_DELAY   = 15;
    public static final int MAX_TIME_BETWEEN_TURNS = 5;

    // ── State ────────────────────────────────────────────────────────────────
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private SwitchCompat switchSound;
    private SwitchCompat switchEffects;

    private SeekBar sliderSfx;
    private SeekBar sliderMusic;
    private SeekBar sliderTurnSpeed;
    private SeekBar sliderShakeThreshold;
    private SeekBar sliderShakePrecision;
    private SeekBar sliderShakeDelay;

    private TextView tvSfxValue;
    private TextView tvMusicValue;
    private TextView tvTurnSpeedValue;
    private TextView tvShakeThresholdValue;
    private TextView tvShakePrecisionValue;
    private TextView tvShakeDelayValue;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        prefs  = getSharedPreferences("Settings", MODE_PRIVATE);
        editor = prefs.edit();

        bindViews();
        loadPrefs();
        setupListeners();

        findViewById(R.id.btnSettingsBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRestoreDefaults).setOnClickListener(v -> restoreDefaults());
    }

    // ── Bind Views ───────────────────────────────────────────────────────────

    private void bindViews() {
        switchSound    = findViewById(R.id.switchSound);
        switchEffects  = findViewById(R.id.switchEffects);

        sliderSfx            = findViewById(R.id.sliderSfxVolume);
        sliderMusic          = findViewById(R.id.sliderMusicVolume);
        sliderTurnSpeed      = findViewById(R.id.sliderTurnSpeed);
        sliderShakeThreshold = findViewById(R.id.sliderShakeThreshold);
        sliderShakePrecision = findViewById(R.id.sliderShakePrecision);
        sliderShakeDelay     = findViewById(R.id.sliderShakeDelay);

        tvSfxValue            = findViewById(R.id.tvSfxVolumeValue);
        tvMusicValue          = findViewById(R.id.tvMusicVolumeValue);
        tvTurnSpeedValue      = findViewById(R.id.tvTurnSpeedValue);
        tvShakeThresholdValue = findViewById(R.id.tvShakeThresholdValue);
        tvShakePrecisionValue = findViewById(R.id.tvShakePrecisionValue);
        tvShakeDelayValue     = findViewById(R.id.tvShakeDelayValue);
    }

    // ── Load Saved Preferences ───────────────────────────────────────────────

    private void loadPrefs() {
        int sfx       = prefs.getInt(GamePreferences.KEY_SFX_VOLUME,
                         prefs.getInt(KEY_SOUND_VOLUME, DEF_SOUND_VOLUME));
        int music     = prefs.getInt(GamePreferences.KEY_MUSIC_VOLUME,
                         GamePreferences.DEFAULT_MUSIC_VOLUME);
        int turnSpeed = prefs.getInt(KEY_TIME_BETWEEN_TURNS, DEF_TIME_BETWEEN_TURNS);
        int threshold = prefs.getInt(KEY_DICE_TRESHOLD, DEF_DICE_TRAESHOLD);
        int precision = prefs.getInt(KEY_TIME_SAMPLE, DEF_TIME_SAMPLE);
        int delay     = prefs.getInt(KEY_DICE_SHAKE_DELAY, DEF_DICE_SHAKE_DELAY);
        boolean soundOn   = prefs.getBoolean(KEY_SOUND_ENABLED, DEF_SOUND_ENABLED);
        boolean effectsOn = prefs.getBoolean(KEY_EFFECTS_ENABLED, DEF_EFFECTS_ENABLED);

        switchSound.setChecked(soundOn);
        switchEffects.setChecked(effectsOn);

        setSlider(sliderSfx,            tvSfxValue,            sfx,       "%d%%");
        setSlider(sliderMusic,          tvMusicValue,          music,     "%d%%");
        setSlider(sliderTurnSpeed,      tvTurnSpeedValue,      turnSpeed, "%d");
        setSlider(sliderShakeThreshold, tvShakeThresholdValue, threshold, "%d");
        setSlider(sliderShakePrecision, tvShakePrecisionValue, precision, "%d ms");
        setSlider(sliderShakeDelay,     tvShakeDelayValue,     delay,     "%d");
    }

    private void setSlider(SeekBar bar, TextView label, int value, String fmt) {
        bar.setProgress(value);
        label.setText(String.format(fmt, value));
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    private void setupListeners() {
        switchSound.setOnCheckedChangeListener((v, checked) ->
                editor.putBoolean(KEY_SOUND_ENABLED, checked).apply());

        switchEffects.setOnCheckedChangeListener((v, checked) ->
                editor.putBoolean(KEY_EFFECTS_ENABLED, checked).apply());

        sliderSfx.setOnSeekBarChangeListener(makeListener(tvSfxValue, "%d%%", val -> {
            editor.putInt(GamePreferences.KEY_SFX_VOLUME, val)
                  .putInt(KEY_SOUND_VOLUME, val)
                  .apply();
        }));

        sliderMusic.setOnSeekBarChangeListener(makeListener(tvMusicValue, "%d%%", val ->
                editor.putInt(GamePreferences.KEY_MUSIC_VOLUME, val).apply()));

        sliderTurnSpeed.setOnSeekBarChangeListener(makeListener(tvTurnSpeedValue, "%d", val ->
                editor.putInt(KEY_TIME_BETWEEN_TURNS, val).apply()));

        sliderShakeThreshold.setOnSeekBarChangeListener(makeListener(tvShakeThresholdValue, "%d", val ->
                editor.putInt(KEY_DICE_TRESHOLD, val).apply()));

        sliderShakePrecision.setOnSeekBarChangeListener(makeListener(tvShakePrecisionValue, "%d ms", val ->
                editor.putInt(KEY_TIME_SAMPLE, val).apply()));

        sliderShakeDelay.setOnSeekBarChangeListener(makeListener(tvShakeDelayValue, "%d", val ->
                editor.putInt(KEY_DICE_SHAKE_DELAY, val).apply()));
    }

    // ── SeekBar Listener Factory ─────────────────────────────────────────────

    private interface IntConsumer { void accept(int val); }

    private SeekBar.OnSeekBarChangeListener makeListener(TextView label, String fmt, IntConsumer onStop) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                label.setText(String.format(fmt, progress));
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                onStop.accept(bar.getProgress());
            }
        };
    }

    // ── Restore Defaults ─────────────────────────────────────────────────────

    private void restoreDefaults() {
        editor.putInt(KEY_DICE_TRESHOLD,      DEF_DICE_TRAESHOLD)
              .putInt(KEY_TIME_SAMPLE,         DEF_TIME_SAMPLE)
              .putInt(KEY_SOUND_VOLUME,        DEF_SOUND_VOLUME)
              .putInt(GamePreferences.KEY_SFX_VOLUME,   DEF_SOUND_VOLUME)
              .putInt(GamePreferences.KEY_MUSIC_VOLUME, GamePreferences.DEFAULT_MUSIC_VOLUME)
              .putBoolean(KEY_SOUND_ENABLED,   DEF_SOUND_ENABLED)
              .putBoolean(KEY_EFFECTS_ENABLED, DEF_EFFECTS_ENABLED)
              .putInt(KEY_DICE_SHAKE_DELAY,    DEF_DICE_SHAKE_DELAY)
              .putInt(KEY_TIME_BETWEEN_TURNS,  DEF_TIME_BETWEEN_TURNS)
              .apply();
        loadPrefs();
    }
}
```

---

## Phase 5 — Verification

After implementing all phases, run:

```
.\gradlew.bat compileFossDebugJavaWithJavac
```

Expected: **BUILD SUCCESSFUL** with zero errors.

Confirm:
- `R.id.switchSound`, `R.id.switchEffects`, `R.id.sliderSfxVolume`, `R.id.sliderMusicVolume`, `R.id.sliderTurnSpeed`, `R.id.sliderShakeThreshold`, `R.id.sliderShakePrecision`, `R.id.sliderShakeDelay` all resolve
- `R.id.tvSfxVolumeValue`, `R.id.tvMusicVolumeValue`, `R.id.tvTurnSpeedValue`, `R.id.tvShakeThresholdValue`, `R.id.tvShakePrecisionValue`, `R.id.tvShakeDelayValue` all resolve
- `R.id.btnSettingsBack`, `R.id.btnRestoreDefaults` resolve
- `R.string.settings_section_audio`, `R.string.settings_section_gameplay`, `R.string.settings_section_shake` resolve
- `R.drawable.settings_section_card` resolves
- `restoreDef(View)` public method removed (replaced by `btnRestoreDefaults` click listener; remove `android:onClick="restoreDef"` from XML — already absent in new layout)
- No references to old IDs: `seekBar`, `seekBar2`, `seekBar3`, `seekBar4`, `seekBar5`, `seekBar6`, `textView`, `textView2`, `textView3`, `textView4`, `textView5`, `textView6`, `soundEnabledCheckBox`, `effectsEnabledCheckBox`, `restore`

Commit with message: `feat: redesign settings screen with section cards, SwitchCompat toggles, gold slider tinting`
