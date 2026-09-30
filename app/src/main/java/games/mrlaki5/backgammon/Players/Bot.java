package games.mrlaki5.backgammon.Players;

import java.util.List;
import java.util.Random;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GameControllers.GameLogic;
import games.mrlaki5.backgammon.GameControllers.GameMoveExecutor;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.GameView.OnBoardImage;

/** Bot with four genuinely different move-selection profiles. */
public class Bot extends Player {
    private final Model model;
    private final GameMoveExecutor moveExecutor;
    private final BotMoveStrategy moveStrategy = new BotMoveStrategy();
    private final Random random = new Random();

    public Bot(GameActivity currGame, String playerName, Model model) {
        super(currGame, playerName);
        this.model = model;
        this.moveExecutor = new GameMoveExecutor(model);
    }

    private int difficulty() {
        return getCurrGame().getBotDifficulty();
    }

    private long thinkTime() {
        switch (difficulty()) {
            case GamePreferences.BOT_EASY: return 600L;
            case GamePreferences.BOT_HARD: return 330L;
            case GamePreferences.BOT_ROYAL: return 260L;
            default: return 430L;
        }
    }

    @Override
    public synchronized void actionMove() {
        OnBoardImage boardImage = getCurrGame().getBoardImage();
        GameLogic gameLogic = getCurrGame().getGameLogic();
        setWaitCond(1);

        while (!model.getNextMoves().isEmpty()) {
            try {
                wait(thinkTime());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            if (getWaitCond() == 0) return;

            NextJump jump = moveStrategy.chooseMove(model, model.getNextMoves(), difficulty(),
                    random);
            applyMove(jump, boardImage);
            boardImage.postInvalidate();
            model.setNextMoves(gameLogic.calculateMoves(
                    model.getBoardFields(), model.getCurrentPlayer(), model.getDiceThrows()));
        }
    }

    private void applyMove(NextJump jump, OnBoardImage boardImage) {
        GameMoveExecutor.MoveResult moveResult = moveExecutor.applyMove(jump);
        boardImage.setDices(model.getDiceThrows());
        getCurrGame().onCheckerMoved(moveResult);
    }

    @Override
    public synchronized void actionRoll() {
        setWaitCond(1);
        getCurrGame().setMPlayer(1);
        try {
            wait(Math.max(220L, thinkTime() - 80L));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        if (getWaitCond() == 0) return;
        getCurrGame().setMPlayer(2);
        model.setDiceThrows(getCurrGame().getGameLogic().rollDices());
        getCurrGame().getBoardImage().setDices(model.getDiceThrows());
        getCurrGame().getBoardImage().postInvalidate();
    }
}
