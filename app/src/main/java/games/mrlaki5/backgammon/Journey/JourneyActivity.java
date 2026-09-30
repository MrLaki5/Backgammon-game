package games.mrlaki5.backgammon.Journey;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Menus.VariantPicker;
import games.mrlaki5.backgammon.R;

/**
 * The journey: themed chapters that unlock one by one and teach every variant.
 *
 * The screen is a road read from the leading edge outward. The panel on that edge
 * answers "where am I", the cards answer "what is next", and the dashed rule
 * between them turns solid turquoise behind the chapters already won.
 */
public class JourneyActivity extends AppCompatActivity {

    /** One struck seal per chapter, rendered by tools/generate_royal_assets.py. */
    private static final int[] CHAPTER_EMBLEMS = {
            R.drawable.emblem_chapter_1, R.drawable.emblem_chapter_2,
            R.drawable.emblem_chapter_3, R.drawable.emblem_chapter_4,
            R.drawable.emblem_chapter_5, R.drawable.emblem_chapter_6,
            R.drawable.emblem_chapter_7, R.drawable.emblem_chapter_8,
            R.drawable.emblem_chapter_9,
    };

    /** Width of the link drawn between two chapter cards. */
    private static final int ROAD_SEGMENT_WIDTH_DP = 30;
    /** Each card enters a beat after the one before it. */
    private static final long STAGGER_MS = 55L;

