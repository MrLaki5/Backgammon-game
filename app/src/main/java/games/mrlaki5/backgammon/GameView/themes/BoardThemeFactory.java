package games.mrlaki5.backgammon.GameView.themes;

import games.mrlaki5.backgammon.GamePreferences;

/**
 * Factory providing BoardTheme instances corresponding to theme IDs.
 */
public class BoardThemeFactory {

    private static final BoardTheme ROYAL = new RoyalTheme();
    private static final BoardTheme POP_ART = new PopArtTheme();
    private static final BoardTheme CYBERPUNK = new CyberpunkTheme();
    private static final BoardTheme LUXURY = new LuxuryTheme();
    private static final BoardTheme WOODLAND = new WoodlandTheme();
    private static final BoardTheme GALAXY = new GalaxyTheme();
    private static final BoardTheme ANCIENT_EGYPT = new AncientEgyptTheme();
    private static final BoardTheme NEON_RETRO = new NeonRetroTheme();

    public static int themeIdFromString(String themeString) {
        if (themeString == null) return GamePreferences.THEME_ROYAL;
        switch (themeString) {
            case "theme_pop_art": return GamePreferences.THEME_POP_ART;
            case "theme_cyberpunk": return GamePreferences.THEME_CYBERPUNK;
            case "theme_luxury": return GamePreferences.THEME_LUXURY;
            case "theme_woodland": return GamePreferences.THEME_WOODLAND;
            case "theme_galaxy": return GamePreferences.THEME_GALAXY;
            case "theme_ancient_egypt":
            case "theme_egypt":
                return GamePreferences.THEME_ANCIENT_EGYPT;
            case "theme_neon_retro":
            case "theme_neon":
                return GamePreferences.THEME_NEON_RETRO;
            case "theme_royal":
            default:
                return GamePreferences.THEME_ROYAL;
        }
    }

    public static String themeStringFromId(int themeId) {
        switch (themeId) {
            case GamePreferences.THEME_POP_ART: return "theme_pop_art";
            case GamePreferences.THEME_CYBERPUNK: return "theme_cyberpunk";
            case GamePreferences.THEME_LUXURY: return "theme_luxury";
            case GamePreferences.THEME_WOODLAND: return "theme_woodland";
            case GamePreferences.THEME_GALAXY: return "theme_galaxy";
            case GamePreferences.THEME_ANCIENT_EGYPT: return "theme_ancient_egypt";
            case GamePreferences.THEME_NEON_RETRO: return "theme_neon_retro";
            case GamePreferences.THEME_ROYAL:
            default:
                return "theme_royal";
        }
    }

    public static BoardTheme getTheme(int themeId) {
        switch (themeId) {
            case GamePreferences.THEME_POP_ART:
                return POP_ART;
            case GamePreferences.THEME_CYBERPUNK:
                return CYBERPUNK;
            case GamePreferences.THEME_LUXURY:
                return LUXURY;
            case GamePreferences.THEME_WOODLAND:
                return WOODLAND;
            case GamePreferences.THEME_GALAXY:
                return GALAXY;
            case GamePreferences.THEME_ANCIENT_EGYPT:
                return ANCIENT_EGYPT;
            case GamePreferences.THEME_NEON_RETRO:
                return NEON_RETRO;
            case GamePreferences.THEME_ROYAL:
            default:
                return ROYAL;
        }
    }

    public static BoardTheme getTheme(String themeString) {
        return getTheme(themeIdFromString(themeString));
    }
}
