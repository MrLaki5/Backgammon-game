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
 * Cyberpunk theme: futuristic neon glow, purple and turquoise rings, deep dark vs white body.
 */
public class CyberpunkTheme implements BoardTheme {

    // Player 1 (user/light): white body, purple inner ring & rim
    private static final int CYBER_P1_BASE = Color.rgb(245, 245, 250);
    private static final int CYBER_P1_LIGHT = Color.rgb(255, 255, 255);
    private static final int CYBER_P1_DARK = Color.rgb(180, 180, 200);
    private static final int CYBER_P1_GLOW = Color.rgb(200, 160, 255);
    private static final int CYBER_P1_RIM = Color.rgb(130, 50, 200);
    private static final int CYBER_P1_INNER = Color.rgb(140, 60, 210);

    // Player 2 (opponent/dark): very dark purple body, turquoise inner ring, black rim
    private static final int CYBER_P2_BASE = Color.rgb(35, 15, 60);
    private static final int CYBER_P2_LIGHT = Color.rgb(70, 35, 100);
    private static final int CYBER_P2_DARK = Color.rgb(15, 5, 30);
    private static final int CYBER_P2_GLOW = Color.rgb(0, 210, 210);
    private static final int CYBER_P2_RIM = Color.rgb(10, 10, 10);
    private static final int CYBER_P2_INNER = Color.rgb(0, 200, 200);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_CYBERPUNK;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return R.drawable.board_cyberpunk;
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint, Paint contactShadowPaint,
                         RectF shadowRect, RectF specRect) {
        int baseColor = player == 1 ? CYBER_P1_BASE : CYBER_P2_BASE;
        int lightColor = player == 1 ? CYBER_P1_LIGHT : CYBER_P2_LIGHT;
        int darkColor = player == 1 ? CYBER_P1_DARK : CYBER_P2_DARK;
        int glowColor = player == 1 ? CYBER_P1_GLOW : CYBER_P2_GLOW;
        int rimColor = player == 1 ? CYBER_P1_RIM : CYBER_P2_RIM;
        int innerColor = player == 1 ? CYBER_P1_INNER : CYBER_P2_INNER;

        float cx = rect.centerX();
        float cy = rect.centerY();
        float chipRadius = Math.max(rect.width(), rect.height()) / 2F;

        // 1. Outer neon glow
        int glowAlpha = Color.argb(90, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor));
        glowPaint.setColor(glowAlpha);
        glowPaint.setMaskFilter(new BlurMaskFilter(chipRadius * 0.35F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawCircle(cx, cy, chipRadius * 0.92F, glowPaint);

        // 2. Drop shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.05F, rect.height() * 0.08F);
        canvas.drawOval(shadowRect, shadowPaint);

        // 3. Main body with radial gradient
        float gradRadius = chipRadius * 1.2F;
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.18F,
                cy - rect.height() * 0.22F,
                gradRadius,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.5F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // 4. Outer rim
        rimPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.06F));
        rimPaint.setColor(rimColor);
        canvas.drawOval(rect, rimPaint);

        // 5. Inner ring
        RectF innerRing = new RectF(rect);
        float inset = rect.width() * 0.16F;
        innerRing.inset(inset, inset);
        highlightPaint.setStrokeWidth(Math.max(2F, rect.width() * 0.05F));
        highlightPaint.setColor(innerColor);
        canvas.drawOval(innerRing, highlightPaint);

        // 6. Top specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.28F, rect.height() * 0.28F);
        specRect.offset(-rect.width() * 0.08F, -rect.height() * 0.12F);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.6F,
                new int[]{Color.argb(60, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                new float[]{0F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawOval(specRect, specPaint);
    }

    @Override
    public void drawEndChip(Canvas canvas, RectF rect, int player,
                            Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                            Paint highlightPaint, Paint glowPaint, Paint contactShadowPaint,
                            RectF shadowRect) {
        int baseColor = player == 1 ? CYBER_P1_BASE : CYBER_P2_BASE;
        int glowColor = player == 1 ? CYBER_P1_GLOW : CYBER_P2_GLOW;
        int rimColor = player == 1 ? CYBER_P1_RIM : CYBER_P2_RIM;
        int darkColor = player == 1 ? CYBER_P1_DARK : CYBER_P2_DARK;
        int lightColor = player == 1 ? CYBER_P1_LIGHT : CYBER_P2_LIGHT;

        float cornerR = rect.width() * 0.12F;

        // Neon glow behind
        int glowAlpha = Color.argb(70, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor));
        glowPaint.setColor(glowAlpha);
        glowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.25F, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(rect, cornerR, cornerR, glowPaint);

        // Shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.05F, rect.height() * 0.15F);
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, shadowPaint);

        // Body with gradient
        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.15F,
                rect.centerY() - rect.height() * 0.2F,
                Math.max(rect.width(), rect.height()) * 0.8F,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0F, 0.5F, 1F},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerR, cornerR, fillPaint);
        fillPaint.setShader(null);

        // Neon rim
        rimPaint.setStrokeWidth(Math.max(1F, rect.width() * 0.05F));
        rimPaint.setColor(rimColor);
        canvas.drawRoundRect(rect, cornerR, cornerR, rimPaint);
    }
}
