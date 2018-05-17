package games.mrlaki5.backgammon.Players;

import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GameControllers.GameLogic;
import games.mrlaki5.backgammon.Model;
import games.mrlaki5.backgammon.OnBoardImage;

//Implementation of player, bot (phone)
public class Bot extends Player {

    //Time bot uses to think before chip move and
    //time bot uses to shake dices
    public static long BOT_THINK_TIME=1500;
    //Game model used for storing game state
    private Model model;

    //Constructor for bot
    public Bot(GameActivity currGame, String playerName, Model model) {
        super(currGame, playerName);
        this.model=model;
    }

    //Method used when bot needs to move chips
    //it has synchronization with UI thread, its called from GameTask
    @Override
    public synchronized void actionMove() {
        //Get view
        OnBoardImage BoardImage=super.getCurrGame().getBoardImage();
        //Get game logic
        GameLogic gameLogic=super.getCurrGame().getGameLogic();
        //Set synchronization flag
        super.setWaitCond(1);
        //Move chips until there is no more next moves (calculated in GameLogic)
        while(!model.getNextMoves().isEmpty()) {
            //Wait until think time finishes (bot thinks where to move)
            try {
                this.wait(BOT_THINK_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //If synchronization flag is unset process should end
            if (super.getWaitCond() == 0) {
                return;
            }
            //Choose next jump, pick random element of list of next jumps
            int nextJumpId=(int)(Math.random()*(model.getNextMoves().size()));
            //Get destination field from chosen element
            int dstField=model.getNextMoves().get(nextJumpId).getDstField();
            //Get source field from chosen element
            int srcField=model.getNextMoves().get(nextJumpId).getSrcField();
            //Get dice number of chosen element
            int throwNum=model.getNextMoves().get(nextJumpId).getJumpNumber();
            //Lower number of chips from source field
            model.getBoardFields()[srcField].setNumberOfChips(
                    model.getBoardFields()[srcField].getNumberOfChips()-1);
            //If lowered number is 0 then unset player from field
            if(model.getBoardFields()[srcField].getNumberOfChips()==0) {
                model.getBoardFields()[srcField].setPlayer(0);
            }
            //Go through dice throws and find chosen dice number
            for (DiceThrow tempThrow : model.getDiceThrows()) {
                if (tempThrow.getThrowNumber() == throwNum && tempThrow.getAlreadyUsed() == 0) {
                    //set that throw as used
                    tempThrow.setAlreadyUsed(1);
                    //update view with new information
                    BoardImage.setDices(model.getDiceThrows());
                    break;
                }
            }
            //Get player from destination field
            int tmpPlayer = model.getBoardFields()[dstField].getPlayer();
            //If destination field has one chip from other player (not from current turn player)
            if (model.getBoardFields()[dstField].getNumberOfChips() == 1 && tmpPlayer
                    != model.getCurrentPlayer()) {
                //Set number of chips on side border for one more (chip has been eaten)
                model.getBoardFields()[23 + tmpPlayer].setNumberOfChips(
                        model.getBoardFields()[23 + tmpPlayer].getNumberOfChips() + 1);
                //If new set number is one set player on side border, too
                if (model.getBoardFields()[23 + tmpPlayer].getNumberOfChips() == 1) {
                    model.getBoardFields()[23 + tmpPlayer].setPlayer(tmpPlayer);
                }
                //But dont remove chip from destination just change its player
            } else {
                //If there is no other player chip, add one chip to destination field
                model.getBoardFields()[dstField].setNumberOfChips(
                        model.getBoardFields()[dstField].getNumberOfChips() + 1);
            }
            //If number of chips on destination field is 1 set field player to current
            if (model.getBoardFields()[dstField].getNumberOfChips() == 1) {
                model.getBoardFields()[dstField].setPlayer(model.getCurrentPlayer());
            }
            //Invalidate view (it will be drawn again) with changes on model
            BoardImage.postInvalidate();
            //Calculate all possible next moves for current player
            model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                    model.getCurrentPlayer(), model.getDiceThrows()));
        }
    }

    //Method used when bot needs to roll dices
    //it has synchronization with UI thread, its called from GameTask
    @Override
    public synchronized void actionRoll() {
        //Set synchronization flag
        super.setWaitCond(1);
        //Play roll dice sound
        super.getCurrGame().setMPlayer(1);
        //Wait roll time (bot rolls dices)
        try {
            this.wait(BOT_THINK_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //If synchronization flag is unset process should end
        if (super.getWaitCond() == 0) {
            return;
        }
        //Play throw dice sound
        super.getCurrGame().setMPlayer(2);
        //Update model with new dice throws
        model.setDiceThrows(super.getCurrGame().getGameLogic().rollDices());
        //Update view with new dice throws
        super.getCurrGame().getBoardImage().setDices(model.getDiceThrows());
        //Invalidate view (it will be drawn again) with changes on model
        super.getCurrGame().getBoardImage().postInvalidate();
    }
}
