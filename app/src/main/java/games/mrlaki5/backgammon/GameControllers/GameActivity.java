package games.mrlaki5.backgammon.GameControllers;

import androidx.annotation.Nullable;

import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.scoring.MatchState;

import games.mrlaki5.backgammon.Challenge.ChallengeCode;
import games.mrlaki5.backgammon.Challenge.ChallengeHistory;
import games.mrlaki5.backgammon.Journey.JourneyManager;
import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.Menus.VariantPicker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GameModel.ModelLoader;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.GameView.OnBoardImage;
import games.mrlaki5.backgammon.GameView.themes.BoardTheme;
import games.mrlaki5.backgammon.GameView.themes.BoardThemeFactory;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Menus.SettingsActivity;
import games.mrlaki5.backgammon.Monetization.ads.AdCallback;
import games.mrlaki5.backgammon.Monetization.ads.AdManager;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdPlacement;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdTracker;
import games.mrlaki5.backgammon.Players.BotMoveStrategy;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;
import games.mrlaki5.backgammon.R;
import games.mrlaki5.backgammon.Retention.AchievementManager;
import games.mrlaki5.backgammon.Retention.WeeklyChallenge;

/**
 * Main gameplay activity coordinating game loop, user interactions, audio,
 * and specialized controllers (ShakeController, TutorialController, GameOverHandler,
 * PauseMenuHandler, PassAndPlayManager).
 */
