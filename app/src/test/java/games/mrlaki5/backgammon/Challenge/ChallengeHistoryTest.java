package games.mrlaki5.backgammon.Challenge;

import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChallengeHistoryTest {

    private static String raw(long timestamp, String code, Variant variant, boolean won, int points) {
        return timestamp + "|" + new ChallengeHistory.Entry(code, variant, won, points).serialize();
    }

    @Test
    public void entriesComeBackNewestFirst() {
        List<String> stored = Arrays.asList(
                raw(100L, "AAA-1-2", Variant.STANDARD, true, 1),
                raw(300L, "CCC-3-4", Variant.PLAKOTO, false, 2),
                raw(200L, "BBB-2-3", Variant.NARDY, true, 2));

        List<ChallengeHistory.Entry> entries = ChallengeHistory.parseSorted(stored);

        assertEquals(3, entries.size());
        assertEquals("CCC-3-4", entries.get(0).code);
        assertEquals("BBB-2-3", entries.get(1).code);
        assertEquals("AAA-1-2", entries.get(2).code);
        assertEquals(Variant.PLAKOTO, entries.get(0).variant);
        assertTrue(!entries.get(0).won);
        assertEquals(2, entries.get(0).points);
    }

    @Test
    public void damagedEntriesAreSkipped() {
        List<String> stored = Arrays.asList(
                "not-a-record",
                raw(10L, "AAA-1-2", Variant.FEVGA, true, 3),
                "50|only\u0001two");
        List<ChallengeHistory.Entry> entries = ChallengeHistory.parseSorted(stored);
        assertEquals(1, entries.size());
        assertEquals(Variant.FEVGA, entries.get(0).variant);
    }

    @Test
    public void aRecordSurvivesSerialisation() {
        ChallengeHistory.Entry entry =
                new ChallengeHistory.Entry("PLA-9Z-4", Variant.ACEY_DEUCEY, true, 7);
        ChallengeHistory.Entry parsed = ChallengeHistory.Entry.parse(entry.serialize());
        assertEquals(entry.code, parsed.code);
        assertEquals(entry.variant, parsed.variant);
        assertEquals(entry.won, parsed.won);
        assertEquals(entry.points, parsed.points);
    }
}
