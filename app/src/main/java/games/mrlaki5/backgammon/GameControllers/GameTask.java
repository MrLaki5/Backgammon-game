package games.mrlaki5.backgammon.GameControllers;

import java.util.concurrent.atomic.AtomicInteger;

import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.variant.OpeningRoll;

import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GameView.OnBoardImage;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.R;

/**
 * Game execution thread using GameTaskExecutor for thread-safe turn handling.
 */
public class GameTask {

    // Time between two turns
    private final long sleepTime;
    // Flag for work, when set to 0 execution loop terminates
    private final AtomicInteger workFlag = new AtomicInteger(1);
    // Flag set to 1 when task finishes execution
    private final AtomicInteger finishedFlag = new AtomicInteger(0);
    // Flag for synchronization of end routines
    private final AtomicInteger endRoutineStarted = new AtomicInteger(0);

    // Executor replacing AsyncTask
    private final GameTaskExecutor executor = new GameTaskExecutor();

    // Game activity context
    private final GameActivity gameActivity;
    // Model of game state
    private final Model model;
    // Object of game logic
    private final GameLogic gameLogic;
    // View
    private final OnBoardImage onBoardImage;

    // Constructor
    public GameTask(Model model, GameLogic gameLogic, OnBoardImage onBoardImage, long sleepTime,
                    GameActivity gameActivity) {
        this.model = model;
        this.gameLogic = gameLogic;
        this.onBoardImage = onBoardImage;
        this.sleepTime = (sleepTime == 0) ? 1 : sleepTime;
        this.gameActivity = gameActivity;
    }

    // Method for writing text on view
    private void writeMessage(String text) {
        writeMessage(text, false);
    }

    private void writeMessage(String text, boolean rollPrompt) {
        onBoardImage.setMessage(model.getCurrentObjectPlayer().getPlayerName() +
                ", " + text, model.getCurrentPlayer(), rollPrompt);
        onBoardImage.postInvalidate();
    }

    /**
     * Starts execution of the game loop on a background thread.
     */
    public void execute() {
        executor.execute(new GameTaskExecutor.GameTaskCallback() {
            @Override
            public void onPreExecute() {
                // UI preparation if needed
            }

            @Override
            public Object doInBackground() {
                runGameLoop();
                return null;
            }

            @Override
            public void onPostExecute(Object result) {
            }

            @Override
            public void onCancelled() {
                finishedFlag.set(1);
                synchronized (GameTask.this) {
                    GameTask.this.notifyAll();
                }
            }
        });
    }

    public void execute(Void... voids) {
        execute();
    }

    public void cancel() {
        workFlag.set(0);
        executor.cancel();
    }

    public boolean isCancelled() {
        return executor.isCancelled();
    }

    /**
     * Shuts down or cancels the background execution.
     */
    public void shutdown() {
        cancel();
    }

