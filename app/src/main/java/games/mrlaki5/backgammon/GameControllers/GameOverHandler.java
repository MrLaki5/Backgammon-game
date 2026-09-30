package games.mrlaki5.backgammon.GameControllers;

import android.content.Intent;

import androidx.annotation.Nullable;

import games.mrlaki5.backgammon.Analysis.GameAnalyzer;
import games.mrlaki5.backgammon.Analysis.GameReviewActivity;
import games.mrlaki5.backgammon.Analysis.GameReviewData;
import games.mrlaki5.backgammon.Analysis.TurnAnalysis;
import games.mrlaki5.backgammon.Analysis.TurnRecord;

import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.model.WinType;
import com.royalbackgammon.core.scoring.MatchState;
import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.Menus.VariantPicker;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.GameResultRecorder;
import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Monetization.ads.AdCallback;
import games.mrlaki5.backgammon.Monetization.ads.AdManager;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdPlacement;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdTracker;
import games.mrlaki5.backgammon.R;
import games.mrlaki5.backgammon.Retention.AchievementManager;
import games.mrlaki5.backgammon.Retention.DailyChallenge;
import games.mrlaki5.backgammon.Retention.ReviewPromptManager;
import games.mrlaki5.backgammon.Retention.WeeklyChallenge;
import games.mrlaki5.backgammon.Util.DateUtil;
import games.mrlaki5.backgammon.WinStreakTracker;

/**
 * Handles game finish recording, coin economy rewards, win streaks, ELO updates,
 * analytics, ads, and redesigned game-over UI dialog.
 */
public class GameOverHandler {

    public interface OnGameOverActionListener {
        /** Turn history of the finished game, for the post-game review. */
        java.util.List<TurnRecord> onRequestTurnHistory();
        int onRequestHumanPlayer();
        void onPlayEffect(int effectId);
        void onRematch();
        void onChangeSettings(int winningPlayer, String p1Name, String p2Name, String gameMode);
        void onMainMenu(int winningPlayer, String p1Name, String p2Name, String gameMode);
    }

    private final Activity activity;
    private final CoinManager coinManager;
    private final PlayerProfileManager profileManager;
    private final RewardedAdTracker rewardedAdTracker;
    private final GameResultHandler gameResultHandler;
    private AlertDialog gameOverDialog;
    private boolean gameResultRecorded = false;
    private OnGameOverActionListener listener;

    public GameOverHandler(Activity activity, CoinManager coinManager) {
        this.activity = activity;
        this.coinManager = coinManager;
        this.profileManager = new PlayerProfileManager(activity);
        this.rewardedAdTracker = new RewardedAdTracker(activity);
        this.gameResultHandler = new GameResultHandler(activity, this.profileManager, coinManager);
    }

    public void setListener(OnGameOverActionListener listener) {
        this.listener = listener;
    }

    public boolean isResultRecorded() {
        return gameResultRecorded;
    }

    public GameResultHandler getGameResultHandler() {
        return gameResultHandler;
    }

