package games.mrlaki5.backgammon.Menus;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.R;

//Activity for showing results of games played by two same players
public class ResultsActivity extends AppCompatActivity {

    //Player1 name
    String Player1="";
    //Player2 name
    String Player2="";
    //Number of player1 wins
    int Player1Wins=0;
    //Number of player2 wins
    int Player2Wins=0;
    //Database helper
    DbHelper Helper=null;
    //List view used for representation of game results
    ListView myList;

    //Binder used to map data from results to view
    SimpleCursorAdapter.ViewBinder binder=new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            //Check which part of database results is up
            switch(cursor.getColumnName(columnIndex)){
                //Player names
                case ScoresTableEntry.COLUMN_PLAYER1_NAME:
                case ScoresTableEntry.COLUMN_PLAYER2_NAME:
                    ((TextView) view).setText(cursor.getString(columnIndex)+": ");
                    break;
                //Player scores
                case ScoresTableEntry.COLUMN_PLAYER1_WIN:
                case ScoresTableEntry.COLUMN_PLAYER2_WIN:
                    TextView tempTW=(TextView) view;
                    //Set text and color of text depending on win or lose
                    if(cursor.getInt(columnIndex)==1){
                        tempTW.setText("WIN");
                        tempTW.setTextColor(Color.rgb(52, 158, 70));
                    }
                    else{
                        tempTW.setText("LOSE");
                        tempTW.setTextColor(Color.rgb(212, 31, 38));
                    }
                    break;
                //Date of end game time
                case ScoresTableEntry.COLUMN_END_GAME_TIME:
                    ((TextView) view).setText("Time: "+cursor.getString(columnIndex));
                    break;
            }
            return true;
        }
    };

    //Method called on creation of ResultsActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_results);
        //Create database helper
        Helper=new DbHelper(this);
        //Find list on view
        myList=(ListView) findViewById(R.id.resultList);
        //Get data sent by starting activity
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            //Save player names
            Player1=extras.getString(MenuActivity.EXTRA_PLAYER1_NAME);
            Player2=extras.getString(MenuActivity.EXTRA_PLAYER2_NAME);
            //Load results to list on view
            loadAdapter();
        }
    }

    //Method called to reset results for games between those two players
    public void resetResults(View view) {
        //Create sql query for deleting all rows for results of games with current players
        SQLiteDatabase db=Helper.getWritableDatabase();
        String whereQ="( ? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+" ) "+
                " OR ( ? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+" )";
        String[] whereFill={Player1, Player2, Player1, Player2};
        //Run query
        db.delete(ScoresTableEntry.TABLE_NAME, whereQ, whereFill);
        //Refresh list view
        loadAdapter();
    }

    //Method called to load view list with data from database
    public void loadAdapter(){
        //Get database instance
        SQLiteDatabase db = Helper.getReadableDatabase();
        //String with all fields loaded from database (_ID must be in)
        String[] columnsRet={ScoresTableEntry.COLUMN_PLAYER1_WIN,
                ScoresTableEntry.COLUMN_PLAYER2_WIN,
                ScoresTableEntry.COLUMN_END_GAME_TIME,
                ScoresTableEntry.COLUMN_PLAYER1_NAME,
                ScoresTableEntry.COLUMN_PLAYER2_NAME,
                ScoresTableEntry._ID};
        //Where part of sql query, find results for games with those two players
        String whereQ="( ? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+" ) "+
                " OR ( ? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+" )";
        //Fill Where part with dynamic parts of where query (player names)
        String[] whereFill={Player1, Player2, Player1, Player2};
        //Get list elem view fields in which database information will be printed
        int[] fields={R.id.rListP1State, R.id.rListP2State, R.id.rListDate,
                R.id.rListP1Name, R.id.rListP2Name, R.id.rListDummuText};
        //Execute query and get cursor with results
        Cursor cursor=db.query(ScoresTableEntry.TABLE_NAME, columnsRet, whereQ, whereFill,
                null,null,null);
        //Create adapter with cursor which is used for loading cursor data to view list with binder
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this, R.layout.result_list,
                cursor, columnsRet, fields, 0);
        //Set binder to adapter
        adapter.setViewBinder(binder);
        //Set adapter to list
        myList.setAdapter(adapter);
        //Get second cursor with same data
        Cursor cursor2=db.query(ScoresTableEntry.TABLE_NAME, columnsRet, whereQ, whereFill,
                null,null,null);
        //Init player wins to 0
        Player1Wins=0;
        Player2Wins=0;
        //Go through data and find in every result which player won
        while(cursor2.moveToNext()){
            if(cursor2.getInt(cursor2.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_WIN))==1){
                if(cursor2.getString(cursor2.getColumnIndex(
                        ScoresTableEntry.COLUMN_PLAYER1_NAME)).equals(Player1)){
                    Player1Wins++;
                }
                else{
                    Player2Wins++;
                }
            }
            else{
                if(cursor2.getString(cursor2.getColumnIndex(
                        ScoresTableEntry.COLUMN_PLAYER1_NAME)).equals(Player1)){
                    Player2Wins++;
                }
                else{
                    Player1Wins++;
                }
            }
        }
        //Close second cursor
        cursor2.close();
        //Set view with overall wins
        String tempStr=Player1+" "+Player1Wins+":"+Player2Wins+" "+Player2;
        ((TextView) findViewById(R.id.mainResultsText)).setText(tempStr);
    }
}
