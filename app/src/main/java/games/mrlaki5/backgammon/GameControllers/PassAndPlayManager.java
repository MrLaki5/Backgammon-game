package games.mrlaki5.backgammon.GameControllers;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GameView.OnBoardImage;
import games.mrlaki5.backgammon.R;

/**
 * Manages Pass & Play turn transitions, overlay UI, and board perspective rotation.
 */
public class PassAndPlayManager {

    public interface OnPassAndPlayListener {
        void onPlayEffect(int effectId);
    }

    private final Activity activity;
    private final boolean passAndPlayMode;
    private final FrameLayout turnSwitchOverlay;
    private final TextView turnSwitchMessage;
    private final Button turnSwitchReady;

    private final Object turnSwitchLock = new Object();
    private volatile boolean turnSwitchWaiting = false;
    private OnPassAndPlayListener listener;

    public PassAndPlayManager(Activity activity, boolean passAndPlayMode,
                              FrameLayout overlay, TextView messageView, Button readyButton) {
        this.activity = activity;
        this.passAndPlayMode = passAndPlayMode;
        this.turnSwitchOverlay = overlay;
        this.turnSwitchMessage = messageView;
        this.turnSwitchReady = readyButton;

        setupOverlay();
    }

    public void setListener(OnPassAndPlayListener listener) {
        this.listener = listener;
    }

    private void setupOverlay() {
        if (turnSwitchReady != null) {
            turnSwitchReady.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                hideOverlay();
            });
        }
    }

    /**
     * Shows turn switch overlay and blocks the calling thread (e.g. GameTask) until user taps "Ready".
     */
    public void showTurnSwitchAndWait(String nextPlayerName, int nextPlayer, OnBoardImage boardImage) {
        if (!passAndPlayMode || turnSwitchOverlay == null) return;

        turnSwitchWaiting = true;
        activity.runOnUiThread(() -> {
            if (turnSwitchMessage != null) {
                String msg = activity.getString(R.string.turn_switch_message, nextPlayerName);
                turnSwitchMessage.setText(msg);
            }
            turnSwitchOverlay.setVisibility(View.VISIBLE);
            turnSwitchOverlay.setAlpha(0f);
            turnSwitchOverlay.animate().alpha(1f).setDuration(200).start();

            if (boardImage != null) {
                float rotation = (nextPlayer == 2) ? 180f : 0f;
                boardImage.animate().rotation(rotation).setDuration(300).start();
            }
        });

        synchronized (turnSwitchLock) {
            while (turnSwitchWaiting) {
                try {
                    turnSwitchLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void hideOverlay() {
        if (turnSwitchOverlay == null) return;
        turnSwitchOverlay.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            if (turnSwitchOverlay != null) {
                turnSwitchOverlay.setVisibility(View.GONE);
            }
        }).start();

        synchronized (turnSwitchLock) {
            turnSwitchWaiting = false;
            turnSwitchLock.notifyAll();
        }
    }

    /**
     * Unblocks waiting thread if activity finishes or is paused.
     */
    public void release() {
        synchronized (turnSwitchLock) {
            turnSwitchWaiting = false;
            turnSwitchLock.notifyAll();
        }
    }

    public boolean isPassAndPlayMode() {
        return passAndPlayMode;
    }
}
