package games.mrlaki5.backgammon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    private int [][] BoardFields= new int[24][2];   // 2 red, 1 white
    private OnBoardImage BoardImage;


    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x_cord = event.getRawX();
                    float xx= event.getX();
                    float y_cord = event.getRawY();
                    float yy= event.getY();
                    int touchedNum=BoardImage.triangleTouched(xx,yy);
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, xx, yy);
                    BoardImage.invalidate();
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

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);
    }
}
