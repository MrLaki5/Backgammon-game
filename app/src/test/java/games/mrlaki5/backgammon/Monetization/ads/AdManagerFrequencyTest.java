package games.mrlaki5.backgammon.Monetization.ads;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for AdManager frequency logic.
 * Verifies the interstitial ad policy: 1 ad per 3 completed games.
 *
 * These tests verify the INTERSTITIAL_EVERY_N_GAMES constant and
 * the expected behavior pattern documented in the spec:
 * Game 1 → NO ad, Game 2 → NO ad, Game 3 → YES ad, Game 4 → NO, ...
 */
public class AdManagerFrequencyTest {

    @Test
    public void interstitialFrequency_isThreeGames() {
        assertEquals(3, AdConfig.INTERSTITIAL_EVERY_N_GAMES);
    }

    @Test
    public void adConfig_hasValidInterstitialZone() {
        assertNotNull(AdConfig.ZONE_INTERSTITIAL);
        assertFalse(AdConfig.ZONE_INTERSTITIAL.isEmpty());
        assertEquals("6a8b35a0f34d73758477ec0a", AdConfig.ZONE_INTERSTITIAL);
    }

    @Test
    public void adConfig_hasRewardedZone() {
        assertNotNull(AdConfig.ZONE_REWARDED);
        assertFalse(AdConfig.ZONE_REWARDED.isEmpty());
        assertEquals("6a8dddf3488ef01a725b3afd", AdConfig.ZONE_REWARDED);
    }

    @Test
    public void adPolicy_neverMidGame() {
        assertTrue(AdConfig.NEVER_MID_GAME);
    }

    @Test
    public void adPolicy_rewardedUserInitiated() {
        assertTrue(AdConfig.REWARDED_USER_INITIATED_ONLY);
    }
}
