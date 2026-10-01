package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.ScoreElem;
import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.PlayerProfile;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.R;

/**
 * Redesigned ScoresActivity showing a 3-tab leaderboard:
 * - This Week
 * - This Month
 * - All Time
 *
 * Also provides access to legacy head-to-head scores via a bottom button.
 */
public class ScoresActivity extends AppCompatActivity {

    private static final int TAB_WEEK = 0;
    private static final int TAB_MONTH = 1;
    private static final int TAB_ALL_TIME = 2;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    private DbHelper dbHelper;
    private ListView leaderboardList;
    private TextView emptyText;
    private Button tabWeek, tabMonth, tabAllTime;
    private int currentTab = TAB_WEEK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_leaderboard);

        dbHelper = new DbHelper(this);
        leaderboardList = findViewById(R.id.leaderboardList);
        emptyText = findViewById(R.id.emptyLeaderboardText);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabAllTime = findViewById(R.id.tabAllTime);

        tabWeek.setOnClickListener(v -> selectTab(TAB_WEEK));
        tabMonth.setOnClickListener(v -> selectTab(TAB_MONTH));
        tabAllTime.setOnClickListener(v -> selectTab(TAB_ALL_TIME));

        selectTab(TAB_WEEK);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        updateTabAppearance();
        loadLeaderboard();
    }

    private void updateTabAppearance() {
        // Using selected state to style the active tab via segmented_toggle_button selector
        tabWeek.setSelected(currentTab == TAB_WEEK);
        tabMonth.setSelected(currentTab == TAB_MONTH);
        tabAllTime.setSelected(currentTab == TAB_ALL_TIME);
    }

    private void loadLeaderboard() {
        List<PlayerProfile> profiles;
        switch (currentTab) {
            case TAB_WEEK:
                profiles = dbHelper.getLeaderboardThisWeek();
                break;
            case TAB_MONTH:
                profiles = dbHelper.getLeaderboardThisMonth();
                break;
            case TAB_ALL_TIME:
            default:
                profiles = dbHelper.getLeaderboardAllTime();
                break;
        }

        boolean hasData = !profiles.isEmpty();
        emptyText.setVisibility(hasData ? View.GONE : View.VISIBLE);
        leaderboardList.setVisibility(hasData ? View.VISIBLE : View.GONE);

        if (hasData) {
            LeaderboardAdapter adapter = new LeaderboardAdapter(this, profiles);
            leaderboardList.setAdapter(adapter);
        }
    }

    /**
     * Shows the legacy head-to-head scores from the original scores table.
     */
    public void showLegacyScores(View view) {
        Intent intent = new Intent(this, LegacyScoresActivity.class);
        startActivity(intent);
    }
}
