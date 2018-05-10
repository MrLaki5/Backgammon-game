package games.mrlaki5.backgammon;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private int ShakeSensibilityValue=0;
    private int TimeSampleValue=0;
    private int SoundValue=0;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

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

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(shakeSensibilityListener);
        seekBar.setProgress(ShakeSensibilityValue);

        SeekBar seekBar2 = findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(timeSampleListener);
        seekBar2.setProgress(TimeSampleValue);

        SeekBar seekBar3 = findViewById(R.id.seekBar3);
        seekBar3.setOnSeekBarChangeListener(soundListener);
        seekBar3.setProgress(SoundValue);
    }
}
