package games.mrlaki5.backgammon.Analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Hands the finished analysis to GameReviewActivity without serialising it through an Intent. */
public final class GameReviewData {

    private static List<TurnAnalysis> pending = Collections.emptyList();

    private GameReviewData() {}

    public static void put(List<TurnAnalysis> analyses) {
        pending = new ArrayList<>(analyses);
    }

    /** Returns the analysis and clears it, so a stale review is never shown twice. */
    public static List<TurnAnalysis> take() {
        List<TurnAnalysis> result = pending;
        pending = Collections.emptyList();
        return result;
    }
}