    private void runGameLoop() {
        try {
            while (workFlag.get() == 1 && !Thread.currentThread().isInterrupted() && !executor.isCancelled()) {
                switch (model.getState()) {
                    // State 0: Player 1 rolls one die for opening
                    case 0:
                        writeMessage(gameActivity.getString(R.string.roll_dice), true);
                        model.getCurrentObjectPlayer().actionRoll();
                        if (workFlag.get() == 0 || executor.isCancelled()) break;

                        model.setState(1);
                        model.changeCurrentPlayer();

                        // Show turn-switch overlay in Pass & Play for initial roll
                        if (gameActivity.isPassAndPlayMode()) {
                            gameActivity.showTurnSwitchAndWait(
                                    model.getCurrentObjectPlayer().getPlayerName(),
                                    model.getCurrentPlayer());
                            if (workFlag.get() == 0 || executor.isCancelled()) break;
                        }

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        break;

                    // State 1: Player 2 rolls one die for opening
                    case 1:
                        writeMessage(gameActivity.getString(R.string.roll_dice), true);
                        model.getCurrentObjectPlayer().actionRoll();
                        if (workFlag.get() == 0 || executor.isCancelled()) break;

                        resolveOpeningRoll();

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        break;

                    // State 2: Current player moves checkers
                    case 2:
                        model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                                model.getCurrentPlayer(), model.getDiceThrows()));

                        model.beginTurnRecord();
                        if (!model.getNextMoves().isEmpty()) {
                            writeMessage(gameActivity.getString(R.string.move_checkers));
                            model.getCurrentObjectPlayer().actionMove();
                            if (workFlag.get() == 0 || executor.isCancelled()) break;
                        }

                        // Acey-deucey: after the 1-2 the player names a double and plays it
                        if (model.isBonusDoublePending()) {
                            int value = gameActivity.chooseBonusDouble();
                            if (workFlag.get() == 0 || executor.isCancelled()) break;
                            model.setDiceThrows(bonusDice(value));
                            model.setBonusDoublePending(false);
                            onBoardImage.setDices(model.getDiceThrows());
                            onBoardImage.postInvalidate();
                            break;  // replay state 2 with the named double
                        }

                        // Check if current player finished game
                        if (gameLogic.isGameOver()) {
                            if (endRoutineStarted.compareAndSet(0, 1)) {
                                workFlag.set(0);
                                int winningPlayer = gameLogic.getCurrPlayerFinished();
                                String p1Name = model.getPlayers()[0].getPlayerName();
                                String p2Name = model.getPlayers()[1].getPlayerName();
                                String gameMode;
                                if (gameActivity.isPassAndPlayMode()) {
                                    gameMode = MenuActivity.GAME_MODE_PASS_AND_PLAY;
                                } else {
                                    gameMode = MenuActivity.GAME_MODE_VS_BOT;
                                }
                                model.flushTurnRecord();
                                GameResult result = gameLogic.calculateResult();
                                if (result != null && !gameActivity.isTutorialMode()) {
                                    model.getMatch().record(result);
                                }
                                gameActivity.playGameFinishedEffect();
                                gameActivity.onGameFinished(winningPlayer, p1Name, p2Name, gameMode, result);
                            }
                            break;
                        }

                        // Acey-deucey: the 1-2 roll earns another roll for the same player
                        if (model.isExtraTurnPending()) {
                            model.setExtraTurnPending(false);
                            model.flushTurnRecord();
                            model.setState(3);
                            break;
                        }

                        model.onTurnEnded();
                        model.changeCurrentPlayer();
                        model.setState(3);

                        // Tutorial mode: skip bot turn entirely, stay on player 1
                        if (gameActivity.isTutorialMode()) {
                            model.setCurrentPlayer(1);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ignored) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            break;
                        }

                        // Show turn-switch overlay in Pass & Play mode
                        if (gameActivity.isPassAndPlayMode()) {
                            gameActivity.showTurnSwitchAndWait(
                                    model.getCurrentObjectPlayer().getPlayerName(),
                                    model.getCurrentPlayer());
                            if (workFlag.get() == 0 || executor.isCancelled()) break;
                        }

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        break;

                    // State 3: Current player rolls dice
                    case 3:
                        writeMessage(gameActivity.getString(R.string.roll_dice), true);
                        model.getCurrentObjectPlayer().actionRoll();
                        if (workFlag.get() == 0 || executor.isCancelled()) break;

                        model.setState(2);
                        break;
                }
            }
        } finally {
            finishedFlag.set(1);
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private static DiceThrow[] bonusDice(int value) {
        DiceThrow[] dice = new DiceThrow[4];
        for (int i = 0; i < dice.length; i++) {
            dice[i] = new DiceThrow(value);
        }
        return dice;
    }

    // Higher opening die starts; a tie restarts the opening roll
    private void resolveOpeningRoll() {
        DiceThrow[] dice = model.getDiceThrows();
        int whiteRoll = dice[0].getThrowNumber();
        int redRoll = dice[1].getThrowNumber();
        if (whiteRoll == redRoll) {
            dice[2].setAlreadyUsed(1);
            dice[3].setAlreadyUsed(1);
            model.setCurrentPlayer(1);
            model.setState(0);
            onBoardImage.setMessage(gameActivity.getString(R.string.opening_tie), 1, false);
            onBoardImage.postInvalidate();
            return;
        }
        int starter = whiteRoll > redRoll ? 1 : 2;
        boolean handOver = starter != model.getCurrentPlayer();
        model.setCurrentPlayer(starter);
        if (handOver && gameActivity.isPassAndPlayMode()) {
            gameActivity.showTurnSwitchAndWait(model.getCurrentObjectPlayer().getPlayerName(), starter);
        }
        if (model.getVariant().getOpeningRoll() == OpeningRoll.STARTER_REROLLS) {
            for (DiceThrow die : dice) {
                die.setAlreadyUsed(1);
            }
            model.setState(3);
        } else {
            model.setState(2);
        }
    }

    // Getters and setters for compatibility
    public int getWorkFlag() {
        return workFlag.get();
    }

    public void setWorkFlag(int workFlag) {
        this.workFlag.set(workFlag);
    }

    public int getFinishedFlag() {
        return finishedFlag.get();
    }

    public void setFinishedFlag(int finishedFlag) {
        this.finishedFlag.set(finishedFlag);
    }

    public int getEndRoutineStarted() {
        return endRoutineStarted.get();
    }

    public void setEndRoutineStarted(int endRoutineStarted) {
        this.endRoutineStarted.set(endRoutineStarted);
    }
}
