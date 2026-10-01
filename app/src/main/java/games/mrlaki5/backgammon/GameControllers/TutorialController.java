package games.mrlaki5.backgammon.GameControllers;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GameView.OnBoardImage;
import games.mrlaki5.backgammon.R;

/**
 * Manages tutorial lifecycle, step advancement, instruction display, and scenario loading.
 */
public class TutorialController {

    public interface OnTutorialListener {
        void onPlayEffect(int effectId);
        void onTutorialStepCompleted(int step);
    }

    private final Activity activity;
    private final boolean tutorialMode;
    private final LinearLayout tutorialPanel;
    private final TextView tutorialBody;
    private final Button tutorialNextButton;

    private int tutorialStep = 0;
    private volatile boolean tutorialIntroBlocking = false;
    private OnTutorialListener listener;

    private Model model;
    private GameLogic gameLogic;
    private OnBoardImage boardImage;

    private final int[] tutorialMessages = new int[]{
            R.string.tutorial_intro,    // 0
            R.string.tutorial_roll,     // 1
            R.string.tutorial_move,     // 2
            R.string.tutorial_hit,      // 3
            R.string.tutorial_bar,      // 4
            R.string.tutorial_bear_off, // 5
            R.string.tutorial_finished  // 6
    };

    public TutorialController(Activity activity, boolean tutorialMode,
                              LinearLayout panel, TextView body, Button nextButton) {
        this.activity = activity;
        this.tutorialMode = tutorialMode;
        this.tutorialPanel = panel;
        this.tutorialBody = body;
        this.tutorialNextButton = nextButton;

        setupPanel();
    }

    public void setGameDependencies(Model model, GameLogic gameLogic, OnBoardImage boardImage) {
        this.model = model;
        this.gameLogic = gameLogic;
        this.boardImage = boardImage;
    }

    public void setListener(OnTutorialListener listener) {
        this.listener = listener;
    }

    private void setupPanel() {
        if (!tutorialMode || tutorialPanel == null) {
            if (tutorialPanel != null) {
                tutorialPanel.setVisibility(View.GONE);
            }
            return;
        }

        tutorialIntroBlocking = true;
        tutorialStep = 0;
        tutorialPanel.setVisibility(View.VISIBLE);
        if (tutorialBody != null) {
            tutorialBody.setText(tutorialMessages[0]);
        }
        if (tutorialNextButton != null) {
            tutorialNextButton.setVisibility(View.VISIBLE);
            tutorialNextButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                tutorialNextButton.setVisibility(View.GONE);
                tutorialIntroBlocking = false;
                synchronized (tutorialPanel) {
                    tutorialPanel.notifyAll();
                }
            });
        }
    }

    /**
     * Blocks the calling thread until the user taps "Next" on the intro tutorial screen.
     */
    public void waitForTutorialIntro() {
        if (!tutorialIntroBlocking || tutorialPanel == null) return;
        synchronized (tutorialPanel) {
            while (tutorialIntroBlocking) {
                try {
                    tutorialPanel.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Advances to the next tutorial step and applies the corresponding scenario.
     */
    public void advanceToNextStep() {
        if (!tutorialMode) return;
        tutorialStep++;
        if (listener != null) {
            listener.onTutorialStepCompleted(tutorialStep);
        }
        if (tutorialStep >= TutorialScenarios.TOTAL_STEPS) {
            activity.runOnUiThread(() -> {
                if (tutorialPanel != null) {
                    tutorialPanel.setVisibility(View.GONE);
                }
            });
            return;
        }
        loadTutorialScenario(tutorialStep);
    }

    /**
     * Loads a specific tutorial scenario onto the board.
     */
    public void loadTutorialScenario(int step) {
        if (model == null || step >= TutorialScenarios.TOTAL_STEPS) return;

        if (step == TutorialScenarios.STEP_DONE) {
            showTutorialText(step);
            return;
        }

        TutorialScenarios.applyScenario(model, step);

        if (boardImage != null) {
            boardImage.setChipMatrix(model.getBoardFields());
            boardImage.setDices(model.getDiceThrows());
            boardImage.postInvalidate();
        }

        if (gameLogic != null && model.getState() == 2) {
            model.setNextMoves(gameLogic.calculateMoves(
                    model.getBoardFields(), model.getCurrentPlayer(), model.getDiceThrows()));
        }

        showTutorialText(step);
    }

    /**
     * Updates the UI text and position for the given tutorial step.
     */
    public void showTutorialText(int step) {
        if (!tutorialMode || tutorialPanel == null || tutorialBody == null) return;
        final int safeStep = Math.min(step, tutorialMessages.length - 1);
        activity.runOnUiThread(() -> {
            tutorialPanel.setVisibility(View.VISIBLE);
            tutorialBody.setText(tutorialMessages[safeStep]);

            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) tutorialPanel.getLayoutParams();
            if (params != null) {
                if (safeStep == TutorialScenarios.STEP_BEAR_OFF) {
                    params.bottomToBottom = -1;
                    params.topToTop = R.id.boardImage;
                    params.topMargin = (int) (14 * activity.getResources().getDisplayMetrics().density);
                } else {
                    params.topToTop = -1;
                    params.bottomToBottom = R.id.boardImage;
                    params.bottomMargin = (int) (14 * activity.getResources().getDisplayMetrics().density);
                }
                tutorialPanel.setLayoutParams(params);
            }

            if (safeStep >= tutorialMessages.length - 1) {
                tutorialPanel.postDelayed(() -> {
                    if (tutorialPanel != null) {
                        tutorialPanel.setVisibility(View.GONE);
                    }
                }, 4000);
            }
        });
    }

    /**
     * Contextual tutorial message triggered by game events.
     */
    public void showTutorialMessage(int step) {
        if (!tutorialMode || tutorialPanel == null) return;
        showTutorialText(step);
    }

    /**
     * Unblocks waiting thread if activity finishes or paused.
     */
    public void release() {
        tutorialIntroBlocking = false;
        if (tutorialPanel != null) {
            synchronized (tutorialPanel) {
                tutorialPanel.notifyAll();
            }
        }
    }

    public boolean isTutorialMode() {
        return tutorialMode;
    }

    public int getTutorialStep() {
        return tutorialStep;
    }

    public void setTutorialStep(int tutorialStep) {
        this.tutorialStep = tutorialStep;
    }
}
