package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;
import android.util.Log;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;

import ir.tapsell.plus.AdRequestCallback;
import ir.tapsell.plus.AdShowListener;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.model.TapsellPlusAdModel;
import ir.tapsell.plus.model.TapsellPlusErrorModel;

/**
 * Real ad provider backed by TapsellPlus SDK.
 *
 * Architecture:
 *   AdManager → AdProvider (interface) → TapsellAdProvider → TapsellPlus SDK
 *
 * Zone IDs:
 *   - Interstitial: Configured via constructor (default from TapsellAdHelper)
 *   - Rewarded: Configured via constructor (REQUIRES zone ID from Tapsell Panel)
 *
 * Thread safety:
 *   All TapsellPlus calls must be made on the main thread (SDK requirement).
 *   Callbacks are delivered on the main thread by the SDK.
 */
public class TapsellAdProvider implements AdProvider {

    private static final String TAG = "TapsellAdProvider";

    private final String interstitialZoneId;
    private final String rewardedZoneId;

    // Response IDs for loaded ads
    private volatile String interstitialResponseId = null;
    private volatile String rewardedResponseId = null;

    // Ready state
    private volatile boolean interstitialReady = false;
    private volatile boolean rewardedReady = false;

    // Track the activity for ad loading
    private Activity currentActivity;

    /**
     * @param interstitialZoneId Zone ID for interstitial ads from Tapsell Panel
     * @param rewardedZoneId     Zone ID for rewarded ads from Tapsell Panel (can be null if not yet created)
     */
    public TapsellAdProvider(String interstitialZoneId, String rewardedZoneId) {
        this.interstitialZoneId = interstitialZoneId;
        this.rewardedZoneId = rewardedZoneId;
    }

    @Override
    public void initialize(Activity activity) {
        this.currentActivity = activity;
        Log.d(TAG, "TapsellAdProvider initialized");
        // TapsellPlus.initialize() is already called in BackgammonApp
        // We just preload ads here
    }

    @Override
    public void loadAd(AdType type, AdCallback callback) {
        if (currentActivity == null) {
            Log.w(TAG, "Cannot load ad: no activity reference");
            if (callback != null) callback.onAdFailedToLoad("No activity");
            return;
        }

        switch (type) {
            case INTERSTITIAL:
                loadInterstitial(callback);
                break;
            case REWARDED:
                loadRewarded(callback);
                break;
        }
    }

    @Override
    public boolean showAd(Activity activity, AdType type, AdCallback callback) {
        switch (type) {
            case INTERSTITIAL:
                return showInterstitial(activity, callback);
            case REWARDED:
                return showRewarded(activity, callback);
            default:
                return false;
        }
    }

    @Override
    public boolean isAdReady(AdType type) {
        switch (type) {
            case INTERSTITIAL:
                return interstitialReady && interstitialResponseId != null;
            case REWARDED:
                return rewardedReady && rewardedResponseId != null;
            default:
                return false;
        }
    }

    @Override
    public void destroy() {
        currentActivity = null;
        interstitialResponseId = null;
        rewardedResponseId = null;
        interstitialReady = false;
        rewardedReady = false;
        Log.d(TAG, "TapsellAdProvider destroyed");
    }

    // ==================== Interstitial ====================

    private void loadInterstitial(AdCallback callback) {
        if (interstitialZoneId == null || interstitialZoneId.isEmpty()) {
            Log.w(TAG, "No interstitial zone ID configured");
            if (callback != null) callback.onAdFailedToLoad("No zone ID");
            return;
        }

        GameAnalytics.get().trackAdRequested("interstitial", "post_game");

        TapsellPlus.requestInterstitialAd(currentActivity, interstitialZoneId,
                new AdRequestCallback() {
                    @Override
                    public void response(TapsellPlusAdModel adModel) {
                        Log.d(TAG, "Interstitial loaded");
                        interstitialResponseId = adModel.getResponseId();
                        interstitialReady = true;
                        GameAnalytics.get().trackAdLoaded("interstitial", "post_game");
                        if (callback != null) callback.onAdLoaded();
                    }

                    @Override
                    public void error(String error) {
                        Log.w(TAG, "Interstitial load failed: " + error);
                        interstitialReady = false;
                        interstitialResponseId = null;
                        GameAnalytics.get().trackAdFailed("interstitial", "post_game", error);
                        if (callback != null) callback.onAdFailedToLoad(error);
                    }
                });
    }

