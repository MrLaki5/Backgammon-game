package games.mrlaki5.backgammon.Journey;

import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import games.mrlaki5.backgammon.GamePreferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** The journey has to teach every variant, in a sane order of difficulty. */
public class JourneyStageTest {

    @Test
    public void everyPlayableVariantAppears() {
        Set<Variant> covered = EnumSet.noneOf(Variant.class);
        for (JourneyStage stage : JourneyStage.values()) {
            covered.add(stage.getVariant());
            if (stage.isTavliRotation()) {
                covered.addAll(Variant.TAVLI_ROTATION);
            }
        }
        assertEquals(EnumSet.allOf(Variant.class), covered);
    }

    @Test
    public void difficultyAndRewardsNeverDrop() {
        int previousDifficulty = -1;
        int previousReward = 0;
        for (JourneyStage stage : JourneyStage.values()) {
            assertTrue(stage.getDifficulty() >= previousDifficulty);
            assertTrue(stage.getRewardCoins() >= previousReward);
            previousDifficulty = stage.getDifficulty();
            previousReward = stage.getRewardCoins();
        }
        assertEquals(GamePreferences.BOT_EASY, JourneyStage.values()[0].getDifficulty());
        assertEquals(GamePreferences.BOT_ROYAL,
                JourneyStage.values()[JourneyStage.values().length - 1].getDifficulty());
    }

    @Test
    public void everyChapterHasABoardTheme() {
        for (JourneyStage stage : JourneyStage.values()) {
            assertTrue(stage.getBoardTheme() >= 0 && stage.getBoardTheme() <= 7);
        }
    }

    @Test
    public void onlyTheLastChapterIsATavliMatch() {
        JourneyStage[] stages = JourneyStage.values();
        for (int i = 0; i < stages.length - 1; i++) {
            assertTrue(!stages[i].isTavliRotation());
        }
        assertTrue(stages[stages.length - 1].isTavliRotation());
        assertTrue(stages[stages.length - 1].getMatchTarget() >= 5);
    }
}
