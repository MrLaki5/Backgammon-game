package games.mrlaki5.backgammon.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String SQL_CREATE_ENTRIES="CREATE TABLE " + ScoresTableEntry.TABLE_NAME +
            " ( " + ScoresTableEntry._ID + " INTEGER PRIMARY KEY, " +
            ScoresTableEntry.COLUMN_PLAYER1_NAME + " TEXT, " +
            ScoresTableEntry.COLUMN_PLAYER2_NAME + " TEXT, " +
            ScoresTableEntry.COLUMN_PLAYER1_WIN + " INTEGER, " +
            ScoresTableEntry.COLUMN_PLAYER2_WIN + " INTEGER, " +
            ScoresTableEntry.COLUMN_END_GAME_TIME + " TEXT );";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + ScoresTableEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BackgammonScores.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
