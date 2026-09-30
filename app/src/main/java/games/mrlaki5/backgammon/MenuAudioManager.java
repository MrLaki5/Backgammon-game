package games.mrlaki5.backgammon;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * Manages menu background music and UI click sounds.
 * - music_menu.mp3: loops in all screens EXCEPT GameActivity (which has its own music)
 * - sfx_click.mp3: plays on every button tap across the app
 */
public class MenuAudioManager {

    private static MenuAudioManager instance;

    private MediaPlayer menuMusic;
    private SoundPool soundPool;
    private int clickSoundId;
    private boolean clickLoaded = false;
    private boolean musicPlaying = false;

    private MenuAudioManager() {}

    public static MenuAudioManager get() {
        if (instance == null) {
            instance = new MenuAudioManager();
        }
        return instance;
    }

    /**
     * Initialize sound pool and load click sound.
     * Call once from Application or first Activity.
     */
    public void init(Context context) {
        if (soundPool != null) return; // Already initialized

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();
        clickSoundId = soundPool.load(context, R.raw.sfx_click, 1);
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (sampleId == clickSoundId && status == 0) {
                clickLoaded = true;
            }
        });
    }

    /**
     * Plays the click sound effect. Call on every button tap.
     */
    public void playClick() {
        if (soundPool != null && clickLoaded) {
            soundPool.play(clickSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }

    /**
     * Starts the menu background music (looping).
     * Call in onResume of menu activities.
     */
    public void startMenuMusic(Context context) {
        if (musicPlaying && menuMusic != null && menuMusic.isPlaying()) return;

        if (menuMusic == null) {
            menuMusic = MediaPlayer.create(context, R.raw.music_menu);
            if (menuMusic != null) {
                menuMusic.setLooping(true);
                menuMusic.setVolume(0.5f, 0.5f);
            }
        }
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.start();
            musicPlaying = true;
        }
    }

    /**
     * Pauses menu music. Call in onPause of menu activities
     * or when entering GameActivity.
     */
    public void pauseMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.pause();
            musicPlaying = false;
        }
    }

    /**
     * Stops and releases menu music. Call when app is closing.
     */
    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.release();
            menuMusic = null;
            musicPlaying = false;
        }
    }

    /**
     * Releases all audio resources.
     */
    public void release() {
        stopMenuMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            clickLoaded = false;
        }
        instance = null;
    }
}
