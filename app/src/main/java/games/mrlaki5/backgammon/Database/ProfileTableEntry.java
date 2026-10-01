package games.mrlaki5.backgammon.Database;

import android.provider.BaseColumns;

/**
 * Schema for the player_profiles table.
 */
public abstract class ProfileTableEntry implements BaseColumns {

    public static final String TABLE_NAME = "player_profiles";

    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_AVATAR_ID = "avatar_id";
    public static final String COLUMN_ELO = "elo";
    public static final String COLUMN_WINS = "wins";
    public static final String COLUMN_LOSSES = "losses";
    public static final String COLUMN_TOTAL_GAMES = "total_games";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LAST_PLAYED_AT = "last_played_at";
}