    private JourneyManager journeyManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_journey);
        journeyManager = new JourneyManager(this);

        View back = findViewById(R.id.btnJourneyBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindStages();
    }

    private void bindStages() {
        LinearLayout list = findViewById(R.id.journeyList);
        if (list == null) return;
        list.removeAllViews();

        JourneyStage[] stages = JourneyStage.values();
        int completed = journeyManager.getCompletedCount();
        bindProgress(completed, stages.length);

        LayoutInflater inflater = LayoutInflater.from(this);
        int firstOpen = -1;
        for (int i = 0; i < stages.length; i++) {
            if (i > 0) {
                list.addView(roadSegment(journeyManager.isCompleted(i - 1)));
            }
            View card = stageCard(inflater, list, i, stages[i]);
            list.addView(card);
            if (firstOpen < 0 && journeyManager.isUnlocked(i) && !journeyManager.isCompleted(i)) {
                firstOpen = i;
            }
            animateIn(card, i);
        }
        bindHint(stages, firstOpen);
        scrollToCurrent(list, firstOpen);
    }

    private void bindProgress(int completed, int total) {
        TextView progress = findViewById(R.id.tvJourneyProgress);
        if (progress != null) {
            progress.setText(getString(R.string.journey_progress, completed, total));
        }
        ProgressBar bar = findViewById(R.id.journeyProgressBar);
        if (bar != null) {
            bar.setProgress(total == 0 ? 0 : Math.round(completed * 100f / total));
        }
    }

    private void bindHint(JourneyStage[] stages, int firstOpen) {
        TextView hint = findViewById(R.id.tvJourneyHint);
        if (hint == null) return;
        if (firstOpen < 0) {
            hint.setText(R.string.journey_hint_complete);
        } else {
            hint.setText(getString(R.string.journey_hint_current,
                    getString(stages[firstOpen].getTitleRes())));
        }
    }

    /** Leaves the player looking at the chapter they are about to play, not at chapter one. */
    private void scrollToCurrent(LinearLayout list, int firstOpen) {
        if (firstOpen <= 0) return;
        HorizontalScrollView scroll = findViewById(R.id.journeyRoadScroll);
        if (scroll == null) return;
        // Two views per chapter after the first: the road segment and the card.
        int childIndex = firstOpen * 2;
        list.post(() -> {
            if (childIndex >= list.getChildCount()) return;
            View target = list.getChildAt(childIndex);
            int centred = target.getLeft() - (scroll.getWidth() - target.getWidth()) / 2;
            scroll.smoothScrollTo(Math.max(0, centred), 0);
        });
    }

    private View roadSegment(boolean travelled) {
        float density = getResources().getDisplayMetrics().density;
        View segment = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (ROAD_SEGMENT_WIDTH_DP * density), (int) (2 * density));
        params.gravity = android.view.Gravity.CENTER_VERTICAL;
        segment.setLayoutParams(params);
        segment.setBackgroundResource(
                travelled ? R.drawable.road_segment_done : R.drawable.road_segment);
        // A dashed line is drawn by a stroke, which needs a layer to draw into.
        segment.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return segment;
    }

    private View stageCard(LayoutInflater inflater, ViewGroup parent, int index, JourneyStage stage) {
        View card = inflater.inflate(R.layout.item_journey_stage, parent, false);
        boolean unlocked = journeyManager.isUnlocked(index);
        boolean completed = journeyManager.isCompleted(index);

        TextView number = card.findViewById(R.id.stageNumber);
        TextView title = card.findViewById(R.id.stageTitle);
        TextView opponent = card.findViewById(R.id.stageOpponent);
        TextView variantChip = card.findViewById(R.id.stageVariantChip);
        TextView matchChip = card.findViewById(R.id.stageMatchChip);
        TextView flavor = card.findViewById(R.id.stageFlavor);
        TextView reward = card.findViewById(R.id.stageReward);
        ImageView emblem = card.findViewById(R.id.stageEmblem);
        ImageView badge = card.findViewById(R.id.stageBadge);
        ImageView go = card.findViewById(R.id.stageGo);
        ImageView coin = card.findViewById(R.id.stageCoinIcon);

        number.setText(getString(R.string.journey_chapter_number, index + 1));
        emblem.setImageResource(CHAPTER_EMBLEMS[index % CHAPTER_EMBLEMS.length]);
        title.setText(stage.getTitleRes());
        opponent.setText(getString(R.string.journey_opponent_line,
                getString(stage.getOpponentRes())));
        variantChip.setText(stage.isTavliRotation()
                ? getString(R.string.variant_tavli)
                : getString(VariantPicker.nameRes(stage.getVariant())));
        matchChip.setText(VariantPicker.matchLabel(this, stage.getMatchTarget()));
        reward.setText(getString(R.string.journey_reward_line, stage.getRewardCoins()));
        flavor.setText(unlocked ? getString(stage.getFlavorRes()) : getString(R.string.journey_locked));

        if (completed) {
            card.setBackgroundResource(R.drawable.bg_card_stage_done);
            badge.setImageResource(R.drawable.ic_royal_seal_check);
            badge.setVisibility(View.VISIBLE);
            matchChip.setBackgroundResource(R.drawable.bg_chip_turquoise);
        } else if (unlocked) {
            card.setBackgroundResource(R.drawable.bg_card_stage_current);
            badge.setVisibility(View.GONE);
        } else {
            card.setBackgroundResource(R.drawable.bg_card_stage_locked);
            badge.setImageResource(R.drawable.ic_royal_lock);
            badge.setVisibility(View.VISIBLE);
            // The seal has not been struck yet: show it in cold metal.
            emblem.setColorFilter(greyscale());
            number.setTextColor(getResources().getColor(R.color.ink_muted));
            title.setTextColor(getResources().getColor(R.color.ink_muted));
            go.setVisibility(View.INVISIBLE);
            coin.setAlpha(0.35f);
            reward.setAlpha(0.35f);
            variantChip.setAlpha(0.45f);
            matchChip.setAlpha(0.45f);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
        params.gravity = android.view.Gravity.CENTER_VERTICAL;
        card.setLayoutParams(params);

        if (unlocked) {
            card.setOnClickListener(v -> startStage(index, stage));
        } else {
            card.setOnClickListener(v ->
                    Toast.makeText(this, R.string.journey_locked, Toast.LENGTH_SHORT).show());
        }
        return card;
    }

    /** Drains the colour from a locked chapter's seal without dimming its relief. */
    private static android.graphics.ColorMatrixColorFilter greyscale() {
        android.graphics.ColorMatrix matrix = new android.graphics.ColorMatrix();
        matrix.setSaturation(0.12f);
        return new android.graphics.ColorMatrixColorFilter(matrix);
    }

    private void animateIn(View card, int index) {
        card.setAlpha(0f);
        card.postDelayed(() -> {
            card.setAlpha(1f);
            card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.stage_enter));
        }, index * STAGGER_MS);
    }

    private void startStage(int index, JourneyStage stage) {
        PlayerProfileManager profile = PlayerProfileManager.getInstance(this);
        java.io.File save = new java.io.File(getFilesDir().getAbsolutePath(),
                MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        save.delete();

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, profile.getDisplayName());
        intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, getString(stage.getOpponentRes()));
        intent.putExtra(MenuActivity.EXTRA_PLAYER1_KIND, "Player");
        intent.putExtra(MenuActivity.EXTRA_PLAYER2_KIND, "Bot");
        intent.putExtra(MenuActivity.EXTRA_VARIANT, stage.getVariant().name());
        intent.putExtra(MenuActivity.EXTRA_MATCH_TARGET, stage.getMatchTarget());
        intent.putExtra(MenuActivity.EXTRA_TAVLI, stage.isTavliRotation());
        intent.putExtra(MenuActivity.EXTRA_BOT_DIFFICULTY, stage.getDifficulty());
        intent.putExtra(MenuActivity.EXTRA_JOURNEY_STAGE, index);
        if (GamePreferences.isThemeUnlocked(this, stage.getBoardTheme())) {
            intent.putExtra(MenuActivity.EXTRA_BOARD_THEME, stage.getBoardTheme());
        }
        startActivity(intent);
    }
}
