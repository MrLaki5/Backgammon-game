package games.mrlaki5.backgammon.Retention;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for DailyChallenge definitions.
 * Verifies the challenge set is valid and well-structured.
 */
public class DailyChallengeDefinitionsTest {

    @Test
    public void challenges_haveEightItems() {
        assertEquals(8, DailyChallenge.CHALLENGES.length);
    }

    @Test
    public void variantOfDay_isStableAndCoversEveryVariant() {
        java.util.Set<com.royalbackgammon.core.variant.Variant> seen = new java.util.HashSet<>();
        for (int day = 1; day <= 366; day++) {
            com.royalbackgammon.core.variant.Variant variant = DailyChallenge.variantOfDay(day);
            assertNotNull(variant);
            assertEquals("the same day always gives the same variant",
                    variant, DailyChallenge.variantOfDay(day));
            seen.add(variant);
        }
        assertEquals(DailyChallenge.DAILY_VARIANTS.length, seen.size());
    }

    @Test
    public void challenges_allHaveUniqueIds() {
        String[] seen = new String[DailyChallenge.CHALLENGES.length];
        for (int i = 0; i < DailyChallenge.CHALLENGES.length; i++) {
            DailyChallenge.Challenge c = DailyChallenge.CHALLENGES[i];
            assertNotNull("Challenge " + i + " has null id", c.id);
            assertFalse("Challenge " + i + " has empty id", c.id.isEmpty());
            for (int j = 0; j < i; j++) {
                assertNotEquals("Duplicate challenge id at " + i + " and " + j,
                        seen[j], c.id);
            }
            seen[i] = c.id;
        }
    }

    @Test
    public void challenges_allHaveValidType() {
        for (DailyChallenge.Challenge c : DailyChallenge.CHALLENGES) {
            assertNotNull("Challenge " + c.id + " has null type", c.type);
        }
    }

    @Test
    public void challenges_allHavePositiveTarget() {
        for (DailyChallenge.Challenge c : DailyChallenge.CHALLENGES) {
            assertTrue("Challenge " + c.id + " has non-positive target: " + c.targetValue,
                    c.targetValue > 0);
        }
    }

    @Test
    public void challenges_coverDifferentTypes() {
        boolean hasWin = false, hasComplete = false, hasDifficulty = false;
        for (DailyChallenge.Challenge c : DailyChallenge.CHALLENGES) {
            if (c.type == DailyChallenge.ChallengeType.WIN_GAMES) hasWin = true;
            if (c.type == DailyChallenge.ChallengeType.COMPLETE_GAMES) hasComplete = true;
            if (c.type == DailyChallenge.ChallengeType.BEAT_DIFFICULTY) hasDifficulty = true;
        }
        assertTrue("Should have WIN_GAMES challenge", hasWin);
        assertTrue("Should have COMPLETE_GAMES challenge", hasComplete);
        assertTrue("Should have BEAT_DIFFICULTY challenge", hasDifficulty);
    }
}
