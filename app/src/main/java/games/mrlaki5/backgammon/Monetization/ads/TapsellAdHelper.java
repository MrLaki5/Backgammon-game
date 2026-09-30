package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;
import android.util.Log;

import ir.tapsell.plus.AdRequestCallback;
import ir.tapsell.plus.AdShowListener;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.model.TapsellPlusAdModel;
import ir.tapsell.plus.model.TapsellPlusErrorModel;

/**
 * Helper for Tapsell Plus Interstitial ads.
 * Shown between screens (after game ends, before results).
 *
 * Zone ID: 6a8b35a0f34d73758477ec0a
 *
 * Usage:
 *   TapsellAdHelper.requestAd(activity);       // Pre-fetch early
 *   TapsellAdHelper.showAdIfReady(activity);   // Show after game ends
 */
public final class TapsellAdHelper {

    private static final String TAG = "TapsellAd";
    public static final String ZONE_INTERSTITIAL = "6a8b35a0f34d73758477ec0a";

    private static String responseId = null;
    private static boolean adReady = false;

    private TapsellAdHelper() {}

    /**
     * Requests (pre-fetches) an interstitial ad.
     * Call in MenuActivity.onCreate() so it's ready when game ends.
     */
    public static void requestAd(Activity activity) {
        adReady = false;
        responseId = null;

        TapsellPlus.requestInterstitialAd(activity, ZONE_INTERSTITIAL, new AdRequestCallback() {
            @Override
            public void response(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Interstitial ad ready");
                responseId = adModel.getResponseId();
                adReady = true;
            }

            @Override
            public void error(String error) {
                Log.w(TAG, "Interstitial request failed: " + error);
                adReady = false;
                responseId = null;
            }
        });
    }

    /**
     * Shows the interstitial if ready. Call after game ends.
     * @return true if ad was shown
     */
    public static boolean showAdIfReady(Activity activity) {
        if (!adReady || responseId == null) {
            Log.d(TAG, "Ad not ready, requesting for next time");
            requestAd(activity);
            return false;
        }

        TapsellPlus.showInterstitialAd(activity, responseId, new AdShowListener() {
            @Override
            public void onOpened(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Ad opened");
            }

            @Override
            public void onClosed(TapsellPlusAdModel adModel) {
                Log.d(TAG, "Ad closed");
                adReady = false;
                responseId = null;
                // Pre-fetch next one
                requestAd(activity);
            }

            @Override
            public void onError(TapsellPlusErrorModel errorModel) {
                Log.w(TAG, "Ad show error: " + errorModel.getErrorMessage());
                adReady = false;
                responseId = null;
            }

            @Override
            public void onRewarded(TapsellPlusAdModel adModel) {
                // Not used for interstitial
            }
        });

        return true;
    }

    /**
     * Returns true if an ad is loaded and ready.
     */
    public static boolean isAdReady() {
        return adReady && responseId != null;
    }
}
