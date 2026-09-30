package games.mrlaki5.backgammon.Analysis;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Monetization.ads.AdCallback;
import games.mrlaki5.backgammon.Monetization.ads.AdManager;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdPlacement;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdTracker;
import games.mrlaki5.backgammon.R;

/** Post-game review: every turn the player made, graded against the engine's best play. */
public class GameReviewActivity extends AppCompatActivity {

    private static final int COLOR_EXCELLENT = 0xFF4CAF50;
    private static final int COLOR_GOOD = 0xFF9CCC65;
    private static final int COLOR_MISTAKE = 0xFFFFB300;
    private static final int COLOR_BLUNDER = 0xFFE57373;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_review);

        View back = findViewById(R.id.btnReviewBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        List<TurnAnalysis> analyses = GameReviewData.take();
        TextView summary = findViewById(R.id.tvReviewSummary);
        LinearLayout container = findViewById(R.id.reviewList);
        if (summary == null || container == null) {
            return;
        }

        if (analyses.isEmpty()) {
            summary.setText(R.string.review_empty);
            return;
        }

        summary.setText(getString(R.string.review_accuracy, GameAnalyzer.accuracy(analyses),
                analyses.size()));
        setupRewardButton();
        for (TurnAnalysis analysis : analyses) {
            container.addView(row(analysis));
        }
    }

    /** The review itself is free; watching a video afterwards pays a few coins. */
    private void setupRewardButton() {
        Button reward = findViewById(R.id.btnReviewReward);
        if (reward == null) {
            return;
        }
        RewardedAdTracker tracker = new RewardedAdTracker(this);
        AdManager adManager = MenuActivity.getSharedAdManager();
        if (adManager == null || !tracker.canShow(RewardedAdPlacement.GAME_REVIEW)) {
            reward.setVisibility(View.GONE);
            return;
        }
        reward.setVisibility(View.VISIBLE);
        reward.setText(getString(R.string.review_reward_button, CoinConfig.GAME_REVIEW_REWARD));
        reward.setOnClickListener(v -> {
            if (!adManager.isRewardedAdReady()) {
                adManager.preloadAds();
                Toast.makeText(this, R.string.iap_purchase_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            adManager.showRewardedAd(this, RewardedAdPlacement.GAME_REVIEW, new AdCallback() {
                @Override public void onAdLoaded() {}
                @Override public void onAdFailedToLoad(String error) {}
                @Override public void onAdShown() {}
                @Override public void onAdDismissed() {}
                @Override public void onAdClicked() {}

                @Override
                public void onRewardEarned() {
                    new CoinManager(GameReviewActivity.this)
                            .earn(CoinConfig.GAME_REVIEW_REWARD, "game_review");
                    tracker.recordShow(RewardedAdPlacement.GAME_REVIEW);
                    runOnUiThread(() -> {
                        reward.setVisibility(View.GONE);
                        Toast.makeText(GameReviewActivity.this,
                                getString(R.string.coin_earned, CoinConfig.GAME_REVIEW_REWARD),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private View row(TurnAnalysis analysis) {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding((int) (10 * density), (int) (8 * density),
                (int) (10 * density), (int) (8 * density));

        TextView header = new TextView(this);
        header.setText(getString(R.string.review_turn_header, analysis.getTurnNumber(),
                analysis.getDiceLabel(), analysis.getPlayedNotation()));
        header.setTextColor(Color.WHITE);
        header.setTextSize(14f);
        row.addView(header);

        TextView verdict = new TextView(this);
        verdict.setTextColor(gradeColor(analysis.getGrade()));
        verdict.setTextSize(13f);
        verdict.setGravity(Gravity.START);
        String label = getString(gradeLabel(analysis.getGrade()));
        verdict.setText(analysis.isBestPlay()
                ? label
                : getString(R.string.review_better_move, label, analysis.getBestNotation()));
        row.addView(verdict);
        return row;
    }

    private static int gradeColor(TurnAnalysis.Grade grade) {
        switch (grade) {
            case EXCELLENT: return COLOR_EXCELLENT;
            case GOOD: return COLOR_GOOD;
            case MISTAKE: return COLOR_MISTAKE;
            default: return COLOR_BLUNDER;
        }
    }

    private static int gradeLabel(TurnAnalysis.Grade grade) {
        switch (grade) {
            case EXCELLENT: return R.string.review_grade_excellent;
            case GOOD: return R.string.review_grade_good;
            case MISTAKE: return R.string.review_grade_mistake;
            default: return R.string.review_grade_blunder;
        }
    }
}
