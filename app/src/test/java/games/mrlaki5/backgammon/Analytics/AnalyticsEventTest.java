package games.mrlaki5.backgammon.Analytics;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for AnalyticsEvent constants.
 * Verifies naming conventions, completeness, and Firebase compatibility.
 */
public class AnalyticsEventTest {

    @Test
    public void eventNames_areSnakeCase() {
        // All event name constants (public static final String fields starting with uppercase)
        String[] events = {
                AnalyticsEvent.APP_OPEN, AnalyticsEvent.FIRST_LAUNCH,
                AnalyticsEvent.SESSION_START, AnalyticsEvent.SESSION_END,
                AnalyticsEvent.TUTORIAL_STARTED, AnalyticsEvent.TUTORIAL_COMPLETED,
                AnalyticsEvent.GAME_STARTED, AnalyticsEvent.GAME_COMPLETED,
                AnalyticsEvent.GAME_WON, AnalyticsEvent.GAME_LOST,
                AnalyticsEvent.GAME_ABANDONED, AnalyticsEvent.REMATCH_CLICKED,
                AnalyticsEvent.REMATCH_STARTED, AnalyticsEvent.REMATCH_COMPLETED,
                AnalyticsEvent.AD_REQUESTED, AnalyticsEvent.AD_LOADED,
                AnalyticsEvent.AD_SHOWN, AnalyticsEvent.AD_FAILED,
                AnalyticsEvent.MENU_PLAY_CLICKED, AnalyticsEvent.SETTINGS_OPENED,
                AnalyticsEvent.PURCHASE_STARTED, AnalyticsEvent.PURCHASE_SUCCESS,
                AnalyticsEvent.DAILY_REWARD_OPENED, AnalyticsEvent.MISSION_COMPLETED,
                AnalyticsEvent.ACHIEVEMENT_UNLOCKED
        };
        for (String event : events) {
            assertNotNull(event);
            // Snake case: lowercase letters, digits, underscores only
            assertTrue("Event name not snake_case: " + event,
                    event.matches("[a-z][a-z0-9_]*"));
        }
    }

    @Test
    public void eventNames_notExceedFirebaseLimit() {
        // Firebase limit: 40 characters max for event names
        String[] events = {
                AnalyticsEvent.APP_OPEN, AnalyticsEvent.FIRST_LAUNCH,
                AnalyticsEvent.SESSION_START, AnalyticsEvent.SESSION_END,
                AnalyticsEvent.GAME_STARTED, AnalyticsEvent.GAME_COMPLETED,
                AnalyticsEvent.GAME_WON, AnalyticsEvent.GAME_LOST,
                AnalyticsEvent.GAME_ABANDONED, AnalyticsEvent.GAME_RESTARTED,
                AnalyticsEvent.REMATCH_CLICKED, AnalyticsEvent.REMATCH_STARTED,
                AnalyticsEvent.REMATCH_COMPLETED,
                AnalyticsEvent.AD_REQUESTED, AnalyticsEvent.AD_LOADED,
                AnalyticsEvent.AD_SHOWN, AnalyticsEvent.AD_FAILED, AnalyticsEvent.AD_CLICKED,
                AnalyticsEvent.REWARDED_AD_STARTED, AnalyticsEvent.REWARDED_AD_COMPLETED,
                AnalyticsEvent.REWARDED_AD_FAILED,
                AnalyticsEvent.MENU_PLAY_CLICKED, AnalyticsEvent.TUTORIAL_CLICKED,
                AnalyticsEvent.SETTINGS_OPENED, AnalyticsEvent.SCORES_OPENED,
                AnalyticsEvent.THEME_SELECTED, AnalyticsEvent.DIFFICULTY_SELECTED,
                AnalyticsEvent.LANGUAGE_CHANGED,
                AnalyticsEvent.PURCHASE_STARTED, AnalyticsEvent.PURCHASE_SUCCESS,
                AnalyticsEvent.PURCHASE_FAILED, AnalyticsEvent.REMOVE_ADS_CLICKED,
                AnalyticsEvent.DAILY_REWARD_OPENED, AnalyticsEvent.DAILY_REWARD_CLAIMED,
                AnalyticsEvent.MISSION_STARTED, AnalyticsEvent.MISSION_COMPLETED,
                AnalyticsEvent.ACHIEVEMENT_UNLOCKED
        };
        for (String event : events) {
            assertTrue("Event name too long (>40): " + event + " (" + event.length() + ")",
                    event.length() <= 40);
        }
    }

    @Test
    public void paramNames_areSnakeCase() {
        String[] params = {
                AnalyticsEvent.PARAM_MODE, AnalyticsEvent.PARAM_DIFFICULTY,
                AnalyticsEvent.PARAM_THEME, AnalyticsEvent.PARAM_WINNER,
                AnalyticsEvent.PARAM_DURATION_SEC, AnalyticsEvent.PARAM_GAMES_PLAYED,
                AnalyticsEvent.PARAM_ELO_BEFORE, AnalyticsEvent.PARAM_ELO_AFTER,
                AnalyticsEvent.PARAM_AD_TYPE, AnalyticsEvent.PARAM_PLACEMENT,
                AnalyticsEvent.PARAM_GAME_NUMBER, AnalyticsEvent.PARAM_REWARD_TYPE,
                AnalyticsEvent.PARAM_ERROR, AnalyticsEvent.PARAM_STEP,
                AnalyticsEvent.PARAM_LANGUAGE, AnalyticsEvent.PARAM_SKU,
                AnalyticsEvent.PARAM_STORE, AnalyticsEvent.PARAM_WIN_STREAK,
                AnalyticsEvent.PARAM_DAY_STREAK, AnalyticsEvent.PARAM_MISSION_ID,
                AnalyticsEvent.PARAM_ACHIEVEMENT_ID
        };
        for (String param : params) {
            assertNotNull(param);
            assertTrue("Param name not snake_case: " + param,
                    param.matches("[a-z][a-z0-9_]*"));
        }
    }

    @Test
    public void funnelEvents_exist() {
        // Minimum funnel: app_open → game_started → game_completed → rematch_clicked → rematch_started → rematch_completed
        assertEquals("app_open", AnalyticsEvent.APP_OPEN);
        assertEquals("game_started", AnalyticsEvent.GAME_STARTED);
        assertEquals("game_completed", AnalyticsEvent.GAME_COMPLETED);
        assertEquals("rematch_clicked", AnalyticsEvent.REMATCH_CLICKED);
        assertEquals("rematch_started", AnalyticsEvent.REMATCH_STARTED);
        assertEquals("rematch_completed", AnalyticsEvent.REMATCH_COMPLETED);
    }

    @Test
    public void sessionEvents_exist() {
        assertEquals("session_start", AnalyticsEvent.SESSION_START);
        assertEquals("session_end", AnalyticsEvent.SESSION_END);
    }

    @Test
    public void retentionEvents_exist() {
        assertEquals("first_launch", AnalyticsEvent.FIRST_LAUNCH);
        assertEquals("daily_reward_opened", AnalyticsEvent.DAILY_REWARD_OPENED);
        assertEquals("mission_completed", AnalyticsEvent.MISSION_COMPLETED);
        assertEquals("achievement_unlocked", AnalyticsEvent.ACHIEVEMENT_UNLOCKED);
    }
}
