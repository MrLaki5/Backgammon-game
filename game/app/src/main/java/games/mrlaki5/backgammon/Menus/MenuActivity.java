package games.mrlaki5.backgammon.Menus;

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
import games.mrlaki5.backgammon.R;

//Activity class for main menu
public class MenuActivity extends AppCompatActivity {

    //Player1 name key intent value
    public static String EXTRA_PLAYER1_NAME="p1name";
    //Player2 name key intent value
    public static String EXTRA_PLAYER2_NAME="p2name";
    //Player1 kind key intent value
    public static String EXTRA_PLAYER1_KIND="p1kind";
    //Player2 kind key intent value
    public static String EXTRA_PLAYER2_KIND="p2kind";
    //Wining player key intent value
    public static String EXTRA_WINING_PLAYER="pWin";
    //Name of save file
    public static String GAME_CONTINUE_SAVE_FILE_NAME="gameSave";
    //Value of return int after game finishes for back pressed
    public static final int GAME_PRESSED_BACK=55;
    //Value of return int after game finishes for player won
    public static final int GAME_ENDED_OK=56;
    //Value of send int to starting a game
    public static final int REQUEST_CODE_GAME=65;
    //Dialog opened before new game starts
    private AlertDialog myDialog;
    //View of dialog opened before new game starts
    private View myView;

