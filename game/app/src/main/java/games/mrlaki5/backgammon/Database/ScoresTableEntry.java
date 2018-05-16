package games.mrlaki5.backgammon.Database;

import android.provider.BaseColumns;

public abstract class ScoresTableEntry implements BaseColumns {
    public static final String TABLE_NAME="scores";
    public static final String COLUMN_PLAYER1_NAME="p1name";
    public static final String COLUMN_PLAYER2_NAME="p2name";
    public static final String COLUMN_PLAYER1_WIN="pwin1";
    public static final String COLUMN_PLAYER2_WIN="pwin2";
    public static final String COLUMN_END_GAME_TIME="endtime";
}
