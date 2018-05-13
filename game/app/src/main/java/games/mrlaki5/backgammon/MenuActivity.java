package games.mrlaki5.backgammon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MenuActivity extends AppCompatActivity {

    public static String EXTRA_PLAYER1_NAME="p1name";
    public static String EXTRA_PLAYER2_NAME="p2name";
    public static String EXTRA_PLAYER1_KIND="p1kind";
    public static String EXTRA_PLAYER2_KIND="p2kind";

    private AlertDialog myDialog;
    private View myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        if(!preferences.contains(SettingsActivity.KEY_DICE_TRESHOLD)){
            SharedPreferences.Editor editor=preferences.edit();
            editor.putInt(SettingsActivity.KEY_DICE_TRESHOLD, SettingsActivity.DEF_DICE_TRAESHOLD);
            editor.putInt(SettingsActivity.KEY_TIME_SAMPLE, SettingsActivity.DEF_TIME_SAMPLE);
            editor.putInt(SettingsActivity.KEY_SOUND_VOLUME, SettingsActivity.DEF_SOUND_VOLUME);
            editor.putInt(SettingsActivity.KEY_DICE_SHAKE_DELAY, SettingsActivity.DEF_DICE_SHAKE_DELAY);
            editor.putInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS, SettingsActivity.DEF_TIME_BETWEEN_TURNS);

            editor.putInt(SettingsActivity.KEY_DEF_DICE_TRESHOLD, SettingsActivity.DEF_DICE_TRAESHOLD);
            editor.putInt(SettingsActivity.KEY_DEF_TIME_SAMPLE, SettingsActivity.DEF_TIME_SAMPLE);
            editor.putInt(SettingsActivity.KEY_DEF_SOUND_VOLUME, SettingsActivity.DEF_SOUND_VOLUME);
            editor.putInt(SettingsActivity.KEY_DEF_DICE_SHAKE_DELAY, SettingsActivity.DEF_DICE_SHAKE_DELAY);
            editor.putInt(SettingsActivity.KEY_DEF_TIME_BETWEEN_TURNS, SettingsActivity.DEF_TIME_BETWEEN_TURNS);
            editor.commit();
        }
    }

    public void startNewGame(View view) {
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        myView= getLayoutInflater().inflate(R.layout.meny_dialog, null);
        ((Button) myView.findViewById(R.id.dialogCancel)).setOnClickListener(CancelListener);
        ((Button) myView.findViewById(R.id.dialogPlay)).setOnClickListener(PlayListener);
        mBulder.setView(myView);
        myDialog=mBulder.create();
        myDialog.show();
    }

    public void OpenSettings(View view) {
        Intent intent= new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private View.OnClickListener CancelListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myDialog != null){
                myDialog.dismiss();
                myDialog = null;
                myView=null;
            }
        }
    };

    private View.OnClickListener PlayListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myDialog != null && myView!=null){
                String playerName1=((EditText)myView.findViewById(R.id.dialogPName1))
                        .getText().toString();
                String playerName2=((EditText)myView.findViewById(R.id.dialogPName2))
                        .getText().toString();
                if((!playerName1.isEmpty()) && (!playerName2.isEmpty())){

                    int idRGp1=((RadioGroup)myView.findViewById(R.id.dialogRadioGP1))
                            .getCheckedRadioButtonId();

                    int idRGp2=((RadioGroup)myView.findViewById(R.id.dialogRadioGP2))
                            .getCheckedRadioButtonId();

                    if(idRGp1!=-1 && idRGp2!=-1){
                        String playerKind1=((RadioButton) myView.findViewById(idRGp1))
                                .getText().toString();
                        String playerKind2=((RadioButton) myView.findViewById(idRGp2))
                                .getText().toString();
                        myDialog.dismiss();
                        myDialog = null;
                        myView=null;
                        Intent intent= new Intent(MenuActivity.this, GameActivity.class);
                        intent.putExtra(EXTRA_PLAYER1_NAME, playerName1);
                        intent.putExtra(EXTRA_PLAYER2_NAME, playerName2);
                        intent.putExtra(EXTRA_PLAYER1_KIND, playerKind1);
                        intent.putExtra(EXTRA_PLAYER2_KIND, playerKind2);
                        startActivity(intent);
                    }
                }
            }
        }
    };
}
