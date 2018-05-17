package games.mrlaki5.backgammon;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.GameControllers.GameActivity;

public class MenuActivity extends AppCompatActivity {

    public static String EXTRA_PLAYER1_NAME="p1name";
    public static String EXTRA_PLAYER2_NAME="p2name";
    public static String EXTRA_PLAYER1_KIND="p1kind";
    public static String EXTRA_PLAYER2_KIND="p2kind";
    public static String EXTRA_WINING_PLAYER="pWin";

    public static String GAME_CONTINUE_SAVE_FILE_NAME="gameSave";
    public static final int GAME_PRESSED_BACK=55;
    public static final int GAME_ENDED_OK=56;
    public static final int REQUEST_CODE_GAME=65;

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

        checkAndChangeButtonColor();
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
                        File file=new File(MenuActivity.this.getFilesDir().getAbsolutePath(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
                        file.delete();
                        Intent intent= new Intent(MenuActivity.this, GameActivity.class);
                        intent.putExtra(EXTRA_PLAYER1_NAME, playerName1);
                        intent.putExtra(EXTRA_PLAYER2_NAME, playerName2);
                        intent.putExtra(EXTRA_PLAYER1_KIND, playerKind1);
                        intent.putExtra(EXTRA_PLAYER2_KIND, playerKind2);
                        startActivityForResult(intent, REQUEST_CODE_GAME);
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CODE_GAME:
                switch(resultCode){
                    case GAME_PRESSED_BACK:
                        checkAndChangeButtonColor();
                        break;
                    case GAME_ENDED_OK:
                        Bundle extras = data.getExtras();
                        checkAndChangeButtonColor();
                        if(extras!=null) {
                            DbHelper helper = new DbHelper(MenuActivity.this);
                            SQLiteDatabase db = helper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(ScoresTableEntry.COLUMN_PLAYER1_NAME,
                                    extras.getString(EXTRA_PLAYER1_NAME));
                            values.put(ScoresTableEntry.COLUMN_PLAYER2_NAME,
                                    extras.getString(EXTRA_PLAYER2_NAME));
                            if (extras.getInt(EXTRA_WINING_PLAYER) == 1) {
                                values.put(ScoresTableEntry.COLUMN_PLAYER1_WIN, 1);
                                values.put(ScoresTableEntry.COLUMN_PLAYER2_WIN, 0);
                            } else {
                                values.put(ScoresTableEntry.COLUMN_PLAYER1_WIN, 0);
                                values.put(ScoresTableEntry.COLUMN_PLAYER2_WIN, 1);
                            }
                            Date currDate = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                            values.put(ScoresTableEntry.COLUMN_END_GAME_TIME, format.format(currDate));
                            db.insert(ScoresTableEntry.TABLE_NAME, null, values);
                            Intent intent=new Intent(MenuActivity.this, ResultsActivity.class);
                            intent.putExtra(EXTRA_PLAYER1_NAME, extras.getString(EXTRA_PLAYER1_NAME));
                            intent.putExtra(EXTRA_PLAYER2_NAME, extras.getString(EXTRA_PLAYER2_NAME));
                            startActivity(intent);
                        }
                        break;
                }
                break;
        }
    }

    public boolean checkContinueGame(){
        File file=new File(this.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        if(file.exists()){
            return true;
        }
        return false;
    }

    public void checkAndChangeButtonColor(){
        Button button=((Button) findViewById(R.id.continueGame));
        if(checkContinueGame()){
            button.setTextColor(Color.rgb(217, 253, 223));
        }
        else{
            button.setTextColor(Color.rgb(130, 135, 131));
        }
    }

    public void continueGame(View view) {
        if(checkContinueGame()){
            Intent intent= new Intent(MenuActivity.this, GameActivity.class);
            startActivityForResult(intent, REQUEST_CODE_GAME);
        }
    }

    public void scores(View view) {
        Intent intent= new Intent(MenuActivity.this, ScoresActivity.class);
        startActivity(intent);
    }
}
