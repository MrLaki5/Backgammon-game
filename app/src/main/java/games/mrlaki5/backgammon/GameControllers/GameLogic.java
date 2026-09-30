package games.mrlaki5.backgammon.GameControllers;

import java.util.ArrayList;
import java.util.List;

import com.royalbackgammon.core.logic.BackgammonRules;
import com.royalbackgammon.core.logic.PositionMapper;
import com.royalbackgammon.core.model.BoardField;
import com.royalbackgammon.core.model.Die;
import com.royalbackgammon.core.model.GamePhase;
import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.model.Move;
import com.royalbackgammon.core.model.TurnContext;
import com.royalbackgammon.core.variant.RuleFamily;
import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameModel.Model;

//Android-side adapter: converts Model beans to game-core types; all rules live in game-core
public class GameLogic {

    //Model for saving state of game
    private final Model model;
    private final Variant fixedVariant;
    //Flag for signaling wining player
    private int CurrPlayerFinished=0;
    //Set once the game has ended; CurrPlayerFinished is 0 for a draw
    private boolean gameOver=false;

    //Controller
    public GameLogic(Model model) {
        this.model = model;
        this.fixedVariant = null;
    }

    //Used for position mapping alone (analysis), without a live model
    public GameLogic(Variant variant) {
        this.model = null;
        this.fixedVariant = variant;
    }

    //Board/position rules are delegated to game-core (single source of truth for all variants).
    //Matrix layout: 0-23 points, 24 white bar, 25 red bar, 26 red bear-off, 27 white bear-off.

    //Method for calculating real position from matrix position depending on which player
    public int calculateRealPosition(int ChipMatrixPos, int PlayerNum){
        return PositionMapper.toReal(ChipMatrixPos, PlayerNum, variant());
    }

    //Method for calculating matrix position from real position depending on which player
    public int calculateMatrixPosition(int ChipRealPos, int PlayerNum){
        return PositionMapper.toMatrix(ChipRealPos, PlayerNum, variant());
    }

    //Method called to calculate next moves for specific chip from list of all next moves
    public int[] calculateNextMovesForSpecificField(List<NextJump> MovesList, int Field){
        int[] NextArray=new int[28];
        int flag=0;
        //Go through list
        for (NextJump tempJump: MovesList) {
            //Find jump which source is like current field and add it to specific next moves
            if(tempJump.getSrcField()==Field){
                NextArray[tempJump.getDstField()]=1;
                flag=1;
            }
        }
        //If no jumps where found return null so game can now that chip cant be moved
        if(flag==1) {
            return NextArray;
        }
        else{
            return null;
        }
    }

    //Method called to calculate all next moves for current player
    public List<NextJump> calculateMoves(BoardFieldState[] ChipMatrix, int PlayerNum,
                                         DiceThrow[] Throws){
        BoardField[] board = toCoreBoard(ChipMatrix);
        ArrayList<NextJump> jumps=new ArrayList<>();
        if (BackgammonRules.gamePhase(board, PlayerNum, variant()) == GamePhase.FINISHED) {
            CurrPlayerFinished=PlayerNum;
            gameOver=true;
            return jumps;
        }
        //Plakoto can end mid-turn on a mother pin (or draw when both mothers are pinned)
        if (variant().getFamily() == RuleFamily.PINNING) {
            GameResult result = BackgammonRules.gameResult(board, variant());
            if (result != null) {
                CurrPlayerFinished=result.getWinner();
                gameOver=true;
                return jumps;
            }
        }
        TurnContext turn = model != null ? model.getTurnContext() : TurnContext.NONE;
        for (Move move : BackgammonRules.calculateLegalMoves(board, PlayerNum, toCoreDice(Throws),
                variant(), turn)) {
            jumps.add(new NextJump(move.getDieValue(), move.getFrom(), move.getTo()));
        }
        return jumps;
    }