    /**
     * Handles complete game over flow: stats recording, economy rewards, ads, dialog display.
     */
    public void handleGameFinished(int winningPlayer, String p1Name, String p2Name, String gameMode,
                                   boolean passAndPlayMode, boolean tutorialMode, boolean isRematchGame,
                                   int sessionGameNumber, long durationSeconds, String difficultyName,
                                   @Nullable GameResult gameResult, Variant variant, MatchState match,
                                   int botDifficulty) {
        boolean draw = winningPlayer == 0;
        if (!gameResultRecorded) {
            gameResultRecorded = true;
            if (!draw) {
                recordGameResult(winningPlayer, p1Name, p2Name, gameMode);
            }

            AdManager adMgr = MenuActivity.getSharedAdManager();
            if (adMgr != null) {
                adMgr.onGameCompleted();
            }
        }

        // Track analytics
        String winner = (winningPlayer == 1) ? "player" : "opponent";
        if (passAndPlayMode) {
            winner = (winningPlayer == 1) ? "player1" : "player2";
        }
        if (draw) {
            winner = "draw";
        }
        GameAnalytics.get().trackGameCompleted(gameMode, winner, durationSeconds,
                sessionGameNumber, 0, 0);

        if (!passAndPlayMode && !tutorialMode && !draw) {
            if (winningPlayer == 1) {
                GameAnalytics.get().trackGameWon(gameMode, difficultyName, durationSeconds);
            } else {
                GameAnalytics.get().trackGameLost(gameMode, difficultyName, durationSeconds);
            }
        }

        if (isRematchGame) {
            GameAnalytics.get().trackRematchCompleted(gameMode, difficultyName, winner);
        }

        // Delete save file
        File saveFile = new File(activity.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        saveFile.delete();

        // Update ELO rating & economy via GameResultHandler
        int points = gameResult != null ? gameResult.getPoints() : 1;
        GameResultHandler.ProcessedResult result = draw
                ? new GameResultHandler.ProcessedResult(0, 0, 0, 0, "")
                : gameResultHandler.processResult(winningPlayer == 1, gameMode, passAndPlayMode,
                        tutorialMode, variant, points, botDifficulty);

        final String winnerName = (winningPlayer == 1) ? p1Name : p2Name;
        final int streak = result.currentStreak;
        final int prevStreak = result.previousStreak;
        final int totalEarned = result.coinsEarned;
        final int delta = result.eloDelta;
        final String breakdown = result.coinBreakdown;

        activity.runOnUiThread(() -> {
            showInterstitialIfAllowed();
            showGameOverDialog(winnerName, winningPlayer, p1Name, p2Name, gameMode,
                    passAndPlayMode, tutorialMode, streak, prevStreak, totalEarned,
                    breakdown, delta, durationSeconds, sessionGameNumber, difficultyName,
                    gameResult, variant, match);
        });
    }

    private void recordGameResult(int winningPlayer, String p1Name, String p2Name, String gameMode) {
        // Legacy database
        try {
            DbHelper helper = new DbHelper(activity);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ScoresTableEntry.COLUMN_PLAYER1_NAME, p1Name);
            values.put(ScoresTableEntry.COLUMN_PLAYER2_NAME, p2Name);
            if (winningPlayer == 1) {
                values.put(ScoresTableEntry.COLUMN_PLAYER1_WIN, 1);
                values.put(ScoresTableEntry.COLUMN_PLAYER2_WIN, 0);
            } else {
                values.put(ScoresTableEntry.COLUMN_PLAYER1_WIN, 0);
                values.put(ScoresTableEntry.COLUMN_PLAYER2_WIN, 1);
            }
            Date currDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US);
            values.put(ScoresTableEntry.COLUMN_END_GAME_TIME, format.format(currDate));
            db.insert(ScoresTableEntry.TABLE_NAME, null, values);
        } catch (Exception e) {
            GameAnalytics.get().reportError(e, "recordGameResult_legacy");
        }

