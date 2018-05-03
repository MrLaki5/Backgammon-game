package games.mrlaki5.backgammon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

public class GameActivity extends AppCompatActivity {

    private BoardFieldState[] BoardFields= new BoardFieldState[26];   // 2 red, 1 white 24-white, 25-red side board
    private DiceThrow[] diceThrows=new DiceThrow[4];

    private GameLogic gameLogic;
    private OnBoardImage BoardImage;

    private List<NextJump> nextMoves=null;
    private int [] NextMoves=new int[24];

    private int CurrentPlayer;
    private int MoveFieldSrc;

    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x_touch= event.getX();
            float y_touch= event.getY();
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    int touchedNum=BoardImage.triangleTouched(x_touch,y_touch);
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, x_touch, y_touch);
                    if(isTouched){

                        if(BoardFields[touchedNum].getPlayer()==CurrentPlayer) {
                            nextMoves = gameLogic.calculateMoves(BoardFields, BoardFields[touchedNum].getPlayer(), diceThrows);
                            NextMoves = gameLogic.calculateNextMovesForSpecificField(nextMoves, touchedNum);
                            if (NextMoves!=null) {
                                BoardFields[touchedNum].setNumberOfChips(BoardFields[touchedNum].getNumberOfChips()-1);
                                if(BoardFields[touchedNum].getNumberOfChips()==0) {
                                    BoardFields[touchedNum].setPlayer(0);
                                }
                                BoardImage.setNextMoveArray(NextMoves);
                                MoveFieldSrc=touchedNum;
                                BoardImage.setMoveChip(x_touch, y_touch, CurrentPlayer);
                                BoardImage.invalidate();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(BoardImage.moveMoveChip(x_touch, y_touch)) {
                        BoardImage.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(BoardImage.unsetMoveChip()) {
                        int dstField = BoardImage.triangleTouched(x_touch,y_touch);
                        if(dstField!=-1) {
                            int throwNum = 0;
                            for (NextJump tempJump: nextMoves) {
                                if(tempJump.getSrcField()==MoveFieldSrc && tempJump.getDstField()==dstField){
                                    throwNum=tempJump.getJumpNumber();
                                    break;
                                }
                            }
                            if(throwNum==0){
                                BoardFields[MoveFieldSrc].setNumberOfChips(BoardFields[MoveFieldSrc].getNumberOfChips()+1);
                                if(BoardFields[MoveFieldSrc].getNumberOfChips()==1){
                                    BoardFields[MoveFieldSrc].setPlayer(CurrentPlayer);
                                }
                            }
                            else {
                                for (DiceThrow tempThrow : diceThrows) {
                                    if (tempThrow.getThrowNumber() == throwNum && tempThrow.getAlreadyUsed() == 0) {
                                        tempThrow.setAlreadyUsed(1);
                                        break;
                                    }
                                }
                                int tmpPlayer=BoardFields[dstField].getPlayer();
                                if(BoardFields[dstField].getNumberOfChips()==1 && tmpPlayer!=CurrentPlayer){
                                    BoardFields[23+tmpPlayer].setNumberOfChips(BoardFields[23+tmpPlayer].getNumberOfChips()+1);
                                    if(BoardFields[23+tmpPlayer].getNumberOfChips()==1){
                                        BoardFields[23+tmpPlayer].setPlayer(tmpPlayer);
                                    }
                                }
                                else {
                                    BoardFields[dstField].setNumberOfChips(BoardFields[dstField].getNumberOfChips() + 1);
                                }
                                if(BoardFields[dstField].getNumberOfChips()==1){
                                    BoardFields[dstField].setPlayer(CurrentPlayer);
                                }
                            }

                        }
                        else{
                            BoardFields[MoveFieldSrc].setNumberOfChips(BoardFields[MoveFieldSrc].getNumberOfChips()+1);
                            if(BoardFields[MoveFieldSrc].getNumberOfChips()==1){
                                BoardFields[MoveFieldSrc].setPlayer(CurrentPlayer);
                            }
                        }
                        NextMoves = null;
                        BoardImage.setNextMoveArray(NextMoves);
                        BoardImage.invalidate();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameLogic = new GameLogic();

        for(int i=0; i<diceThrows.length; i++){
            diceThrows[i]=new DiceThrow(0);
            diceThrows[i].setAlreadyUsed(1);
        }

        diceThrows[0].setThrowNumber(6);
        diceThrows[0].setAlreadyUsed(0);
        diceThrows[1].setThrowNumber(5);
        diceThrows[1].setAlreadyUsed(0);


        CurrentPlayer=1;

        for(int i=0; i<BoardFields.length; i++){
            BoardFields[i]=new BoardFieldState();
        }

        BoardFields[0].setNumberOfChips(5);
        BoardFields[0].setPlayer(1);

        BoardFields[11].setNumberOfChips(2);
        BoardFields[11].setPlayer(1);

        BoardFields[16].setNumberOfChips(3);
        BoardFields[16].setPlayer(1);

        BoardFields[18].setNumberOfChips(5);
        BoardFields[18].setPlayer(1);


        BoardFields[4].setNumberOfChips(3);
        BoardFields[4].setPlayer(2);

        BoardFields[6].setNumberOfChips(5);
        BoardFields[6].setPlayer(2);

        BoardFields[12].setNumberOfChips(5);
        BoardFields[12].setPlayer(2);

        BoardFields[23].setNumberOfChips(2);
        BoardFields[23].setPlayer(2);


        //TEST PART

        BoardFields[24].setNumberOfChips(1);
        BoardFields[24].setPlayer(1);

        BoardFields[25].setNumberOfChips(1);
        BoardFields[25].setPlayer(2);


        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);
    }
}
