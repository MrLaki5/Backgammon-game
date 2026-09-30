package games.mrlaki5.backgammon;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GamePreferencesTest {
    private SharedPreferences preferences;

    @Before
    public void setUp() {
        preferences = fakePreferences();
    }

    @Test
    public void readsDefaultSelections() {
        assertEquals(GamePreferences.BOT_MEDIUM, GamePreferences.getBotDifficulty(preferences));
        assertEquals(GamePreferences.THEME_ROYAL, GamePreferences.getBoardTheme(preferences));
        assertEquals(GamePreferences.DEFAULT_MUSIC_VOLUME,
                GamePreferences.getMusicVolume(preferences));
        assertEquals(GamePreferences.DEFAULT_SFX_VOLUME,
                GamePreferences.getSfxVolume(preferences));
    }

    @Test
    public void savesGameSelections() {
        GamePreferences.saveSelections(preferences, GamePreferences.BOT_ROYAL,
                GamePreferences.THEME_CYBERPUNK);

        assertEquals(GamePreferences.BOT_ROYAL, GamePreferences.getBotDifficulty(preferences));
        assertEquals(GamePreferences.THEME_CYBERPUNK, GamePreferences.getBoardTheme(preferences));
    }

    @Test
    public void savesAudioVolumesSeparately() {
        GamePreferences.saveAudioVolumes(preferences, 35, 72);

        assertEquals(35, GamePreferences.getMusicVolume(preferences));
        assertEquals(72, GamePreferences.getSfxVolume(preferences));
    }

    private static SharedPreferences fakePreferences() {
        Map<String, Object> values = new HashMap<>();
        return (SharedPreferences) Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getInt".equals(name)) {
                        return values.containsKey(args[0]) ? values.get(args[0]) : args[1];
                    }
                    if ("edit".equals(name)) {
                        return fakeEditor(values);
                    }
                    if ("contains".equals(name)) {
                        return values.containsKey(args[0]);
                    }
                    if ("getAll".equals(name)) {
                        return new HashMap<>(values);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static SharedPreferences.Editor fakeEditor(Map<String, Object> values) {
        return (SharedPreferences.Editor) Proxy.newProxyInstance(
                SharedPreferences.Editor.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.Editor.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("putInt".equals(name)) {
                        values.put((String) args[0], args[1]);
                        return proxy;
                    }
                    if ("clear".equals(name)) {
                        values.clear();
                        return proxy;
                    }
                    if ("commit".equals(name)) {
                        return true;
                    }
                    if ("apply".equals(name)) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == Boolean.TYPE) {
            return false;
        }
        if (returnType == Integer.TYPE) {
            return 0;
        }
        if (returnType == Float.TYPE) {
            return 0F;
        }
        if (returnType == Long.TYPE) {
            return 0L;
        }
        return null;
    }
}
