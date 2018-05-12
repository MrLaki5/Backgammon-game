package games.mrlaki5.backgammon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        if(!preferences.contains("sensor_sensibility")){
            SharedPreferences.Editor editor=preferences.edit();
            editor.putInt("sensor_sensibility", SettingsActivity.DEF_DICE_TRAESHOLD);
            editor.putInt("sample_time", SettingsActivity.DEF_TIME_SAMPLE);
            editor.putInt("sound", SettingsActivity.DEF_SOUND_VOLUME);
            editor.putInt("delay", SettingsActivity.DEF_DICE_SHAKE_DELAY);
            editor.putInt("defSensor_sensibility", SettingsActivity.DEF_DICE_TRAESHOLD);
            editor.putInt("defSample_time", SettingsActivity.DEF_TIME_SAMPLE);
            editor.putInt("defSound", SettingsActivity.DEF_SOUND_VOLUME);
            editor.putInt("delay", SettingsActivity.DEF_DICE_SHAKE_DELAY);
            editor.commit();
        }
    }

    public void startNewGame(View view) {
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        View mView= getLayoutInflater().inflate(R.layout.meny_dialog, null);
        mBulder.setView(mView);
        AlertDialog dialog=mBulder.create();
        dialog.show();
        //Intent intent= new Intent(MenuActivity.this, GameActivity.class);
        //startActivity(intent);
    }

    public void OpenSettings(View view) {
        Intent intent= new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
