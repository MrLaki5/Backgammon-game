package games.mrlaki5.backgammon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    private int [][] BoardFields= new int[24][2];   // 2 red, 1 white
    private int [] NextMoves=new int[24];
    private OnBoardImage BoardImage;


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
                        BoardImage.setMoveChip(xx,yy,BoardFields[touchedNum][1]);
                        BoardImage.invalidate();
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


        BoardFields[0][0]=5;
        BoardFields[0][1]=1;

        BoardFields[11][0]=2;
        BoardFields[11][1]=1;

        BoardFields[16][0]=3;
        BoardFields[16][1]=1;

        BoardFields[18][0]=5;
        BoardFields[18][1]=1;



        BoardFields[4][0]=3;
        BoardFields[4][1]=2;

        BoardFields[6][0]=5;
        BoardFields[6][1]=2;

        BoardFields[12][0]=5;
        BoardFields[12][1]=2;

        BoardFields[23][0]=2;
        BoardFields[23][1]=2;

        NextMoves[0]=1;
        NextMoves[1]=1;
        NextMoves[12]=1;
        NextMoves[3]=1;
        NextMoves[4]=1;
        NextMoves[6]=1;
        NextMoves[7]=1;
        NextMoves[18]=1;
        NextMoves[19]=1;
        NextMoves[10]=1;
        NextMoves[11]=1;
        NextMoves[15]=1;
        NextMoves[16]=1;

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.setNextMoveArray(NextMoves);
        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);
    }
}
