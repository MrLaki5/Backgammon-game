package games.mrlaki5.backgammon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    private int [][] BoardFields= new int[24][2];   // 2 red, 1 white
    private OnBoardImage BoardImage;

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
    }
}
