package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
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
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.R;

/**
 * Legacy head-to-head scores view (preserved from original implementation).
 * Accessible from the new leaderboard screen via "View Match History" button.
 */
public class LegacyScoresActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    private DbHelper helper;
    private ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scores);
        helper = new DbHelper(this);
        myList = findViewById(R.id.scoreList);
        loadAdapter();
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String p1Name = ((TextView) view.findViewById(R.id.sListP1Name)).getText().toString();
                String p2Name = ((TextView) view.findViewById(R.id.sListP2Name)).getText().toString();
                Intent intent = new Intent(LegacyScoresActivity.this, ResultsActivity.class);
                intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, p1Name);
                intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, p2Name);
                startActivity(intent);
            }
        });
    }

    public void resetScores(View view) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(ScoresTableEntry.TABLE_NAME, null, null);
        loadAdapter();
    }

    public void loadAdapter() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columnsRet = {ScoresTableEntry.COLUMN_PLAYER1_WIN,
                ScoresTableEntry.COLUMN_PLAYER2_WIN,
                ScoresTableEntry.COLUMN_END_GAME_TIME,
                ScoresTableEntry.COLUMN_PLAYER1_NAME,
                ScoresTableEntry.COLUMN_PLAYER2_NAME,
                ScoresTableEntry._ID};
        Cursor cursor = db.query(ScoresTableEntry.TABLE_NAME, columnsRet,
                null, null, null, null, null);
        ArrayList<ScoreElem> scoreList = new ArrayList<>();
        int player1WinColumn = cursor.getColumnIndexOrThrow(ScoresTableEntry.COLUMN_PLAYER1_WIN);
        int player2WinColumn = cursor.getColumnIndexOrThrow(ScoresTableEntry.COLUMN_PLAYER2_WIN);
        int player1NameColumn = cursor.getColumnIndexOrThrow(ScoresTableEntry.COLUMN_PLAYER1_NAME);
        int player2NameColumn = cursor.getColumnIndexOrThrow(ScoresTableEntry.COLUMN_PLAYER2_NAME);
        while (cursor.moveToNext()) {
            String tmpName1 = cursor.getString(player1NameColumn);
            String tmpName2 = cursor.getString(player2NameColumn);
            int tmpScore1 = cursor.getInt(player1WinColumn);
            int tmpScore2 = cursor.getInt(player2WinColumn);
            boolean elemFound = false;
            for (ScoreElem elem : scoreList) {
                if (elem.checkAndAdd(tmpName1, tmpName2, tmpScore1, tmpScore2)) {
                    elemFound = true;
                    break;
                }
            }
            if (!elemFound) {
                scoreList.add(new ScoreElem(tmpName1, tmpName2, tmpScore1, tmpScore2));
            }
        }
        cursor.close();
        ScoresAdapter adapter = new ScoresAdapter(this, scoreList);
        myList.setAdapter(adapter);
        TextView empty = findViewById(R.id.emptyScoreText);
        boolean hasScores = !scoreList.isEmpty();
        empty.setVisibility(hasScores ? View.GONE : View.VISIBLE);
        myList.setVisibility(hasScores ? View.VISIBLE : View.GONE);
    }
}
