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
 * Luxury Persian theme: ivory-pearl vs espresso-brown checkers, warm-gold rims,
 * satin finish and antique specular highlights.
 */
public class LuxuryTheme implements BoardTheme {

    // Player 1 (user/light): ivory-pearl, champagne, satin finish, warm-gold rim
    private static final int LUX_P1_BASE = Color.rgb(240, 225, 195);
    private static final int LUX_P1_LIGHT = Color.rgb(255, 250, 235);
    private static final int LUX_P1_DARK = Color.rgb(185, 155, 110);
    private static final int LUX_P1_RIM = Color.rgb(195, 160, 80);
    private static final int LUX_P1_OUTLINE = Color.rgb(70, 45, 25);
    private static final int LUX_P1_HIGHLIGHT = Color.rgb(255, 252, 240);

    // Player 2 (opponent/dark): espresso-brown, mahogany, antique-gold rim
    private static final int LUX_P2_BASE = Color.rgb(55, 30, 20);
    private static final int LUX_P2_LIGHT = Color.rgb(100, 60, 40);
    private static final int LUX_P2_DARK = Color.rgb(25, 12, 8);
    private static final int LUX_P2_RIM = Color.rgb(155, 120, 55);
    private static final int LUX_P2_OUTLINE = Color.rgb(15, 8, 5);
    private static final int LUX_P2_HIGHLIGHT = Color.rgb(140, 95, 60);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_LUXURY;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return R.drawable.board_luxury;
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint, Paint contactShadowPaint,
                         RectF shadowRect, RectF specRect) {
        int baseColor = player == 1 ? LUX_P1_BASE : LUX_P2_BASE;
        int lightColor = player == 1 ? LUX_P1_LIGHT : LUX_P2_LIGHT;
        int darkColor = player == 1 ? LUX_P1_DARK : LUX_P2_DARK;
        int rimColor = player == 1 ? LUX_P1_RIM : LUX_P2_RIM;
        int outlineColor = player == 1 ? LUX_P1_OUTLINE : LUX_P2_OUTLINE;
        int highlightColor = player == 1 ? LUX_P1_HIGHLIGHT : LUX_P2_HIGHLIGHT;

        float cx = rect.centerX();
        float cy = rect.centerY();
        float chipRadius = Math.max(rect.width(), rect.height()) / 2F;

        // 1. Soft contact shadow beneath
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.08F);
        contactShadowPaint.setColor(Color.argb(90, 30, 15, 5));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(chipRadius * 0.18F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // 2. Main body - satin surface with radial gradient
        float gradRadius = chipRadius * 1.15F;
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.15F,
                cy - rect.height() * 0.18F,
                gradRadius,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.5F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // 3. Clean outline
        rimPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.055F));
        rimPaint.setColor(outlineColor);
        canvas.drawOval(rect, rimPaint);

        // 4. Warm-gold / antique-gold rim
        RectF rimRing = new RectF(rect);
        float rimInset = rect.width() * 0.06F;
        rimRing.inset(rimInset, rimInset);
        highlightPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.04F));
        highlightPaint.setColor(rimColor);
        canvas.drawOval(rimRing, highlightPaint);

        // 5. Subtle bevel inner ring
        RectF bevelRing = new RectF(rect);
        float bevelInset = rect.width() * 0.18F;
        bevelRing.inset(bevelInset, bevelInset);
        bevelPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.02F));
        bevelPaint.setColor(Color.argb(40, Color.red(rimColor), Color.green(rimColor), Color.blue(rimColor)));
        canvas.drawOval(bevelRing, bevelPaint);

        // 6. Soft cream-white highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30F, rect.height() * 0.32F);
        specRect.offset(-rect.width() * 0.10F, -rect.height() * 0.13F);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55F,
                new int[]{Color.argb(75, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)),
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
        int baseColor = player == 1 ? LUX_P1_BASE : LUX_P2_BASE;
        int lightColor = player == 1 ? LUX_P1_LIGHT : LUX_P2_LIGHT;
        int darkColor = player == 1 ? LUX_P1_DARK : LUX_P2_DARK;
        int rimColor = player == 1 ? LUX_P1_RIM : LUX_P2_RIM;
        int outlineColor = player == 1 ? LUX_P1_OUTLINE : LUX_P2_OUTLINE;

        float cornerR = rect.width() * 0.12F;

        // Contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.12F);
        contactShadowPaint.setColor(Color.argb(70, 30, 15, 5));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.10F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, contactShadowPaint);

        // Body with satin gradient
        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.10F,
                rect.centerY() - rect.height() * 0.15F,
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

        // Gold rim accent
        RectF innerRect = new RectF(rect);
        innerRect.inset(rect.width() * 0.06F, rect.height() * 0.06F);
        highlightPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.03F));
        highlightPaint.setColor(rimColor);
        canvas.drawRoundRect(innerRect, cornerR * 0.8F, cornerR * 0.8F, highlightPaint);
    }
}
