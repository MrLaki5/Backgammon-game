package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;
import android.util.Log;

/**
 * Stub ad provider for development/testing.
 * Always reports ads as loaded and simulates showing them.
 * Replace with real SDK implementation (Tapsell, AdMob, etc.) in production.
 */
public class StubAdProvider implements AdProvider {

    private static final String TAG = "StubAdProvider";
    private boolean interstitialReady = false;
    private boolean rewardedReady = false;

    @Override
    public void initialize(Activity activity) {
        Log.d(TAG, "Ad SDK initialized (stub)");
        // Pre-load ads
        interstitialReady = true;
        rewardedReady = true;
    }

    @Override
    public void loadAd(AdType type, AdCallback callback) {
        Log.d(TAG, "Loading ad: " + type);
        switch (type) {
            case INTERSTITIAL:
                interstitialReady = true;
                break;
            case REWARDED:
                rewardedReady = true;
                break;
        }
        if (callback != null) callback.onAdLoaded();
    }

    @Override
    public boolean showAd(Activity activity, AdType type, AdCallback callback) {
        Log.d(TAG, "Showing ad: " + type + " (stub — no real ad)");
        if (!isAdReady(type)) return false;

        switch (type) {
            case INTERSTITIAL:
                interstitialReady = false;
                break;
            case REWARDED:
                rewardedReady = false;
                break;
        }

        if (callback != null) {
            callback.onAdShown();
            if (type == AdType.REWARDED) {
                callback.onRewardEarned();
            }
            callback.onAdDismissed();
        }
        return true;
    }

    @Override
    public boolean isAdReady(AdType type) {
        switch (type) {
            case INTERSTITIAL: return interstitialReady;
            case REWARDED: return rewardedReady;
            default: return false;
        }
    }

    @Override
    public void destroy() {
        Log.d(TAG, "Ad SDK destroyed (stub)");
    }
}
