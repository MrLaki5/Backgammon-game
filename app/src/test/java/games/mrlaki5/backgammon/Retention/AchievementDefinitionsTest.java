package games.mrlaki5.backgammon.Retention;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Achievement definitions.
 * Verifies the achievement set is valid and complete.
 */
public class AchievementDefinitionsTest {

    @Test
    public void achievements_haveTenItems() {
        assertEquals(10, AchievementManager.ACHIEVEMENT_IDS.length);
    }

    @Test
    public void achievements_allHaveNonEmptyIds() {
        for (int i = 0; i < AchievementManager.ACHIEVEMENT_IDS.length; i++) {
            String id = AchievementManager.ACHIEVEMENT_IDS[i];
            assertNotNull("Achievement " + i + " is null", id);
            assertFalse("Achievement " + i + " is empty", id.isEmpty());
        }
    }

    @Test
    public void achievements_allHaveUniqueIds() {
        for (int i = 0; i < AchievementManager.ACHIEVEMENT_IDS.length; i++) {
            for (int j = i + 1; j < AchievementManager.ACHIEVEMENT_IDS.length; j++) {
                assertNotEquals("Duplicate achievement ID at " + i + " and " + j,
                        AchievementManager.ACHIEVEMENT_IDS[i],
                        AchievementManager.ACHIEVEMENT_IDS[j]);
            }
        }
    }

    @Test
    public void achievements_containsExpectedIds() {
        String[] expected = {
                "first_win", "games_10", "games_50", "games_100",
                "streak_5", "beat_hard", "beat_royal",
                "no_quit_10", "daily_win", "tutorial_done"
        };
        for (String expectedId : expected) {
            boolean found = false;
            for (String actual : AchievementManager.ACHIEVEMENT_IDS) {
                if (actual.equals(expectedId)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Missing achievement: " + expectedId, found);
        }
    }
}
