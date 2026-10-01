package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

import games.mrlaki5.backgammon.Menus.SettingsActivity;

public class GameAudio {
    public static final int EFFECT_MENU_TAP = 1;
    public static final int EFFECT_CHECKER_MOVE = 2;
    public static final int EFFECT_CHECKER_HIT = 3;
    public static final int EFFECT_BEAR_OFF = 4;
    public static final int EFFECT_CUBE_OFFER = 5;
    public static final int EFFECT_CUBE_ACCEPT = 6;
    public static final int EFFECT_GAME_WIN = 7;
    public static final int EFFECT_GAME_LOSS = 8;
    public static final int EFFECT_DICE_SHAKE = 9;
    public static final int EFFECT_DICE_ROLL = 10;

    private static final long MUSIC_FADE_IN_MS = 420L;
    private static final long MUSIC_FADE_OUT_MS = 520L;

    private final Context context;
    private final SoundPool soundPool;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    private final int menuTapSound;
    private final int checkerMoveSound;
    private final int checkerHitSound;
    private final int bearOffSound;
    private final int cubeOfferSound;
    private final int cubeAcceptSound;
    private final int gameWinSound;
    private final int gameLossSound;
    private final int diceShakeSound;
    private final int diceRollSound;
    private MediaPlayer backgroundMusic;
    private Runnable musicFadeRunnable;
    private int diceShakeStreamId;
    private float currentMusicVolume;

