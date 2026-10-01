package games.mrlaki5.backgammon.Database;

import android.provider.BaseColumns;

/**
 * Schema for the leaderboard_entries table.
 * Each row records a single game result with timestamp for time-based filtering.
 */
public abstract class LeaderboardEntry implements BaseColumns {

    public static final String TABLE_NAME = "leaderboard_entries";

    public static final String COLUMN_PROFILE_ID = "profile_id";
    public static final String COLUMN_OPPONENT_PROFILE_ID = "opponent_profile_id";
    public static final String COLUMN_WON = "won";              // 1 = win, 0 = loss
    public static final String COLUMN_ELO_BEFORE = "elo_before";
    public static final String COLUMN_ELO_AFTER = "elo_after";
    public static final String COLUMN_GAME_MODE = "game_mode";  // "vs_bot", "pass_and_play", "online"
    public static final String COLUMN_PLAYED_AT = "played_at";  // Unix timestamp millis
}
