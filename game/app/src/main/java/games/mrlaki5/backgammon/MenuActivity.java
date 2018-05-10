package games.mrlaki5.backgammon;

import android.content.Intent;
import android.content.SharedPreferences;
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
            editor.putInt("sensor_sensibility", 400);
            editor.putInt("sample_time", 100);
            editor.putInt("sound", 80);
            editor.putInt("defSensor_sensibility", 400);
            editor.putInt("defSample_time", 100);
            editor.putInt("defSound", 80);
            editor.commit();
        }
    }

    public void startNewGame(View view) {
        Intent intent= new Intent(MenuActivity.this, GameActivity.class);
        startActivity(intent);
    }

    public void OpenSettings(View view) {
        Intent intent= new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
