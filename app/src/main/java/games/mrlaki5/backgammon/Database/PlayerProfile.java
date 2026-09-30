package games.mrlaki5.backgammon.Database;

/**
 * Represents a player's profile for the leaderboard system.
 * Stored locally in SQLite; will sync to server for online leaderboards.
 */
public class PlayerProfile {

    public static final int DEFAULT_ELO = 1200;
    public static final int DEFAULT_AVATAR = 0;

    private long id;
    private String uid;           // Online UID (null for local-only players)
    private String displayName;
    private int avatarId;         // Index into predefined avatars array
    private int elo;
    private int wins;
    private int losses;
    private int totalGames;
    private long createdAt;       // Unix timestamp millis
    private long lastPlayedAt;    // Unix timestamp millis

    public PlayerProfile() {
        this.elo = DEFAULT_ELO;
        this.avatarId = DEFAULT_AVATAR;
        this.createdAt = System.currentTimeMillis();
        this.lastPlayedAt = System.currentTimeMillis();
    }

    public PlayerProfile(String displayName) {
        this();
        this.displayName = displayName;
    }

    public PlayerProfile(String uid, String displayName, int avatarId) {
        this();
        this.uid = uid;
        this.displayName = displayName;
        this.avatarId = avatarId;
    }

    // --- Score update ---

    /**
     * Records a win and updates ELO based on opponent's rating.
     * @param opponentElo the opponent's ELO before this game
     */
    public void recordWin(int opponentElo) {
        wins++;
        totalGames++;
        elo = EloCalculator.newRating(elo, opponentElo, true);
        lastPlayedAt = System.currentTimeMillis();
    }

    /**
     * Records a loss and updates ELO based on opponent's rating.
     * @param opponentElo the opponent's ELO before this game
     */
    public void recordLoss(int opponentElo) {
        losses++;
        totalGames++;
        elo = EloCalculator.newRating(elo, opponentElo, false);
        lastPlayedAt = System.currentTimeMillis();
    }

    /**
     * Returns win rate as a percentage (0–100).
     */
    public int getWinRate() {
        if (totalGames == 0) return 0;
        return (int) Math.round((wins * 100.0) / totalGames);
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getAvatarId() { return avatarId; }
    public void setAvatarId(int avatarId) { this.avatarId = avatarId; }

    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(long lastPlayedAt) { this.lastPlayedAt = lastPlayedAt; }
}
