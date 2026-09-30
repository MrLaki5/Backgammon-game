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

    // Legacy default preference keys
    public static final String KEY_DEF_SOUND_VOLUME        = "DEFvolume";
    public static final String KEY_DEF_DICE_TRESHOLD       = "DEFsensor_sensibility";
    public static final String KEY_DEF_TIME_SAMPLE         = "DEFsample_time";
    public static final String KEY_DEF_DICE_SHAKE_DELAY    = "DEFdelay";
    public static final String KEY_DEF_TIME_BETWEEN_TURNS  = "DEFturnBTime";

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
