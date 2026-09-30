package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import games.mrlaki5.backgammon.Retention.AchievementManager;
import games.mrlaki5.backgammon.Retention.DailyChallenge;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for retention features.
 * These require Android context for SharedPreferences.
 */
@RunWith(AndroidJUnit4.class)
public class RetentionFlowTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Clear prefs before each test
        clearPrefs("daily_challenge_prefs");
        clearPrefs("achievements_prefs");
        clearPrefs("win_streak_prefs");
    }

    private void clearPrefs(String name) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // ==================== WinStreak Tests ====================

    @Test
    public void winStreak_startsAtZero() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        assertEquals(0, tracker.getCurrentStreak());
        assertEquals(0, tracker.getBestStreak());
    }

    @Test
    public void winStreak_incrementsOnWin() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        assertEquals(1, tracker.recordWin());
        assertEquals(2, tracker.recordWin());
        assertEquals(3, tracker.recordWin());
        assertEquals(3, tracker.getCurrentStreak());
    }

    @Test
    public void winStreak_resetsOnLoss() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        tracker.recordWin();
        tracker.recordWin();
        tracker.recordLoss();
        assertEquals(0, tracker.getCurrentStreak());
    }

    @Test
    public void winStreak_tracksBest() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        tracker.recordWin();
        tracker.recordWin();
        tracker.recordWin(); // streak = 3
        tracker.recordLoss(); // streak = 0
        tracker.recordWin(); // streak = 1
        assertEquals(1, tracker.getCurrentStreak());
        assertEquals(3, tracker.getBestStreak());
    }

    // ==================== Achievement Tests ====================

    @Test
    public void achievement_firstWin_unlocksOnWin() {
        AchievementManager am = new AchievementManager(context);
        assertFalse(am.isUnlocked("first_win"));
        am.onGameCompleted(true, 0, 1);
        assertTrue(am.isUnlocked("first_win"));
    }

    @Test
    public void achievement_firstWin_doesNotUnlockOnLoss() {
        AchievementManager am = new AchievementManager(context);
        am.onGameCompleted(false, 0, 0);
        assertFalse(am.isUnlocked("first_win"));
    }

    @Test
    public void achievement_games10_afterTenGames() {
        AchievementManager am = new AchievementManager(context);
        for (int i = 0; i < 9; i++) {
            am.onGameCompleted(false, 0, 0);
        }
        assertFalse(am.isUnlocked("games_10"));
        am.onGameCompleted(false, 0, 0);
        assertTrue(am.isUnlocked("games_10"));
    }

    @Test
    public void achievement_beatHard_requiresHardDifficulty() {
        AchievementManager am = new AchievementManager(context);
        am.onGameCompleted(true, 1, 1); // medium win
        assertFalse(am.isUnlocked("beat_hard"));
        am.onGameCompleted(true, 2, 2); // hard win
        assertTrue(am.isUnlocked("beat_hard"));
    }

    @Test
    public void achievement_beatRoyal_requiresRoyalDifficulty() {
        AchievementManager am = new AchievementManager(context);
        am.onGameCompleted(true, 2, 1); // hard win
        assertFalse(am.isUnlocked("beat_royal"));
        am.onGameCompleted(true, 3, 2); // royal win
        assertTrue(am.isUnlocked("beat_royal"));
    }

    @Test
    public void achievement_streak5_requiresFiveConsecutiveWins() {
        AchievementManager am = new AchievementManager(context);
        am.onGameCompleted(true, 0, 4); // streak 4
        assertFalse(am.isUnlocked("streak_5"));
        am.onGameCompleted(true, 0, 5); // streak 5
        assertTrue(am.isUnlocked("streak_5"));
    }

    @Test
    public void achievement_noQuit_resetsOnAbandon() {
        AchievementManager am = new AchievementManager(context);
        for (int i = 0; i < 9; i++) {
            am.onGameCompleted(false, 0, 0);
        }
        am.onGameAbandoned(); // resets counter
        for (int i = 0; i < 9; i++) {
            am.onGameCompleted(false, 0, 0);
        }
        assertFalse(am.isUnlocked("no_quit_10")); // still only 9 since reset
        am.onGameCompleted(false, 0, 0); // now 10
        assertTrue(am.isUnlocked("no_quit_10"));
    }

    @Test
    public void achievement_tutorialDone() {
        AchievementManager am = new AchievementManager(context);
        assertFalse(am.isUnlocked("tutorial_done"));
        am.onTutorialCompleted();
        assertTrue(am.isUnlocked("tutorial_done"));
    }

    @Test
    public void achievement_unlockedCount() {
        AchievementManager am = new AchievementManager(context);
        assertEquals(0, am.getUnlockedCount());
        am.onGameCompleted(true, 0, 1); // unlocks first_win
        assertEquals(1, am.getUnlockedCount());
        am.onTutorialCompleted(); // unlocks tutorial_done
        assertEquals(2, am.getUnlockedCount());
    }

    // ==================== DailyChallenge Tests ====================

    @Test
    public void dailyChallenge_startsNotCompleted() {
        DailyChallenge dc = new DailyChallenge(context);
        assertFalse(dc.isCompleted());
        assertEquals(0, dc.getProgress());
    }

    @Test
    public void dailyChallenge_hasValidChallenge() {
        DailyChallenge dc = new DailyChallenge(context);
        DailyChallenge.Challenge challenge = dc.getTodayChallenge();
        assertNotNull(challenge);
        assertNotNull(challenge.id);
        assertNotNull(challenge.type);
        assertTrue(challenge.targetValue > 0);
    }

    @Test
    public void dailyChallenge_totalCompleted_startsAtZero() {
        DailyChallenge dc = new DailyChallenge(context);
        assertEquals(0, dc.getTotalCompleted());
    }

    // ==================== Multiple Rematches Flow ====================

    @Test
    public void multipleRematches_streakAccumulates() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        // Simulate: Game 1 win, Rematch, Game 2 win, Rematch, Game 3 win
        assertEquals(1, tracker.recordWin());
        assertEquals(2, tracker.recordWin());
        assertEquals(3, tracker.recordWin());
        assertEquals(3, tracker.getCurrentStreak());
        assertEquals(3, tracker.getBestStreak());
    }

    @Test
    public void multipleRematches_lossResetsStreak() {
        WinStreakTracker tracker = new WinStreakTracker(context);
        tracker.recordWin();  // Game 1: win, streak=1
        tracker.recordWin();  // Game 2 (rematch): win, streak=2
        tracker.recordLoss(); // Game 3 (rematch): loss, streak=0
        tracker.recordWin();  // Game 4 (rematch): win, streak=1
        assertEquals(1, tracker.getCurrentStreak());
        assertEquals(2, tracker.getBestStreak());
    }
}
