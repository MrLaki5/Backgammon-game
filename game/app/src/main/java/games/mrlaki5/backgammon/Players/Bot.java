package games.mrlaki5.backgammon.Players;

import android.media.MediaPlayer;

import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameActivity;
import games.mrlaki5.backgammon.GameLogic;
import games.mrlaki5.backgammon.Model;
import games.mrlaki5.backgammon.OnBoardImage;
import games.mrlaki5.backgammon.R;

public class Bot extends Player {

    public static long BOT_THINK_TIME=1500;

    private Model model;

    public Bot(GameActivity currGame, String playerName, Model model) {
        super(currGame, playerName);
        this.model=model;
    }

    @Override
    public synchronized void actionMove() {
        OnBoardImage BoardImage=super.getCurrGame().getBoardImage();
        GameLogic gameLogic=super.getCurrGame().getGameLogic();
        super.setWaitCond(1);
        while(!model.getNextMoves().isEmpty()) {
            try {
                this.wait(BOT_THINK_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (super.getWaitCond() == 0) {
                return;
            }
            int nextJumpId=(int)(Math.random()*(model.getNextMoves().size()));
            int dstField=model.getNextMoves().get(nextJumpId).getDstField();
            int srcField=model.getNextMoves().get(nextJumpId).getSrcField();
            int throwNum=model.getNextMoves().get(nextJumpId).getJumpNumber();
            model.getBoardFields()[srcField].setNumberOfChips(model.getBoardFields()[srcField].getNumberOfChips()-1);
            if(model.getBoardFields()[srcField].getNumberOfChips()==0) {
                model.getBoardFields()[srcField].setPlayer(0);
            }
            for (DiceThrow tempThrow : model.getDiceThrows()) {
                if (tempThrow.getThrowNumber() == throwNum && tempThrow.getAlreadyUsed() == 0) {
                    tempThrow.setAlreadyUsed(1);
                    BoardImage.setDices(model.getDiceThrows());
                    break;
                }
            }
            int tmpPlayer = model.getBoardFields()[dstField].getPlayer();
            if (model.getBoardFields()[dstField].getNumberOfChips() == 1 && tmpPlayer != model.getCurrentPlayer()) {
                model.getBoardFields()[23 + tmpPlayer].setNumberOfChips(model.getBoardFields()[23 + tmpPlayer].getNumberOfChips() + 1);
                if (model.getBoardFields()[23 + tmpPlayer].getNumberOfChips() == 1) {
                    model.getBoardFields()[23 + tmpPlayer].setPlayer(tmpPlayer);
                }
            } else {
                model.getBoardFields()[dstField].setNumberOfChips(model.getBoardFields()[dstField].getNumberOfChips() + 1);
            }
            if (model.getBoardFields()[dstField].getNumberOfChips() == 1) {
                model.getBoardFields()[dstField].setPlayer(model.getCurrentPlayer());
            }
            BoardImage.postInvalidate();
            model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(), model.getCurrentPlayer(), model.getDiceThrows()));
        }
    }
    @Override
    public synchronized void actionRoll() {
        super.setWaitCond(1);
        super.getCurrGame().setMPlayer(1);
        try {
            this.wait(BOT_THINK_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (super.getWaitCond() == 0) {
            return;
        }
        super.getCurrGame().setMPlayer(2);
        model.setDiceThrows(super.getCurrGame().getGameLogic().rollDices());
        super.getCurrGame().getBoardImage().setDices(model.getDiceThrows());
        super.getCurrGame().getBoardImage().postInvalidate();
    }
}
