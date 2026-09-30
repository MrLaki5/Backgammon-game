package games.mrlaki5.backgammon.Monetization.ads;

/**
 * Callback for ad lifecycle events.
 */
public interface AdCallback {
    /** Ad loaded and ready to display. */
    void onAdLoaded();

    /** Ad failed to load. */
    void onAdFailedToLoad(String error);

    /** Ad was displayed to the user. */
    void onAdShown();

    /** Ad was dismissed (closed by user). */
    void onAdDismissed();

    /** User clicked the ad. */
    void onAdClicked();

    /** Rewarded ad: user earned the reward (watched to completion). */
    default void onRewardEarned() {}
}
