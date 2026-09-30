package games.mrlaki5.backgammon.Analysis;

import com.royalbackgammon.core.logic.PositionMapper;
import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GameAnalyzerTest {

    private static final int WHITE = 1;
    private static final int RED = 2;

    private static int m(int real, int player) {
        return PositionMapper.toMatrix(real, player, Variant.STANDARD);
    }

    /** White can hit a lone red checker, or shuffle quietly at the other end of the board. */
    private static BoardFieldState[] hitOrQuietBoard() {
        BoardFieldState[] board = new BoardFieldState[28];
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardFieldState(0, 0);
        }
        board[m(5, WHITE)] = new BoardFieldState(1, WHITE);
        board[m(20, WHITE)] = new BoardFieldState(14, WHITE);
        board[m(8, WHITE)] = new BoardFieldState(1, RED);          // red blot
        board[m(2, RED)] = new BoardFieldState(14, RED);
        return board;
    }

    private static DiceThrow[] dice(int a, int b) {
        return new DiceThrow[]{new DiceThrow(a), new DiceThrow(b),
                new DiceThrow(0, 1), new DiceThrow(0, 1)};
    }

    private static TurnRecord record(List<NextJump> moves) {
        TurnRecord record = new TurnRecord(Variant.STANDARD, WHITE, hitOrQuietBoard(), dice(3, 1));
        for (NextJump move : moves) {
            record.addMove(move);
        }
        return record;
    }

    @Test
    public void hittingTheBlotIsGradedExcellent() {
        List<NextJump> hit = Arrays.asList(
                new NextJump(3, m(5, WHITE), m(8, WHITE)),
                new NextJump(1, m(8, WHITE), m(9, WHITE)));
        List<TurnAnalysis> result = new GameAnalyzer().analyze(
                Collections.singletonList(record(hit)), WHITE);

        assertEquals(1, result.size());
        assertEquals(TurnAnalysis.Grade.EXCELLENT, result.get(0).getGrade());
        assertEquals("3-1", result.get(0).getDiceLabel());
        assertEquals("5/8 8/9", result.get(0).getPlayedNotation());
    }

    @Test
    public void ignoringTheBlotIsGradedWorse() {
        List<NextJump> quiet = Arrays.asList(
                new NextJump(3, m(20, WHITE), m(23, WHITE)),
                new NextJump(1, m(23, WHITE), m(24, WHITE)));
        List<TurnAnalysis> result = new GameAnalyzer().analyze(
                Collections.singletonList(record(quiet)), WHITE);

        assertEquals(1, result.size());
        TurnAnalysis analysis = result.get(0);
        assertNotEquals(TurnAnalysis.Grade.EXCELLENT, analysis.getGrade());
        assertTrue(analysis.getBestNotation().contains("5/8"));
    }

    @Test
    public void onlyTheAnalysedPlayersTurnsAreGraded() {
        TurnRecord redTurn = new TurnRecord(Variant.STANDARD, RED, hitOrQuietBoard(), dice(3, 1));
        redTurn.addMove(new NextJump(3, m(2, RED), m(5, RED)));
        assertTrue(new GameAnalyzer().analyze(Collections.singletonList(redTurn), WHITE).isEmpty());
    }

    @Test
    public void accuracyCountsExcellentAndGoodTurns() {
        List<TurnAnalysis> analyses = new ArrayList<>();
        analyses.add(new TurnAnalysis(1, "3-1", "a", "a", TurnAnalysis.Grade.EXCELLENT));
        analyses.add(new TurnAnalysis(2, "5-2", "b", "c", TurnAnalysis.Grade.GOOD));
        analyses.add(new TurnAnalysis(3, "6-4", "d", "e", TurnAnalysis.Grade.MISTAKE));
        analyses.add(new TurnAnalysis(4, "2-2", "f", "g", TurnAnalysis.Grade.BLUNDER));
        assertEquals(50, GameAnalyzer.accuracy(analyses));
        assertEquals(100, GameAnalyzer.accuracy(new ArrayList<TurnAnalysis>()));
    }

    @Test
    public void analysingAFullDoublesTurnStaysFast() {
        BoardFieldState[] board = new BoardFieldState[28];
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardFieldState(0, 0);
        }
        // Spread checkers so 2-2 branches widely
        for (int real : new int[]{3, 6, 9, 12, 15, 18}) {
            board[m(real, WHITE)] = new BoardFieldState(2, WHITE);
        }
        board[m(1, WHITE)] = new BoardFieldState(3, WHITE);
        board[m(4, RED)] = new BoardFieldState(15, RED);
        TurnRecord record = new TurnRecord(Variant.STANDARD, WHITE, board,
                new DiceThrow[]{new DiceThrow(2), new DiceThrow(2), new DiceThrow(2), new DiceThrow(2)});
        record.addMove(new NextJump(2, m(3, WHITE), m(5, WHITE)));
        record.addMove(new NextJump(2, m(5, WHITE), m(7, WHITE)));
        record.addMove(new NextJump(2, m(6, WHITE), m(8, WHITE)));
        record.addMove(new NextJump(2, m(9, WHITE), m(11, WHITE)));

        long start = System.nanoTime();
        List<TurnAnalysis> result = new GameAnalyzer().analyze(
                Collections.singletonList(record), WHITE);
        long ms = (System.nanoTime() - start) / 1_000_000;

        assertEquals(1, result.size());
        assertTrue("analysis took " + ms + "ms", ms < 4000);
    }
}
