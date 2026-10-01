package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;

/**
 * Iranian Royal theme: warm ivory-pearl vs deep walnut checkers, aged-brass rims,
 * and Persian Khatam-inspired turquoise/lapis accents.
 */
public class RoyalTheme implements BoardTheme {

    // Player 1 (user/light): warm ivory-pearl, champagne cream, aged-brass rim, turquoise accent
    private static final int IRAN_P1_BASE = Color.rgb(235, 218, 185);
    private static final int IRAN_P1_LIGHT = Color.rgb(252, 245, 228);
    private static final int IRAN_P1_DARK = Color.rgb(175, 145, 100);
    private static final int IRAN_P1_RIM = Color.rgb(170, 140, 65);
    private static final int IRAN_P1_OUTLINE = Color.rgb(60, 38, 20);
    private static final int IRAN_P1_HIGHLIGHT = Color.rgb(255, 248, 232);
    private static final int IRAN_P1_ACCENT = Color.rgb(0, 155, 145);

    // Player 2 (opponent/dark): deep walnut, mahogany, aged-brass rim, lapis accent
    private static final int IRAN_P2_BASE = Color.rgb(65, 35, 20);
    private static final int IRAN_P2_LIGHT = Color.rgb(110, 65, 40);
    private static final int IRAN_P2_DARK = Color.rgb(30, 15, 8);
    private static final int IRAN_P2_RIM = Color.rgb(145, 115, 50);
    private static final int IRAN_P2_OUTLINE = Color.rgb(18, 10, 5);
    private static final int IRAN_P2_HIGHLIGHT = Color.rgb(150, 105, 55);
    private static final int IRAN_P2_ACCENT = Color.rgb(30, 60, 140);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_ROYAL;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return R.drawable.board_royal;
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint, Paint contactShadowPaint,
                         RectF shadowRect, RectF specRect) {
        int baseColor = player == 1 ? IRAN_P1_BASE : IRAN_P2_BASE;
        int lightColor = player == 1 ? IRAN_P1_LIGHT : IRAN_P2_LIGHT;
        int darkColor = player == 1 ? IRAN_P1_DARK : IRAN_P2_DARK;
        int rimColor = player == 1 ? IRAN_P1_RIM : IRAN_P2_RIM;
        int outlineColor = player == 1 ? IRAN_P1_OUTLINE : IRAN_P2_OUTLINE;
        int highlightColor = player == 1 ? IRAN_P1_HIGHLIGHT : IRAN_P2_HIGHLIGHT;
        int accentColor = player == 1 ? IRAN_P1_ACCENT : IRAN_P2_ACCENT;

        float cx = rect.centerX();
        float cy = rect.centerY();
        float chipRadius = Math.max(rect.width(), rect.height()) / 2F;

        // 1. Soft contact shadow (natural, warm)
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.07F);
        contactShadowPaint.setColor(Color.argb(85, 25, 12, 5));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(chipRadius * 0.16F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // 2. Main body - satin surface with warm radial gradient
        float gradRadius = chipRadius * 1.15F;
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.14F,
                cy - rect.height() * 0.17F,
                gradRadius,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.52F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // 3. Clean outline (dark walnut / very dark)
        rimPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.055F));
        rimPaint.setColor(outlineColor);
        canvas.drawOval(rect, rimPaint);

        // 4. Thin aged-brass rim
        RectF rimRing = new RectF(rect);
        float rimInset = rect.width() * 0.065F;
        rimRing.inset(rimInset, rimInset);
        highlightPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.035F));
        highlightPaint.setColor(rimColor);
        canvas.drawOval(rimRing, highlightPaint);

        // 5. Subtle geometric accent ring (turquoise/lapis)
        RectF accentRing = new RectF(rect);
        float accentInset = rect.width() * 0.20F;
        accentRing.inset(accentInset, accentInset);
        accentPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.022F));
        accentPaint.setColor(Color.argb(100, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
        canvas.drawOval(accentRing, accentPaint);

        // 6. Inner tiny dot pattern (Khatam-like subtle geometry - 4 small dots)
        dotPaint.setColor(Color.argb(60, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
        float dotR = rect.width() * 0.02F;
        float dotDist = rect.width() * 0.12F;
        canvas.drawCircle(cx, cy - dotDist, dotR, dotPaint);
        canvas.drawCircle(cx, cy + dotDist, dotR, dotPaint);
        canvas.drawCircle(cx - dotDist, cy, dotR, dotPaint);
        canvas.drawCircle(cx + dotDist, cy, dotR, dotPaint);

        // 7. Soft satin highlight (top-left, warm)
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30F, rect.height() * 0.32F);
        specRect.offset(-rect.width() * 0.10F, -rect.height() * 0.12F);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55F,
                new int[]{Color.argb(65, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)),
                        Color.argb(0, 255, 255, 255)},
                new float[]{0F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(specRect, specPaint);
    }

    @Override
    public void drawEndChip(Canvas canvas, RectF rect, int player,
                            Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                            Paint highlightPaint, Paint glowPaint, Paint contactShadowPaint,
                            RectF shadowRect) {
        int baseColor = player == 1 ? IRAN_P1_BASE : IRAN_P2_BASE;
        int lightColor = player == 1 ? IRAN_P1_LIGHT : IRAN_P2_LIGHT;
        int darkColor = player == 1 ? IRAN_P1_DARK : IRAN_P2_DARK;
        int rimColor = player == 1 ? IRAN_P1_RIM : IRAN_P2_RIM;
        int outlineColor = player == 1 ? IRAN_P1_OUTLINE : IRAN_P2_OUTLINE;

        float cornerR = rect.width() * 0.12F;

        // Contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.12F);
        contactShadowPaint.setColor(Color.argb(70, 25, 12, 5));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.10F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, contactShadowPaint);

        // Body with satin gradient
        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.10F,
                rect.centerY() - rect.height() * 0.14F,
                Math.max(rect.width(), rect.height()) * 0.7F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.5F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerR, cornerR, fillPaint);
        fillPaint.setShader(null);

        // Outline
        rimPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.05F));
        rimPaint.setColor(outlineColor);
        canvas.drawRoundRect(rect, cornerR, cornerR, rimPaint);

        // Aged-brass rim accent
        RectF innerRect = new RectF(rect);
        innerRect.inset(rect.width() * 0.06F, rect.height() * 0.06F);
        highlightPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.03F));
        highlightPaint.setColor(rimColor);
        canvas.drawRoundRect(innerRect, cornerR * 0.8F, cornerR * 0.8F, highlightPaint);
    }
}
