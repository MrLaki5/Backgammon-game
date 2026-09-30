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
 * Pop Art theme: vibrant turquoise discs vs deep navy discs, bold comic-book outlines,
 * and halftone dot pattern.
 */
public class PopArtTheme implements BoardTheme {

    // Player 1 (user/light): light cyan/turquoise disc, bold comic-book style
    private static final int POP_P1_BASE = Color.rgb(80, 210, 220);
    private static final int POP_P1_LIGHT = Color.rgb(150, 240, 245);
    private static final int POP_P1_DARK = Color.rgb(40, 120, 150);
    private static final int POP_P1_OUTLINE = Color.rgb(15, 30, 60);
    private static final int POP_P1_HIGHLIGHT = Color.rgb(230, 255, 255);

    // Player 2 (opponent/dark): deep navy/dark blue disc
    private static final int POP_P2_BASE = Color.rgb(20, 35, 75);
    private static final int POP_P2_LIGHT = Color.rgb(50, 75, 130);
    private static final int POP_P2_DARK = Color.rgb(8, 15, 40);
    private static final int POP_P2_OUTLINE = Color.rgb(2, 5, 15);
    private static final int POP_P2_HIGHLIGHT = Color.rgb(70, 120, 180);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_POP_ART;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return R.drawable.board_pop_art;
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint, Paint contactShadowPaint,
                         RectF shadowRect, RectF specRect) {
        int baseColor = player == 1 ? POP_P1_BASE : POP_P2_BASE;
        int lightColor = player == 1 ? POP_P1_LIGHT : POP_P2_LIGHT;
        int darkColor = player == 1 ? POP_P1_DARK : POP_P2_DARK;
        int outlineColor = player == 1 ? POP_P1_OUTLINE : POP_P2_OUTLINE;
        int highlightColor = player == 1 ? POP_P1_HIGHLIGHT : POP_P2_HIGHLIGHT;

        float cx = rect.centerX();
        float cy = rect.centerY();

        // 1. Soft contact shadow (short, subtle beneath)
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.09F);
        contactShadowPaint.setColor(Color.argb(80, 0, 0, 0));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.12F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // 2. Main body - comic-book radial gradient
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.12F,
                cy - rect.height() * 0.15F,
                Math.max(rect.width(), rect.height()) * 0.7F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.55F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // 3. Bold thick outline (comic-book crisp stroke)
        rimPaint.setStrokeWidth(Math.max(3F, rect.width() * 0.09F));
        rimPaint.setColor(outlineColor);
        canvas.drawOval(rect, rimPaint);

        // 4. Inner bevel ring
        RectF bevelRing = new RectF(rect);
        float bevelInset = rect.width() * 0.14F;
        bevelRing.inset(bevelInset, bevelInset);
        highlightPaint.setStrokeWidth(Math.max(1.5F, rect.width() * 0.03F));
        highlightPaint.setColor(Color.argb(50, Color.red(lightColor), Color.green(lightColor), Color.blue(lightColor)));
        canvas.drawOval(bevelRing, highlightPaint);

        // 5. Comic-book glossy specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.32F, rect.height() * 0.35F);
        specRect.offset(-rect.width() * 0.14F, -rect.height() * 0.16F);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55F,
                new int[]{Color.argb(110, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)),
                        Color.argb(0, 255, 255, 255)},
                new float[]{0F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(specRect, specPaint);

        // 6. Halftone dot pattern
        dotPaint.setColor(Color.argb(25, Color.red(darkColor), Color.green(darkColor), Color.blue(darkColor)));
        float dotRadius = rect.width() * 0.025F;
        float spacing = rect.width() * 0.18F;
        float startX = cx - spacing;
        float startY = cy;
        float chipR = rect.width() * 0.38F;
        for (float dx = 0; dx <= spacing * 2; dx += spacing) {
            for (float dy = 0; dy <= spacing; dy += spacing) {
                float px = startX + dx;
                float py = startY + dy;
                float dist = (float) Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy));
                if (dist < chipR) {
                    canvas.drawCircle(px, py, dotRadius, dotPaint);
                }
            }
        }
    }

    @Override
    public void drawEndChip(Canvas canvas, RectF rect, int player,
                            Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                            Paint highlightPaint, Paint glowPaint, Paint contactShadowPaint,
                            RectF shadowRect) {
        int baseColor = player == 1 ? POP_P1_BASE : POP_P2_BASE;
        int darkColor = player == 1 ? POP_P1_DARK : POP_P2_DARK;
        int lightColor = player == 1 ? POP_P1_LIGHT : POP_P2_LIGHT;
        int outlineColor = player == 1 ? POP_P1_OUTLINE : POP_P2_OUTLINE;

        float cornerR = rect.width() * 0.12F;

        // Contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03F, rect.height() * 0.12F);
        contactShadowPaint.setColor(Color.argb(70, 0, 0, 0));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.10F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, contactShadowPaint);

        // Body with gradient
        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.1F,
                rect.centerY() - rect.height() * 0.15F,
                Math.max(rect.width(), rect.height()) * 0.7F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.5F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerR, cornerR, fillPaint);
        fillPaint.setShader(null);

        // Bold outline
        rimPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.07F));
        rimPaint.setColor(outlineColor);
        canvas.drawRoundRect(rect, cornerR, cornerR, rimPaint);
    }
}
