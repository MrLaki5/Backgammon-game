package games.mrlaki5.backgammon;

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

public class ResultsActivity extends AppCompatActivity {

    String Player1="";
    String Player2="";
    int Player1Wins=0;
    int Player2Wins=0;
    DbHelper Helper=null;

    ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_results);

        Helper=new DbHelper(this);
        myList=(ListView) findViewById(R.id.resultList);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            Player1=extras.getString(MenuActivity.EXTRA_PLAYER1_NAME);
            Player2=extras.getString(MenuActivity.EXTRA_PLAYER2_NAME);
            loadAdapter();
        }
    }

    public void resetResults(View view) {
        SQLiteDatabase db=Helper.getWritableDatabase();
        String whereQ="( ? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+" ) "+
                " OR ( ? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+" )";

        String[] whereFill={Player1, Player2, Player1, Player2};
        db.delete(ScoresTableEntry.TABLE_NAME, whereQ, whereFill);
        loadAdapter();
    }

    public void loadAdapter(){
        SQLiteDatabase db = Helper.getReadableDatabase();
        String[] columnsRet={ScoresTableEntry.COLUMN_PLAYER1_WIN,
                ScoresTableEntry.COLUMN_PLAYER2_WIN,
                ScoresTableEntry.COLUMN_END_GAME_TIME,
                ScoresTableEntry.COLUMN_PLAYER1_NAME,
                ScoresTableEntry.COLUMN_PLAYER2_NAME,
                ScoresTableEntry._ID};

        String whereQ="( ? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+" ) "+
                " OR ( ? = "+ScoresTableEntry.COLUMN_PLAYER2_NAME+
                " AND "+"? = "+ScoresTableEntry.COLUMN_PLAYER1_NAME+" )";

        String[] whereFill={Player1, Player2, Player1, Player2};

        int[] fields={R.id.rListP1State, R.id.rListP2State, R.id.rListDate,
                R.id.rListP1Name, R.id.rListP2Name, R.id.rListDummuText};

        Cursor cursor=db.query(ScoresTableEntry.TABLE_NAME, columnsRet, whereQ, whereFill,
                null,null,null);
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this, R.layout.result_list,
                cursor, columnsRet, fields, 0);

        adapter.setViewBinder(binder);
        myList.setAdapter(adapter);

        Cursor cursor2=db.query(ScoresTableEntry.TABLE_NAME, columnsRet, whereQ, whereFill,
                null,null,null);

        Player1Wins=0;
        Player2Wins=0;
        while(cursor2.moveToNext()){
            if(cursor2.getInt(cursor2.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_WIN))==1){
                if(cursor2.getString(cursor2.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_NAME)).equals(Player1)){
                    Player1Wins++;
                }
                else{
                    Player2Wins++;
                }
            }
            else{
                if(cursor2.getString(cursor2.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_NAME)).equals(Player1)){
                    Player2Wins++;
                }
                else{
                    Player1Wins++;
                }
            }
        }
        cursor2.close();
        String tempStr=Player1+" "+Player1Wins+":"+Player2Wins+" "+Player2;
        ((TextView) findViewById(R.id.mainResultsText)).setText(tempStr);
    }

    SimpleCursorAdapter.ViewBinder binder=new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch(cursor.getColumnName(columnIndex)){
                case ScoresTableEntry.COLUMN_PLAYER1_NAME:
                case ScoresTableEntry.COLUMN_PLAYER2_NAME:
                    ((TextView) view).setText(cursor.getString(columnIndex)+": ");
                    break;
                case ScoresTableEntry.COLUMN_PLAYER1_WIN:
                case ScoresTableEntry.COLUMN_PLAYER2_WIN:
                    TextView tempTW=(TextView) view;
                    if(cursor.getInt(columnIndex)==1){
                        tempTW.setText("WIN");
                        tempTW.setTextColor(Color.rgb(52, 158, 70));
                    }
                    else{
                        tempTW.setText("LOSE");
                        tempTW.setTextColor(Color.rgb(212, 31, 38));
                    }
                    break;
                case ScoresTableEntry.COLUMN_END_GAME_TIME:
                    ((TextView) view).setText("Time: "+cursor.getString(columnIndex));
                    break;
            }
            return true;
        }
    };
}