public class GameActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    // Audio & Settings
    private int soundVolume = 0;
    private boolean soundEnabled = true;
    private boolean effectsEnabled = true;
    private GameAudio gameAudio;
    private int timeBetweenTurns = 0;

    // Game Core Objects
    private GameLogic gameLogic;
    private GameMoveExecutor moveExecutor;
    private ModelLoader modelLoader;
    private Model model;
    private GameTask gameTask;
    private OnBoardImage BoardImage;

    // UI Controls
    private View rollDiceButton;
    private Button btnHint;
    private Button btnUndo;

    // Specialized Controllers
    private ShakeController shakeController;
    private TutorialController tutorialController;
    private GameOverHandler gameOverHandler;
    private PauseMenuHandler pauseMenuHandler;
    private PassAndPlayManager passAndPlayManager;
    private HintManager hintManager;
    private UndoManager undoManager;

    // Economy & Ads
    private CoinManager coinManager;
    private RewardedAdTracker rewardedAdTracker;
    private PlayerProfileManager profileManager;

    // Undo move snapshot
    private static class TurnSnapshot {
        final games.mrlaki5.backgammon.Beans.BoardFieldState[] boardFields;
        final games.mrlaki5.backgammon.Beans.DiceThrow[] diceThrows;
        final java.util.List<NextJump> nextMoves;

        TurnSnapshot(games.mrlaki5.backgammon.Beans.BoardFieldState[] fields,
                     games.mrlaki5.backgammon.Beans.DiceThrow[] dice,
                     java.util.List<NextJump> moves) {
            this.boardFields = new games.mrlaki5.backgammon.Beans.BoardFieldState[fields.length];
            for (int i = 0; i < fields.length; i++) {
                this.boardFields[i] = fields[i].copy();
            }
            this.diceThrows = new games.mrlaki5.backgammon.Beans.DiceThrow[dice.length];
            for (int i = 0; i < dice.length; i++) {
                this.diceThrows[i] = new games.mrlaki5.backgammon.Beans.DiceThrow(
                        dice[i].getThrowNumber(), dice[i].getAlreadyUsed());
            }
            this.nextMoves = new java.util.ArrayList<>();
            if (moves != null) {
                for (NextJump j : moves) {
                    this.nextMoves.add(new NextJump(j.getJumpNumber(), j.getSrcField(), j.getDstField()));
                }
            }
        }

        games.mrlaki5.backgammon.Beans.BoardFieldState[] copyBoard() {
            games.mrlaki5.backgammon.Beans.BoardFieldState[] copy =
                    new games.mrlaki5.backgammon.Beans.BoardFieldState[boardFields.length];
            for (int i = 0; i < boardFields.length; i++) {
                copy[i] = boardFields[i].copy();
            }
            return copy;
        }

        games.mrlaki5.backgammon.Beans.DiceThrow[] copyDice() {
            games.mrlaki5.backgammon.Beans.DiceThrow[] copy =
                    new games.mrlaki5.backgammon.Beans.DiceThrow[diceThrows.length];
            for (int i = 0; i < diceThrows.length; i++) {
                copy[i] = new games.mrlaki5.backgammon.Beans.DiceThrow(
                        diceThrows[i].getThrowNumber(), diceThrows[i].getAlreadyUsed());
            }
            return copy;
        }

        java.util.List<NextJump> copyMoves() {
            java.util.List<NextJump> copy = new java.util.ArrayList<>();
            for (NextJump j : nextMoves) {
                copy.add(new NextJump(j.getJumpNumber(), j.getSrcField(), j.getDstField()));
            }
            return copy;
        }
    }

    private TurnSnapshot turnSnapshot = null;
    private boolean undoUsedThisTurn = false;

    // Game Flow State
    private int pauseDone = 0;
    private int MoveFieldSrc;
    private int CurrentFingerPointer = -1;
    private long gameStartTimeMs;
    private int sessionGameNumber = 1;
    private boolean isRematchGame = false;

    private static final String EXTRA_SESSION_GAME_NUMBER = "session_game_number";
    private static final String EXTRA_IS_REMATCH = "is_rematch";

    // Touch listener for human checker moves
    private final View.OnTouchListener BoardListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x_touch = event.getX();
            float y_touch = event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);

                    int touchedNum = BoardImage.triangleTouched(x_touch, y_touch);
                    boolean isTouched = BoardImage.chipPTouched(touchedNum, x_touch, y_touch);

                    if (isTouched && model != null) {
                        if (model.getBoardFields()[touchedNum].getPlayer() == model.getCurrentPlayer()) {
                            int[] currNextMoves = gameLogic.calculateNextMovesForSpecificField(
                                    model.getNextMoves(), touchedNum);
                            if (currNextMoves != null) {
                                model.getBoardFields()[touchedNum].setNumberOfChips(
                                        model.getBoardFields()[touchedNum].getNumberOfChips() - 1);
                                if (model.getBoardFields()[touchedNum].getNumberOfChips() == 0) {
                                    model.getBoardFields()[touchedNum].setPlayer(0);
                                }
                                BoardImage.setNextMoveArray(currNextMoves);
                                MoveFieldSrc = touchedNum;
                                BoardImage.setMoveChip(x_touch, y_touch, model.getCurrentPlayer());
                                BoardImage.postInvalidateOnAnimation();
                                CurrentFingerPointer = event.getPointerId(0);
                            } else {
                                v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                            }
                        } else {
                            v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == CurrentFingerPointer) {
                            x_touch = event.getX(i);
                            y_touch = event.getY(i);
                            break;
                        }
                    }
                    if (BoardImage.moveMoveChip(x_touch, y_touch)) {
                        BoardImage.postInvalidateOnAnimation();
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    if (CurrentFingerPointer == -1) {
                        break;
                    }
                    int tempFlag = 1;
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == CurrentFingerPointer && event.getActionIndex() != i) {
                            tempFlag = 0;
                            break;
                        }
                    }
                    if (tempFlag == 0) {
                        break;
                    } else {
                        CurrentFingerPointer = -1;
                    }

                    x_touch = BoardImage.getXMovPos();
                    y_touch = BoardImage.getYMovPos();

                    if (BoardImage.unsetMoveChip()) {
                        int dstField = BoardImage.triangleTouched(x_touch, y_touch);
                        boolean moveApplied = false;

                        if (dstField != -1) {
                            GameMoveExecutor.MoveResult moveResult =
                                    moveExecutor.applyPickedUpMoveWithResult(MoveFieldSrc, dstField, model.getNextMoves());
                            moveApplied = moveResult.isApplied();
                            if (moveApplied) {
                                BoardImage.playMoveFeedback(moveResult.getDestinationField(), moveResult.isHit());
                                playMoveEffect(moveResult);

                                if (tutorialController.isTutorialMode()
                                        && tutorialController.getTutorialStep() >= TutorialScenarios.STEP_MOVE) {
                                    tutorialController.advanceToNextStep();
                                }
                            }
                        } else {
                            moveExecutor.applyPickedUpMove(MoveFieldSrc, -1, model.getNextMoves());
                        }

                        if (moveApplied) {
                            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                            BoardImage.setDices(model.getDiceThrows());
                            model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                                    model.getCurrentPlayer(), model.getDiceThrows()));
                        } else {
                            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                        }

                        BoardImage.setNextMoveArray(null);

                        if (model.getNextMoves().isEmpty()) {
                            synchronized (model.getCurrentObjectPlayer()) {
                                model.getCurrentObjectPlayer().setWaitCond(0);
                                model.getCurrentObjectPlayer().notifyAll();
                            }
                        }
                        BoardImage.postInvalidateOnAnimation();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileManager = PlayerProfileManager.getInstance(this);
        profileManager.checkAndExpireRentals();
        undoManager = new UndoManager(profileManager);
        undoManager.reset();
        hintManager = new HintManager(this, profileManager);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        applySelectedBoardTheme();
        gameAudio = new GameAudio(this);

        Bundle extras = getIntent().getExtras();
        boolean tutorialMode = extras != null && extras.getBoolean(MenuActivity.EXTRA_TUTORIAL_MODE, false);
        boolean passAndPlayMode = false;
        if (extras != null) {
            String gameMode = extras.getString(MenuActivity.EXTRA_GAME_MODE, "");
            passAndPlayMode = MenuActivity.GAME_MODE_PASS_AND_PLAY.equals(gameMode);
            sessionGameNumber = extras.getInt(EXTRA_SESSION_GAME_NUMBER, 1);
            isRematchGame = extras.getBoolean(EXTRA_IS_REMATCH, false);
        }

        // Initialize Settings
        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        int shakeThreshold = preferences.getInt(SettingsActivity.KEY_DICE_TRESHOLD, SettingsActivity.DEF_DICE_TRAESHOLD);
        int sampleTime = preferences.getInt(SettingsActivity.KEY_TIME_SAMPLE, SettingsActivity.DEF_TIME_SAMPLE);
        int diceDelay = preferences.getInt(SettingsActivity.KEY_DICE_SHAKE_DELAY, SettingsActivity.DEF_DICE_SHAKE_DELAY);
        timeBetweenTurns = preferences.getInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS, SettingsActivity.DEF_TIME_BETWEEN_TURNS);

        soundVolume = GamePreferences.getSfxVolume(this);
        soundEnabled = preferences.getBoolean(SettingsActivity.KEY_SOUND_ENABLED, SettingsActivity.DEF_SOUND_ENABLED);
        effectsEnabled = preferences.getBoolean(SettingsActivity.KEY_EFFECTS_ENABLED, SettingsActivity.DEF_EFFECTS_ENABLED);

        if (gameAudio != null) {
            gameAudio.startBackgroundMusic();
        }

        coinManager = new CoinManager(this);
        rewardedAdTracker = new RewardedAdTracker(this);

        // View Setup
        BoardImage = findViewById(R.id.boardImage);
        BoardImage.setBoardTheme(getBoardThemeForGame());

        rollDiceButton = findViewById(R.id.rollDiceButton);
        rollDiceButton.setOnClickListener(v -> rollDiceFromButton(v));
        setRollDiceButtonVisible(false);

        setupHintButton();

        // Model & Logic
        modelLoader = new ModelLoader();
        model = modelLoader.loadModel(extras, this);
        gameLogic = new GameLogic(model);
        moveExecutor = new GameMoveExecutor(model);

        // Subsystem Controllers
        initControllers(tutorialMode, passAndPlayMode, shakeThreshold, sampleTime, diceDelay);

        showVariantBanner(tutorialMode);

        // Board initial draw
        BoardImage.setChipMatrix(model.getBoardFields());
        BoardImage.setDices(model.getDiceThrows());
        BoardImage.invalidate();

        // Start Game Thread
        gameTask = new GameTask(model, gameLogic, BoardImage, getTurnTransitionDelayMs(), this);
        gameTask.execute();

        trackGameStartedEvent();
    }

    private void initControllers(boolean tutorialMode, boolean passAndPlayMode,
                                int shakeThreshold, int sampleTime, int diceDelay) {
        // 1. ShakeController
        shakeController = new ShakeController(this);
        shakeController.updateSettings(shakeThreshold, sampleTime, diceDelay);
        shakeController.setOnShakeListener(new ShakeController.OnShakeListener() {
            @Override
            public void onShakeStarted() {
                setMPlayer(1);
            }

            @Override
            public void onShakeCompleted() {
                completeHumanDiceRoll();
            }
        });

        // 2. TutorialController
        LinearLayout tutorialPanel = findViewById(R.id.tutorialPanel);
        TextView tutorialBody = findViewById(R.id.tutorialBody);
        Button tutorialNextButton = findViewById(R.id.tutorialNextButton);
        tutorialController = new TutorialController(this, tutorialMode,
                tutorialPanel, tutorialBody, tutorialNextButton);
        tutorialController.setGameDependencies(model, gameLogic, BoardImage);
        tutorialController.setListener(new TutorialController.OnTutorialListener() {
            @Override
            public void onPlayEffect(int effectId) {
                playEffect(effectId);
            }

            @Override
            public void onTutorialStepCompleted(int step) {
                trackTutorialStepCompleted(step);
            }
        });

        // 3. PassAndPlayManager
        FrameLayout turnSwitchOverlay = findViewById(R.id.turnSwitchRoot);
        TextView turnSwitchMessage = findViewById(R.id.turnSwitchMessage);
        Button turnSwitchReady = findViewById(R.id.turnSwitchReady);
        passAndPlayManager = new PassAndPlayManager(this, passAndPlayMode,
                turnSwitchOverlay, turnSwitchMessage, turnSwitchReady);
        passAndPlayManager.setListener(effectId -> playEffect(effectId));

        // 4. PauseMenuHandler
        pauseMenuHandler = new PauseMenuHandler(this);
        pauseMenuHandler.setListener(new PauseMenuHandler.OnPauseMenuActionListener() {
            @Override
            public void onPlayEffect(int effectId) {
                playEffect(effectId);
            }

            @Override
            public void onResumeGame() {}

            @Override
            public void onRestartGame() {
                restartGame();
            }

            @Override
            public void onQuitGame() {
                String mode = isTutorialMode() ? "tutorial" : (isPassAndPlayMode() ? "pass_and_play" : "vs_bot");
                GameAnalytics.get().trackGameAbandoned(mode, getDifficultyName(), getGameDurationSeconds());
                if (!isTutorialMode() && !isPassAndPlayMode()) {
                    new AchievementManager(GameActivity.this).onGameAbandoned();
                }
                leaveMethod();
                Intent data = new Intent();
                setResult(MenuActivity.GAME_PRESSED_BACK, data);
                finish();
            }

            @Override
            public void onVolumeChanged(int newVolume) {
                soundVolume = newVolume;
            }
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            playEffect(GameAudio.EFFECT_MENU_TAP);
            pauseMenuHandler.showPauseDialog();
        });

        // 5. GameOverHandler
        gameOverHandler = new GameOverHandler(this, coinManager);
        gameOverHandler.setListener(new GameOverHandler.OnGameOverActionListener() {
            @Override
            public void onPlayEffect(int effectId) {
                playEffect(effectId);
            }

            @Override
            public void onRematch() {
                startRematch();
            }

            @Override
            public void onChangeSettings(int winningPlayer, String p1Name, String p2Name, String gameMode) {
                finishWithResult(winningPlayer, p1Name, p2Name, gameMode);
            }

            @Override
            public java.util.List<games.mrlaki5.backgammon.Analysis.TurnRecord> onRequestTurnHistory() {
                return model != null ? model.getTurnHistory() : null;
            }

            @Override
            public int onRequestHumanPlayer() {
                if (model == null || model.getPlayers() == null) return 0;
                for (int i = 0; i < model.getPlayers().length; i++) {
                    if (model.getPlayers()[i] instanceof games.mrlaki5.backgammon.Players.Human) {
                        return i + 1;
                    }
                }
                return 0;
            }

            @Override
            public void onMainMenu(int winningPlayer, String p1Name, String p2Name, String gameMode) {
                finishWithResult(winningPlayer, p1Name, p2Name, gameMode);
            }
        });
    }

    public void activateTouchListener() {
        if (turnSnapshot == null && !undoUsedThisTurn && model != null) {
            turnSnapshot = new TurnSnapshot(model.getBoardFields(), model.getDiceThrows(), model.getNextMoves());
        }
        BoardImage.setOnTouchListener(BoardListener);
        if (tutorialController.isTutorialMode()) {
            tutorialController.showTutorialText(tutorialController.getTutorialStep());
        }
    }

    public void deactivateTouchListener() {
        BoardImage.setOnTouchListener(null);
    }

    public void activateShakeListener() {
        turnSnapshot = null;
        undoUsedThisTurn = false;
        waitForTutorialIntro();
        if (tutorialController.isTutorialMode() && tutorialController.getTutorialStep() == TutorialScenarios.STEP_INTRO) {
            tutorialController.setTutorialStep(TutorialScenarios.STEP_ROLL);
            tutorialController.loadTutorialScenario(TutorialScenarios.STEP_ROLL);
        } else if (tutorialController.isTutorialMode()) {
            tutorialController.showTutorialText(tutorialController.getTutorialStep());
        }
        shakeController.startListening();
        setRollDiceButtonVisible(true);
    }

    public void deactivateShakeListener() {
        shakeController.stopListening();
        setRollDiceButtonVisible(false);
    }

    private void setRollDiceButtonVisible(final boolean visible) {
        if (rollDiceButton == null) return;
        runOnUiThread(() -> {
            rollDiceButton.animate().cancel();
            rollDiceButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            rollDiceButton.setEnabled(visible);
            if (visible) {
                rollDiceButton.setScaleX(0.88F);
                rollDiceButton.setScaleY(0.88F);
                rollDiceButton.setAlpha(0F);
                rollDiceButton.animate()
                        .alpha(1F)
                        .scaleX(1F)
                        .scaleY(1F)
                        .setDuration(170L)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }
        });
    }

    private void completeHumanDiceRoll() {
        if (model == null || gameLogic == null || BoardImage == null) return;

        Player currentPlayer = model.getCurrentObjectPlayer();
        if (currentPlayer == null || !(currentPlayer instanceof Human)) return;

        synchronized (currentPlayer) {
            if (currentPlayer.getWaitCond() != 1) return;

            shakeController.setShakeStarted(2);
            deactivateShakeListener();
            setMPlayer(2);
            model.setDiceThrows(gameLogic.rollDices());
            BoardImage.setDices(model.getDiceThrows());
            BoardImage.invalidate();
            currentPlayer.setWaitCond(0);
            currentPlayer.notifyAll();

            if (tutorialController.isTutorialMode()
                    && tutorialController.getTutorialStep() == TutorialScenarios.STEP_ROLL) {
                tutorialController.advanceToNextStep();
            }
        }
    }

    public void rollDiceFromButton(View view) {
        playEffect(GameAudio.EFFECT_MENU_TAP);
        if (model == null) return;

        int state = model.getState();
        if (state == 0 || state == 1 || state == 3) {
            completeHumanDiceRoll();
        }
    }

    public void waitForTutorialIntro() {
        if (tutorialController != null) {
            tutorialController.waitForTutorialIntro();
        }
    }

    public void advanceToNextTutorialStep() {
        if (tutorialController != null) {
            tutorialController.advanceToNextStep();
        }
    }

    public void showTutorialMessage(final int step) {
        if (tutorialController != null) {
            tutorialController.showTutorialMessage(step);
        }
    }

    public void advanceTutorial(View view) {
        // XML onClick compat — no-op
    }

    public void showTurnSwitchAndWait(String nextPlayerName, int nextPlayer) {
        if (passAndPlayManager != null) {
            passAndPlayManager.showTurnSwitchAndWait(nextPlayerName, nextPlayer, BoardImage);
        }
    }

    public boolean isPassAndPlayMode() {
        return passAndPlayManager != null && passAndPlayManager.isPassAndPlayMode();
    }

    public boolean isTutorialMode() {
        return tutorialController != null && tutorialController.isTutorialMode();
    }

    @Override
    public void onBackPressed() {
        if (gameOverHandler != null && gameOverHandler.isDialogShowing()) {
            gameOverHandler.dismissDialog();
            finish();
            return;
        }
        if (pauseMenuHandler != null) {
            pauseMenuHandler.showPauseDialog();
        }
    }

    private void restartGame() {
        String mode = isTutorialMode() ? "tutorial" : (isPassAndPlayMode() ? "pass_and_play" : "vs_bot");
        GameAnalytics.get().trackGameRestarted(mode, getDifficultyName());
        leaveMethod();
        File file = new File(getFilesDir().getAbsolutePath(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        file.delete();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pauseDone == 1) {
            File file = new File(getFilesDir().getAbsolutePath(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
            file.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pauseDone == 1) {
            pauseDone = 0;
            if (gameAudio != null) {
                gameAudio.startBackgroundMusic();
            }
            gameTask = new GameTask(model, gameLogic, BoardImage, getTurnTransitionDelayMs(), this);
            gameTask.execute();
        }
    }

    private int getTurnTransitionDelayMs() {
        return Math.max(80, Math.min(550, 80 + (timeBetweenTurns * 90)));
    }

    private void applySelectedBoardTheme() {
        int themeId = getBoardThemeForGame();
        BoardTheme theme = BoardThemeFactory.getTheme(themeId);
        android.view.View root = findViewById(R.id.gameRoot);
        android.graphics.drawable.Drawable programmatic = theme.createBackgroundDrawable();
        if (programmatic != null) {
            root.setBackground(programmatic);
        } else {
            root.setBackgroundResource(theme.getBackgroundDrawableRes());
        }
    }

    @Override
    protected void onPause() {
        if (gameTask != null) {
            gameTask.setWorkFlag(0);
            synchronized (gameTask) {
                if (gameTask.getEndRoutineStarted() == 0) {
                    gameTask.setEndRoutineStarted(1);
                    pauseDone = 1;
                }
            }
            if (pauseDone == 1) {
                synchronized (model.getCurrentObjectPlayer()) {
                    model.getCurrentObjectPlayer().setWaitCond(0);
                    model.getCurrentObjectPlayer().notifyAll();
                }
                clearMPlayer();
                if (gameAudio != null) {
                    gameAudio.stopBackgroundMusic();
                }
                shakeController.stopListening();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (gameAudio != null) {
            gameAudio.release();
            gameAudio = null;
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (pauseDone == 1) {
            synchronized (gameTask) {
                while (gameTask.getFinishedFlag() != 1) {
                    try {
                        gameTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            modelLoader.saveModel(model, this);
        }
        super.onStop();
    }

    public void leaveMethod() {
        if (passAndPlayManager != null) {
            passAndPlayManager.release();
        }
        if (tutorialController != null) {
            tutorialController.release();
        }
        if (gameTask != null) {
            gameTask.setWorkFlag(0);
            synchronized (model.getCurrentObjectPlayer()) {
                model.getCurrentObjectPlayer().setWaitCond(0);
                model.getCurrentObjectPlayer().notifyAll();
            }
            clearMPlayer();
            shakeController.stopListening();
            int tempFlag = 0;
            synchronized (gameTask) {
                if (gameTask.getEndRoutineStarted() == 0) {
                    gameTask.setEndRoutineStarted(1);
                    tempFlag = 1;
                }
                while (gameTask.getFinishedFlag() != 1) {
                    try {
                        gameTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (tempFlag == 1) {
                modelLoader.saveModel(model, this);
            }
            if (gameOverHandler != null && !gameOverHandler.isResultRecorded()
                    && !isPassAndPlayMode() && !isTutorialMode()) {
                WeeklyChallenge weeklyChallenge = new WeeklyChallenge(this);
                weeklyChallenge.onGameAbandoned();
            }
        }
    }

    public void clearMPlayer() {
        if (gameAudio != null) {
            gameAudio.stopDiceShake();
        }
    }

    public void onCheckerMoved(final GameMoveExecutor.MoveResult moveResult) {
        if (moveResult == null || !moveResult.isApplied() || BoardImage == null) {
            return;
        }
        runOnUiThread(() -> {
            BoardImage.playMoveFeedback(moveResult.getDestinationField(), moveResult.isHit());
            playMoveEffect(moveResult);
        });
    }

    public void playGameFinishedEffect() {
        runOnUiThread(() -> playEffect(GameAudio.EFFECT_GAME_WIN));
    }

    private void playEffect(int effectNum) {
        if (!soundEnabled || !effectsEnabled || soundVolume <= 0) {
            return;
        }
        if (gameAudio != null) {
            gameAudio.play(effectNum);
        }
    }

    private void playMoveEffect(GameMoveExecutor.MoveResult moveResult) {
        if (moveResult.getDestinationField() == 26 || moveResult.getDestinationField() == 27) {
            playEffect(GameAudio.EFFECT_BEAR_OFF);
        } else {
            playEffect(moveResult.isHit()
                    ? GameAudio.EFFECT_CHECKER_HIT
                    : GameAudio.EFFECT_CHECKER_MOVE);
        }
    }

    public void setMPlayer(int SongNum) {
        if (!soundEnabled || GamePreferences.getSfxVolume(this) <= 0) {
            clearMPlayer();
            return;
        }
        if (gameAudio == null) return;
        if (SongNum == 1) {
            gameAudio.startDiceShake();
        } else {
            gameAudio.finishDiceRoll();
        }
    }

    // Getters and Setters
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public OnBoardImage getBoardImage() {
        return BoardImage;
    }

    public void setBoardImage(OnBoardImage boardImage) {
        BoardImage = boardImage;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    // Analytics helpers
    private void trackGameStartedEvent() {
        gameStartTimeMs = System.currentTimeMillis();
        String mode;
        if (isTutorialMode()) {
            mode = "tutorial";
            GameAnalytics.get().trackTutorialStarted();
        } else if (isPassAndPlayMode()) {
            mode = "pass_and_play";
        } else {
            mode = "vs_bot";
        }
        GameAnalytics.get().trackGameStarted(mode, getDifficultyName(), getThemeName());
        GameAnalytics.get().setUserProperty("last_game_number", String.valueOf(sessionGameNumber));
    }

    public void trackTutorialStepCompleted(int step) {
        GameAnalytics.get().trackTutorialStep(step);
        if (step >= TutorialScenarios.TOTAL_STEPS - 1) {
            GameAnalytics.get().trackTutorialCompleted();
            new AchievementManager(this).onTutorialCompleted();
        }
    }

    public long getGameDurationSeconds() {
        return (System.currentTimeMillis() - gameStartTimeMs) / 1000;
    }

    private String getDifficultyName() {
        int diff = getBotDifficulty();
        switch (diff) {
            case 0: return "easy";
            case 1: return "medium";
            case 2: return "hard";
            case 3: return "royal";
            default: return "unknown";
        }
    }

    private String getThemeName() {
        int theme = getBoardThemeForGame();
        switch (theme) {
            case GamePreferences.THEME_POP_ART: return "pop_art";
            case GamePreferences.THEME_CYBERPUNK: return "cyberpunk";
            case GamePreferences.THEME_LUXURY: return "luxury";
            case GamePreferences.THEME_WOODLAND: return "woodland";
            case GamePreferences.THEME_GALAXY: return "galaxy";
            case GamePreferences.THEME_ANCIENT_EGYPT: return "ancient_egypt";
            case GamePreferences.THEME_NEON_RETRO: return "neon_retro";
            default: return "royal";
        }
    }

    private final Object bonusDoubleLock = new Object();
    private volatile int bonusDoubleChoice = 0;

    /**
     * Acey-deucey: asks the player (or the bot) which double to play after a 1-2.
     * Called from the game thread and blocks until a value is chosen.
     */
    public int chooseBonusDouble() {
        if (!(model.getCurrentObjectPlayer() instanceof games.mrlaki5.backgammon.Players.Human)) {
            return new games.mrlaki5.backgammon.Players.BotMoveStrategy()
                    .chooseBonusDouble(model, getBotDifficulty(), new java.util.Random());
        }
        bonusDoubleChoice = 0;
        runOnUiThread(this::showBonusDoubleDialog);
        synchronized (bonusDoubleLock) {
            while (bonusDoubleChoice == 0) {
                try {
                    bonusDoubleLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 6;
                }
            }
        }
        return bonusDoubleChoice;
    }

    private void showBonusDoubleDialog() {
        String[] options = new String[6];
        for (int i = 0; i < 6; i++) {
            options[i] = getString(R.string.acey_bonus_option, i + 1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme);
        builder.setTitle(R.string.acey_bonus_title);
        builder.setCancelable(false);
        builder.setItems(options, (dialog, which) -> setBonusDoubleChoice(which + 1));
        builder.show();
    }

    private void setBonusDoubleChoice(int value) {
        synchronized (bonusDoubleLock) {
            bonusDoubleChoice = value;
            bonusDoubleLock.notifyAll();
        }
    }

    /** Board theme for this game: a journey chapter may override the player's choice. */
    public int getBoardThemeForGame() {
        int override = getIntent().getIntExtra(MenuActivity.EXTRA_BOARD_THEME, -1);
        return override >= 0 ? override : GamePreferences.getBoardTheme(this);
    }

    /** Difficulty for this game: a journey chapter overrides the player's setting. */
    public int getBotDifficulty() {
        int override = getIntent().getIntExtra(MenuActivity.EXTRA_BOT_DIFFICULTY, -1);
        return override >= 0 ? override : GamePreferences.getBotDifficulty(this);
    }

    //Marks a journey chapter finished once its match is won
    private void checkJourneyCompletion(int winningPlayer) {
        int stage = getIntent().getIntExtra(MenuActivity.EXTRA_JOURNEY_STAGE, -1);
        if (stage < 0 || winningPlayer != 1) {
            return;
        }
        MatchState match = model.getMatch();
        if (match.getTargetPoints() > 1 && !match.isOver()) {
            return;
        }
        int reward = new JourneyManager(this).completeStage(stage, new CoinManager(this));
        if (reward > 0) {
            runOnUiThread(() -> Toast.makeText(this,
                    getString(R.string.journey_stage_done, reward), Toast.LENGTH_LONG).show());
        }
    }

    //Challenge games are logged so the player can compare results with whoever shared the code
    private void recordChallengeResult(int winningPlayer, @Nullable GameResult result) {
        if (model.getDiceSeed() == 0 || result == null) {
            return;
        }
        String code = ChallengeCode.encode(model.getVariant(), model.getDiceSeed(),
                model.getMatch().getTargetPoints());
        new ChallengeHistory(this).record(new ChallengeHistory.Entry(code, model.getVariant(),
                winningPlayer == 1, result.getPoints()));
    }

    public void onGameFinished(int winningPlayer, String p1Name, String p2Name, String gameMode,
                               @Nullable GameResult result) {
        checkJourneyCompletion(winningPlayer);
        recordChallengeResult(winningPlayer, result);
        if (gameOverHandler != null) {
            gameOverHandler.handleGameFinished(winningPlayer, p1Name, p2Name, gameMode,
                    isPassAndPlayMode(), isTutorialMode(), isRematchGame,
                    sessionGameNumber, getGameDurationSeconds(), getDifficultyName(),
                    result, model.getVariant(), model.getMatch(), getBotDifficulty());
        }
    }

    private void finishWithResult(int winningPlayer, String p1Name, String p2Name, String gameMode) {
        Intent data = new Intent();
        data.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, p1Name);
        data.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, p2Name);
        data.putExtra(MenuActivity.EXTRA_WINING_PLAYER, winningPlayer);
        data.putExtra(MenuActivity.EXTRA_GAME_MODE, gameMode);
        setResult(MenuActivity.GAME_ENDED_OK, data);
        finish();
    }

    // Brief reminder of the variant and running match score when it is not a plain single game
    private void showVariantBanner(boolean tutorialMode) {
        MatchState match = model.getMatch();
        if (tutorialMode || (model.getVariant() == Variant.STANDARD && match.getTargetPoints() <= 1
                && model.getDiceSeed() == 0)) {
            return;
        }
        String text = getString(VariantPicker.nameRes(model.getVariant()));
        if (model.getDiceSeed() != 0) {
            text = getString(R.string.challenge_banner, text);
        }
        if (match.getTargetPoints() > 1) {
            text += " • " + getString(R.string.game_over_match_score, match.getTargetPoints(),
                    model.getPlayers()[0].getPlayerName(), match.getWhiteScore(),
                    match.getRedScore(), model.getPlayers()[1].getPlayerName());
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void startRematch() {
        sessionGameNumber++;
        String mode = isPassAndPlayMode() ? "pass_and_play" : "vs_bot";
        GameAnalytics.get().trackRematchStarted(mode, getDifficultyName());

        File saveFile = new File(getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        saveFile.delete();

        Intent intent = getIntent();
        // Continue the running match, or start a fresh one of the same length and variant
        MatchState match = model.getMatch();
        boolean matchOngoing = !match.isOver();
        intent.putExtra(MenuActivity.EXTRA_VARIANT, model.getVariant().name());
        intent.putExtra(MenuActivity.EXTRA_TAVLI, !match.getRotation().isEmpty());
        intent.putExtra(MenuActivity.EXTRA_MATCH_TARGET, match.getTargetPoints());
        intent.putExtra(MenuActivity.EXTRA_MATCH_WHITE_SCORE, matchOngoing ? match.getWhiteScore() : 0);
        intent.putExtra(MenuActivity.EXTRA_MATCH_RED_SCORE, matchOngoing ? match.getRedScore() : 0);
        intent.putExtra(MenuActivity.EXTRA_MATCH_GAMES, matchOngoing ? match.getGamesPlayed() : 0);
        intent.putExtra(EXTRA_SESSION_GAME_NUMBER, sessionGameNumber);
        intent.putExtra(EXTRA_IS_REMATCH, true);
        finish();
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupHintButton() {
        btnHint = findViewById(R.id.btnHint);
        if (btnHint != null) {
            btnHint.setOnClickListener(v -> showHintChoiceDialog());
        }
        btnUndo = findViewById(R.id.btnUndo);
        if (btnUndo != null) {
            btnUndo.setOnClickListener(v -> performUndo());
        }
    }

    private void performUndo() {
        if (model == null || model.getState() != 2 || turnSnapshot == null || undoUsedThisTurn) {
            android.widget.Toast.makeText(this, R.string.undo_unavailable, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        if (undoManager == null) {
            undoManager = new UndoManager(PlayerProfileManager.getInstance(this));
        }

        undoManager.requestUndo(new UndoManager.UndoCallback() {
            @Override
            public void onUndoGranted() {
                undoUsedThisTurn = true;
                model.setBoardFields(turnSnapshot.copyBoard());
                model.setDiceThrows(turnSnapshot.copyDice());
                model.setNextMoves(turnSnapshot.copyMoves());
                model.setHeadMovesThisTurn(0);
                model.beginTurnRecord();
                BoardImage.setChipMatrix(model.getBoardFields());
                BoardImage.setDices(model.getDiceThrows());
                BoardImage.setNextMoveArray(null);
                BoardImage.postInvalidateOnAnimation();
                android.widget.Toast.makeText(GameActivity.this, R.string.undo_success, android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUndoDenied(String reason) {
                android.widget.Toast.makeText(GameActivity.this, R.string.insufficient_coins, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHintChoiceDialog() {
        if (model == null || model.getState() != 2 || model.getNextMoves() == null || model.getNextMoves().isEmpty()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialogTheme);
        builder.setTitle(R.string.hint_choice_title);
        String[] options = new String[]{
                getString(R.string.hint_use_coins, CoinConfig.HINT_COST),
                getString(R.string.hint_watch_video)
        };
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (hintManager == null) {
                    hintManager = new HintManager(this, PlayerProfileManager.getInstance(this));
                }
                hintManager.requestHint(new HintManager.HintCallback() {
                    @Override
                    public void onHintGranted() {
                        revealBestMove();
                    }

                    @Override
                    public void onHintDenied(String reason) {
                        android.widget.Toast.makeText(GameActivity.this, R.string.insufficient_coins, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (which == 1) {
                if (rewardedAdTracker != null && rewardedAdTracker.canShow(RewardedAdPlacement.HINT)) {
                    AdManager adMgr = MenuActivity.getSharedAdManager();
                    if (adMgr != null && adMgr.isRewardedAdReady()) {
                        adMgr.showRewardedAd(this, RewardedAdPlacement.HINT, new AdCallback() {
                            @Override
                            public void onAdLoaded() {}

                            @Override
                            public void onAdFailedToLoad(String error) {
                                runOnUiThread(() -> android.widget.Toast.makeText(GameActivity.this,
                                        R.string.iap_purchase_failed, android.widget.Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onAdShown() {}

                            @Override
                            public void onAdDismissed() {}

                            @Override
                            public void onAdClicked() {}

                            @Override
                            public void onRewardEarned() {
                                if (rewardedAdTracker != null) {
                                    rewardedAdTracker.recordShow(RewardedAdPlacement.HINT);
                                }
                                GameAnalytics.get().trackRewardedAdWatched("hint");
                                runOnUiThread(() -> revealBestMove());
                            }
                        });
                    } else {
                        if (adMgr != null) adMgr.preloadAds();
                        android.widget.Toast.makeText(this, R.string.iap_purchase_failed, android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.widget.Toast.makeText(this, R.string.insufficient_coins, android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void revealBestMove() {
        if (model == null || model.getNextMoves() == null || model.getNextMoves().isEmpty() || BoardImage == null || gameLogic == null) return;
        BotMoveStrategy strategy = new BotMoveStrategy();
        NextJump bestMove = strategy.chooseMove(model, model.getNextMoves(), 3, new java.util.Random());
        if (bestMove != null) {
            int[] currNextMoves = gameLogic.calculateNextMovesForSpecificField(model.getNextMoves(), bestMove.getSrcField());
            if (currNextMoves != null) {
                BoardImage.setNextMoveArray(currNextMoves);
                BoardImage.invalidate();
                android.widget.Toast.makeText(this, R.string.hint_best_move, android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}
