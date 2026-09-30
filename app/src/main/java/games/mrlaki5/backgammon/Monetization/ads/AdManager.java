package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages ad display logic including frequency capping and premium status checks.
 *
 * Rules (from product spec — critical for user retention):
 * - Interstitial: ONLY between games, max 1 per 3 completed games
 * - Rewarded: Available on-demand for hints/retry, no frequency cap
 * - NEVER show ads mid-game or mid-turn
 * - NEVER show banner ads on game screen
 * - If user purchased "remove ads", no interstitials shown (rewarded still available optionally)
 */
public class AdManager {

    private static final String PREFS_NAME = "ad_manager_prefs";
    private static final String KEY_GAMES_SINCE_LAST_AD = "games_since_ad";

    private final AdProvider adProvider;
    private final SharedPreferences prefs;
    private final RewardedAdTracker rewardedAdTracker;
    private final Context appContext;
    private boolean adsRemoved = false;

    public AdManager(Context context, AdProvider adProvider) {
        this(context, adProvider, new RewardedAdTracker(context));
    }

    public AdManager(Context context, AdProvider adProvider, RewardedAdTracker rewardedAdTracker) {
        this.adProvider = adProvider;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.rewardedAdTracker = rewardedAdTracker;
        Context application = context.getApplicationContext();
        this.appContext = application != null ? application : context;
    }

    public RewardedAdTracker getRewardedAdTracker() {
        return rewardedAdTracker;
    }

    /**
     * Initializes the ad SDK. Does NOT preload: preloading touches the network and
     * must never run on the frame that builds the first screen. Callers schedule
     * {@link #preloadAds()} themselves once their UI is visible.
     */
    public void initialize(Activity activity) {
        adProvider.initialize(activity);
    }

    /**
     * Sets whether ads have been removed by IAP.
     * When true, interstitial ads will never show.
     */
    public void setAdsRemoved(boolean removed) {
        this.adsRemoved = removed;
    }

    public boolean areAdsRemoved() {
        return adsRemoved;
    }

    /**
     * Call after each completed game to track frequency.
     * Returns true if an interstitial should be shown.
     */
    public boolean onGameCompleted() {
        if (adsRemoved) return false;

        int count = prefs.getInt(KEY_GAMES_SINCE_LAST_AD, 0) + 1;
        prefs.edit().putInt(KEY_GAMES_SINCE_LAST_AD, count).apply();

        return count >= AdConfig.INTERSTITIAL_EVERY_N_GAMES;
    }

    /**
     * Shows an interstitial ad if the frequency cap allows it.
     * Call this from the results screen or between games.
     *
     * @return true if an ad was shown
     */
    public boolean showInterstitialIfReady(Activity activity, AdCallback callback) {
        if (adsRemoved) return false;

        int count = prefs.getInt(KEY_GAMES_SINCE_LAST_AD, 0);
        if (count < AdConfig.INTERSTITIAL_EVERY_N_GAMES) return false;

        if (!adProvider.isAdReady(AdType.INTERSTITIAL)) {
            // Not ready — try loading for next time
            adProvider.loadAd(AdType.INTERSTITIAL, null);
            return false;
        }

        // Reset counter and show
        prefs.edit().putInt(KEY_GAMES_SINCE_LAST_AD, 0).apply();
        return adProvider.showAd(activity, AdType.INTERSTITIAL, new AdCallback() {
            @Override
            public void onAdLoaded() {}

            @Override
            public void onAdFailedToLoad(String error) {
                if (callback != null) callback.onAdFailedToLoad(error);
            }

            @Override
            public void onAdShown() {
                if (callback != null) callback.onAdShown();
            }

            @Override
            public void onAdDismissed() {
                // Preload next ad
                adProvider.loadAd(AdType.INTERSTITIAL, null);
                if (callback != null) callback.onAdDismissed();
            }

            @Override
            public void onAdClicked() {
                if (callback != null) callback.onAdClicked();
            }
        });
    }

    /**
     * Shows a rewarded ad for the given placement (hints, retry, double reward, etc).
     * Enforces daily limits and cooldowns through RewardedAdTracker.
     *
     * @return true if the ad was shown
     */
    public boolean showRewardedAd(Activity activity, RewardedAdPlacement placement, AdCallback callback) {
        if (rewardedAdTracker != null && !rewardedAdTracker.canShow(placement)) {
            if (callback != null) {
                callback.onAdFailedToLoad("Ad cap reached");
            }
            return false;
        }

        if (!adProvider.isAdReady(AdType.REWARDED)) {
            adProvider.loadAd(AdType.REWARDED, null);
            return false;
        }

        return adProvider.showAd(activity, AdType.REWARDED, new AdCallback() {
            @Override
            public void onAdLoaded() {
                if (callback != null) callback.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(String error) {
                if (callback != null) callback.onAdFailedToLoad(error);
            }

            @Override
            public void onAdShown() {
                if (callback != null) callback.onAdShown();
            }

            @Override
            public void onAdDismissed() {
                adProvider.loadAd(AdType.REWARDED, null);
                if (callback != null) callback.onAdDismissed();
            }

            @Override
            public void onAdClicked() {
                if (callback != null) callback.onAdClicked();
            }

            @Override
            public void onRewardEarned() {
                if (rewardedAdTracker != null) {
                    rewardedAdTracker.recordShow(placement);
                }
                if (callback != null) callback.onRewardEarned();
            }
        });
    }

    public boolean showRewardedAd(RewardedAdPlacement placement, AdCallback callback) {
        return showRewardedAd(null, placement, callback);
    }

    /**
     * Returns true if a rewarded ad is ready (for showing hint button state).
     */
    public boolean isRewardedAdReady() {
        return adProvider.isAdReady(AdType.REWARDED);
    }

    /**
     * Preloads ads for next use.
     */
    public void preloadAds() {
        if (!games.mrlaki5.backgammon.Diagnostics.Connectivity.isOnline(appContext)) {
            return;
        }
        if (!games.mrlaki5.backgammon.BackgammonApp.areAdsReady()) {
            // Requesting before the SDK reports success makes the request wait on
            // initialization — on the main thread. Later call sites retry.
            return;
        }
        if (!adsRemoved) {
            adProvider.loadAd(AdType.INTERSTITIAL, null);
        }
        adProvider.loadAd(AdType.REWARDED, null);
    }

    /**
     * Releases ad resources.
     */
    public void destroy() {
        adProvider.destroy();
    }
}
