package games.mrlaki5.backgammon.GameControllers;

import com.royalbackgammon.core.logic.BackgammonRules;
import com.royalbackgammon.core.model.BoardField;
import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.model.GameState;
import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.Players.BotMoveStrategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Bot-vs-bot games and move execution through the app's Model / GameLogic / GameMoveExecutor stack. */
public class VariantGameFlowTest {

    @Test
    public void botGamesFinishUnderNardyRules() {
        for (int seed = 0; seed < 3; seed++) {
            playBotGame(Variant.NARDY, seed);
        }
    }

    @Test
    public void botGamesFinishUnderPlakotoRules() {
        for (int seed = 0; seed < 4; seed++) {
            playBotGame(Variant.PLAKOTO, seed);
        }
    }

    @Test
    public void pinningAndReleaseThroughAppExecutor() {
        Model model = newModel(Variant.PLAKOTO);
        BoardFieldState[] b = model.getBoardFields();
        for (BoardFieldState f : b) { f.setNumberOfChips(0); f.setPlayer(0); }
        b[5] = new BoardFieldState(1, 1);   // white real 7
        b[3] = new BoardFieldState(1, 2);   // white real 9: lone red
        b[11] = new BoardFieldState(14, 1);
        b[23] = new BoardFieldState(14, 2);
        model.setCurrentPlayer(1);
        model.setDiceThrows(new DiceThrow[]{new DiceThrow(2), new DiceThrow(1),
                new DiceThrow(0, 1), new DiceThrow(0, 1)});
        GameMoveExecutor executor = new GameMoveExecutor(model);

        executor.applyMove(new NextJump(2, 5, 3));
        assertEquals(1, b[3].getNumberOfChips());
        assertEquals(1, b[3].getPlayer());
        assertEquals(2, b[3].getPinnedPlayer());
        assertEquals(0, b[25].getNumberOfChips());

        executor.applyMove(new NextJump(1, 3, 2));
        assertEquals(1, b[3].getNumberOfChips());
        assertEquals(2, b[3].getPlayer());
        assertEquals(0, b[3].getPinnedPlayer());
    }

    @Test
    public void botGamesFinishUnderFevgaRules() {
        for (int seed = 0; seed < 3; seed++) {
            playBotGame(Variant.FEVGA, seed);
        }
    }

    @Test
    public void botGamesFinishUnderAceyDeuceyRules() {
        for (int seed = 0; seed < 3; seed++) {
            playBotGame(Variant.ACEY_DEUCEY, seed);
        }
    }

    @Test
    public void botGameStillFinishesUnderStandardRules() {
        playBotGame(Variant.STANDARD, 11);
    }

    @Test
    public void undoResetRestoresHeadMove() {
        Model model = newModel(Variant.NARDY);
        GameLogic logic = new GameLogic(model);
        model.setDiceThrows(logic.rollDices());
        model.setState(2);
        List<NextJump> moves = logic.calculateMoves(model.getBoardFields(), 1, model.getDiceThrows());
        new GameMoveExecutor(model).applyMove(moves.get(0));
        assertEquals(1, model.getHeadMovesThisTurn());
        model.onTurnEnded();
        assertEquals(0, model.getHeadMovesThisTurn());
        assertEquals(1, model.getTurnsPlayed());
    }

    private void playBotGame(Variant variant, int seed) {
        Random random = new Random(seed);
        Model model = newModel(variant);
        GameLogic logic = new GameLogic(model);
        GameMoveExecutor executor = new GameMoveExecutor(model);
        BotMoveStrategy bot = new BotMoveStrategy();
        int head1 = BackgammonRules.headIndex(1, variant);
        int head2 = BackgammonRules.headIndex(2, variant);

        for (int turn = 0; turn < 3000 && !logic.isGameOver(); turn++) {
            if (!model.isBonusDoublePending()) {
                model.setState(3);
                model.setDiceThrows(logic.rollDices());
            }
            model.setState(2);
            int player = model.getCurrentPlayer();
            int headMoves = 0;
            while (true) {
                List<NextJump> moves = logic.calculateMoves(model.getBoardFields(), player,
                        model.getDiceThrows());
                if (logic.isGameOver() || moves.isEmpty()) break;
                NextJump move = bot.chooseMove(model, moves, GamePreferences.BOT_MEDIUM, random);
                if (variant == Variant.NARDY || variant == Variant.FEVGA) {
                    BoardFieldState dst = model.getBoardFields()[move.getDstField()];
                    assertTrue(move.getDstField() >= 24 || dst.getNumberOfChips() == 0
                            || dst.getPlayer() == player);
                    if (variant == Variant.NARDY && move.getSrcField() == (player == 1 ? head1 : head2)) headMoves++;
                }
                executor.applyMove(move);
                assertEquals(30, totalCheckers(model));
            }
            if (variant == Variant.NARDY && model.getTurnsPlayed() >= 2) {
                assertTrue("head rule", headMoves <= 1);
            }
            if (logic.isGameOver()) break;
            if (model.isBonusDoublePending()) {
                int value = new BotMoveStrategy().chooseBonusDouble(model,
                        GamePreferences.BOT_MEDIUM, random);
                DiceThrow[] bonus = new DiceThrow[]{new DiceThrow(value), new DiceThrow(value),
                        new DiceThrow(value), new DiceThrow(value)};
                model.setDiceThrows(bonus);
                model.setBonusDoublePending(false);
                continue;
            }
            if (model.isExtraTurnPending()) {
                model.setExtraTurnPending(false);
                continue;
            }
            model.onTurnEnded();
            model.changeCurrentPlayer();
        }

        GameResult result = logic.calculateResult();
        assertNotNull("game did not finish (" + variant + ", seed " + seed + ")", result);
        assertEquals(logic.getCurrPlayerFinished(), result.getWinner());
    }

    private static Model newModel(Variant variant) {
        Model model = new Model();
        model.setVariant(variant);
        BoardField[] start = GameState.newGame(variant).getBoard();
        BoardFieldState[] board = new BoardFieldState[28];
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardFieldState(start[i].getChipCount(), start[i].getOwner(), start[i].getPinned());
        }
        model.setBoardFields(board);
        model.setCurrentPlayer(1);
        model.setState(3);
        return model;
    }

    private static int totalCheckers(Model model) {
        int total = 0;
        for (BoardFieldState f : model.getBoardFields()) total += f.getVisibleChips();
        return total;
    }
}
