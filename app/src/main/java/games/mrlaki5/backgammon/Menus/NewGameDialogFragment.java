package games.mrlaki5.backgammon.Menus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.TypefaceCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.io.File;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.GameView.themes.ThemeRegistry;
import games.mrlaki5.backgammon.GameView.themes.ThemeThumbnailLoader;
import games.mrlaki5.backgammon.R;

public class NewGameDialogFragment extends BottomSheetDialogFragment {

    private VariantPicker variantPicker;

    private final int[] selectedTheme = new int[1];
    private final int[] selectedDiff = new int[1];

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                int displayHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(displayHeight);

                float translateY = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 80f, getResources().getDisplayMetrics());
                bottomSheet.setTranslationY(translateY);
                bottomSheet.setAlpha(0f);
                bottomSheet.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(280L)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Scroll wrapper keeps the sheet usable on short landscape screens
        NestedScrollView scroll = new NestedScrollView(requireContext());
        scroll.addView(inflater.inflate(R.layout.new_game_bottom_sheet, scroll, false));
        return scroll;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        variantPicker = new VariantPicker(view);

        // Custom font for title
        TextView tvTitle = view.findViewById(R.id.sheetTitle);
        if (tvTitle != null) {
            try {
                Typeface base = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Cinzel-Regular.ttf");
                Typeface typeface = TypefaceCompat.create(requireContext(), base, Typeface.BOLD);
                tvTitle.setTypeface(typeface);
            } catch (Exception ignored) {}
        }

        // Theme selection (2x4 thumbnails)
        selectedTheme[0] = GamePreferences.getBoardTheme(requireContext());
        setupThemePicker(view, selectedTheme);

        // Difficulty selection (4 buttons)
        selectedDiff[0] = GamePreferences.getBotDifficulty(requireContext());
        final Button[] diffBtns = {
                view.findViewById(R.id.diffEasy),
                view.findViewById(R.id.diffMedium),
                view.findViewById(R.id.diffHard),
                view.findViewById(R.id.diffRoyal)
        };
        updateDifficultySelection(diffBtns, selectedDiff[0]);
        for (int i = 0; i < diffBtns.length; i++) {
            final int idx = i;
            if (diffBtns[i] != null) {
                diffBtns[i].setOnClickListener(v -> {
                    selectedDiff[0] = idx;
                    updateDifficultySelection(diffBtns, idx);
                });
            }
        }

        // Player Name
        EditText nameField = view.findViewById(R.id.singlePlayerName);
        if (nameField != null) {
            MenuActivity.setupInlineEditText(nameField, getString(R.string.player_one));
        }

        // Close button (replaces the old Cancel button — dismisses the sheet)
        View btnClose = view.findViewById(R.id.singleCloseButton);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                if (getActivity() instanceof MenuActivity) {
                    ((MenuActivity) getActivity()).playMenuTap();
                }
                dismiss();
            });
        }

        MaterialButton btnPlay = view.findViewById(R.id.singlePlay);
        if (btnPlay != null) {
            applyPlayButtonPolish(btnPlay);
            btnPlay.setOnClickListener(v -> {
                if (getActivity() instanceof MenuActivity) {
                    ((MenuActivity) getActivity()).playMenuTap();
                }
                String playerName = nameField != null ? nameField.getText().toString().trim() : "";
                if (playerName.isEmpty() || playerName.equals(getString(R.string.player_name))) {
                    playerName = getString(R.string.player_one);
                }

                GamePreferences.saveSelections(requireContext(), selectedDiff[0], selectedTheme[0]);
                dismiss();

                File file = new File(requireActivity().getFilesDir().getAbsolutePath(),
                        MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
                file.delete();

                Intent intent = new Intent(requireContext(), GameActivity.class);
                intent.putExtra(MenuActivity.EXTRA_PLAYER1_NAME, playerName);
                intent.putExtra(MenuActivity.EXTRA_PLAYER2_NAME, getString(R.string.bot_player));
                intent.putExtra(MenuActivity.EXTRA_PLAYER1_KIND, "Player");
                intent.putExtra(MenuActivity.EXTRA_PLAYER2_KIND, "Bot");
                variantPicker.applyTo(intent);
                requireActivity().startActivityForResult(intent, MenuActivity.REQUEST_CODE_GAME);
            });
        }
    }

    private void applyPlayButtonPolish(View btnPlay) {
        btnPlay.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(70L).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120L)
                            .setInterpolator(new OvershootInterpolator(0.72f)).start();
                    break;
            }
            return false;
        });

        btnPlay.setAlpha(0f);
        btnPlay.setTranslationY(18f);
        btnPlay.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260L)
                .setInterpolator(new OvershootInterpolator(0.72f))
                .start();
    }

    private void setupThemePicker(View root, int[] selectedRef) {
        int[] thumbIds = {
                R.id.themeThumb0, R.id.themeThumb1, R.id.themeThumb2, R.id.themeThumb3,
                R.id.themeThumb4, R.id.themeThumb5, R.id.themeThumb6, R.id.themeThumb7
        };
        int[] overlayIds = {
                0, R.id.lockOverlay1, R.id.lockOverlay2, R.id.lockOverlay3,
                R.id.lockOverlay4, R.id.lockOverlay5, R.id.lockOverlay6, R.id.lockOverlay7
        };

        FrameLayout[] thumbs = new FrameLayout[thumbIds.length];
        View[] lockViews = new View[overlayIds.length];

        for (int i = 0; i < thumbIds.length; i++) {
            thumbs[i] = root.findViewById(thumbIds[i]);
        }
        for (int i = 0; i < overlayIds.length; i++) {
            if (overlayIds[i] != 0) {
                lockViews[i] = root.findViewById(overlayIds[i]);
            }
        }

        // The 4 photographic previews are large JPEGs — decode them down-sampled to the
        // thumbnail's actual pixel size instead of full resolution.
        int targetPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 52f, getResources().getDisplayMetrics());
        int[] photoImgIds = {R.id.themeImg0, R.id.themeImg1, R.id.themeImg2, R.id.themeImg3};
        int[] photoResIds = {
                R.drawable.preview_theme_royal, R.drawable.preview_theme_pop_art,
                R.drawable.preview_theme_cyberpunk, R.drawable.preview_theme_luxury
        };
        for (int i = 0; i < photoImgIds.length; i++) {
            ImageView iv = root.findViewById(photoImgIds[i]);
            if (iv != null) {
                ThemeThumbnailLoader.loadInto(iv, photoResIds[i], targetPx);
            }
        }

        refreshThemeLocks(lockViews);
        updateThemeSelection(thumbs, selectedRef[0]);

        for (int i = 0; i < thumbs.length; i++) {
            final int idx = i;
            if (thumbs[i] == null) continue;
            thumbs[i].setOnClickListener(v -> {
                if (GamePreferences.isThemeUnlocked(requireContext(), idx)) {
                    selectedRef[0] = idx;
                    updateThemeSelection(thumbs, idx);
                } else {
                    showThemeUnlockPrompt(idx, lockViews, thumbs, selectedRef);
                }
            });
        }
    }

    private void showThemeUnlockPrompt(int idx, View[] lockViews, FrameLayout[] thumbs, int[] selectedRef) {
        if (getContext() == null) return;
        PlayerProfileManager profileMgr = PlayerProfileManager.getInstance(requireContext());
        int currentPrice = profileMgr.getThemePrice(idx);
        CoinManager coinManager = new CoinManager(requireContext());
        int userCoins = coinManager.getBalance();

        ThemeRegistry.ThemeInfo themeInfo = ThemeRegistry.getThemeInfo(idx);
        String themeName = (themeInfo != null) ? getString(themeInfo.getNameResId()) : String.valueOf(idx);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_theme_unlock, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView tvTitle = dialogView.findViewById(R.id.dialogThemeTitle);
        TextView tvCoins = dialogView.findViewById(R.id.dialogThemeCoins);
        MaterialButton btnBuy = dialogView.findViewById(R.id.btnBuyTheme);
        MaterialButton btnAd = dialogView.findViewById(R.id.btnDiscountAd);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelThemeUnlock);

        if (tvTitle != null) {
            tvTitle.setText(themeName);
        }
        if (tvCoins != null) {
            tvCoins.setText(getString(R.string.coin_balance, userCoins));
        }
        if (btnBuy != null) {
            btnBuy.setText(getString(R.string.theme_buy_button, currentPrice));
            btnBuy.setOnClickListener(v -> {
                dialog.dismiss();
                if (profileMgr.purchaseTheme(idx, currentPrice)) {
                    refreshThemeLocks(lockViews);
                    selectedRef[0] = idx;
                    updateThemeSelection(thumbs, idx);
                    Toast.makeText(requireContext(), R.string.theme_unlocked_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.theme_not_enough_coins, Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnAd != null) {
            btnAd.setOnClickListener(v -> {
                dialog.dismiss();
                if (getActivity() instanceof MenuActivity) {
                    ((MenuActivity) getActivity()).showThemeDiscountAd(idx, () -> {
                        profileMgr.applyThemeDiscount(idx, 0.5f, 3600_000L);
                        Toast.makeText(requireContext(), R.string.theme_discount_activated, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void refreshThemeLocks(View[] lockViews) {
        for (int i = 0; i < lockViews.length; i++) {
            if (lockViews[i] != null) {
                lockViews[i].setVisibility(
                        GamePreferences.isThemeUnlocked(requireContext(), i) ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void updateThemeSelection(FrameLayout[] thumbs, int selected) {
        for (int i = 0; i < thumbs.length; i++) {
            if (thumbs[i] == null) continue;
            boolean isSel = (i == selected);
            thumbs[i].setBackgroundResource(isSel
                    ? R.drawable.bg_theme_thumb_selected
                    : R.drawable.bg_theme_thumb_normal);
            thumbs[i].setAlpha(isSel ? 1.0f : 0.6f);
            if (!isSel) {
                thumbs[i].setScaleX(1f);
                thumbs[i].setScaleY(1f);
            }
        }
        if (selected >= 0 && selected < thumbs.length && thumbs[selected] != null) {
            View selectedThumbView = thumbs[selected];
            selectedThumbView.animate().scaleX(1.12f).scaleY(1.12f).setDuration(100)
                    .withEndAction(() -> selectedThumbView.animate().scaleX(1f).scaleY(1f)
                            .setInterpolator(new OvershootInterpolator(3f)).setDuration(150).start())
                    .start();
        }
    }

    private void updateDifficultySelection(Button[] btns, int selected) {
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == null) continue;
            if (i == selected) {
                btns[i].setBackgroundResource(R.drawable.bg_difficulty_selected);
                btns[i].setTextColor(0xFFE6A100);
            } else {
                btns[i].setBackgroundResource(R.drawable.bg_difficulty_normal);
                btns[i].setTextColor(0xFF7A8A99);
            }
        }
    }
}