package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;

import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.GameModel.Model;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;

/** Central preferences for bot strength and visual board theme. */
public final class GamePreferences {
    public static final String FILE_NAME = "Settings";
    public static final String KEY_BOT_DIFFICULTY = "botDifficulty";
    public static final String KEY_BOARD_THEME = "boardTheme";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_MUSIC_VOLUME = "musicVolume";
    public static final String KEY_SFX_VOLUME = "sfxVolume";
    public static final String KEY_VARIANT = "variant";
    public static final String KEY_MATCH_LENGTH = "matchLength";
    public static final String KEY_TAVLI_MATCH = "tavliMatch";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_FA = "fa";

    public static final int BOT_EASY = 0;
    public static final int BOT_MEDIUM = 1;
    public static final int BOT_HARD = 2;
    public static final int BOT_ROYAL = 3;

    public static final int THEME_ROYAL = 0;
    public static final int THEME_POP_ART = 1;
    public static final int THEME_CYBERPUNK = 2;
    public static final int THEME_LUXURY = 3;
    public static final int THEME_WOODLAND = 4;
    public static final int THEME_GALAXY = 5;
    public static final int THEME_ANCIENT_EGYPT = 6;
    public static final int THEME_NEON_RETRO = 7;
    public static final int DEFAULT_MUSIC_VOLUME = 55;
    public static final int DEFAULT_SFX_VOLUME = 80;

    private GamePreferences() {}

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static int getBotDifficulty(Context context) {
        return getBotDifficulty(preferences(context));
    }

    public static int getBoardTheme(Context context) {
        return getBoardTheme(preferences(context));
    }

    public static String getLanguage(Context context) {
        return preferences(context).getString(KEY_LANGUAGE, LANGUAGE_EN);
    }

    public static int getMusicVolume(Context context) {
        return getMusicVolume(preferences(context));
    }

    public static int getSfxVolume(Context context) {
        return getSfxVolume(preferences(context));
    }

    public static void saveAudioVolumes(Context context, int musicVolume, int sfxVolume) {
        saveAudioVolumes(preferences(context), musicVolume, sfxVolume);
    }

    /**
     * Returns true if the given theme is available to play.
     * Themes 0 (Royal), 1 (Pop Art), 2 (Cyberpunk), 4 (Woodland) are free.
     * Themes 3 (Luxury), 5 (Galaxy), 6 (Ancient Egypt), 7 (Neon Retro) are coin-locked.
     */
    public static boolean isThemeUnlocked(int themeId) {
        if (themeId == THEME_ROYAL ||
                themeId == THEME_POP_ART ||
                themeId == THEME_CYBERPUNK ||
                themeId == THEME_WOODLAND) {
            return true;
        }
        PlayerProfileManager ppm = PlayerProfileManager.getInstance();
        if (ppm != null) {
            return ppm.hasPurchasedTheme(themeId);
        }
        return false;
    }

    public static boolean isThemeUnlocked(Context context, int themeId) {
        if (themeId == THEME_ROYAL ||
                themeId == THEME_POP_ART ||
                themeId == THEME_CYBERPUNK ||
                themeId == THEME_WOODLAND) {
            return true;
        }
        PlayerProfileManager ppm = PlayerProfileManager.getInstance(context);
        if (ppm != null) {
            return ppm.hasPurchasedTheme(themeId);
        }
        return preferences(context).getBoolean("theme_unlocked_" + themeId, false);
    }

    /** Permanently marks a theme as unlocked. */
    public static void unlockTheme(Context context, int themeId) {
        preferences(context).edit()
                .putBoolean("theme_unlocked_" + themeId, true)
                .apply();
    }

    public static void toggleLanguage(Context context) {
        String nextLanguage=LANGUAGE_EN.equals(getLanguage(context)) ? LANGUAGE_FA : LANGUAGE_EN;
        preferences(context).edit()
                .putString(KEY_LANGUAGE, nextLanguage)
                .apply();
    }

    public static Variant getVariant(Context context) {
        return Model.parseVariant(preferences(context).getString(KEY_VARIANT, null));
    }

    /** Points needed to win a match; 1 means a single game. */
    public static int getMatchLength(Context context) {
        return preferences(context).getInt(KEY_MATCH_LENGTH, 1);
    }

    /** True when the player picked the Tavli rotation instead of a single variant. */
    public static boolean isTavliMatch(Context context) {
        return preferences(context).getBoolean(KEY_TAVLI_MATCH, false);
    }

    public static void saveVariantSelection(Context context, Variant variant, int matchLength,
                                            boolean tavliMatch) {
        preferences(context).edit()
                .putString(KEY_VARIANT, variant.name())
                .putInt(KEY_MATCH_LENGTH, matchLength)
                .putBoolean(KEY_TAVLI_MATCH, tavliMatch)
                .apply();
    }

    public static void saveSelections(Context context, int difficulty, int theme) {
        saveSelections(preferences(context), difficulty, theme);
    }

    static int getBotDifficulty(SharedPreferences preferences) {
        return preferences.getInt(KEY_BOT_DIFFICULTY, BOT_MEDIUM);
    }

    static int getBoardTheme(SharedPreferences preferences) {
        return preferences.getInt(KEY_BOARD_THEME, THEME_ROYAL);
    }

    static int getMusicVolume(SharedPreferences preferences) {
        return preferences.getInt(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
    }

    static int getSfxVolume(SharedPreferences preferences) {
        return preferences.getInt(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME);
    }

    static void saveSelections(SharedPreferences preferences, int difficulty, int theme) {
        preferences.edit()
                .putInt(KEY_BOT_DIFFICULTY, difficulty)
                .putInt(KEY_BOARD_THEME, theme)
                .apply();
    }

    static void saveAudioVolumes(SharedPreferences preferences, int musicVolume, int sfxVolume) {
        preferences.edit()
                .putInt(KEY_MUSIC_VOLUME, musicVolume)
                .putInt(KEY_SFX_VOLUME, sfxVolume)
                .apply();
    }
}
