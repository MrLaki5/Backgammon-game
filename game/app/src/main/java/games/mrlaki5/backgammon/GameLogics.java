package games.mrlaki5.backgammon;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

public class GameLogics {

    public int CalculateRealPosition(int ChipMatrixPos, int PlayerNum){
        if(PlayerNum==2){
            return 25-CalculateRealPosition(ChipMatrixPos, 1);
        }
        if(ChipMatrixPos<=11){
            return 12-ChipMatrixPos;
        }
        else{
            return 1+ChipMatrixPos;
        }
    }

    public int CalculateMatrixPosition(int ChipRealPos, int PlayerNum){
        if(PlayerNum==2){
            return CalculateMatrixPosition(25-ChipRealPos, 1);
        }
        if(ChipRealPos<=12){
            return 12-ChipRealPos;
        }
        else{
            return ChipRealPos-1;
        }
    }
    
    public int[] CalculateNextMovesForSpecificField(List<NextJump> MovesList, int Field){
        int[] NextArray=new int[24];
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

    public List<NextJump> caluculateMoves(BoardFieldState[] ChipMatrix, int PlayerNum, DiceThrow[] Throws){
        ArrayList<NextJump> jumps=new ArrayList<>();
        for(int i=0; i<ChipMatrix.length; i++){
            if(PlayerNum==ChipMatrix[i].getPlayer()){
                int realPos=CalculateRealPosition(i, PlayerNum);
                for(int j=0; j<Throws.length; j++){
                    if(Throws[j].getAlreadyUsed()==1){
                        continue;
                    }
                    int realNextPos=realPos+Throws[j].getThrowNumber();
                    if(realNextPos>24){
                        continue;
                    }
                    int matrixNextPos=CalculateMatrixPosition(realNextPos, PlayerNum);
                    if((ChipMatrix[matrixNextPos].getPlayer()==PlayerNum) || (ChipMatrix[matrixNextPos].getNumberOfChips()<=1)){
                        jumps.add(new NextJump(Throws[j].getThrowNumber(), i, matrixNextPos));
                    }
                }
            }
        }
        return jumps;
    }

}
