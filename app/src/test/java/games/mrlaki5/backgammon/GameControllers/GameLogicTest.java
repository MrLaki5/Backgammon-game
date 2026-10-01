package games.mrlaki5.backgammon.GameControllers;

import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.model.WinType;
import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameModel.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameLogicTest {

    @Test
    public void calculateMoves_movesCheckerByUnusedDice() {
        Model model = modelWithEmptyBoard(1, dice(3, 5));
        board(model)[11] = field(1, 1);
        GameLogic logic = new GameLogic(model);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());

        assertTrue(hasMove(moves, 11, logic.calculateMatrixPosition(4, 1), 3));
        assertTrue(hasMove(moves, 11, logic.calculateMatrixPosition(6, 1), 5));
    }

    @Test
    public void calculateMoves_requiresBarEntryBeforeOtherMoves() {
        Model model = modelWithEmptyBoard(1, dice(1, 2));
        board(model)[24] = field(1, 1);
        board(model)[11] = field(1, 1);
        GameLogic logic = new GameLogic(model);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());

        assertTrue(hasMove(moves, 24, logic.calculateMatrixPosition(1, 1), 1));
        assertTrue(hasMove(moves, 24, logic.calculateMatrixPosition(2, 1), 2));
        assertFalse(hasSource(moves, 11));
    }

    @Test
    public void calculateMoves_allowsBearingOffFromHomeBoard() {
        Model model = modelWithEmptyBoard(1, dice(1, 2));
        board(model)[23] = field(1, 1);
        GameLogic logic = new GameLogic(model);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());

        // Only one die can be used for the last checker, so the higher die is forced
        assertTrue(hasMove(moves, 23, 27, 2));
        assertFalse(hasMove(moves, 23, 27, 1));
    }

    @Test
    public void calculateMoves_rejectsFirstMoveThatStrandsSecondDie() {
        Model model = modelWithEmptyBoard(1, dice(6, 5));
        GameLogic logic = new GameLogic(model);
        int a = logic.calculateMatrixPosition(1, 1);
        int b = logic.calculateMatrixPosition(18, 1);
        board(model)[a] = field(1, 1);
        board(model)[b] = field(1, 1);
        board(model)[logic.calculateMatrixPosition(24, 1)] = field(13, 1);
        board(model)[logic.calculateMatrixPosition(6, 1)] = field(2, 2);
        board(model)[logic.calculateMatrixPosition(12, 2)] = field(13, 2);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());

        assertFalse(hasMove(moves, b, logic.calculateMatrixPosition(24, 1), 6));
        assertTrue(hasMove(moves, b, logic.calculateMatrixPosition(23, 1), 5));
        assertTrue(hasMove(moves, a, logic.calculateMatrixPosition(7, 1), 6));
    }

    @Test
    public void calculateMoves_flagsFinishedPlayer() {
        Model model = modelWithEmptyBoard(1, dice(1, 2));
        board(model)[27] = field(15, 1);
        board(model)[0] = field(15, 2);
        GameLogic logic = new GameLogic(model);

        assertTrue(logic.calculateMoves(board(model), 1, model.getDiceThrows()).isEmpty());
        assertEquals(1, logic.getCurrPlayerFinished());
        assertEquals(2, logic.whatPartOfGame(board(model), 1));
    }

    @Test
    public void calculateResult_scoresBackgammonByVariant() {
        Model model = modelWithEmptyBoard(1, dice(1, 2));
        board(model)[27] = field(15, 1);
        board(model)[25] = field(1, 2);
        board(model)[0] = field(14, 2);
        GameLogic logic = new GameLogic(model);

        GameResult standard = logic.calculateResult();
        assertEquals(WinType.BACKGAMMON, standard.getWinType());
        assertEquals(3, standard.getPoints());

        model.setVariant(Variant.TAVLA);
        assertEquals(2, logic.calculateResult().getPoints());
    }

    @Test
    public void parseVariant_fallsBackToStandard() {
        assertEquals(Variant.STANDARD, Model.parseVariant(null));
        assertEquals(Variant.STANDARD, Model.parseVariant("NARDY_FROM_FUTURE"));
        assertEquals(Variant.PORTES, Model.parseVariant("PORTES"));
    }

    @Test
    public void moveExecutor_hitsOpponentBlotAndConsumesDice() {
        Model model = modelWithEmptyBoard(1, dice(3, 5));
        board(model)[11] = field(1, 1);
        board(model)[14] = field(1, 2);
        GameMoveExecutor executor = new GameMoveExecutor(model);

        executor.applyMove(new NextJump(3, 11, 14));

        assertEquals(0, board(model)[11].getNumberOfChips());
        assertEquals(0, board(model)[11].getPlayer());
        assertEquals(1, board(model)[14].getNumberOfChips());
        assertEquals(1, board(model)[14].getPlayer());
        assertEquals(1, board(model)[25].getNumberOfChips());
        assertEquals(2, board(model)[25].getPlayer());
        assertEquals(1, model.getDiceThrows()[0].getAlreadyUsed());
    }

    @Test
    public void moveExecutor_restoresPickedUpCheckerWhenDestinationIsIllegal() {
        Model model = modelWithEmptyBoard(1, dice(3, 5));
        board(model)[11] = field(0, 0);
        GameMoveExecutor executor = new GameMoveExecutor(model);

        boolean applied = executor.applyPickedUpMove(11, 12,
                Arrays.asList(new NextJump(3, 11, 14)));

        assertFalse(applied);
        assertEquals(1, board(model)[11].getNumberOfChips());
        assertEquals(1, board(model)[11].getPlayer());
    }

    private static Model modelWithEmptyBoard(int currentPlayer, DiceThrow[] diceThrows) {
        Model model = new Model();
        BoardFieldState[] board = new BoardFieldState[28];
        for (int i = 0; i < board.length; i++) {
            board[i] = field(0, 0);
        }
        model.setBoardFields(board);
        model.setCurrentPlayer(currentPlayer);
        model.setDiceThrows(diceThrows);
        return model;
    }

    private static BoardFieldState[] board(Model model) {
        return model.getBoardFields();
    }

    private static BoardFieldState field(int chips, int player) {
        return new BoardFieldState(chips, player);
    }

    private static DiceThrow[] dice(int first, int second) {
        DiceThrow[] dice = new DiceThrow[4];
        dice[0] = new DiceThrow(first);
        dice[1] = new DiceThrow(second);
        dice[2] = new DiceThrow(0);
        dice[3] = new DiceThrow(0);
        dice[2].setAlreadyUsed(1);
        dice[3].setAlreadyUsed(1);
        return dice;
    }

    private static boolean hasMove(List<NextJump> moves, int src, int dst, int jump) {
        for (NextJump move : moves) {
            if (move.getSrcField() == src && move.getDstField() == dst
                    && move.getJumpNumber() == jump) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSource(List<NextJump> moves, int src) {
        for (NextJump move : moves) {
            if (move.getSrcField() == src) {
                return true;
            }
        }
        return false;
    }
}
