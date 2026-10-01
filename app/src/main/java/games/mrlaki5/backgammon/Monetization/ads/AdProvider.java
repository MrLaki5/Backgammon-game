package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;

/**
 * Abstraction for ad SDK integration. Implementations can use:
 * - Tapsell (Iranian ad network, supports local payment)
 * - AdMob (Google, may have Iran access issues)
 * - Bazaar Ads (CafeBazaar's native ad SDK)
 * - Any other compatible ad network
 *
 * The app NEVER shows banner ads on the game screen (policy decision).
 * Only interstitial (between games) and rewarded (for hints) are used.
 */
public interface AdProvider {

    /**
     * Initializes the ad SDK. Call once in Application.onCreate() or first Activity.
     */
    void initialize(Activity activity);

    /**
     * Requests loading of an ad of the given type.
     * The ad is cached and ready to show when onAdLoaded fires.
     */
    void loadAd(AdType type, AdCallback callback);

    /**
     * Shows a previously loaded ad.
     * @return true if the ad was shown, false if not ready.
     */
    boolean showAd(Activity activity, AdType type, AdCallback callback);

    /**
     * Returns true if an ad of the given type is loaded and ready to show.
     */
    boolean isAdReady(AdType type);

    /**
     * Destroys ad resources. Call when the activity/app is being destroyed.
     */
    void destroy();
}
