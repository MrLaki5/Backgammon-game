package games.mrlaki5.backgammon.Challenge;

import com.royalbackgammon.core.variant.Variant;

import java.util.Locale;

/**
 * Short shareable code for a challenge game: the variant, the dice seed and the match length.
 * Two players who enter the same code get the same variant and the same dice, entirely offline.
 * Format: LLL-XXXXXXX-C — variant tag, base-36 payload, one checksum character.
 */
public final class ChallengeCode {

    private static final Variant[] VARIANTS = Variant.values();
    private static final int MAX_MATCH = 15;

    public static class Challenge {
        public final Variant variant;
        public final long seed;
        public final int matchTarget;

        public Challenge(Variant variant, long seed, int matchTarget) {
            this.variant = variant;
            this.seed = seed;
            this.matchTarget = matchTarget;
        }
    }

    private ChallengeCode() {}

    public static String encode(Variant variant, long seed, int matchTarget) {
        long safeSeed = Math.abs(seed) % 0x7FFFFFFFFFL;
        int match = Math.max(1, Math.min(MAX_MATCH, matchTarget));
        long payload = safeSeed * (MAX_MATCH + 1) + match;
        String body = Long.toString(payload, 36).toUpperCase(Locale.US);
        String tag = tagOf(variant);
        return tag + "-" + body + "-" + checksum(tag + body);
    }

    /** Parses a code, ignoring case, spaces and dashes; returns null when it is not valid. */
    public static Challenge decode(String code) {
        if (code == null) return null;
        String clean = code.trim().toUpperCase(Locale.US).replace(" ", "").replace("-", "");
        if (clean.length() < 5) return null;

        char check = clean.charAt(clean.length() - 1);
        String rest = clean.substring(0, clean.length() - 1);
        if (rest.length() < 4 || checksum(rest) != check) return null;

        String tag = rest.substring(0, 3);
        String body = rest.substring(3);
        Variant variant = variantOf(tag);
        if (variant == null || body.isEmpty()) return null;

        long payload;
        try {
            payload = Long.parseLong(body, 36);
        } catch (NumberFormatException e) {
            return null;
        }
        if (payload < 0) return null;
        int match = (int) (payload % (MAX_MATCH + 1));
        long seed = payload / (MAX_MATCH + 1);
        if (match < 1) return null;
        return new Challenge(variant, seed, match);
    }

    /** Three-letter tag of a variant, stable across releases as long as names keep their start. */
    private static String tagOf(Variant variant) {
        String name = variant.name().replace("_", "");
        return (name + "XXX").substring(0, 3);
    }

    private static Variant variantOf(String tag) {
        for (Variant variant : VARIANTS) {
            if (tagOf(variant).equals(tag)) {
                return variant;
            }
        }
        return null;
    }

    private static char checksum(String value) {
        int sum = 7;
        for (int i = 0; i < value.length(); i++) {
            sum = (sum * 31 + value.charAt(i)) % 36;
        }
        return Character.toUpperCase(Character.forDigit(sum, 36));
    }
}
