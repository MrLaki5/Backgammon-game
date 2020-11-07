package games.mrlaki5.backgammon.Menus;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import games.mrlaki5.backgammon.Beans.ScoreElem;
import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.R;

//Activity for showing scores of all duels
public class ScoresActivity extends AppCompatActivity {

    //Database helper
    DbHelper Helper=null;
    //List view used for representation of scores
    ListView myList;

    //Method called on creation of ScoresActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scores);
        //Create database helper
        Helper=new DbHelper(this);
        //Find list on view
        myList=(ListView) findViewById(R.id.scoreList);
        //Load results to list on view
        loadAdapter();
        //Set listener to list
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get player names of clicked element
                String p1Name=((TextView)view.findViewById(R.id.sListP1Name)).getText().toString();
                String p2Name=((TextView)view.findViewById(R.id.sListP2Name)).getText().toString();
                //Create and start new ResultActivity for showing results for those two players
                Intent intent=new Intent(ScoresActivity.this, ResultsActivity.class);
                intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, p1Name);
                intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, p2Name);
                startActivity(intent);
            }
        });
    }

    //Method used to delete all scores from database
    public void resetScores(View view) {
        //Create delete query and execute it
        SQLiteDatabase db=Helper.getWritableDatabase();
        db.delete(ScoresTableEntry.TABLE_NAME, null, null);
        loadAdapter();
    }

    //Method called to load view list with data
    public void loadAdapter(){
        //Create sql query to get all data from database
        SQLiteDatabase db = Helper.getReadableDatabase();
        String[] columnsRet={ScoresTableEntry.COLUMN_PLAYER1_WIN,
                ScoresTableEntry.COLUMN_PLAYER2_WIN,
                ScoresTableEntry.COLUMN_END_GAME_TIME,
                ScoresTableEntry.COLUMN_PLAYER1_NAME,
                ScoresTableEntry.COLUMN_PLAYER2_NAME,
                ScoresTableEntry._ID};
        //Execute query and get cursor
        Cursor cursor=db.query(ScoresTableEntry.TABLE_NAME, columnsRet,
                null, null,
                null,null,null);
        //Initialize empty score data list
        ArrayList<ScoreElem> scoreList= new ArrayList<ScoreElem>();
        //Go through cursor (database scores) and add every different player
        // combination and their scores to score data list
        while(cursor.moveToNext()){
            //Get player names and scores
            String tmpName1=cursor.getString(cursor.getColumnIndex(
                    ScoresTableEntry.COLUMN_PLAYER1_NAME));
            String tmpName2=cursor.getString(cursor.getColumnIndex(
                    ScoresTableEntry.COLUMN_PLAYER2_NAME));
            int tmpScore1=cursor.getInt(cursor.getColumnIndex(
                    ScoresTableEntry.COLUMN_PLAYER1_WIN));
            int tmpScore2=cursor.getInt(cursor.getColumnIndex(
                    ScoresTableEntry.COLUMN_PLAYER2_WIN));
            boolean elemFound=false;
            //Go through score data list and check where to add score
            for (ScoreElem elem: scoreList) {
                if(elem.checkAndAdd(tmpName1, tmpName2, tmpScore1, tmpScore2)){
                    elemFound=true;
                    break;
                }
            }
            //If element was not found its new combination so add it like new element
            if(!elemFound){
                scoreList.add(new ScoreElem(tmpName1, tmpName2, tmpScore1, tmpScore2));
            }
        }
        //Close cursor
        cursor.close();
        //Create score adapter with data score list
        ScoresAdapter adapter=new ScoresAdapter(ScoresActivity.this, scoreList);
        //Add score adapter to view list
        myList.setAdapter(adapter);
    }
}