    //Listener used to catch cancel button click on new game dialog
    private View.OnClickListener CancelListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Delete dialog
            if (myDialog != null){
                myDialog.dismiss();
                myDialog = null;
                myView=null;
            }
        }
    };

    //Listener used to catch play button click on new game dialog
    private View.OnClickListener PlayListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myDialog != null && myView!=null){
                //Load player1 name and player2 name from dialog
                String playerName1=((EditText)myView.findViewById(R.id.dialogPName1))
                        .getText().toString();
                String playerName2=((EditText)myView.findViewById(R.id.dialogPName2))
                        .getText().toString();
                //If player names are written
                if((!playerName1.isEmpty()) && (!playerName2.isEmpty())){
                    //Load id of checked radio button of player kind
                    int idRGp1=((RadioGroup)myView.findViewById(R.id.dialogRadioGP1))
                            .getCheckedRadioButtonId();
                    int idRGp2=((RadioGroup)myView.findViewById(R.id.dialogRadioGP2))
                            .getCheckedRadioButtonId();
                    //If player kinds are chosen
                    if(idRGp1!=-1 && idRGp2!=-1){
                        //Load player kinds from dialog view
                        String playerKind1=((RadioButton) myView.findViewById(idRGp1))
                                .getText().toString();
                        String playerKind2=((RadioButton) myView.findViewById(idRGp2))
                                .getText().toString();
                        //Delete dialog
                        myDialog.dismiss();
                        myDialog = null;
                        myView=null;
                        //If file with saved game exists, delete it
                        File file=new File(MenuActivity.this.getFilesDir().getAbsolutePath(),
                                MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
                        file.delete();
                        //Start game activity with new players
                        Intent intent=
                                new Intent(MenuActivity.this, GameActivity.class);
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

    //Method called on creation of MenuActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
        //Load preferences
        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        //If values in preferences dont exist (on first start), create them
        if(!preferences.contains(SettingsActivity.KEY_DICE_TRESHOLD)){
            SharedPreferences.Editor editor=preferences.edit();
            //Set shake treshold value
            editor.putInt(SettingsActivity.KEY_DICE_TRESHOLD,
                    SettingsActivity.DEF_DICE_TRAESHOLD);
            //Set time value between two shake sensor events
            editor.putInt(SettingsActivity.KEY_TIME_SAMPLE,
                    SettingsActivity.DEF_TIME_SAMPLE);
            //Set value of sound volume
            editor.putInt(SettingsActivity.KEY_SOUND_VOLUME,
                    SettingsActivity.DEF_SOUND_VOLUME);
            //Set value of shake delays
            editor.putInt(SettingsActivity.KEY_DICE_SHAKE_DELAY,
                    SettingsActivity.DEF_DICE_SHAKE_DELAY);
            //Set value of time between turns in game
            editor.putInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS,
                    SettingsActivity.DEF_TIME_BETWEEN_TURNS);
            //Set default shake treshold value
            editor.putInt(SettingsActivity.KEY_DEF_DICE_TRESHOLD,
                    SettingsActivity.DEF_DICE_TRAESHOLD);
            //Set default time value between two shake sensor events
            editor.putInt(SettingsActivity.KEY_DEF_TIME_SAMPLE,
                    SettingsActivity.DEF_TIME_SAMPLE);
            //Set default value of sound volume
            editor.putInt(SettingsActivity.KEY_DEF_SOUND_VOLUME,
                    SettingsActivity.DEF_SOUND_VOLUME);
            //Set default value of shake delays
            editor.putInt(SettingsActivity.KEY_DEF_DICE_SHAKE_DELAY,
                    SettingsActivity.DEF_DICE_SHAKE_DELAY);
            //Set default value of time between turns in game
            editor.putInt(SettingsActivity.KEY_DEF_TIME_BETWEEN_TURNS,
                    SettingsActivity.DEF_TIME_BETWEEN_TURNS);
            editor.commit();
        }
        //Change color of continue game button from gray to yellow if save file exists
        checkAndChangeButtonColor();
    }

    //Method called when new game is chosen
    public void startNewGame(View view) {
        //Create dialog
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        //Load dialog view
        myView= getLayoutInflater().inflate(R.layout.meny_dialog, null);
        //Add to buttons on dialog view click listeners
        ((Button) myView.findViewById(R.id.dialogCancel)).setOnClickListener(CancelListener);
        ((Button) myView.findViewById(R.id.dialogPlay)).setOnClickListener(PlayListener);
        //Set view of dialog
        mBulder.setView(myView);
        //Create and show dialog
        myDialog=mBulder.create();
        myDialog.show();
    }

    //Method called when settings is chosen
    public void OpenSettings(View view) {
        //Create and start settings activity
        Intent intent= new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    //Method called when continue game is chosen
    public void continueGame(View view) {
        //If save file exists start game activity with saved model
        if(checkContinueGame()){
            Intent intent= new Intent(MenuActivity.this, GameActivity.class);
            startActivityForResult(intent, REQUEST_CODE_GAME);
        }
    }

    //Method called when scores is chosen
    public void scores(View view) {
        //Create and start scores activity
        Intent intent= new Intent(MenuActivity.this, ScoresActivity.class);
        startActivity(intent);
    }

    //Method called to check if save file exists
    public boolean checkContinueGame(){
        //Open file and check
        File file=new File(this.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        if(file.exists()){
            return true;
        }
        return false;
    }

    //Method called to set continue button color depending on save file
    public void checkAndChangeButtonColor(){
        Button button=((Button) findViewById(R.id.continueGame));
        //If file exists set red color, if not, set grey color
        if(checkContinueGame()){
            button.setTextColor(Color.rgb(217, 253, 223));
        }
        else{
            button.setTextColor(Color.rgb(130, 135, 131));
        }
    }

    //Method called on return from finished child activity
    //  will be called when activity started with startActivityForResult(...
    //  will not be called when activity started with startActivity(...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check request code
        switch(requestCode){
            //If request code equals with start game code
            case REQUEST_CODE_GAME:
                //Check result code
                switch(resultCode){
                    //If result code is back pressed
                    case GAME_PRESSED_BACK:
                        //Set color of continue button
                        checkAndChangeButtonColor();
                        break;
                    //If result code is player won
                    case GAME_ENDED_OK:
                        //Get data of which player won
                        Bundle extras = data.getExtras();
                        //Set color of continue button
                        checkAndChangeButtonColor();
                        if(extras!=null) {
                            //Save game result data into database
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
                            SimpleDateFormat format =
                                    new SimpleDateFormat("HH:mm dd/MM/yyyy");
                            values.put(ScoresTableEntry.COLUMN_END_GAME_TIME,
                                    format.format(currDate));
                            db.insert(ScoresTableEntry.TABLE_NAME, null, values);
                            //Create and start activity for showing game results
                            Intent intent=new Intent(MenuActivity.this,
                                    ResultsActivity.class);
                            intent.putExtra(EXTRA_PLAYER1_NAME,
                                    extras.getString(EXTRA_PLAYER1_NAME));
                            intent.putExtra(EXTRA_PLAYER2_NAME,
                                    extras.getString(EXTRA_PLAYER2_NAME));
                            startActivity(intent);
                        }
                        break;
                }
                break;
        }
    }
}
