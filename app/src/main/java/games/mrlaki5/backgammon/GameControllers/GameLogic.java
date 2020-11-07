package games.mrlaki5.backgammon.GameControllers;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameModel.Model;

//Class for storing rules and logic of game
public class GameLogic {

    //Model for saving state of game
    private Model model;
    //Flag for signaling wining player
    private int CurrPlayerFinished=0;

    //Controller
    public GameLogic(Model model) {
        this.model = model;
    }

    //TriangleBoardFields:
    //  Matrix pos: 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23
    //  White real: 12,11,10,9,8,7,6,5,4,3,2,1,13,14,15,16,17,18,19,20,21,22,23,24
    //  Red real: 13,14,15,16,17,18,19,20,21,22,23,24,12,11,10,9,8,7,6,5,4,3,2,1
    //Side board:
    //  Matrix pos: 24,25
    //  White real: 0,X
    //  Red real: X,0
    //End board:
    //  Matrix po: 26,27
    //  White real: X,100
    //  Red real: 100,X

    //Method for calculating real position from matrix position depending on which player
    public int calculateRealPosition(int ChipMatrixPos, int PlayerNum){
        if(PlayerNum==2){
            if(ChipMatrixPos==25){
                return 0;
            }
            if(ChipMatrixPos==26){
                return 100;
            }
            return 25-calculateRealPosition(ChipMatrixPos, 1);
        }
        if(ChipMatrixPos==24){
            return 0;
        }
        if(ChipMatrixPos==27){
            return 100;
        }
        if(ChipMatrixPos<=11){
            return 12-ChipMatrixPos;
        }
        else{
            return 1+ChipMatrixPos;
        }
    }

    //Method for calculating matrix position from real position depending on which player
    public int calculateMatrixPosition(int ChipRealPos, int PlayerNum){
        if(PlayerNum==2){
            if(ChipRealPos==0){
                return 25;
            }
            if(ChipRealPos==100){
                return 26;
            }
            return calculateMatrixPosition(25-ChipRealPos, 1);
        }
        if(ChipRealPos ==0){
            return 24;
        }
        if(ChipRealPos==100){
            return 27;
        }
        if(ChipRealPos<=12){
            return 12-ChipRealPos;
        }
        else{
            return ChipRealPos-1;
        }
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
        ArrayList<NextJump> jumps=new ArrayList<>();
        int i=0;
        //Call method to find out is game in PlayPart, EndPart or Finished
        int whichPart=whatPartOfGame(ChipMatrix, PlayerNum);
        switch(whichPart){
            //In play part
            case 0:
            //In End part
            case 1:
                //If there are chips in side board set iterations to side board
                //(sideboard must be played first)
                if(ChipMatrix[24].getNumberOfChips()>0 && PlayerNum==1){
                    i=24;
                }
                if(ChipMatrix[25].getNumberOfChips()>0 && PlayerNum==2){
                    i=25;
                }
                //Flag used for end game part to know what is lowest field with chips in it
                int firstFieldWithChip=-1;
                //Go through fields
                for(; i<26; i++){
                    //If current player and field player are same
                    if(PlayerNum==ChipMatrix[i].getPlayer()){
                        //Calculate real position of field
                        int realPos=calculateRealPosition(i, PlayerNum);
                        //If first with chips is not set, set it
                        if(firstFieldWithChip==-1 && ChipMatrix[i].getNumberOfChips()>0){
                            firstFieldWithChip=realPos;
                        }
                        //Go though throws and calculate next moves for current field
                        for(int j=0; j<Throws.length; j++){
                            //If throw is used continue
                            if(Throws[j].getAlreadyUsed()==1){
                                continue;
                            }
                            //Calculate possible next position
                            int realNextPos=realPos+Throws[j].getThrowNumber();
                            if(realNextPos>24){
                                //If next position is out of board and it is not end game
                                // dont add it
                                if(whichPart==0) {
                                    continue;
                                }
                                //It is en game but its over end board or its not lowest fild
                                if(!(firstFieldWithChip==realPos)){
                                    if(!(realNextPos==25)){
                                        continue;
                                    }
                                }
                                //It is end board and ok with rules in end board
                                realNextPos=100;
                            }
                            int matrixNextPos=calculateMatrixPosition(realNextPos, PlayerNum);
                            //Check if player of jumping filed is current player or that
                            //there are no chips on jumping field
                            if((ChipMatrix[matrixNextPos].getPlayer()==PlayerNum) ||
                                    (ChipMatrix[matrixNextPos].getNumberOfChips()<=1)){
                                //Add jump to list
                                jumps.add(new NextJump(Throws[j].getThrowNumber(), i,
                                        matrixNextPos));
                            }
                        }
                    }
                }
                break;
            //Game finished
            case 2:
                //Set finish flag
                CurrPlayerFinished=PlayerNum;
                break;
        }
        return jumps;
    }

    //Method for checking in what part game is
    //return value 0-game goes, 1-last phase, 2-game done
    public int whatPartOfGame(BoardFieldState[] ChipMatrix, int PlayerNum){
        int leftChipNum=0;
        boolean isLastPart=true;
        //Go through all fields and check where are chips
        for (int i=0; i<26; i++){
            //If player of field is same as current player
            if(ChipMatrix[i].getPlayer()==PlayerNum){
                //Calculate on field left chips
                leftChipNum+=ChipMatrix[i].getNumberOfChips();
                //Calculate real position of filed
                int fieldPos=calculateRealPosition(i, PlayerNum);
                //If it is not in last part then game is not in end part
                if(!(fieldPos>=19 && fieldPos<=24)){
                    isLastPart= false;
                }
            }
        }
        //If game is in end part
        if(isLastPart){
            //No chips left, game finished
            if(leftChipNum==0){
                return 2;
            }
            //There are chips left, game is in end part
            else{
                return 1;
            }
        }
        //Game is in going part
        else{
            return 0;
        }
    }

    //Method for rolling dices
    public DiceThrow[] rollDices(){
        //Create new array for rolled dices
        DiceThrow[] retDices=new DiceThrow[4];
        //Get two numbers, rolled numbers
        int rollOne=(int)(Math.random()*6)+1;
        int rollTwo=(int)(Math.random()*6)+1;
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
        return retDices;
    }

    //Getters and setters
    public int getCurrPlayerFinished() {
        return CurrPlayerFinished;
    }

    public void setCurrPlayerFinished(int currPlayerFinished) {
        CurrPlayerFinished = currPlayerFinished;
    }
}
