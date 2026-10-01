package games.mrlaki5.backgammon.Monetization.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import games.mrlaki5.backgammon.R;

/**
 * Shared UI logic for "watch ad for coins" entry points (menu button, shop button)
 * so both present the same reward amount, cooldown text, and reward-earned dialog.
 */
public final class RewardedAdUiHelper {

    private RewardedAdUiHelper() {}

    /**
     * Updates a rewarded-ad button's enabled state and label to reflect whether it's
     * ready, on cooldown (with a live mm:ss countdown), or out of uses for today.
     */
    public static void refreshButton(Activity activity, Button button,
                                      RewardedAdTracker tracker, RewardedAdPlacement placement,
                                      int coinAmount) {
        if (activity == null || button == null || tracker == null) return;

        if (tracker.isDailyLimitReached(placement)) {
            button.setEnabled(false);
            button.setText(activity.getString(R.string.ad_daily_limit_reached));
            return;
        }

        long remaining = tracker.getCooldownRemainingSeconds(placement);
        if (remaining > 0) {
            button.setEnabled(false);
            button.setText(activity.getString(R.string.ad_on_cooldown, formatMmSs(remaining)));
            return;
        }

        button.setEnabled(true);
        button.setText(activity.getString(R.string.watch_ad_for_coins, coinAmount));
    }

    private static String formatMmSs(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format(java.util.Locale.US, "%d:%02d", m, s);
    }

    /** Shows a small celebratory dialog confirming the coins were granted. */
    public static void showRewardDialog(Activity activity, int coinAmount, Runnable onDismiss) {
        if (activity == null || activity.isFinishing()) return;

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_reward_earned, null);
        TextView tvAmount = view.findViewById(R.id.dlgRewardAmount);
        if (tvAmount != null) {
            tvAmount.setText("+" + coinAmount + " 🪙");
        }

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.CustomBottomSheetDialogTheme)
                .setView(view)
                .setCancelable(true)
                .create();

        View btnOk = view.findViewById(R.id.dlgRewardOk);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.setOnDismissListener(d -> {
            if (onDismiss != null) onDismiss.run();
        });

        dialog.show();

        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(220L)
                .start();
    }
}
