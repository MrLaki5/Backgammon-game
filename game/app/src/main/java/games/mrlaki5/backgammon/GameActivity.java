package games.mrlaki5.backgammon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    private int [][] BoardFields= new int[24][2];
    private OnBoardImage BoardImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        BoardFields[2][0]=2;
        BoardFields[5][0]=6;
        BoardFields[13][0]=6;
        BoardFields[23][0]=3;

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.invalidate();
    }
}
