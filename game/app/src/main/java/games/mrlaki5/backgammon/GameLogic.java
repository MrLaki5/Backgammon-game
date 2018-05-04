package games.mrlaki5.backgammon;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

public class GameLogic {

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
    
    public int[] calculateNextMovesForSpecificField(List<NextJump> MovesList, int Field){
        int[] NextArray=new int[28];
        int flag=0;
        for (NextJump tempJump: MovesList) {
            if(tempJump.getSrcField()==Field){
                NextArray[tempJump.getDstField()]=1;
                flag=1;
            }
        }
        if(flag==1) {
            return NextArray;
        }
        else{
            return null;
        }
    }

    public List<NextJump> calculateMoves(BoardFieldState[] ChipMatrix, int PlayerNum, DiceThrow[] Throws){
        ArrayList<NextJump> jumps=new ArrayList<>();
        int i=0;
        int whichPart=whatPartOfGame(ChipMatrix, PlayerNum);
        switch(whichPart){
            case 0:
            case 1:
                if(ChipMatrix[24].getNumberOfChips()>0 && PlayerNum==1){
                    i=24;
                }
                if(ChipMatrix[25].getNumberOfChips()>0 && PlayerNum==2){
                    i=25;
                }
                for(; i<26; i++){
                    if(PlayerNum==ChipMatrix[i].getPlayer()){
                        int realPos=calculateRealPosition(i, PlayerNum);
                        for(int j=0; j<Throws.length; j++){
                            if(Throws[j].getAlreadyUsed()==1){
                                continue;
                            }
                            int realNextPos=realPos+Throws[j].getThrowNumber();
                            if(realNextPos>24){
                                if(whichPart==0) {
                                    continue;
                                }
                                realNextPos=100;
                                //TODO: Last part of game part
                            }
                            int matrixNextPos=calculateMatrixPosition(realNextPos, PlayerNum);
                            if((ChipMatrix[matrixNextPos].getPlayer()==PlayerNum) || (ChipMatrix[matrixNextPos].getNumberOfChips()<=1)){
                                jumps.add(new NextJump(Throws[j].getThrowNumber(), i, matrixNextPos));
                            }
                        }
                    }
                }
                break;
            case 2:
                //TODO: Game done part
                break;
        }
        return jumps;
    }

    //0-game goes, 1-last phase, 2-game done
    public int whatPartOfGame(BoardFieldState[] ChipMatrix, int PlayerNum){
        int leftChipNum=0;
        boolean isLastPart=true;
        for (int i=0; i<26; i++){
            if(ChipMatrix[i].getPlayer()==PlayerNum){
                leftChipNum+=ChipMatrix[i].getNumberOfChips();
                int fieldPos=calculateRealPosition(i, PlayerNum);
                if(!(fieldPos>=19 && fieldPos<=24)){
                    isLastPart= false;
                }
            }
        }
        if(isLastPart){
            if(leftChipNum==0){
                return 2;
            }
            else{
                return 1;
            }
        }
        else{
            return 0;
        }
    }

}