    //Method for checking in what part game is
    //return value 0-game goes, 1-last phase, 2-game done
    public int whatPartOfGame(BoardFieldState[] ChipMatrix, int PlayerNum){
        switch (BackgammonRules.gamePhase(toCoreBoard(ChipMatrix), PlayerNum, variant())) {
            case FINISHED:
                return 2;
            case BEARING_OFF:
                return 1;
            default:
                return 0;
        }
    }

    //Scored result of the finished game under the model's variant, null while ongoing
    public GameResult calculateResult(){
        return BackgammonRules.gameResult(toCoreBoard(model.getBoardFields()), model.getVariant());
    }

    private Variant variant(){
        if (model != null) {
            return model.getVariant();
        }
        return fixedVariant != null ? fixedVariant : Variant.STANDARD;
    }

    private static BoardField[] toCoreBoard(BoardFieldState[] chipMatrix){
        BoardField[] board=new BoardField[chipMatrix.length];
        for(int i=0; i<chipMatrix.length; i++){
            board[i]=new BoardField(chipMatrix[i].getNumberOfChips(), chipMatrix[i].getPlayer(),
                    chipMatrix[i].getPinnedPlayer());
        }
        return board;
    }

    private static Die[] toCoreDice(DiceThrow[] throwsArray){
        Die[] dice=new Die[throwsArray.length];
        for(int i=0; i<throwsArray.length; i++){
            dice[i]=new Die(throwsArray[i].getThrowNumber(), throwsArray[i].getAlreadyUsed()==1);
        }
        return dice;
    }

    private int nextDie(){
        return model != null ? model.nextDieValue() : (int)(Math.random()*6)+1;
    }

    //Acey-deucey: a 1-2 roll owes a named double and an extra roll
    private void markAceyDeuceyRoll(DiceThrow[] dices){
        if(model==null || !model.getVariant().getAceyDeuceyRoll()){
            return;
        }
        int a=dices[0].getThrowNumber();
        int b=dices[1].getThrowNumber();
        boolean acey=(a==1 && b==2) || (a==2 && b==1);
        if(acey && model.getState()>=2){
            model.setBonusDoublePending(true);
            model.setExtraTurnPending(true);
        }
    }

    //Method for rolling dices
    public DiceThrow[] rollDices(){
        //Create new array for rolled dices
        DiceThrow[] retDices=new DiceThrow[4];
        //Get two numbers, rolled numbers
        int rollOne=nextDie();
        int rollTwo=nextDie();
        //If game state is 0 or 1 (first throws)
        if(model.getState()<2){
            //If first throw save one thrown number other copy from last throws
            if(model.getState()==0){
                retDices[0] = new DiceThrow(rollOne);
                retDices[1] = model.getDiceThrows()[1];
            }
            //If second throw save other thrown number first copy from last throws
            else{
                retDices[0] = model.getDiceThrows()[0];
                rollOne=retDices[0].getThrowNumber();
                retDices[1] = new DiceThrow(rollTwo);
            }
        }
        //If not in state 0 or 1 save both throw numbers
        else {
            retDices[0] = new DiceThrow(rollOne);
            retDices[1] = new DiceThrow(rollTwo);
        }
        //If its not state 0 and throw numbers are same, add throw number 3,4
        if(rollOne==rollTwo && model.getState()!=0){
            retDices[2]=new DiceThrow(rollOne);
            retDices[3]=new DiceThrow(rollOne);
        }
        //If they are not same make 3,4 empty
        else{
            retDices[2]=new DiceThrow(0);
            retDices[3]=new DiceThrow(0);
            retDices[2].setAlreadyUsed(1);
            retDices[3].setAlreadyUsed(1);
        }
        markAceyDeuceyRoll(retDices);
        return retDices;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    //Getters and setters
    public int getCurrPlayerFinished() {
        return CurrPlayerFinished;
    }

    public void setCurrPlayerFinished(int currPlayerFinished) {
        CurrPlayerFinished = currPlayerFinished;
    }
}
