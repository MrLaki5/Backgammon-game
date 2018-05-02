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

    private BoardFieldState[] BoardFields= new BoardFieldState[24];   // 2 red, 1 white
    private DiceThrow[] diceThrows=new DiceThrow[4];

    private GameLogics gameLogics;
    private OnBoardImage BoardImage;

    private List<NextJump> nextMoves=null;
    private int [] NextMoves=new int[24];

    private int CurrentPlayer;

    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    float xx= event.getX();
                    float yy= event.getY();
                    int touchedNum=BoardImage.triangleTouched(xx,yy);
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, xx, yy);
                    if(isTouched){

                        if(BoardFields[touchedNum].getPlayer()==CurrentPlayer) {
                            nextMoves = gameLogics.caluculateMoves(BoardFields, BoardFields[touchedNum].getPlayer(), diceThrows);
                            NextMoves = gameLogics.CalculateNextMovesForSpecificField(nextMoves, touchedNum);
                            if (NextMoves!=null) {
                                BoardImage.setNextMoveArray(NextMoves);

                                BoardImage.setMoveChip(xx, yy, BoardFields[touchedNum].getPlayer());
                                BoardImage.invalidate();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float xx1= event.getX();
                    float yy1= event.getY();
                    if(BoardImage.moveMoveChip(xx1, yy1)) {
                        BoardImage.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(BoardImage.unsetMoveChip()) {
                        NextMoves=null;
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

        gameLogics= new GameLogics();

        for(int i=0; i<4; i++){
            diceThrows[i]=new DiceThrow(0);
            diceThrows[i].setAlreadyUsed(1);
        }

        diceThrows[0].setThrowNumber(6);
        diceThrows[0].setAlreadyUsed(0);
        diceThrows[1].setThrowNumber(5);
        diceThrows[1].setAlreadyUsed(0);


        CurrentPlayer=1;

        for(int i=0; i<24; i++){
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


        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);
    }
}
