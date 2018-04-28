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

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.invalidate();
    }
}
