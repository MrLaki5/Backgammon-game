package games.mrlaki5.backgammon.Challenge;

import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import games.mrlaki5.backgammon.GameModel.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ChallengeCodeTest {

    @Test
    public void everyVariantRoundTrips() {
        for (Variant variant : Variant.values()) {
            for (int match : new int[]{1, 3, 5, 7}) {
                String code = ChallengeCode.encode(variant, 123456789L, match);
                ChallengeCode.Challenge parsed = ChallengeCode.decode(code);
                assertNotNull(code, parsed);
                assertEquals(variant, parsed.variant);
                assertEquals(123456789L, parsed.seed);
                assertEquals(match, parsed.matchTarget);
            }
        }
    }

    @Test
    public void codesAreForgivingAboutFormatting() {
        String code = ChallengeCode.encode(Variant.PLAKOTO, 42L, 5);
        ChallengeCode.Challenge parsed = ChallengeCode.decode(" " + code.toLowerCase() + " ");
        assertNotNull(parsed);
        assertEquals(Variant.PLAKOTO, parsed.variant);
        assertEquals(42L, parsed.seed);
        assertEquals(ChallengeCode.decode(code.replace("-", "")).seed, parsed.seed);
    }

    @Test
    public void brokenCodesAreRejected() {
        assertNull(ChallengeCode.decode(null));
        assertNull(ChallengeCode.decode(""));
        assertNull(ChallengeCode.decode("HELLO"));
        String code = ChallengeCode.encode(Variant.NARDY, 99L, 3);
        // a single wrong character must not pass the checksum
        char[] broken = code.toCharArray();
        broken[code.length() - 2] = broken[code.length() - 2] == 'A' ? 'B' : 'A';
        assertNull(ChallengeCode.decode(new String(broken)));
    }

    @Test
    public void seededDiceRepeatForTheSameCode() {
        ChallengeCode.Challenge challenge =
                ChallengeCode.decode(ChallengeCode.encode(Variant.STANDARD, 777L, 1));
        int[] first = rollSequence(challenge.seed, 40);
        int[] second = rollSequence(challenge.seed, 40);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], second[i]);
            assertTrue(first[i] >= 1 && first[i] <= 6);
        }
        assertTrue("a different seed gives different dice",
                !java.util.Arrays.equals(first, rollSequence(778L, 40)));
    }

    @Test
    public void reloadingAModelContinuesTheSameDiceSequence() {
        Model model = new Model();
        model.setDiceSeed(2024L);
        int[] expected = rollSequence(2024L, 10);
        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i], model.nextDieValue());
        }

        // a reloaded save resumes where the sequence left off
        Model reloaded = new Model();
        reloaded.setDiceSeed(model.getDiceSeed());
        reloaded.setDiceRollsUsed(model.getDiceRollsUsed());
        for (int i = 4; i < expected.length; i++) {
            assertEquals(expected[i], reloaded.nextDieValue());
        }
    }

    private static int[] rollSequence(long seed, int count) {
        Model model = new Model();
        model.setDiceSeed(seed);
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = model.nextDieValue();
        }
        return values;
    }
}
