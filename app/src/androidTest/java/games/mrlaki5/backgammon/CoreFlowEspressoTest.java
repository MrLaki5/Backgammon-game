package games.mrlaki5.backgammon;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import games.mrlaki5.backgammon.Database.DbHelper;
import games.mrlaki5.backgammon.Database.ScoresTableEntry;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Menus.ScoresActivity;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CoreFlowEspressoTest {

    @Test
    public void startsNewGameFromMenuActivity() {
        try (ActivityScenario<MenuActivity> scenario = ActivityScenario.launch(MenuActivity.class)) {
            Espresso.onView(withId(R.id.playGame)).perform(click());
            Espresso.onView(withId(R.id.choiceNewGame)).perform(click());
            Espresso.onView(withId(R.id.dialogPlay)).perform(click());
            Espresso.onView(withId(R.id.boardImage)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void gameActivityShowsTurnControlsForNewMatch() {
        Intent intent = new Intent(appContext(), GameActivity.class);
        intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, "Player One");
        intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, "Bot");
        intent.putExtra(MenuActivity.EXTRA_PLAYER1_KIND, "Player");
        intent.putExtra(MenuActivity.EXTRA_PLAYER2_KIND, "Bot");

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(withId(R.id.boardImage)).check(matches(isDisplayed()));
            Espresso.onView(withId(R.id.rollDiceButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void scoresActivityShowsFinishedGameResult() {
        seedFinishedGame();
        try (ActivityScenario<ScoresActivity> scenario = ActivityScenario.launch(ScoresActivity.class)) {
            Espresso.onView(withId(R.id.mainScoreText)).check(matches(isDisplayed()));
            Espresso.onView(withText("Player One")).check(matches(isDisplayed()));
        }
    }

    private static void seedFinishedGame() {
        Context context = appContext();
        context.deleteDatabase(DbHelper.DATABASE_NAME);
        DbHelper helper = new DbHelper(context);
        ContentValues values = new ContentValues();
        values.put(ScoresTableEntry.COLUMN_PLAYER1_NAME, "Player One");
        values.put(ScoresTableEntry.COLUMN_PLAYER2_NAME, "Bot");
        values.put(ScoresTableEntry.COLUMN_PLAYER1_WIN, 1);
        values.put(ScoresTableEntry.COLUMN_PLAYER2_WIN, 0);
        values.put(ScoresTableEntry.COLUMN_END_GAME_TIME, "00:10");
        helper.getWritableDatabase().insert(ScoresTableEntry.TABLE_NAME, null, values);
        helper.close();
    }

    private static Context appContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}
