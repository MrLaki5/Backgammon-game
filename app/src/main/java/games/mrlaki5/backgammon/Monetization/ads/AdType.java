package games.mrlaki5.backgammon.Monetization.ads;

/**
 * Types of ads supported by the application.
 *
 * Policy (from product spec):
 * - INTERSTITIAL: ONLY between games, max 1 per 3 games. Never mid-turn.
 * - REWARDED: For hints (best move suggestion) or tournament retry. Gives value to user.
 * - BANNER: EXPLICITLY FORBIDDEN on game screen (causes negative reviews per competitor analysis).
 */
public enum AdType {
    INTERSTITIAL,
    REWARDED
    // BANNER intentionally excluded — see policy above
}
