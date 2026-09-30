package games.mrlaki5.backgammon.Challenge;

import android.content.Context;
import android.content.SharedPreferences;

import com.royalbackgammon.core.variant.Variant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Results of challenge games, so two players can compare how the same dice treated them. */
public class ChallengeHistory {

    private static final String PREFS_NAME = "challenge_history_prefs";
    private static final String KEY_ENTRIES = "entries";
    private static final int MAX_ENTRIES = 20;
    private static final String FIELD_SEPARATOR = "\u0001";

    public static class Entry {
        public final String code;
        public final Variant variant;
        public final boolean won;
        public final int points;

        public Entry(String code, Variant variant, boolean won, int points) {
            this.code = code;
            this.variant = variant;
            this.won = won;
            this.points = points;
        }

        String serialize() {
            return code + FIELD_SEPARATOR + variant.name() + FIELD_SEPARATOR
                    + (won ? "1" : "0") + FIELD_SEPARATOR + points;
        }

        static Entry parse(String raw) {
            String[] parts = raw.split(FIELD_SEPARATOR, -1);
            if (parts.length != 4) {
                return null;
            }
            try {
                return new Entry(parts[0], Variant.valueOf(parts[1]), "1".equals(parts[2]),
                        Integer.parseInt(parts[3]));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private final SharedPreferences prefs;

    public ChallengeHistory(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Newest first. */
    public List<Entry> getEntries() {
        return parseSorted(prefs.getStringSet(KEY_ENTRIES, new LinkedHashSet<>()));
    }

    public void record(Entry entry) {
        Set<String> stored = new LinkedHashSet<>(
                prefs.getStringSet(KEY_ENTRIES, new LinkedHashSet<>()));
        stored.add(System.currentTimeMillis() + "|" + entry.serialize());
        if (stored.size() > MAX_ENTRIES) {
            List<String> sorted = new ArrayList<>(stored);
            java.util.Collections.sort(sorted, (a, b) -> Long.compare(timestampOf(b), timestampOf(a)));
            stored = new LinkedHashSet<>(sorted.subList(0, MAX_ENTRIES));
        }
        prefs.edit().putStringSet(KEY_ENTRIES, stored).apply();
    }

    public void clear() {
        prefs.edit().remove(KEY_ENTRIES).apply();
    }

    /** Sorts raw entries newest first and returns their parsed form. */
    public static List<Entry> parseSorted(Iterable<String> raw) {
        List<String> sorted = new ArrayList<>();
        for (String value : raw) {
            sorted.add(value);
        }
        java.util.Collections.sort(sorted, (a, b) -> Long.compare(timestampOf(b), timestampOf(a)));
        List<Entry> entries = new ArrayList<>();
        for (String value : sorted) {
            int split = value.indexOf('|');
            Entry entry = Entry.parse(split >= 0 ? value.substring(split + 1) : value);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private static long timestampOf(String raw) {
        int split = raw.indexOf('|');
        if (split < 0) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.substring(0, split));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

}
