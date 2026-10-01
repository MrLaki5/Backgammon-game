package games.mrlaki5.backgammon.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper managing all game tables:
 * - scores (legacy, v1)
 * - player_profiles (v2)
 * - leaderboard_entries (v2)
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "BackgammonScores.db";

    // --- SQL Create statements ---

    public static final String SQL_CREATE_SCORES = "CREATE TABLE " + ScoresTableEntry.TABLE_NAME +
            " ( " + ScoresTableEntry._ID + " INTEGER PRIMARY KEY, " +
            ScoresTableEntry.COLUMN_PLAYER1_NAME + " TEXT, " +
            ScoresTableEntry.COLUMN_PLAYER2_NAME + " TEXT, " +
            ScoresTableEntry.COLUMN_PLAYER1_WIN + " INTEGER, " +
            ScoresTableEntry.COLUMN_PLAYER2_WIN + " INTEGER, " +
            ScoresTableEntry.COLUMN_END_GAME_TIME + " TEXT );";

    public static final String SQL_CREATE_PROFILES = "CREATE TABLE " + ProfileTableEntry.TABLE_NAME +
            " ( " + ProfileTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProfileTableEntry.COLUMN_UID + " TEXT UNIQUE, " +
            ProfileTableEntry.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, " +
            ProfileTableEntry.COLUMN_AVATAR_ID + " INTEGER DEFAULT 0, " +
            ProfileTableEntry.COLUMN_ELO + " INTEGER DEFAULT 1200, " +
            ProfileTableEntry.COLUMN_WINS + " INTEGER DEFAULT 0, " +
            ProfileTableEntry.COLUMN_LOSSES + " INTEGER DEFAULT 0, " +
            ProfileTableEntry.COLUMN_TOTAL_GAMES + " INTEGER DEFAULT 0, " +
            ProfileTableEntry.COLUMN_CREATED_AT + " INTEGER, " +
            ProfileTableEntry.COLUMN_LAST_PLAYED_AT + " INTEGER );";

    public static final String SQL_CREATE_LEADERBOARD = "CREATE TABLE " + LeaderboardEntry.TABLE_NAME +
            " ( " + LeaderboardEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LeaderboardEntry.COLUMN_PROFILE_ID + " INTEGER NOT NULL, " +
            LeaderboardEntry.COLUMN_OPPONENT_PROFILE_ID + " INTEGER, " +
            LeaderboardEntry.COLUMN_WON + " INTEGER NOT NULL, " +
            LeaderboardEntry.COLUMN_ELO_BEFORE + " INTEGER, " +
            LeaderboardEntry.COLUMN_ELO_AFTER + " INTEGER, " +
            LeaderboardEntry.COLUMN_GAME_MODE + " TEXT, " +
            LeaderboardEntry.COLUMN_PLAYED_AT + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + LeaderboardEntry.COLUMN_PROFILE_ID + ") REFERENCES " +
            ProfileTableEntry.TABLE_NAME + "(" + ProfileTableEntry._ID + ") );";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SCORES);
        db.execSQL(SQL_CREATE_PROFILES);
        db.execSQL(SQL_CREATE_LEADERBOARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new tables without dropping the legacy scores table
            db.execSQL(SQL_CREATE_PROFILES);
            db.execSQL(SQL_CREATE_LEADERBOARD);
        }
    }

    // --- Profile CRUD ---

    /**
     * Inserts or updates a player profile. Returns the profile ID.
     */
    public long saveProfile(PlayerProfile profile) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProfileTableEntry.COLUMN_UID, profile.getUid());
        values.put(ProfileTableEntry.COLUMN_DISPLAY_NAME, profile.getDisplayName());
        values.put(ProfileTableEntry.COLUMN_AVATAR_ID, profile.getAvatarId());
        values.put(ProfileTableEntry.COLUMN_ELO, profile.getElo());
        values.put(ProfileTableEntry.COLUMN_WINS, profile.getWins());
        values.put(ProfileTableEntry.COLUMN_LOSSES, profile.getLosses());
        values.put(ProfileTableEntry.COLUMN_TOTAL_GAMES, profile.getTotalGames());
        values.put(ProfileTableEntry.COLUMN_CREATED_AT, profile.getCreatedAt());
        values.put(ProfileTableEntry.COLUMN_LAST_PLAYED_AT, profile.getLastPlayedAt());

        if (profile.getId() > 0) {
            db.update(ProfileTableEntry.TABLE_NAME, values,
                    ProfileTableEntry._ID + "=?",
                    new String[]{String.valueOf(profile.getId())});
            return profile.getId();
        } else {
            long id = db.insert(ProfileTableEntry.TABLE_NAME, null, values);
            profile.setId(id);
            return id;
        }
    }

    /**
     * Gets a profile by UID (for online players).
     */
    public PlayerProfile getProfileByUid(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ProfileTableEntry.TABLE_NAME, null,
                ProfileTableEntry.COLUMN_UID + "=?", new String[]{uid},
                null, null, null);
        PlayerProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = cursorToProfile(cursor);
        }
        cursor.close();
        return profile;
    }

    /**
     * Gets a profile by display name (for local players).
     */
    public PlayerProfile getProfileByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ProfileTableEntry.TABLE_NAME, null,
                ProfileTableEntry.COLUMN_DISPLAY_NAME + "=?", new String[]{name},
                null, null, null);
        PlayerProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = cursorToProfile(cursor);
        }
        cursor.close();
        return profile;
    }

    /**
     * Gets or creates a profile for a player name.
     */
    public PlayerProfile getOrCreateProfile(String displayName) {
        PlayerProfile profile = getProfileByName(displayName);
        if (profile == null) {
            profile = new PlayerProfile(displayName);
            saveProfile(profile);
        }
        return profile;
    }

    // --- Leaderboard entries ---

    /**
     * Records a game result in the leaderboard.
     */
    public void recordGameResult(long profileId, long opponentProfileId,
                                  boolean won, int eloBefore, int eloAfter,
                                  String gameMode) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LeaderboardEntry.COLUMN_PROFILE_ID, profileId);
        values.put(LeaderboardEntry.COLUMN_OPPONENT_PROFILE_ID, opponentProfileId);
        values.put(LeaderboardEntry.COLUMN_WON, won ? 1 : 0);
        values.put(LeaderboardEntry.COLUMN_ELO_BEFORE, eloBefore);
        values.put(LeaderboardEntry.COLUMN_ELO_AFTER, eloAfter);
        values.put(LeaderboardEntry.COLUMN_GAME_MODE, gameMode);
        values.put(LeaderboardEntry.COLUMN_PLAYED_AT, System.currentTimeMillis());
        db.insert(LeaderboardEntry.TABLE_NAME, null, values);
    }

    // --- Leaderboard queries ---

    /**
     * Gets the top players by ELO, optionally filtered by time range.
     *
     * @param sinceTimestamp Only include players who played after this time (0 = all time)
     * @param limit Max number of results
     * @return List of profiles sorted by ELO descending
     */
    public List<PlayerProfile> getLeaderboard(long sinceTimestamp, int limit) {
        SQLiteDatabase db = getReadableDatabase();
        List<PlayerProfile> result = new ArrayList<>();

        String query;
        String[] args;

        if (sinceTimestamp > 0) {
            // Only include profiles that have at least one game in the time range
            query = "SELECT p.* FROM " + ProfileTableEntry.TABLE_NAME + " p " +
                    "INNER JOIN " + LeaderboardEntry.TABLE_NAME + " l " +
                    "ON p." + ProfileTableEntry._ID + " = l." + LeaderboardEntry.COLUMN_PROFILE_ID +
                    " WHERE l." + LeaderboardEntry.COLUMN_PLAYED_AT + " >= ? " +
                    "GROUP BY p." + ProfileTableEntry._ID +
                    " ORDER BY p." + ProfileTableEntry.COLUMN_ELO + " DESC " +
                    "LIMIT ?";
            args = new String[]{String.valueOf(sinceTimestamp), String.valueOf(limit)};
        } else {
            // All time — just sort by ELO
            query = "SELECT * FROM " + ProfileTableEntry.TABLE_NAME +
                    " WHERE " + ProfileTableEntry.COLUMN_TOTAL_GAMES + " > 0 " +
                    " ORDER BY " + ProfileTableEntry.COLUMN_ELO + " DESC " +
                    " LIMIT ?";
            args = new String[]{String.valueOf(limit)};
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            result.add(cursorToProfile(cursor));
        }
        cursor.close();
        return result;
    }

    /**
     * Gets all-time leaderboard (top 50).
     */
    public List<PlayerProfile> getLeaderboardAllTime() {
        return getLeaderboard(0, 50);
    }

    /**
     * Gets this month's leaderboard (top 50).
     */
    public List<PlayerProfile> getLeaderboardThisMonth() {
        long monthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        return getLeaderboard(monthAgo, 50);
    }

    /**
     * Gets this week's leaderboard (top 50).
     */
    public List<PlayerProfile> getLeaderboardThisWeek() {
        long weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        return getLeaderboard(weekAgo, 50);
    }

    // --- Helpers ---

    private PlayerProfile cursorToProfile(Cursor cursor) {
        PlayerProfile p = new PlayerProfile();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ProfileTableEntry._ID)));
        p.setUid(cursor.getString(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_UID)));
        p.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_DISPLAY_NAME)));
        p.setAvatarId(cursor.getInt(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_AVATAR_ID)));
        p.setElo(cursor.getInt(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_ELO)));
        p.setWins(cursor.getInt(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_WINS)));
        p.setLosses(cursor.getInt(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_LOSSES)));
        p.setTotalGames(cursor.getInt(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_TOTAL_GAMES)));
        p.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_CREATED_AT)));
        p.setLastPlayedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ProfileTableEntry.COLUMN_LAST_PLAYED_AT)));
        return p;
    }
}
