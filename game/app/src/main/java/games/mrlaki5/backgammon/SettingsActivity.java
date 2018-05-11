package games.mrlaki5.backgammon;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    public static int MAX_SOUND_VOLUME=100;

    private int ShakeSensibilityValue=0;
    private int TimeSampleValue=0;
    private int SoundValue=0;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private SeekBar shakeTresholdSlider;
    private SeekBar timeSampleSlider;
    private SeekBar soundSlider;

    private TextView ShakeSensibilityTextView;
    private TextView TimeSampleTextView;
    private TextView SoundTextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("Settings", 0);
        ShakeSensibilityValue=preferences.getInt("sensor_sensibility", 400);
        TimeSampleValue=preferences.getInt("sample_time", 100);
        SoundValue=preferences.getInt("sound", 80);
        editor=preferences.edit();

        ShakeSensibilityTextView=findViewById(R.id.textView);
        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);

        TimeSampleTextView=findViewById(R.id.textView2);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");

        SoundTextView=findViewById(R.id.textView3);
        SoundTextView.setText("Sound: " + SoundValue + "%");

        shakeTresholdSlider = findViewById(R.id.seekBar);
        shakeTresholdSlider.setOnSeekBarChangeListener(shakeSensibilityListener);
        shakeTresholdSlider.setProgress(ShakeSensibilityValue);

        timeSampleSlider = findViewById(R.id.seekBar2);
        timeSampleSlider.setOnSeekBarChangeListener(timeSampleListener);
        timeSampleSlider.setProgress(TimeSampleValue);

        soundSlider = findViewById(R.id.seekBar3);
        soundSlider.setOnSeekBarChangeListener(soundListener);
        soundSlider.setProgress(SoundValue);
    }

    public void restoreDef(View view) {
        int defTreshold=preferences.getInt("defSensor_sensibility", 400);
        int defTime=preferences.getInt("defSample_time", 100);
        int defSound=preferences.getInt("defSound", 80);

        editor.putInt("sensor_sensibility", defTreshold);
        editor.putInt("sample_time", defTime);
        editor.putInt("sound", defSound);
        editor.commit();

        ShakeSensibilityValue=defTreshold;
        TimeSampleValue=defTime;
        SoundValue=defSound;

        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");
        SoundTextView.setText("Sound: " + SoundValue + "%");

        shakeTresholdSlider.setProgress(ShakeSensibilityValue);
        timeSampleSlider.setProgress(TimeSampleValue);
        soundSlider.setProgress(SoundValue);
    }
}
