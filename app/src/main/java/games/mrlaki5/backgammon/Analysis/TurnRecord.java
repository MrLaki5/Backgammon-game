package games.mrlaki5.backgammon.Analysis;

import com.royalbackgammon.core.variant.Variant;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

/** One player's turn: the position and dice it started from, and the checker moves played. */
public class TurnRecord {

    private final Variant variant;
    private final int player;
    private final BoardFieldState[] board;
    private final DiceThrow[] dice;
    private final List<NextJump> moves = new ArrayList<>();

    public TurnRecord(Variant variant, int player, BoardFieldState[] board, DiceThrow[] dice) {
        this.variant = variant;
        this.player = player;
        this.board = copyBoard(board);
        this.dice = copyDice(dice);
    }

    public void addMove(NextJump move) {
        moves.add(new NextJump(move.getJumpNumber(), move.getSrcField(), move.getDstField()));
    }

    public Variant getVariant() {
        return variant;
    }

    public int getPlayer() {
        return player;
    }

    /** Fresh copy of the position at the start of the turn. */
    public BoardFieldState[] copyStartBoard() {
        return copyBoard(board);
    }

    /** Fresh copy of the dice as they were rolled. */
    public DiceThrow[] copyStartDice() {
        return copyDice(dice);
    }

    public List<NextJump> getMoves() {
        return moves;
    }

    private static BoardFieldState[] copyBoard(BoardFieldState[] source) {
        BoardFieldState[] copy = new BoardFieldState[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].copy();
        }
        return copy;
    }

    private static DiceThrow[] copyDice(DiceThrow[] source) {
        DiceThrow[] copy = new DiceThrow[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new DiceThrow(source[i].getThrowNumber(), source[i].getAlreadyUsed());
        }
        return copy;
    }
}