    private boolean showInterstitial(Activity activity, AdCallback callback) {
        if (!isAdReady(AdType.INTERSTITIAL)) {
            Log.d(TAG, "Interstitial not ready");
            // Try loading for next time
            loadInterstitial(null);
            return false;
        }

        String responseId = interstitialResponseId;
        // Mark as consumed immediately to prevent double-show
        interstitialReady = false;
        interstitialResponseId = null;

        TapsellPlus.showInterstitialAd(activity, responseId, new AdShowListener() {
            @Override
            public void onOpened(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Interstitial opened");
                GameAnalytics.get().trackAdShown("interstitial", "post_game", 0);
                if (callback != null) callback.onAdShown();
            }

            @Override
            public void onClosed(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Interstitial closed");
                if (callback != null) callback.onAdDismissed();
                // Preload next ad
                loadInterstitial(null);
            }

            @Override
            public void onError(TapsellPlusErrorModel errorModel) {
                String errorMsg = errorModel != null ? errorModel.getErrorMessage() : "unknown";
                Log.w(TAG, "Interstitial show error: " + errorMsg);
                GameAnalytics.get().trackAdFailed("interstitial", "post_game", errorMsg);
                if (callback != null) callback.onAdFailedToLoad(errorMsg);
                // Try loading again
                loadInterstitial(null);
            }

            @Override
            public void onRewarded(TapsellPlusAdModel adModel) {
                // Not used for interstitial
            }
        });

        return true;
    }

    // ==================== Rewarded ====================

    private void loadRewarded(AdCallback callback) {
        if (rewardedZoneId == null || rewardedZoneId.isEmpty()) {
            Log.w(TAG, "No rewarded zone ID configured — rewarded ads disabled");
            if (callback != null) callback.onAdFailedToLoad("No rewarded zone ID");
            return;
        }

        GameAnalytics.get().trackAdRequested("rewarded", "hint");

        TapsellPlus.requestRewardedVideoAd(currentActivity, rewardedZoneId,
                new AdRequestCallback() {
                    @Override
                    public void response(TapsellPlusAdModel adModel) {
                        Log.d(TAG, "Rewarded ad loaded");
                        rewardedResponseId = adModel.getResponseId();
                        rewardedReady = true;
                        GameAnalytics.get().trackAdLoaded("rewarded", "hint");
                        if (callback != null) callback.onAdLoaded();
                    }

                    @Override
                    public void error(String error) {
                        Log.w(TAG, "Rewarded ad load failed: " + error);
                        rewardedReady = false;
                        rewardedResponseId = null;
                        GameAnalytics.get().trackAdFailed("rewarded", "hint", error);
                        if (callback != null) callback.onAdFailedToLoad(error);
                    }
                });
    }

    private boolean showRewarded(Activity activity, AdCallback callback) {
        if (!isAdReady(AdType.REWARDED)) {
            Log.d(TAG, "Rewarded ad not ready");
            loadRewarded(null);
            return false;
        }

        String responseId = rewardedResponseId;
        // Mark as consumed immediately
        rewardedReady = false;
        rewardedResponseId = null;

        GameAnalytics.get().trackRewardedAdStarted("hint", "hint");

        TapsellPlus.showRewardedVideoAd(activity, responseId, new AdShowListener() {
            @Override
            public void onOpened(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Rewarded ad opened");
                GameAnalytics.get().trackAdShown("rewarded", "hint", 0);
                if (callback != null) callback.onAdShown();
            }

            @Override
            public void onClosed(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Rewarded ad closed");
                if (callback != null) callback.onAdDismissed();
                // Preload next rewarded ad
                loadRewarded(null);
            }

            @Override
            public void onError(TapsellPlusErrorModel errorModel) {
                String errorMsg = errorModel != null ? errorModel.getErrorMessage() : "unknown";
                Log.w(TAG, "Rewarded ad show error: " + errorMsg);
                GameAnalytics.get().trackRewardedAdFailed("hint", errorMsg);
                if (callback != null) callback.onAdFailedToLoad(errorMsg);
                // Try loading again
                loadRewarded(null);
            }

            @Override
            public void onRewarded(TapsellPlusAdModel adModel) {
                Log.d(TAG, "User earned reward!");
                GameAnalytics.get().trackRewardedAdCompleted("hint", "hint");
                if (callback != null) callback.onRewardEarned();
            }
        });

        return true;
    }

    // ==================== Utility ====================

    /**
     * Updates the activity reference (call when activity changes).
     */
    public void setActivity(Activity activity) {
        this.currentActivity = activity;
    }
}
