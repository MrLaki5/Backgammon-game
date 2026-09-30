package games.mrlaki5.backgammon.GameControllers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.SeekBar;
import androidx.appcompat.app.AlertDialog;

import games.mrlaki5.backgammon.GameAudio;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;

/**
 * Handles the Pause menu dialog, audio volume adjustment, restart, and quit actions.
 */
public class PauseMenuHandler {

    public interface OnPauseMenuActionListener {
        void onPlayEffect(int effectId);
        void onResumeGame();
        void onRestartGame();
        void onQuitGame();
        void onVolumeChanged(int newVolume);
    }

    private final Activity activity;
    private AlertDialog pauseDialog;
    private OnPauseMenuActionListener listener;

    public PauseMenuHandler(Activity activity) {
        this.activity = activity;
    }

    public void setListener(OnPauseMenuActionListener listener) {
        this.listener = listener;
    }

    public void showPauseDialog() {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (pauseDialog != null && pauseDialog.isShowing()) return;

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_pause, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);
        builder.setCancelable(false);
        pauseDialog = builder.create();

        if (pauseDialog.getWindow() != null) {
            pauseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pauseDialog.getWindow().setDimAmount(0.7f);
        }

        // Resume button
        View btnResume = dialogView.findViewById(R.id.pauseResume);
        if (btnResume != null) {
            btnResume.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                    listener.onResumeGame();
                }
                pauseDialog.dismiss();
            });
        }

        // Restart button
        View btnRestart = dialogView.findViewById(R.id.pauseRestart);
        if (btnRestart != null) {
            btnRestart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                pauseDialog.dismiss();
                if (listener != null) {
                    listener.onRestartGame();
                }
            });
        }

        // Quit button
        View btnQuit = dialogView.findViewById(R.id.pauseQuit);
        if (btnQuit != null) {
            btnQuit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayEffect(GameAudio.EFFECT_MENU_TAP);
                }
                pauseDialog.dismiss();
                if (listener != null) {
                    listener.onQuitGame();
                }
            });
        }

        // Volume SeekBar
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.pauseVolumeSeekBar);
        if (volumeSeekBar != null) {
            volumeSeekBar.setProgress(GamePreferences.getSfxVolume(activity));
            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        GamePreferences.saveAudioVolumes(activity, progress, progress);
                        if (listener != null) {
                            listener.onVolumeChanged(progress);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        pauseDialog.show();
    }

    public void dismiss() {
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return pauseDialog != null && pauseDialog.isShowing();
    }
}