    public GameAudio(Context context) {
        this.context = context.getApplicationContext();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attributes)
                .build();
        menuTapSound = soundPool.load(this.context, R.raw.menu_tap, 1);
        checkerMoveSound = soundPool.load(this.context, R.raw.checker_move, 1);
        checkerHitSound = soundPool.load(this.context, R.raw.checker_hit, 1);
        bearOffSound = soundPool.load(this.context, R.raw.bear_off, 1);
        cubeOfferSound = soundPool.load(this.context, R.raw.cube_offer, 1);
        cubeAcceptSound = soundPool.load(this.context, R.raw.cube_accept, 1);
        gameWinSound = soundPool.load(this.context, R.raw.game_win, 1);
        gameLossSound = soundPool.load(this.context, R.raw.game_loss, 1);
        diceShakeSound = soundPool.load(this.context, R.raw.dice_shake, 1);
        diceRollSound = soundPool.load(this.context, R.raw.dice_roll, 1);
    }

    public void play(int effect) {
        SharedPreferences preferences = GamePreferences.preferences(context);
        boolean soundEnabled = preferences.getBoolean(SettingsActivity.KEY_SOUND_ENABLED,
                SettingsActivity.DEF_SOUND_ENABLED);
        boolean effectsEnabled = preferences.getBoolean(SettingsActivity.KEY_EFFECTS_ENABLED,
                SettingsActivity.DEF_EFFECTS_ENABLED);
        int sfxVolume = GamePreferences.getSfxVolume(context);
        if (!soundEnabled || !effectsEnabled || sfxVolume <= 0) {
            return;
        }

        int soundId;
        switch (effect) {
            case EFFECT_CHECKER_MOVE:
                soundId = checkerMoveSound;
                break;
            case EFFECT_CHECKER_HIT:
                soundId = checkerHitSound;
                break;
            case EFFECT_BEAR_OFF:
                soundId = bearOffSound;
                break;
            case EFFECT_CUBE_OFFER:
                soundId = cubeOfferSound;
                break;
            case EFFECT_CUBE_ACCEPT:
                soundId = cubeAcceptSound;
                break;
            case EFFECT_GAME_WIN:
                soundId = gameWinSound;
                break;
            case EFFECT_GAME_LOSS:
                soundId = gameLossSound;
                break;
            case EFFECT_DICE_SHAKE:
                soundId = diceShakeSound;
                break;
            case EFFECT_DICE_ROLL:
                soundId = diceRollSound;
                break;
            default:
                soundId = menuTapSound;
                break;
        }
        float volume = Math.max(0F, Math.min(1F, sfxVolume / 100F));
        soundPool.play(soundId, volume, volume, 1, 0, 1F);
    }

    public void startDiceShake() {
        SharedPreferences preferences = GamePreferences.preferences(context);
        boolean soundEnabled = preferences.getBoolean(SettingsActivity.KEY_SOUND_ENABLED,
                SettingsActivity.DEF_SOUND_ENABLED);
        boolean effectsEnabled = preferences.getBoolean(SettingsActivity.KEY_EFFECTS_ENABLED,
                SettingsActivity.DEF_EFFECTS_ENABLED);
        int sfxVolume = GamePreferences.getSfxVolume(context);
        if (!soundEnabled || !effectsEnabled || sfxVolume <= 0) {
            stopDiceShake();
            return;
        }
        stopDiceShake();
        float volume = Math.max(0F, Math.min(1F, sfxVolume / 100F));
        diceShakeStreamId = soundPool.play(diceShakeSound, volume, volume, 1, -1, 1F);
    }

    public void stopDiceShake() {
        if (diceShakeStreamId != 0) {
            soundPool.stop(diceShakeStreamId);
            diceShakeStreamId = 0;
        }
    }

    public void finishDiceRoll() {
        stopDiceShake();
        play(EFFECT_DICE_ROLL);
    }

    public void startBackgroundMusic() {
        SharedPreferences preferences = GamePreferences.preferences(context);
        boolean soundEnabled = preferences.getBoolean(SettingsActivity.KEY_SOUND_ENABLED,
                SettingsActivity.DEF_SOUND_ENABLED);
        int musicVolume = GamePreferences.getMusicVolume(context);
        if (!soundEnabled || musicVolume <= 0) {
            stopBackgroundMusic();
            return;
        }
        if (backgroundMusic == null) {
            backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
            if (backgroundMusic == null) {
                return;
            }
            backgroundMusic.setLooping(true);
        }
        cancelMusicFade();
        final float targetVolume = Math.max(0F, Math.min(1F, musicVolume / 100F));
        currentMusicVolume = 0F;
        backgroundMusic.setVolume(0F, 0F);
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
        fadeMusicTo(targetVolume, MUSIC_FADE_IN_MS, false);
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            fadeMusicTo(0F, MUSIC_FADE_OUT_MS, true);
        }
    }

    private void cancelMusicFade() {
        if (musicFadeRunnable != null) {
            audioHandler.removeCallbacks(musicFadeRunnable);
            musicFadeRunnable = null;
        }
    }

    private void fadeMusicTo(final float targetVolume, final long durationMs,
                             final boolean pauseAtEnd) {
        if (backgroundMusic == null) {
            return;
        }
        cancelMusicFade();
        final float startVolume = currentMusicVolume;
        final long startTime = System.currentTimeMillis();
        musicFadeRunnable = new Runnable() {
            @Override
            public void run() {
                if (backgroundMusic == null) {
                    musicFadeRunnable = null;
                    return;
                }
                float progress = Math.min(1F,
                        (System.currentTimeMillis() - startTime) / (float) durationMs);
                currentMusicVolume = startVolume + ((targetVolume - startVolume) * progress);
                backgroundMusic.setVolume(currentMusicVolume, currentMusicVolume);
                if (progress < 1F) {
                    audioHandler.postDelayed(this, 16L);
                }
                else {
                    musicFadeRunnable = null;
                    if (pauseAtEnd && backgroundMusic != null && backgroundMusic.isPlaying()) {
                        backgroundMusic.pause();
                        backgroundMusic.seekTo(0);
                    }
                }
            }
        };
        audioHandler.post(musicFadeRunnable);
    }

    public void release() {
        cancelMusicFade();
        stopDiceShake();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        soundPool.release();
    }
}
