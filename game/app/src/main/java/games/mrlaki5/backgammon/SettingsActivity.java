package games.mrlaki5.backgammon;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    public static String KEY_SOUND_VOLUME="volume";
    public static String KEY_DICE_TRESHOLD="sensor_sensibility";
    public static String KEY_TIME_SAMPLE="sample_time";
    public static String KEY_DICE_SHAKE_DELAY="delay";

    public static String KEY_DEF_SOUND_VOLUME="DEFvolume";
    public static String KEY_DEF_DICE_TRESHOLD="DEFsensor_sensibility";
    public static String KEY_DEF_TIME_SAMPLE="DEFsample_time";
    public static String KEY_DEF_DICE_SHAKE_DELAY="DEFdelay";

    public static int MAX_SOUND_VOLUME=100;
    public static int MAX_DICE_TRESHOLD=1000;
    public static int MAX_TIME_SAMPLE=200;
    public static int MAX_DICE_SHAKE_DELAY=15;

    public static int DEF_SOUND_VOLUME=80;
    public static int DEF_DICE_TRAESHOLD=550;
    public static int DEF_TIME_SAMPLE=70;
    public static int DEF_DICE_SHAKE_DELAY=4;

    private int ShakeSensibilityValue=0;
    private int TimeSampleValue=0;
    private int SoundValue=0;
    private int DiceDelayValue=0;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private SeekBar shakeTresholdSlider;
    private SeekBar timeSampleSlider;
    private SeekBar soundSlider;
    private SeekBar delaySlider;

    private TextView ShakeSensibilityTextView;
    private TextView TimeSampleTextView;
    private TextView SoundTextView;
    private TextView DelayTextView;

    private SeekBar.OnSeekBarChangeListener shakeSensibilityListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            ShakeSensibilityTextView.setText("Shake threshold: " + progress);
            ShakeSensibilityValue=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
            editor.putInt("sensor_sensibility", ShakeSensibilityValue);
            editor.commit();
        }
    };

    private SeekBar.OnSeekBarChangeListener timeSampleListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            TimeSampleTextView.setText("Shake time precision: " + progress +"ms");
            TimeSampleValue=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
            editor.putInt("sample_time", TimeSampleValue);
            editor.commit();
        }
    };

    private SeekBar.OnSeekBarChangeListener soundListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            SoundTextView.setText("Sound: " + progress + "%");
            SoundValue=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
            editor.putInt("sound", SoundValue);
            editor.commit();
        }
    };

    private SeekBar.OnSeekBarChangeListener delayListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            DelayTextView.setText("Shake delay: " + progress);
            DiceDelayValue=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
            editor.putInt("delay", DiceDelayValue);
            editor.commit();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("Settings", 0);
        ShakeSensibilityValue=preferences.getInt(KEY_DICE_TRESHOLD, DEF_DICE_TRAESHOLD);
        TimeSampleValue=preferences.getInt(KEY_TIME_SAMPLE, DEF_TIME_SAMPLE);
        SoundValue=preferences.getInt(KEY_SOUND_VOLUME, DEF_SOUND_VOLUME);
        DiceDelayValue=preferences.getInt(KEY_DICE_SHAKE_DELAY, DEF_DICE_SHAKE_DELAY);
        editor=preferences.edit();

        ShakeSensibilityTextView=findViewById(R.id.textView);
        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);

        TimeSampleTextView=findViewById(R.id.textView2);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");

        SoundTextView=findViewById(R.id.textView3);
        SoundTextView.setText("Sound: " + SoundValue + "%");

        DelayTextView=findViewById(R.id.textView4);
        DelayTextView.setText("Shake delay: " + DiceDelayValue);

        shakeTresholdSlider = findViewById(R.id.seekBar);
        shakeTresholdSlider.setOnSeekBarChangeListener(shakeSensibilityListener);
        shakeTresholdSlider.setProgress(ShakeSensibilityValue);

        timeSampleSlider = findViewById(R.id.seekBar2);
        timeSampleSlider.setOnSeekBarChangeListener(timeSampleListener);
        timeSampleSlider.setProgress(TimeSampleValue);

        soundSlider = findViewById(R.id.seekBar3);
        soundSlider.setOnSeekBarChangeListener(soundListener);
        soundSlider.setProgress(SoundValue);

        delaySlider = findViewById(R.id.seekBar4);
        delaySlider.setOnSeekBarChangeListener(delayListener);
        delaySlider.setProgress(DiceDelayValue);
    }

    public void restoreDef(View view) {
        int defTreshold=preferences.getInt(KEY_DEF_DICE_TRESHOLD, DEF_DICE_TRAESHOLD);
        int defTime=preferences.getInt(KEY_DEF_TIME_SAMPLE, DEF_TIME_SAMPLE);
        int defSound=preferences.getInt(KEY_DEF_SOUND_VOLUME, DEF_SOUND_VOLUME);
        int defDelay=preferences.getInt(KEY_DEF_DICE_SHAKE_DELAY, DEF_DICE_SHAKE_DELAY);

        editor.putInt(KEY_DICE_TRESHOLD, defTreshold);
        editor.putInt(KEY_TIME_SAMPLE, defTime);
        editor.putInt(KEY_SOUND_VOLUME, defSound);
        editor.putInt(KEY_DICE_SHAKE_DELAY, defDelay);
        editor.commit();

        ShakeSensibilityValue=defTreshold;
        TimeSampleValue=defTime;
        SoundValue=defSound;
        DiceDelayValue=defDelay;

        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");
        SoundTextView.setText("Sound: " + SoundValue + "%");
        DelayTextView.setText("Shake delay: " + DiceDelayValue);

        shakeTresholdSlider.setProgress(ShakeSensibilityValue);
        timeSampleSlider.setProgress(TimeSampleValue);
        soundSlider.setProgress(SoundValue);
        delaySlider.setProgress(DiceDelayValue);
    }
}
