package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.FrameLayout;
import android.widget.Spinner;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.Retention.DailyLoginManager;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.MenuAudioManager;
import games.mrlaki5.backgammon.Monetization.ads.AdCallback;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdPlacement;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdTracker;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdUiHelper;
import games.mrlaki5.backgammon.Journey.JourneyActivity;
import games.mrlaki5.backgammon.Challenge.ChallengeCode;
import games.mrlaki5.backgammon.Challenge.ChallengeHistory;
import com.royalbackgammon.core.variant.Variant;
import games.mrlaki5.backgammon.Retention.DailyChallenge;
import games.mrlaki5.backgammon.Util.DateUtil;
import games.mrlaki5.backgammon.R;

//Activity class for main menu
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    //Player1 name key intent value
    public static String EXTRA_PLAYER1_NAME="p1name";
    //Player2 name key intent value
    public static String EXTRA_PLAYER2_NAME="p2name";
    //Player1 kind key intent value
    public static String EXTRA_PLAYER1_KIND="p1kind";
    //Player2 kind key intent value
    public static String EXTRA_PLAYER2_KIND="p2kind";
    public static final String EXTRA_VARIANT="variant";
    public static final String EXTRA_MATCH_TARGET="matchTarget";
    public static final String EXTRA_TAVLI="tavliRotation";
    public static final String EXTRA_BOT_DIFFICULTY="botDifficulty";
    public static final String EXTRA_JOURNEY_STAGE="journeyStage";
    public static final String EXTRA_BOARD_THEME="boardTheme";
    public static final String EXTRA_DICE_SEED="diceSeed";
    public static final String EXTRA_MATCH_WHITE_SCORE="matchWhiteScore";
    public static final String EXTRA_MATCH_RED_SCORE="matchRedScore";
    public static final String EXTRA_MATCH_GAMES="matchGames";
    //Wining player key intent value
    public static String EXTRA_WINING_PLAYER="pWin";
    public static String EXTRA_TUTORIAL_MODE="tutorialMode";
    public static String EXTRA_GAME_MODE="gameMode";
    public static String GAME_MODE_PASS_AND_PLAY="pass_and_play";
    public static String GAME_MODE_VS_BOT="vs_bot";
    //Name of save file
    public static String GAME_CONTINUE_SAVE_FILE_NAME="gameSave";
    //Value of return int after game finishes for back pressed
    public static final int GAME_PRESSED_BACK=55;
    //Value of return int after game finishes for player won
    public static final int GAME_ENDED_OK=56;
    //Value of send int to starting a game
    public static final int REQUEST_CODE_GAME=65;
    //Dialog opened before new game starts
    private AlertDialog myDialog;
    //View of dialog opened before new game starts
    private View myView;
    private GameAudio gameAudio;
    private games.mrlaki5.backgammon.Monetization.ads.AdManager adManager;
    private CoinManager coinManager;
    private RewardedAdTracker rewardedAdTracker;
    private games.mrlaki5.backgammon.Database.PlayerProfileManager profileManager;
    private android.widget.TextView tvCoinBalance;
    private android.widget.TextView tvEloRating;
    private Button btnFreeCoins;
    private final android.os.Handler freeCoinsTickHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable freeCoinsTick = new Runnable() {
        @Override
        public void run() {
            refreshFreeCoinsButton();
            freeCoinsTickHandler.postDelayed(this, 1000L);
        }
    };

    /**
     * Returns the shared AdManager instance.
     * Can be used by GameActivity for rewarded ads (hints).
     */
    public games.mrlaki5.backgammon.Monetization.ads.AdManager getAdManager() {
        return adManager;
    }

    // Static reference for GameActivity to access the ad manager
    private static games.mrlaki5.backgammon.Monetization.ads.AdManager sharedAdManager;

    public static games.mrlaki5.backgammon.Monetization.ads.AdManager getSharedAdManager() {
        return sharedAdManager;
    }

    //Listener used to catch cancel button click on new game dialog
    private View.OnClickListener CancelListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playMenuTap();
            //Delete dialog
            if (myDialog != null){
                myDialog.dismiss();
                myDialog = null;
                myView=null;
            }
        }
    };

    //Listener used to catch play button click on new game dialog
    private View.OnClickListener PlayListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playMenuTap();
            if (myDialog != null && myView!=null){
                //Load player1 name and player2 name from dialog
                String playerName1=((EditText)myView.findViewById(R.id.dialogPName1))
                        .getText().toString();
                String playerName2=((EditText)myView.findViewById(R.id.dialogPName2))
                        .getText().toString();
                //If player names are written
                if((!playerName1.isEmpty()) && (!playerName2.isEmpty())){
                    //Load id of checked radio button of player kind
                    int idRGp1=((RadioGroup)myView.findViewById(R.id.dialogRadioGP1))
                            .getCheckedRadioButtonId();
                    int idRGp2=((RadioGroup)myView.findViewById(R.id.dialogRadioGP2))
                            .getCheckedRadioButtonId();
                    //If player kinds are chosen
                    if(idRGp1!=-1 && idRGp2!=-1){
                        //Load player kinds from dialog view
                        String playerKind1 =
                                (idRGp1 == R.id.radioButton) ? "Player" : "Bot";
                        String playerKind2 =
                                (idRGp2 == R.id.radioButton3) ? "Player" : "Bot";
                        Spinner difficulty = myView.findViewById(R.id.dialogBotDifficulty);
                        Spinner theme = myView.findViewById(R.id.dialogBoardTheme);
                        GamePreferences.saveSelections(MenuActivity.this,
                                difficulty.getSelectedItemPosition(),
                                theme.getSelectedItemPosition());
                        //Delete dialog
                        myDialog.dismiss();
                        myDialog = null;
                        myView=null;
                        //If file with saved game exists, delete it
                        File file=new File(MenuActivity.this.getFilesDir().getAbsolutePath(),
                                MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
                        file.delete();
                        //Start game activity with new players
                        Intent intent=
                                new Intent(MenuActivity.this, GameActivity.class);
                        intent.putExtra(EXTRA_PLAYER1_NAME, playerName1);
                        intent.putExtra(EXTRA_PLAYER2_NAME, playerName2);
                        intent.putExtra(EXTRA_PLAYER1_KIND, playerKind1);
                        intent.putExtra(EXTRA_PLAYER2_KIND, playerKind2);
                        startActivityForResult(intent, REQUEST_CODE_GAME);
                    }
                }
            }
        }
    };

    //Method called on creation of MenuActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
        gameAudio = new GameAudio(this);
        // Initialize ad manager with real TapsellAdProvider
        adManager = new games.mrlaki5.backgammon.Monetization.ads.AdManager(this,
                new games.mrlaki5.backgammon.Monetization.ads.TapsellAdProvider(
                        games.mrlaki5.backgammon.Monetization.ads.AdConfig.ZONE_INTERSTITIAL,
                        games.mrlaki5.backgammon.Monetization.ads.AdConfig.ZONE_REWARDED));
        adManager.initialize(this);
        sharedAdManager = adManager;
        // Preloading talks to the network; do it after the menu is on screen
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    try {
                        if (!games.mrlaki5.backgammon.Diagnostics.Connectivity.isOnline(this)) {
                            return;
                        }
                        adManager.preloadAds();
                    } catch (Throwable t) {
                        android.util.Log.w("MenuActivity", "Ad preload failed", t);
                    }
                }, 2500L);
        //Load preferences
        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        //If values in preferences dont exist (on first start), create them
        if(!preferences.contains(SettingsActivity.KEY_DICE_TRESHOLD)){
            SharedPreferences.Editor editor=preferences.edit();
            //Set shake treshold value
            editor.putInt(SettingsActivity.KEY_DICE_TRESHOLD,
                    SettingsActivity.DEF_DICE_TRAESHOLD);
            //Set time value between two shake sensor events
            editor.putInt(SettingsActivity.KEY_TIME_SAMPLE,
                    SettingsActivity.DEF_TIME_SAMPLE);
            //Set value of sound volume
            editor.putInt(SettingsActivity.KEY_SOUND_VOLUME,
                    SettingsActivity.DEF_SOUND_VOLUME);
            editor.putInt(GamePreferences.KEY_SFX_VOLUME,
                    GamePreferences.DEFAULT_SFX_VOLUME);
            editor.putInt(GamePreferences.KEY_MUSIC_VOLUME,
                    GamePreferences.DEFAULT_MUSIC_VOLUME);
            editor.putBoolean(SettingsActivity.KEY_SOUND_ENABLED,
                    SettingsActivity.DEF_SOUND_ENABLED);
            editor.putBoolean(SettingsActivity.KEY_EFFECTS_ENABLED,
                    SettingsActivity.DEF_EFFECTS_ENABLED);
            //Set value of shake delays
            editor.putInt(SettingsActivity.KEY_DICE_SHAKE_DELAY,
                    SettingsActivity.DEF_DICE_SHAKE_DELAY);
            //Set value of time between turns in game
            editor.putInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS,
                    SettingsActivity.DEF_TIME_BETWEEN_TURNS);
            //Set default shake treshold value
            editor.putInt(SettingsActivity.KEY_DEF_DICE_TRESHOLD,
                    SettingsActivity.DEF_DICE_TRAESHOLD);
            //Set default time value between two shake sensor events
            editor.putInt(SettingsActivity.KEY_DEF_TIME_SAMPLE,
                    SettingsActivity.DEF_TIME_SAMPLE);
            //Set default value of sound volume
            editor.putInt(SettingsActivity.KEY_DEF_SOUND_VOLUME,
                    SettingsActivity.DEF_SOUND_VOLUME);
            //Set default value of shake delays
            editor.putInt(SettingsActivity.KEY_DEF_DICE_SHAKE_DELAY,
                    SettingsActivity.DEF_DICE_SHAKE_DELAY);
            //Set default value of time between turns in game
            editor.putInt(SettingsActivity.KEY_DEF_TIME_BETWEEN_TURNS,
                    SettingsActivity.DEF_TIME_BETWEEN_TURNS);
            editor.apply();
        }
        //Change color of continue game button from gray to yellow if save file exists
        checkAndChangeButtonColor();
        polishMenuButtons();

        coinManager = new CoinManager(this);
        rewardedAdTracker = new RewardedAdTracker(this);
        profileManager = new games.mrlaki5.backgammon.Database.PlayerProfileManager(this);
        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        tvEloRating = findViewById(R.id.tvEloRating);
        btnFreeCoins = findViewById(R.id.btnFreeCoins);
        updateCoinDisplay();

        // Start menu background music
        // Decoding the music track is not worth a stutter on the first frame
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> MenuAudioManager.get().startMenuMusic(this), 600L);
    }

    @Override
    protected void onResume() {
        super.onResume();
        freeCoinsTickHandler.removeCallbacks(freeCoinsTick);
        freeCoinsTickHandler.post(freeCoinsTick);
        bindDailyChallenge();
        if (profileManager != null) {
            profileManager.checkAndExpireRentals();
        } else {
            new PlayerProfileManager(this).checkAndExpireRentals();
        }

        DailyLoginManager.LoginResult loginResult = DailyLoginManager.checkDailyLogin(this);
        if (loginResult.isNewDay) {
            showDailyLoginDialog(loginResult);
        }

        MenuAudioManager.get().startMenuMusic(this);
        updateCoinDisplay();
    }

    private void showDailyLoginToast(DailyLoginManager.LoginResult result) {
        String msg = getString(R.string.daily_login_reward,
                result.streak, result.coinsEarned);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void showDailyLoginDialog(DailyLoginManager.LoginResult result) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_daily_login, null);
        TextView tvCoins = view.findViewById(R.id.dlgCoins);
        if (tvCoins != null) {
            tvCoins.setText(getString(R.string.journey_reward_line, result.coinsEarned));
        }
        LinearLayout dayRow = view.findViewById(R.id.dlgDayRow);
        if (dayRow != null) {
            for (int i = 0; i < dayRow.getChildCount(); i++) {
                View dayCard = dayRow.getChildAt(i);
                if (dayCard instanceof ViewGroup) {
                    View dayCircle = ((ViewGroup) dayCard).getChildAt(0);
                    if (dayCircle != null) {
                        int color = (i < result.streak) ? 0xFF4CAF50 : 0xFF616161;
                        dayCircle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                    }
                    View dayLabel = ((ViewGroup) dayCard).getChildAt(1);
                    if (dayLabel instanceof TextView) {
                        ((TextView) dayLabel).setText(getString(R.string.daily_login_day, i + 1));
                    }
                }
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomBottomSheetDialogTheme)
                .setView(view)
                .setCancelable(false)
                .create();
        View btnOk = view.findViewById(R.id.dlgOk);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                updateCoinDisplay();
            });
        }
        dialog.show();
    }

    private void updateCoinDisplay() {
        if (tvCoinBalance != null && coinManager != null) {
            tvCoinBalance.setText(String.valueOf(coinManager.getBalance()));
        }
        if (tvEloRating != null && profileManager != null) {
            tvEloRating.setText(getString(R.string.elo_rating_format, profileManager.getElo()));
        }
        refreshFreeCoinsButton();
    }

    private void refreshFreeCoinsButton() {
        if (btnFreeCoins == null || rewardedAdTracker == null) return;
        RewardedAdUiHelper.refreshButton(this, btnFreeCoins, rewardedAdTracker,
                RewardedAdPlacement.FREE_COINS, CoinConfig.REWARDED_AD_WATCH);
    }

    public void onFreeCoinsClicked(View v) {
        playMenuTap();
        if (rewardedAdTracker != null && rewardedAdTracker.isDailyLimitReached(RewardedAdPlacement.FREE_COINS)) {
            android.widget.Toast.makeText(this, R.string.ad_daily_limit_reached, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (rewardedAdTracker != null && rewardedAdTracker.getCooldownRemainingSeconds(RewardedAdPlacement.FREE_COINS) > 0) {
            refreshFreeCoinsButton();
            return;
        }
        if (adManager != null && adManager.isRewardedAdReady()) {
            adManager.showRewardedAd(this, RewardedAdPlacement.FREE_COINS, new AdCallback() {
                @Override
                public void onAdLoaded() {}

                @Override
                public void onAdFailedToLoad(String error) {
                    runOnUiThread(() -> android.widget.Toast.makeText(MenuActivity.this,
                            R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show());
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
                        coinManager.earn(CoinConfig.REWARDED_AD_WATCH, "free_coins");
                    }
                    if (rewardedAdTracker != null) {
                        rewardedAdTracker.recordShow(RewardedAdPlacement.FREE_COINS);
                    }
                    GameAnalytics.get().trackRewardedAdWatched("free_coins");
                    runOnUiThread(() -> {
                        updateCoinDisplay();
                        RewardedAdUiHelper.showRewardDialog(MenuActivity.this,
                                CoinConfig.REWARDED_AD_WATCH, MenuActivity.this::updateCoinDisplay);
                    });
                }
            });
        } else {
            if (adManager != null) adManager.preloadAds();
            android.widget.Toast.makeText(this, R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    public void openCoinShop(View v) {
        playMenuTap();
        Intent intent = new Intent(this, games.mrlaki5.backgammon.Economy.CoinShopActivity.class);
        startActivity(intent);
    }

    public void openPlayerStats(View v) {
        playMenuTap();
        Intent intent = new Intent(this, games.mrlaki5.backgammon.PlayerStatsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        freeCoinsTickHandler.removeCallbacks(freeCoinsTick);
        MenuAudioManager.get().pauseMenuMusic();
    }

    private void polishMenuButtons() {
        int[] buttonIds = {
                R.id.playGame,
                R.id.passAndPlay,
                R.id.journey,
                R.id.tutorial,
                R.id.scores,
                R.id.btnShop,
                R.id.settings,
                R.id.languageToggle
        };
        for (int i = 0; i < buttonIds.length; i++) {
            View button = findViewById(buttonIds[i]);
            if (button == null) {
                continue;
            }
            button.setOnTouchListener(menuButtonTouchListener);
            button.setAlpha(0F);
            button.setTranslationY(18F);
            button.animate()
                    .alpha(1F)
                    .translationY(0F)
                    .setStartDelay(60L + (i * 45L))
                    .setDuration(260L)
                    .setInterpolator(new OvershootInterpolator(0.72F))
                    .start();
        }
        // Gold does not sit still: a highlight crosses the primary action every
        // few seconds, and it breathes just enough to read as the live one.
        View glint = findViewById(R.id.playGameGlint);
        if (glint != null) {
            RoyalMotion.shimmer(glint);
        }
        View hero = findViewById(R.id.playGameFrame);
        if (hero != null) {
            RoyalMotion.breathe(hero);
        }
        View crest = findViewById(R.id.royalLogo);
        if (crest != null) {
            RoyalMotion.settleIn(crest, 120L);
        }
        View title = findViewById(R.id.menuTitle);
        if (title != null) {
            RoyalMotion.settleIn(title, 60L);
        }

        View panel = findViewById(R.id.menuPanel);
        if (panel != null) {
            panel.setScaleX(0.985F);
            panel.setScaleY(0.985F);
            panel.animate()
                    .scaleX(1F)
                    .scaleY(1F)
                    .setDuration(360L)
                    .setInterpolator(new OvershootInterpolator(0.42F))
                    .start();
        }
    }

    private final View.OnTouchListener menuButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.975F).scaleY(0.975F).setDuration(70L).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1F).scaleY(1F).setDuration(120L).start();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void openPlayOptions(View view) {
        playMenuTap();
        GameAnalytics.get().trackMenuPlayClicked("vs_bot");
        if (checkContinueGame()) {
            showPlayChoiceDialog();
        } else {
            showNewGameDialog();
        }
    }

    private void showPlayChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View choiceView = getLayoutInflater().inflate(R.layout.play_choice_dialog, null);
        builder.setView(choiceView);
        AlertDialog choiceDialog = builder.create();

        choiceView.findViewById(R.id.choiceContinue).setOnClickListener(v -> {
            playMenuTap();
            choiceDialog.dismiss();
            continueSavedGame();
        });
        choiceView.findViewById(R.id.choiceNewGame).setOnClickListener(v -> {
            playMenuTap();
            choiceDialog.dismiss();
            showNewGameDialog();
        });

        choiceDialog.show();
        Window dialogWindow = choiceDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    //Method called when new game is chosen
    public void startNewGame(View view) {
        playMenuTap();
        showNewGameDialog();
    }

    private void showNewGameDialog() {
        NewGameDialogFragment fragment = new NewGameDialogFragment();
        fragment.show(getSupportFragmentManager(), "new_game");
    }

    private void updateThemeSelection(FrameLayout[] thumbs, int selected) {
        for (int i = 0; i < thumbs.length; i++) {
            thumbs[i].setBackgroundResource(i == selected
                    ? R.drawable.bg_theme_thumb_selected
                    : R.drawable.bg_theme_thumb_normal);
            thumbs[i].setAlpha(i == selected ? 1.0f : 0.6f);
        }
        if (selected >= 0 && selected < thumbs.length && thumbs[selected] != null) {
            View selectedThumbView = thumbs[selected];
            selectedThumbView.animate().scaleX(1.12f).scaleY(1.12f).setDuration(100)
                    .withEndAction(() -> selectedThumbView.animate().scaleX(1f).scaleY(1f)
                            .setInterpolator(new OvershootInterpolator(3f)).setDuration(150).start())
                    .start();
        }
    }

    private void updateDifficultySelection(Button[] btns, int selected) {
        for (int i = 0; i < btns.length; i++) {
            if (i == selected) {
                btns[i].setBackgroundResource(R.drawable.bg_difficulty_selected);
                btns[i].setTextColor(0xFFE6A100); // Gold text for selected
            } else {
                btns[i].setBackgroundResource(R.drawable.bg_difficulty_normal);
                btns[i].setTextColor(0xFF7A8A99); // Muted blue-gray for unselected
            }
        }
    }

    //Method called when settings is chosen
    public void OpenSettings(View view) {
        playMenuTap();
        GameAnalytics.get().trackSettingsOpened();
        //Create and start settings activity
        Intent intent= new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    //Method called when Journey is chosen from the menu
    /** Shows today's challenge and its progress under the menu title. */
    private void bindDailyChallenge() {
        TextView view = findViewById(R.id.tvDailyChallenge);
        if (view == null) {
            return;
        }
        DailyChallenge challenge = new DailyChallenge(this);
        view.setOnClickListener(v -> showChallengeDialog());
        DailyChallenge.Challenge today = challenge.getTodayChallenge();
        String description = challengeDescription(today, challenge);
        if (challenge.isCompleted()) {
            view.setText(getString(R.string.daily_challenge_line_done, description));
        } else {
            view.setText(getString(R.string.daily_challenge_line, description,
                    challenge.getProgress(), today.targetValue));
        }
    }

    private String challengeDescription(DailyChallenge.Challenge challenge, DailyChallenge state) {
        switch (challenge.type) {
            case WIN_GAMES:
                return getString(R.string.challenge_win_games, challenge.targetValue);
            case COMPLETE_GAMES:
                return getString(R.string.challenge_complete_games, challenge.targetValue);
            case BEAT_DIFFICULTY:
                return getString(R.string.challenge_beat_difficulty,
                        getString(difficultyName(challenge.targetValue)));
            case WIN_WITHOUT_HIT:
                return getString(R.string.challenge_win_without_hit);
            case WIN_STREAK:
                return getString(R.string.challenge_win_streak, challenge.targetValue);
            case WIN_IN_VARIANT:
            default:
                return getString(R.string.challenge_win_variant,
                        getString(VariantPicker.nameRes(state.getTodayVariant())));
        }
    }

    /** Challenge games: today's shared game, a code to share, or a friend's code. */
    private void showChallengeDialog() {
        playMenuTap();
        String[] options = {
                getString(R.string.challenge_play_today),
                getString(R.string.challenge_share_code),
                getString(R.string.challenge_enter_code),
                getString(R.string.challenge_results)
        };
        new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme)
                .setTitle(R.string.challenge_dialog_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startTodayChallengeGame();
                    } else if (which == 1) {
                        shareChallengeCode();
                    } else if (which == 2) {
                        askForChallengeCode();
                    } else {
                        showChallengeResults();
                    }
                })
                .show();
    }

    private void startTodayChallengeGame() {
        DailyChallenge challenge = new DailyChallenge(this);
        int day = DateUtil.getDayOfYear();
        startChallengeGame(challenge.getTodayVariant(), day * 7919L, 1);
    }

    private void shareChallengeCode() {
        long seed = new java.util.Random().nextInt(Integer.MAX_VALUE);
        Variant variant = GamePreferences.getVariant(this);
        int matchTarget = GamePreferences.getMatchLength(this);
        String code = ChallengeCode.encode(variant, seed, matchTarget);

        new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme)
                .setTitle(R.string.challenge_your_code)
                .setMessage(getString(R.string.challenge_code_message, code,
                        getString(VariantPicker.nameRes(variant))))
                .setPositiveButton(R.string.challenge_share_code, (d, w) -> {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT,
                            getString(R.string.challenge_share_text, code));
                    startActivity(Intent.createChooser(share, getString(R.string.challenge_share_code)));
                })
                .setNeutralButton(R.string.challenge_play_code, (d, w) ->
                        startChallengeGame(variant, seed, matchTarget))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void askForChallengeCode() {
        EditText input = new EditText(this);
        input.setHint(R.string.challenge_code_hint);
        input.setTextColor(getResources().getColor(R.color.text_primary));
        new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme)
                .setTitle(R.string.challenge_enter_code)
                .setView(input)
                .setPositiveButton(R.string.play, (d, w) -> {
                    ChallengeCode.Challenge parsed =
                            ChallengeCode.decode(input.getText().toString());
                    if (parsed == null) {
                        Toast.makeText(this, R.string.challenge_code_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startChallengeGame(parsed.variant, parsed.seed, parsed.matchTarget);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showChallengeResults() {
        java.util.List<ChallengeHistory.Entry> entries = new ChallengeHistory(this).getEntries();
        if (entries.isEmpty()) {
            Toast.makeText(this, R.string.challenge_results_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder text = new StringBuilder();
        for (ChallengeHistory.Entry entry : entries) {
            text.append(getString(entry.won ? R.string.challenge_result_won
                                            : R.string.challenge_result_lost,
                    entry.code, getString(VariantPicker.nameRes(entry.variant)), entry.points))
                    .append('\n');
        }
        new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme)
                .setTitle(R.string.challenge_results)
                .setMessage(text.toString().trim())
                .setPositiveButton(R.string.cancel, null)
                .show();
    }

    private void startChallengeGame(Variant variant, long seed, int matchTarget) {
        File save = new File(getFilesDir().getAbsolutePath(), GAME_CONTINUE_SAVE_FILE_NAME);
        save.delete();

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(EXTRA_PLAYER1_NAME, new PlayerProfileManager(this).getDisplayName());
        intent.putExtra(EXTRA_PLAYER2_NAME, getString(R.string.bot_player));
        intent.putExtra(EXTRA_PLAYER1_KIND, "Player");
        intent.putExtra(EXTRA_PLAYER2_KIND, "Bot");
        intent.putExtra(EXTRA_VARIANT, variant.name());
        intent.putExtra(EXTRA_MATCH_TARGET, Math.max(1, matchTarget));
        intent.putExtra(EXTRA_DICE_SEED, seed);
        startActivityForResult(intent, REQUEST_CODE_GAME);
    }

    private static int difficultyName(int difficulty) {
        switch (difficulty) {
            case 0: return R.string.difficulty_easy;
            case 1: return R.string.difficulty_medium;
            case 3: return R.string.difficulty_royal;
            default: return R.string.difficulty_hard;
        }
    }

    public void openJourney(View view) {
        playMenuTap();
        startActivity(new Intent(MenuActivity.this, JourneyActivity.class));
    }

    public void startTutorial(View view) {
        playMenuTap();
        GameAnalytics.get().trackTutorialClicked();
        File file=new File(MenuActivity.this.getFilesDir().getAbsolutePath(),
                MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        file.delete();
        Intent intent=new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra(EXTRA_PLAYER1_NAME, getString(R.string.tutorial_player));
        intent.putExtra(EXTRA_PLAYER2_NAME, getString(R.string.tutorial_bot));
        intent.putExtra(EXTRA_PLAYER1_KIND, "Player");
        intent.putExtra(EXTRA_PLAYER2_KIND, "Bot");
        intent.putExtra(EXTRA_TUTORIAL_MODE, true);
        startActivityForResult(intent, REQUEST_CODE_GAME);
    }

    //Method called when Pass & Play is chosen from the menu
    public void openPassAndPlay(View view) {
        playMenuTap();
        GameAnalytics.get().trackMenuPlayClicked("pass_and_play");
        showPassAndPlayDialog();
    }

    private void showPassAndPlayDialog() {
        PassAndPlayDialogFragment fragment = new PassAndPlayDialogFragment();
        fragment.show(getSupportFragmentManager(), "pass_and_play");
    }

    public static void setupInlineEditText(EditText field, String defaultText) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String text = field.getText().toString();
                if (text.equals(defaultText)) {
                    field.setText("");
                }
                field.setBackgroundResource(R.drawable.bg_name_field_focused);
            } else {
                if (field.getText().toString().trim().isEmpty()) {
                    field.setText(defaultText);
                }
                field.setBackgroundResource(R.drawable.bg_name_field_normal);
            }
        });
    }

    public void toggleLanguage(View view) {
        playMenuTap();
        // Track the language we're switching TO
        String currentLang = GamePreferences.getLanguage(this);
        String newLang = "en".equals(currentLang) ? "fa" : "en";
        GameAnalytics.get().trackLanguageChanged(newLang);
        GamePreferences.toggleLanguage(this);
        recreate();
    }

    //Method called when continue game is chosen
    public void continueGame(View view) {
        playMenuTap();
        continueSavedGame();
    }

    private void continueSavedGame() {
        //If save file exists start game activity with saved model
        if(checkContinueGame()){
            Intent intent= new Intent(MenuActivity.this, GameActivity.class);
            startActivityForResult(intent, REQUEST_CODE_GAME);
        }
    }

    //Method called when scores is chosen
    public void scores(View view) {
        playMenuTap();
        GameAnalytics.get().trackScoresOpened();
        //Create and start scores activity
        Intent intent= new Intent(MenuActivity.this, ScoresActivity.class);
        startActivity(intent);
    }

    //Method called to check if save file exists
    public boolean checkContinueGame(){
        //Open file and check
        File file=new File(this.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        if(file.exists()){
            return true;
        }
        return false;
    }

    //Method called to set continue button color depending on save file
    public void checkAndChangeButtonColor(){
        //The main Play button now routes to continue/new-game choices when a save exists.
    }

    public void playMenuTap() {
        MenuAudioManager.get().playClick();
    }

    // ── Theme Unlock Helpers ──────────────────────────────────────────────────

    /**
     * Sets up the 2x2 theme picker in a new-game dialog.
     *
     * @param root         The dialog's root View
     * @param thumbIds     Array of 4 FrameLayout IDs (index = theme id)
     * @param overlayIds   Array of 4 lock-overlay IDs; 0 for slots with no overlay (Royal)
     * @param selectedRef  Single-element int[] holding the currently selected theme index
     */
    private void setupThemePicker(View root,
                                   int[] thumbIds, int[] overlayIds,
                                   int[] selectedRef) {
        FrameLayout[] thumbs      = new FrameLayout[thumbIds.length];
        View[]        lockViews   = new View[overlayIds.length];

        for (int i = 0; i < thumbIds.length; i++) {
            thumbs[i] = root.findViewById(thumbIds[i]);
        }
        for (int i = 0; i < overlayIds.length; i++) {
            if (overlayIds[i] != 0) {
                lockViews[i] = root.findViewById(overlayIds[i]);
            }
        }

        refreshThemeLocks(lockViews);
        updateThemeSelection(thumbs, selectedRef[0]);

        for (int i = 0; i < thumbs.length; i++) {
            final int idx = i;
            thumbs[i].setOnClickListener(v -> {
                if (GamePreferences.isThemeUnlocked(MenuActivity.this, idx)) {
                    selectedRef[0] = idx;
                    updateThemeSelection(thumbs, idx);
                } else {
                    showThemeUnlockAd(idx, () -> {
                        GamePreferences.unlockTheme(MenuActivity.this, idx);
                        refreshThemeLocks(lockViews);
                        selectedRef[0] = idx;
                        updateThemeSelection(thumbs, idx);
                        android.widget.Toast.makeText(this,
                                R.string.theme_unlocked_toast,
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void refreshThemeLocks(View[] lockViews) {
        for (int i = 0; i < lockViews.length; i++) {
            if (lockViews[i] != null) {
                lockViews[i].setVisibility(
                        GamePreferences.isThemeUnlocked(this, i) ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void showThemeUnlockAd(int themeIdx, Runnable onUnlocked) {
        if (adManager == null || !adManager.isRewardedAdReady()) {
            if (adManager != null) {
                adManager.preloadAds();
            }
            android.widget.Toast.makeText(this,
                    R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        adManager.showRewardedAd(this, RewardedAdPlacement.THEME_UNLOCK,
                new games.mrlaki5.backgammon.Monetization.ads.AdCallback() {
                    @Override public void onAdLoaded() {}
                    @Override public void onAdFailedToLoad(String error) {
                        runOnUiThread(() -> android.widget.Toast.makeText(MenuActivity.this,
                                R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show());
                    }
                    @Override public void onAdShown() {}
                    @Override public void onAdDismissed() {}
                    @Override public void onAdClicked() {}
                    @Override public void onRewardEarned() {
                        runOnUiThread(onUnlocked);
                    }
                });
    }

    public void showThemeDiscountAd(int themeIdx, Runnable onRewarded) {
        if (adManager == null || !adManager.isRewardedAdReady()) {
            if (adManager != null) {
                adManager.preloadAds();
            }
            android.widget.Toast.makeText(this,
                    R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        adManager.showRewardedAd(this, RewardedAdPlacement.THEME_DISCOUNT,
                new games.mrlaki5.backgammon.Monetization.ads.AdCallback() {
                    @Override public void onAdLoaded() {}
                    @Override public void onAdFailedToLoad(String error) {
                        runOnUiThread(() -> android.widget.Toast.makeText(MenuActivity.this,
                                R.string.ad_not_ready, android.widget.Toast.LENGTH_SHORT).show());
                    }
                    @Override public void onAdShown() {}
                    @Override public void onAdDismissed() {}
                    @Override public void onAdClicked() {}
                    @Override public void onRewardEarned() {
                        runOnUiThread(onRewarded);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if(gameAudio!=null){
            gameAudio.release();
            gameAudio=null;
        }
        super.onDestroy();
    }

    //Method called on return from finished child activity
    //  will be called when activity started with startActivityForResult(...
    //  will not be called when activity started with startActivity(...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check request code
        switch(requestCode){
            //If request code equals with start game code
            case REQUEST_CODE_GAME:
                //Check result code
                switch(resultCode){
                    //If result code is back pressed
                    case GAME_PRESSED_BACK:
                        //Set color of continue button
                        checkAndChangeButtonColor();
                        break;
                    //If result code is player won
                    case GAME_ENDED_OK:
                        //Get data of which player won
                        Bundle extras = data.getExtras();
                        //Set color of continue button
                        checkAndChangeButtonColor();
                        if(extras!=null) {
                            String p1Name = extras.getString(EXTRA_PLAYER1_NAME);
                            String p2Name = extras.getString(EXTRA_PLAYER2_NAME);
                            // Statistics and ads are now handled in GameActivity.onGameFinished()
                            // Here we only launch the results history viewer
                            Intent intent=new Intent(MenuActivity.this,
                                    ResultsActivity.class);
                            intent.putExtra(EXTRA_PLAYER1_NAME, p1Name);
                            intent.putExtra(EXTRA_PLAYER2_NAME, p2Name);
                            startActivity(intent);
                        }
                        break;
                }
                break;
        }
    }
}
