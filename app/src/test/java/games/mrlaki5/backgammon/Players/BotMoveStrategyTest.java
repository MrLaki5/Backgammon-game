package games.mrlaki5.backgammon.Players;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameLogic;
import games.mrlaki5.backgammon.GameControllers.GameMoveExecutor;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GamePreferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BotMoveStrategyTest {

    @Test
    public void hardBotPrefersHittingBlot() {
        Model model = modelWithEmptyBoard(1, dice(3, 5));
        GameLogic logic = new GameLogic(model);
        board(model)[11] = field(1, 1);
        board(model)[10] = field(2, 1);
        board(model)[8] = field(1, 2);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());
        NextJump chosen = new BotMoveStrategy().chooseMove(model, moves,
                GamePreferences.BOT_HARD, new Random(4));

        assertEquals(11, chosen.getSrcField());
        assertEquals(8, chosen.getDstField());
    }

    @Test
    public void royalBotPrefersBearingOffInHomeBoard() {
        Model model = modelWithEmptyBoard(1, dice(1, 2));
        GameLogic logic = new GameLogic(model);
        board(model)[23] = field(1, 1);
        board(model)[22] = field(2, 1);

        List<NextJump> moves = logic.calculateMoves(board(model), 1, model.getDiceThrows());
        NextJump chosen = new BotMoveStrategy().chooseMove(model, moves,
                GamePreferences.BOT_ROYAL, new Random(2));

        assertEquals(27, chosen.getDstField());
    }

    @Test
    public void royalBotBeatsEasyBotAcrossSimulatedMatches() {
        int royalWins = 0;
        int games = 4;
        for (int game = 0; game < games; game++) {
            int royalPlayer = game % 2 == 0 ? 1 : 2;
            int winner = playSimulatedGame(new Random(100 + game), royalPlayer);
            if (winner == royalPlayer) {
                royalWins++;
            }
        }

        assertTrue("Royal bot should win most deterministic simulations, wins=" + royalWins,
                royalWins >= 3);
    }

    @Test(timeout = 3000)
    public void royalBotChoosesOpeningDoubleMoveQuickly() {
        Model model = startingModel(2, dice(6, 6));
        List<NextJump> moves = new GameLogic(model).calculateMoves(board(model),
                model.getCurrentPlayer(), model.getDiceThrows());

        long started = System.nanoTime();
        NextJump chosen = new BotMoveStrategy().chooseMove(model, moves,
                GamePreferences.BOT_ROYAL, new Random(7));
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;

        assertNotNull(chosen);
        assertTrue("Royal bot should not stall a realistic bot turn, elapsedMs=" + elapsedMs,
                elapsedMs < 1500L);
        GameMoveExecutor.MoveResult result = new GameMoveExecutor(model).applyMove(chosen);
        assertTrue(result.isApplied());
        assertEquals(chosen.getDstField(), result.getDestinationField());
    }

    private static int playSimulatedGame(Random random, int royalPlayer) {
        Model model = raceModel(royalPlayer);
        BotMoveStrategy strategy = new BotMoveStrategy();
        for (int turn = 0; turn < 40; turn++) {
            model.setDiceThrows(dice(random.nextInt(6) + 1, random.nextInt(6) + 1));
            GameLogic logic = new GameLogic(model);
            List<NextJump> moves = logic.calculateMoves(board(model), model.getCurrentPlayer(),
                    model.getDiceThrows());
            while (!moves.isEmpty()) {
                int difficulty = model.getCurrentPlayer() == royalPlayer
                        ? GamePreferences.BOT_ROYAL : GamePreferences.BOT_EASY;
                NextJump chosen = strategy.chooseMove(model, moves, difficulty, random);
                new GameMoveExecutor(model).applyMove(chosen);
                if (logic.whatPartOfGame(board(model), model.getCurrentPlayer()) == 2) {
                    return model.getCurrentPlayer();
                }
                moves = logic.calculateMoves(board(model), model.getCurrentPlayer(),
                        model.getDiceThrows());
            }
            model.changeCurrentPlayer();
        }
        return pipScore(model, royalPlayer) <= pipScore(model, royalPlayer == 1 ? 2 : 1)
                ? royalPlayer : royalPlayer == 1 ? 2 : 1;
    }

    private static int pipScore(Model model, int player) {
        GameLogic logic = new GameLogic(model);
        int score = 0;
        for (int i = 0; i < board(model).length; i++) {
            if (board(model)[i].getPlayer() == player) {
                int real = logic.calculateRealPosition(i, player);
                if (real == 100) {
                    continue;
                }
                score += board(model)[i].getNumberOfChips() * (25 - real);
            }
        }
        return score;
    }

    private static Model raceModel(int advantagedPlayer) {
        Model model = modelWithEmptyBoard(1, dice(1, 1));
        int other = advantagedPlayer == 1 ? 2 : 1;
        GameLogic logic = new GameLogic(model);
        board(model)[logic.calculateMatrixPosition(19, advantagedPlayer)] = field(5, advantagedPlayer);
        board(model)[logic.calculateMatrixPosition(21, advantagedPlayer)] = field(5, advantagedPlayer);
        board(model)[logic.calculateMatrixPosition(23, advantagedPlayer)] = field(5, advantagedPlayer);
        board(model)[logic.calculateMatrixPosition(18, other)] = field(5, other);
        board(model)[logic.calculateMatrixPosition(20, other)] = field(5, other);
        board(model)[logic.calculateMatrixPosition(22, other)] = field(5, other);
        return model;
    }

    private static Model startingModel(int currentPlayer, DiceThrow[] diceThrows) {
        Model model = modelWithEmptyBoard(currentPlayer, diceThrows);
        board(model)[0] = field(5, 1);
        board(model)[11] = field(2, 1);
        board(model)[16] = field(3, 1);
        board(model)[18] = field(5, 1);
        board(model)[4] = field(3, 2);
        board(model)[6] = field(5, 2);
        board(model)[12] = field(5, 2);
        board(model)[23] = field(2, 2);
        return model;
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
        dice[2] = new DiceThrow(0, 1);
        dice[3] = new DiceThrow(0, 1);
        return dice;
    }
}
