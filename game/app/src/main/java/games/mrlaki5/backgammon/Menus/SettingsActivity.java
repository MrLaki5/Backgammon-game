package games.mrlaki5.backgammon.Menus;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import games.mrlaki5.backgammon.R;

//Activity for showing settings
public class SettingsActivity extends AppCompatActivity {

    //Volume key preference value
    public static String KEY_SOUND_VOLUME="volume";
    //Shake treshold key preference value
    public static String KEY_DICE_TRESHOLD="sensor_sensibility";
    //Time between two shake sensor events key preference value
    public static String KEY_TIME_SAMPLE="sample_time";
    //Shake delays key preference value
    public static String KEY_DICE_SHAKE_DELAY="delay";
    //Time between turns in game key preference value
    public static String KEY_TIME_BETWEEN_TURNS="turnBTime";
    //Volume default key preference value
    public static String KEY_DEF_SOUND_VOLUME="DEFvolume";
    //Shake default treshold key preference value
    public static String KEY_DEF_DICE_TRESHOLD="DEFsensor_sensibility";
    //Time default between two shake sensor events key preference value
    public static String KEY_DEF_TIME_SAMPLE="DEFsample_time";
    //Shake default delays key preference value
    public static String KEY_DEF_DICE_SHAKE_DELAY="DEFdelay";
    //Time default between turns in game key preference value
    public static String KEY_DEF_TIME_BETWEEN_TURNS="DEFturnBTime";
    //Volume max preference value
    public static int MAX_SOUND_VOLUME=100;
    //Shake treshold max preference value
    public static int MAX_DICE_TRESHOLD=1000;
    //Time between two shake sensor events max preference value
    public static int MAX_TIME_SAMPLE=200;
    //Shake delays max preference value
    public static int MAX_DICE_SHAKE_DELAY=15;
    //Time between turns in game max preference value
    public static int MAX_TIME_BETWEEN_TURNS=5;
    //Volume default preference value
    public static int DEF_SOUND_VOLUME=80;
    //Shake treshold default preference value
    public static int DEF_DICE_TRAESHOLD=550;
    //Time between two shake sensor events default preference value
    public static int DEF_TIME_SAMPLE=70;
    //Shake delays default preference value
    public static int DEF_DICE_SHAKE_DELAY=4;
    //Time between turns in game default preference value
    public static int DEF_TIME_BETWEEN_TURNS=1;
    //Shake treshold current preference value
    private int ShakeSensibilityValue=0;
    //Time between two shake sensor events current preference value
    private int TimeSampleValue=0;
    //Volume current preference value
    private int SoundValue=0;
    //Shake delays current preference value
    private int DiceDelayValue=0;
    //Time between turns in game current preference value
    private int TimeBTurnsValue=0;
    //Shared preferences where settings are stored
    private SharedPreferences preferences;
    //Shared preferences editor used for editing preferences
    private SharedPreferences.Editor editor;
    //Shake treshold slider
    private SeekBar shakeTresholdSlider;
    //Time between two shake sensor events slider
    private SeekBar timeSampleSlider;
    //Volume slider
    private SeekBar soundSlider;
    //Shake delays slider
    private SeekBar delaySlider;
    //Time between turns slider
    private SeekBar turnsSlider;
    //Shake treshold Text View
    private TextView ShakeSensibilityTextView;
    //Time between two shake sensor events Text View
    private TextView TimeSampleTextView;
    //Volume Text View
    private TextView SoundTextView;
    //Shake delays Text View
    private TextView DelayTextView;
    //Time between turns Text View
    private TextView TurnsTextView;

    //Slider listener for shake treshol
    private SeekBar.OnSeekBarChangeListener shakeSensibilityListener =
            new SeekBar.OnSeekBarChangeListener() {

        //Method called when user moves slider
        // updated continuously as the user slides the thumb
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Set new value to text view
            ShakeSensibilityTextView.setText("Shake threshold: " + progress);
            //Set new value to current field
            ShakeSensibilityValue=progress;
        }