        // ELO/profile system
        try {
            GameResultRecorder recorder = new GameResultRecorder(activity);
            String winnerName = (winningPlayer == 1) ? p1Name : p2Name;
            String loserName = (winningPlayer == 1) ? p2Name : p1Name;
            recorder.recordResult(winnerName, loserName, gameMode);
        } catch (Exception e) {
            GameAnalytics.get().reportError(e, "recordGameResult_elo");
        }
    }

    private void showGameOverDialog(String winnerName, int winningPlayer,
                                    String p1Name, String p2Name, String gameMode,
                                    boolean passAndPlayMode, boolean tutorialMode,
                                    int winStreak, int prevStreak, int coinsEarned,
                                    String coinBreakdown, int eloDelta,
                                    long durationSec, int sessionGameNumber,
                                    String difficultyName, @Nullable GameResult gameResult,
                                    Variant variant, MatchState match) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (gameOverDialog != null && gameOverDialog.isShowing()) return;

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        // 1. Set winner text
        TextView winnerText = dialogView.findViewById(R.id.gameOverWinner);
        if (winnerText != null) {
            winnerText.setText(winningPlayer == 0
                    ? activity.getString(R.string.game_over_draw)
                    : activity.getString(R.string.game_over_winner, winnerName));
        }

        // 1b. Variant, win type and match score
        TextView resultTypeText = dialogView.findViewById(R.id.gameOverResultType);
        if (resultTypeText != null && gameResult != null && !tutorialMode) {
            resultTypeText.setVisibility(View.VISIBLE);
            resultTypeText.setText(activity.getString(R.string.game_over_result_line,
                    activity.getString(VariantPicker.nameRes(variant)),
                    activity.getString(winTypeRes(gameResult.getWinType())),
                    gameResult.getPoints()));
        }
        TextView matchText = dialogView.findViewById(R.id.gameOverMatchScore);
        boolean matchOngoing = match.getTargetPoints() > 1 && !match.isOver();
        if (matchText != null && match.getTargetPoints() > 1 && !tutorialMode) {
            matchText.setVisibility(View.VISIBLE);
            String score = activity.getString(R.string.game_over_match_score,
                    match.getTargetPoints(), p1Name, match.getWhiteScore(),
                    match.getRedScore(), p2Name);
            if (match.isOver()) {
                String matchWinner = match.winner() == 1 ? p1Name : p2Name;
                score += "\n" + activity.getString(R.string.game_over_match_winner, matchWinner);
            } else if (!match.getRotation().isEmpty()) {
                score += "  •  " + activity.getString(R.string.next_variant,
                        activity.getString(VariantPicker.nameRes(match.currentVariant())));
            }
            matchText.setText(score);
        }

        // 1c. Post-game review (only meaningful when a human actually played turns)
        Button btnReview = dialogView.findViewById(R.id.gameOverReview);
        if (btnReview != null && listener != null && !tutorialMode) {
            java.util.List<TurnRecord> history = listener.onRequestTurnHistory();
            int humanPlayer = listener.onRequestHumanPlayer();
            if (history != null && !history.isEmpty() && humanPlayer != 0) {
                btnReview.setVisibility(View.VISIBLE);
                btnReview.setOnClickListener(v -> startReview(btnReview, history, humanPlayer));
            }
        }

        // 2. Set game duration
        TextView durationText = dialogView.findViewById(R.id.gameOverDuration);
        if (durationText != null) {
            int minutes = (int) (durationSec / 60);
            int seconds = (int) (durationSec % 60);
            String durationStr = activity.getString(R.string.game_over_duration, minutes, seconds);
            if (sessionGameNumber > 1) {
                durationStr += "  •  " + activity.getString(R.string.game_over_game_number, sessionGameNumber);
            }
            durationText.setText(durationStr);
        }

        // 3. ELO Rating & Delta
        TextView eloText = dialogView.findViewById(R.id.gameOverEloDelta);
        if (eloText != null) {
            if (!passAndPlayMode && !tutorialMode && eloDelta != 0) {
                eloText.setVisibility(View.VISIBLE);
                String deltaStr = (eloDelta > 0)
                        ? activity.getString(R.string.elo_delta_positive, eloDelta)
                        : activity.getString(R.string.elo_delta_negative, eloDelta);
                eloText.setText(deltaStr + " (" + profileManager.getElo(variant) + ")");
                eloText.setTextColor((eloDelta > 0) ? Color.parseColor("#4CAF50") : Color.parseColor("#E57373"));
            } else {
                eloText.setVisibility(View.GONE);
            }
        }

        // 4. Coins Earned Breakdown
        LinearLayout coinsContainer = dialogView.findViewById(R.id.gameOverCoinsContainer);
        TextView tvCoinsEarned = dialogView.findViewById(R.id.gameOverCoinsEarned);
        TextView tvCoinsDetails = dialogView.findViewById(R.id.gameOverCoinsDetails);
        if (coinsContainer != null && tvCoinsEarned != null) {
            if (coinsEarned > 0) {
                coinsContainer.setVisibility(View.VISIBLE);
                tvCoinsEarned.setText(activity.getString(R.string.coin_earned, coinsEarned));
                if (tvCoinsDetails != null && !coinBreakdown.isEmpty()) {
                    tvCoinsDetails.setText(coinBreakdown);
                    tvCoinsDetails.setVisibility(View.VISIBLE);
                }
            } else {
                coinsContainer.setVisibility(View.GONE);
            }
        }

        // 5. Win streak / Daily Challenge status
        TextView streakText = dialogView.findViewById(R.id.gameOverStreak);
        if (streakText != null) {
            if (!passAndPlayMode && !tutorialMode && winningPlayer == 1 && winStreak >= 2) {
                streakText.setVisibility(View.VISIBLE);
                streakText.setText(activity.getString(R.string.game_over_win_streak, winStreak));
            } else if (!passAndPlayMode && !tutorialMode) {
                DailyChallenge dc = new DailyChallenge(activity);
                if (dc.isCompleted()) {
                    streakText.setVisibility(View.VISIBLE);
                    streakText.setText(activity.getString(R.string.daily_challenge_done));
                } else {
                    streakText.setVisibility(View.GONE);
                }
            } else {
                streakText.setVisibility(View.GONE);
            }
        }

        // 6. Double Reward Button (watch rewarded ad to 2x earned coins)
        Button btnDoubleReward = dialogView.findViewById(R.id.gameOverDoubleReward);
        if (btnDoubleReward != null) {
            if (winningPlayer == 1 && coinsEarned > 0 && !passAndPlayMode && !tutorialMode
                    && rewardedAdTracker.canShow(RewardedAdPlacement.DOUBLE_REWARD)) {
                btnDoubleReward.setVisibility(View.VISIBLE);
                btnDoubleReward.setOnClickListener(v -> {
                    AdManager adMgr = MenuActivity.getSharedAdManager();
                    if (adMgr != null && adMgr.isRewardedAdReady()) {
                        adMgr.showRewardedAd(activity, RewardedAdPlacement.DOUBLE_REWARD, new AdCallback() {
                            @Override
                            public void onAdLoaded() {}

                            @Override
                            public void onAdFailedToLoad(String error) {
                                activity.runOnUiThread(() -> Toast.makeText(activity,
                                        R.string.iap_purchase_failed, Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onAdShown() {}

                            @Override
                            public void onAdDismissed() {}

                            @Override
                            public void onAdClicked() {}

                            @Override
                            public void onRewardEarned() {
                                if (coinManager != null) {
                                    coinManager.earn(coinsEarned, "double_reward");
                                }
                                rewardedAdTracker.recordShow(RewardedAdPlacement.DOUBLE_REWARD);
                                GameAnalytics.get().trackRewardedAdWatched("double_reward");
                                activity.runOnUiThread(() -> {
                                    btnDoubleReward.setVisibility(View.GONE);
                                    if (tvCoinsEarned != null) {
                                        tvCoinsEarned.setText(activity.getString(R.string.coin_earned, coinsEarned * 2));
                                    }
                                    Toast.makeText(activity, activity.getString(R.string.coin_earned, coinsEarned), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } else {
                        if (adMgr != null) adMgr.preloadAds();
                        Toast.makeText(activity, R.string.iap_purchase_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnDoubleReward.setVisibility(View.GONE);
            }
        }

        // 7. Save Streak Button (watch rewarded ad on loss if streak >= 3)
        Button btnSaveStreak = dialogView.findViewById(R.id.gameOverSaveStreak);
        if (btnSaveStreak != null) {
            if (winningPlayer == 2 && prevStreak >= 3 && !passAndPlayMode && !tutorialMode
                    && rewardedAdTracker.canShow(RewardedAdPlacement.SAVE_STREAK)) {
                btnSaveStreak.setVisibility(View.VISIBLE);
                btnSaveStreak.setText(activity.getString(R.string.save_streak_desc, prevStreak));
                btnSaveStreak.setOnClickListener(v -> {
                    AdManager adMgr = MenuActivity.getSharedAdManager();
                    if (adMgr != null && adMgr.isRewardedAdReady()) {
                        adMgr.showRewardedAd(activity, RewardedAdPlacement.SAVE_STREAK, new AdCallback() {
                            @Override
                            public void onAdLoaded() {}

                            @Override
                            public void onAdFailedToLoad(String error) {
                                activity.runOnUiThread(() -> Toast.makeText(activity,
                                        R.string.iap_purchase_failed, Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onAdShown() {}

                            @Override
                            public void onAdDismissed() {}

                            @Override
                            public void onAdClicked() {}

                            @Override
                            public void onRewardEarned() {
                                WinStreakTracker streakTracker = new WinStreakTracker(activity);
                                streakTracker.restoreStreak(prevStreak);
                                rewardedAdTracker.recordShow(RewardedAdPlacement.SAVE_STREAK);
                                GameAnalytics.get().trackRewardedAdWatched("save_streak");
                                activity.runOnUiThread(() -> {
                                    btnSaveStreak.setVisibility(View.GONE);
                                    if (streakText != null) {
                                        streakText.setVisibility(View.VISIBLE);
                                        streakText.setText(activity.getString(R.string.game_over_win_streak, prevStreak));
                                    }
                                    Toast.makeText(activity, R.string.iap_purchase_success, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } else {
                        if (adMgr != null) adMgr.preloadAds();
                        Toast.makeText(activity, R.string.iap_purchase_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnSaveStreak.setVisibility(View.GONE);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        builder.setCancelable(false);
        gameOverDialog = builder.create();

        if (gameOverDialog.getWindow() != null) {
            gameOverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            gameOverDialog.getWindow().setDimAmount(0.75f);
        }

        // Rematch button
        Button btnRematch = dialogView.findViewById(R.id.gameOverRematch);
        if (btnRematch != null) {
            if (matchOngoing) {
                btnRematch.setText(R.string.next_game);
            }
            btnRematch.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                GameAnalytics.get().trackRematchClicked(gameMode, difficultyName);
                gameOverDialog.dismiss();
                if (listener != null) {
                    listener.onRematch();
                }
            });
        }

        // Main Menu button
        View btnMainMenu = dialogView.findViewById(R.id.gameOverMainMenu);
        if (btnMainMenu != null) {
            btnMainMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                gameOverDialog.dismiss();
                if (winningPlayer == 1 && !passAndPlayMode && !tutorialMode) {
                    showReviewIfEligible(() -> {
                        if (listener != null) {
                            listener.onMainMenu(winningPlayer, p1Name, p2Name, gameMode);
                        }
                    });
                } else {
                    if (listener != null) {
                        listener.onMainMenu(winningPlayer, p1Name, p2Name, gameMode);
                    }
                }
            });
        }

        gameOverDialog.show();
    }

    /** Grades the game on a background thread, then opens the review screen. */
    private void startReview(Button trigger, java.util.List<TurnRecord> history, int humanPlayer) {
        trigger.setEnabled(false);
        trigger.setText(R.string.review_working);
        new Thread(() -> {
            java.util.List<TurnAnalysis> analyses = new GameAnalyzer().analyze(history, humanPlayer);
            activity.runOnUiThread(() -> {
                trigger.setEnabled(true);
                trigger.setText(R.string.review_button);
                GameReviewData.put(analyses);
                activity.startActivity(new Intent(activity, GameReviewActivity.class));
            });
        }, "game-review").start();
    }

    private static int winTypeRes(WinType type) {
        switch (type) {
            case GAMMON: return R.string.win_type_gammon;
            case BACKGAMMON: return R.string.win_type_backgammon;
            case MOTHER_PINNED: return R.string.win_type_mother;
            case DRAW: return R.string.win_type_draw;
            default: return R.string.win_type_single;
        }
    }

    private void showInterstitialIfAllowed() {
        AdManager adMgr = MenuActivity.getSharedAdManager();
        if (adMgr != null) {
            adMgr.showInterstitialIfReady(activity, null);
        }
    }

    private void showReviewIfEligible(Runnable afterAction) {
        ReviewPromptManager reviewManager = new ReviewPromptManager(activity);
        reviewManager.onGameCompleted();

        if (!reviewManager.shouldShowPrompt()) {
            afterAction.run();
            return;
        }

        reviewManager.onPromptShown();

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_review, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog reviewDialog = builder.create();

        if (reviewDialog.getWindow() != null) {
            reviewDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            reviewDialog.getWindow().setDimAmount(0.75f);
        }

        View btnYes = dialogView.findViewById(R.id.reviewYes);
        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                reviewManager.onUserAccepted();
                reviewDialog.dismiss();
                ReviewPromptManager.openStorePage(activity);
                afterAction.run();
            });
        }

        View btnLater = dialogView.findViewById(R.id.reviewLater);
        if (btnLater != null) {
            btnLater.setOnClickListener(v -> {
                reviewManager.onPromptDismissed();
                reviewDialog.dismiss();
                afterAction.run();
            });
        }

        View btnNever = dialogView.findViewById(R.id.reviewNever);
        if (btnNever != null) {
            btnNever.setOnClickListener(v -> {
                reviewManager.onPromptDismissed();
                reviewManager.onPromptDismissed();
                reviewManager.onPromptDismissed();
                reviewDialog.dismiss();
                afterAction.run();
            });
        }

        reviewDialog.show();
    }

    public void dismissDialog() {
        if (gameOverDialog != null && gameOverDialog.isShowing()) {
            gameOverDialog.dismiss();
        }
    }

    public boolean isDialogShowing() {
        return gameOverDialog != null && gameOverDialog.isShowing();
    }
}
