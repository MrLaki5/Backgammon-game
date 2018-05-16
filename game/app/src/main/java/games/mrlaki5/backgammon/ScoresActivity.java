package games.mrlaki5.backgammon;

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
import java.util.List;

import games.mrlaki5.backgammon.Beans.ScoreElem;
import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;

public class ScoresActivity extends AppCompatActivity {

    DbHelper Helper=null;

    ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scores);

        Helper=new DbHelper(this);
        myList=(ListView) findViewById(R.id.scoreList);

        loadAdapter();

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String p1Name=((TextView)view.findViewById(R.id.sListP1Name)).getText().toString();
                String p2Name=((TextView)view.findViewById(R.id.sListP2Name)).getText().toString();
                Intent intent=new Intent(ScoresActivity.this, ResultsActivity.class);
                intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, p1Name);
                intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, p2Name);
                startActivity(intent);
            }
        });
    }

    public void resetScores(View view) {
        SQLiteDatabase db=Helper.getWritableDatabase();
        db.delete(ScoresTableEntry.TABLE_NAME, null, null);
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

        Cursor cursor=db.query(ScoresTableEntry.TABLE_NAME, columnsRet, null, null,
                null,null,null);

        ArrayList<ScoreElem> scoreList= new ArrayList<ScoreElem>();

        while(cursor.moveToNext()){
            String tmpName1=cursor.getString(cursor.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_NAME));
            String tmpName2=cursor.getString(cursor.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER2_NAME));
            int tmpScore1=cursor.getInt(cursor.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER1_WIN));
            int tmpScore2=cursor.getInt(cursor.getColumnIndex(ScoresTableEntry.COLUMN_PLAYER2_WIN));
            boolean elemFound=false;
            for (ScoreElem elem: scoreList) {
                if(elem.checkAndAdd(tmpName1, tmpName2, tmpScore1, tmpScore2)){
                    elemFound=true;
                    break;
                }
            }
            if(!elemFound){
                scoreList.add(new ScoreElem(tmpName1, tmpName2, tmpScore1, tmpScore2));
            }
        }
        cursor.close();
        ScoresAdapter adapter=new ScoresAdapter(ScoresActivity.this, scoreList);
        myList.setAdapter(adapter);
    }
}