        //Method called when the user first touches the SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Blank
        }

        //Method called after the user finishes moving the SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Update preference with new value
            editor.putInt(KEY_DICE_TRESHOLD, ShakeSensibilityValue);
            editor.commit();
        }
    };

    //Slider listener for time between two shake sensor events
    private SeekBar.OnSeekBarChangeListener timeSampleListener =
            new SeekBar.OnSeekBarChangeListener() {

        //Method called when user moves slider
        // updated continuously as the user slides the thumb
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Set new value to text view
            TimeSampleTextView.setText("Shake time precision: " + progress +"ms");
            //Set new value to current field
            TimeSampleValue=progress;
        }

        //Method called when the user first touches the SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Blank
        }

        //Method called after the user finishes moving the SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Update preference with new value
            editor.putInt(KEY_TIME_SAMPLE, TimeSampleValue);
            editor.commit();
        }
    };

    //Slider listener for volume
    private SeekBar.OnSeekBarChangeListener soundListener =
            new SeekBar.OnSeekBarChangeListener() {

        //Method called when user moves slider
        // updated continuously as the user slides the thumb
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Set new value to text view
            SoundTextView.setText("Sound: " + progress + "%");
            //Set new value to current field
            SoundValue=progress;
        }

        //Method called when the user first touches the SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Blank
        }

        //Method called after the user finishes moving the SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Update preference with new value
            editor.putInt(KEY_SOUND_VOLUME, SoundValue);
            editor.commit();
        }
    };

    //Slider listener for shake delays
    private SeekBar.OnSeekBarChangeListener delayListener =
            new SeekBar.OnSeekBarChangeListener() {

        //Method called when user moves slider
        // updated continuously as the user slides the thumb
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Set new value to text view
            DelayTextView.setText("Shake delay: " + progress);
            //Set new value to current field
            DiceDelayValue=progress;
        }

        //Method called when the user first touches the SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Blank
        }

        //Method called after the user finishes moving the SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Update preference with new value
            editor.putInt(KEY_DICE_SHAKE_DELAY, DiceDelayValue);
            editor.commit();
        }
    };

    //Slider listener for time between turns
    private SeekBar.OnSeekBarChangeListener turnsListener =
            new SeekBar.OnSeekBarChangeListener() {

        //Method called when user moves slider
        // updated continuously as the user slides the thumb
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Set new value to text view
            TurnsTextView.setText("Time between turns: " + progress +"s");
            //Set new value to current field
            TimeBTurnsValue=progress;
        }

        //Method called when the user first touches the SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Blank
        }

        //Method called after the user finishes moving the SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Update preference with new value
            editor.putInt(KEY_TIME_BETWEEN_TURNS, TimeBTurnsValue);
            editor.commit();
        }
    };

    //Method called on creation of SettingsActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        //Get values of shared preferences (game settings parameters)
        preferences = getSharedPreferences("Settings", 0);
        //Get shake treshold value
        ShakeSensibilityValue=preferences.getInt(KEY_DICE_TRESHOLD, DEF_DICE_TRAESHOLD);
        //Get time value between two shake sensor events
        TimeSampleValue=preferences.getInt(KEY_TIME_SAMPLE, DEF_TIME_SAMPLE);
        //Get value of sound volume
        SoundValue=preferences.getInt(KEY_SOUND_VOLUME, DEF_SOUND_VOLUME);
        //Get value of shake delays
        DiceDelayValue=preferences.getInt(KEY_DICE_SHAKE_DELAY, DEF_DICE_SHAKE_DELAY);
        //Get value of time between turns in game
        TimeBTurnsValue=preferences.getInt(KEY_TIME_BETWEEN_TURNS, DEF_TIME_BETWEEN_TURNS);
        //Create shared preferences editor
        editor=preferences.edit();
        //Find shake treshold TextView on view and set text
        ShakeSensibilityTextView=findViewById(R.id.textView);
        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);
        //Find time value between two shake TextView on view and set text
        TimeSampleTextView=findViewById(R.id.textView2);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");
        //Find sound TextView on view and set text
        SoundTextView=findViewById(R.id.textView3);
        SoundTextView.setText("Sound: " + SoundValue + "%");
        //Find dice delay TextView on view and set text
        DelayTextView=findViewById(R.id.textView4);
        DelayTextView.setText("Shake delay: " + DiceDelayValue);
        //Find time between turns in game TextView on view and set text
        TurnsTextView=findViewById(R.id.textView5);
        TurnsTextView.setText("Time between turns: " + TimeBTurnsValue+"s");
        //Find shake treshold slider on view and set listener and progress on slider
        shakeTresholdSlider = findViewById(R.id.seekBar);
        shakeTresholdSlider.setOnSeekBarChangeListener(shakeSensibilityListener);
        shakeTresholdSlider.setProgress(ShakeSensibilityValue);
        //Find time value between two shake slider on view and set listener and progress on slider
        timeSampleSlider = findViewById(R.id.seekBar2);
        timeSampleSlider.setOnSeekBarChangeListener(timeSampleListener);
        timeSampleSlider.setProgress(TimeSampleValue);
        //Find sound slider on view and set listener and progress on slider
        soundSlider = findViewById(R.id.seekBar3);
        soundSlider.setOnSeekBarChangeListener(soundListener);
        soundSlider.setProgress(SoundValue);
        //Find dice delay slider on view and set listener and progress on slider
        delaySlider = findViewById(R.id.seekBar4);
        delaySlider.setOnSeekBarChangeListener(delayListener);
        delaySlider.setProgress(DiceDelayValue);
        //Find time between turns in game slider on view and set listener and progress on slider
        turnsSlider = findViewById(R.id.seekBar5);
        turnsSlider.setOnSeekBarChangeListener(turnsListener);
        turnsSlider.setProgress(TimeBTurnsValue);
    }

    //Method used for restoring default settings values
    public void restoreDef(View view) {
        //Get default preference values
        //Get shake treshold default value
        int defTreshold=preferences.getInt(KEY_DEF_DICE_TRESHOLD, DEF_DICE_TRAESHOLD);
        //Get time default value between two shake sensor events
        int defTime=preferences.getInt(KEY_DEF_TIME_SAMPLE, DEF_TIME_SAMPLE);
        //Get default value of sound volume
        int defSound=preferences.getInt(KEY_DEF_SOUND_VOLUME, DEF_SOUND_VOLUME);
        //Get default value of shake delays
        int defDelay=preferences.getInt(KEY_DEF_DICE_SHAKE_DELAY, DEF_DICE_SHAKE_DELAY);
        //Get default value of time between turns in game
        int defTurns=preferences.getInt(KEY_DEF_TIME_BETWEEN_TURNS, DEF_TIME_BETWEEN_TURNS);
        //Update current values in preferences to default ones
        editor.putInt(KEY_DICE_TRESHOLD, defTreshold);
        editor.putInt(KEY_TIME_SAMPLE, defTime);
        editor.putInt(KEY_SOUND_VOLUME, defSound);
        editor.putInt(KEY_DICE_SHAKE_DELAY, defDelay);
        editor.putInt(KEY_TIME_BETWEEN_TURNS, defTurns);
        editor.commit();
        //Update current activity values to default ones
        ShakeSensibilityValue=defTreshold;
        TimeSampleValue=defTime;
        SoundValue=defSound;
        DiceDelayValue=defDelay;
        TimeBTurnsValue=defTurns;
        //Update text views to default values
        ShakeSensibilityTextView.setText("Shake threshold: " + ShakeSensibilityValue);
        TimeSampleTextView.setText("Shake time precision: " + TimeSampleValue +"ms");
        SoundTextView.setText("Sound: " + SoundValue + "%");
        DelayTextView.setText("Shake delay: " + DiceDelayValue);
        TurnsTextView.setText("Time between turns: " + TimeBTurnsValue+"s");
        //Update sliders to default values
        shakeTresholdSlider.setProgress(ShakeSensibilityValue);
        timeSampleSlider.setProgress(TimeSampleValue);
        soundSlider.setProgress(SoundValue);
        delaySlider.setProgress(DiceDelayValue);
        turnsSlider.setProgress(TimeBTurnsValue);
    }
}
