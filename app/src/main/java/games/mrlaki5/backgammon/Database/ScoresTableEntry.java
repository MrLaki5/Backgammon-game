package games.mrlaki5.backgammon.Database;

import android.provider.BaseColumns;

//Class for storing database strings
public abstract class ScoresTableEntry implements BaseColumns {

    //Name of table in database
    public static final String TABLE_NAME="scores";
    //Name of column for player1 name
    public static final String COLUMN_PLAYER1_NAME="p1name";
    //Name of column for player2 name
    public static final String COLUMN_PLAYER2_NAME="p2name";
    //Name of column for player1 score
    //Score: 0-if lose, 1-if win
    public static final String COLUMN_PLAYER1_WIN="pwin1";
    //Name of column for player2 score
    //Score: 0-if lose, 1-if win
    public static final String COLUMN_PLAYER2_WIN="pwin2";
    //Name of clumn for time when game ended
    public static final String COLUMN_END_GAME_TIME="endtime";
}
