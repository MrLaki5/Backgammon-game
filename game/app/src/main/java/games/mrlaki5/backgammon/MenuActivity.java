package games.mrlaki5.backgammon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void startNewGame(View view) {
        Intent intent= new Intent(MenuActivity.this, GameActivity.class);
        startActivity(intent);
    }
}
