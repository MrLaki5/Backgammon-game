package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import games.mrlaki5.backgammon.GamePreferences;

/**
 * Woodland theme: alabaster marble (player) vs dark walnut (opponent),
 * copper-bronze rims, forest-green accent rings.
 * Board background is drawn programmatically — no PNG asset required.
 */
public class WoodlandTheme implements BoardTheme {

    // Player 1 — alabaster white
    private static final int P1_BASE      = Color.rgb(240, 234, 218);
    private static final int P1_LIGHT     = Color.rgb(255, 252, 245);
    private static final int P1_DARK      = Color.rgb(195, 182, 155);
    private static final int P1_RIM       = Color.rgb(175, 115, 52);
    private static final int P1_OUTLINE   = Color.rgb(80,  55, 25);
    private static final int P1_HIGHLIGHT = Color.rgb(255, 255, 248);
    private static final int P1_ACCENT    = Color.rgb(43,  82, 35);

    // Player 2 — dark espresso walnut
    private static final int P2_BASE      = Color.rgb(58,  31, 10);
    private static final int P2_LIGHT     = Color.rgb(100, 58, 28);
    private static final int P2_DARK      = Color.rgb(25,  12,  4);
    private static final int P2_RIM       = Color.rgb(160, 105, 42);
    private static final int P2_OUTLINE   = Color.rgb(14,   7,  2);
    private static final int P2_HIGHLIGHT = Color.rgb(130,  82, 38);
    private static final int P2_ACCENT    = Color.rgb(43,  82, 35);

    @Override public int getThemeId() { return GamePreferences.THEME_WOODLAND; }

    @Override
    public int getBackgroundDrawableRes() { return 0; }   // unused — programmatic

    @Override
    public Drawable createBackgroundDrawable() {
        return new WoodlandBoardDrawable();
    }

    @Override
    public void drawChip(Canvas canvas, RectF rect, int player,
                         Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                         Paint highlightPaint, Paint specPaint, Paint bevelPaint,
                         Paint accentPaint, Paint dotPaint, Paint glowPaint,
                         Paint contactShadowPaint, RectF shadowRect, RectF specRect) {

        int base      = player == 1 ? P1_BASE      : P2_BASE;
        int light     = player == 1 ? P1_LIGHT     : P2_LIGHT;
        int dark      = player == 1 ? P1_DARK      : P2_DARK;
        int rim       = player == 1 ? P1_RIM       : P2_RIM;
        int outline   = player == 1 ? P1_OUTLINE   : P2_OUTLINE;
        int highlight = player == 1 ? P1_HIGHLIGHT : P2_HIGHLIGHT;
        int accent    = player == 1 ? P1_ACCENT    : P2_ACCENT;

        float cx = rect.centerX(), cy = rect.centerY();
        float r  = Math.max(rect.width(), rect.height()) / 2f;

        // Contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03f, rect.height() * 0.07f);
        contactShadowPaint.setColor(Color.argb(90, 20, 10, 3));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(r * 0.16f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // Main body
        fillPaint.setShader(new RadialGradient(
                cx - rect.width() * 0.14f, cy - rect.height() * 0.17f,
                r * 1.15f,
                new int[]{light, base, dark}, new float[]{0f, 0.50f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, fillPaint);
        fillPaint.setShader(null);

        // Outline
        rimPaint.setStrokeWidth(Math.max(2f, rect.width() * 0.055f));
        rimPaint.setColor(outline);
        canvas.drawOval(rect, rimPaint);

        // Copper rim band
        RectF rimRing = new RectF(rect);
        rimRing.inset(rect.width() * 0.065f, rect.width() * 0.065f);
        highlightPaint.setStrokeWidth(Math.max(1.5f, rect.width() * 0.035f));
        highlightPaint.setColor(rim);
        canvas.drawOval(rimRing, highlightPaint);

        // Forest-green accent ring
        RectF accentRing = new RectF(rect);
        accentRing.inset(rect.width() * 0.20f, rect.width() * 0.20f);
        accentPaint.setStrokeWidth(Math.max(1f, rect.width() * 0.022f));
        accentPaint.setColor(Color.argb(90,
                Color.red(accent), Color.green(accent), Color.blue(accent)));
        canvas.drawOval(accentRing, accentPaint);

        // Inner 4-dot pattern
        dotPaint.setColor(Color.argb(55,
                Color.red(accent), Color.green(accent), Color.blue(accent)));
        float dr = rect.width() * 0.020f, dd = rect.width() * 0.120f;
        canvas.drawCircle(cx,      cy - dd, dr, dotPaint);
        canvas.drawCircle(cx,      cy + dd, dr, dotPaint);
        canvas.drawCircle(cx - dd, cy,      dr, dotPaint);
        canvas.drawCircle(cx + dd, cy,      dr, dotPaint);

        // Specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30f, rect.height() * 0.32f);
        specRect.offset(-rect.width() * 0.10f, -rect.height() * 0.12f);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55f,
                new int[]{Color.argb(70, Color.red(highlight),
                        Color.green(highlight), Color.blue(highlight)),
                        Color.argb(0, 255, 255, 255)},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawOval(specRect, specPaint);
    }

    @Override
    public void drawEndChip(Canvas canvas, RectF rect, int player,
                            Paint fillPaint, Paint rimPaint, Paint shadowPaint,
                            Paint highlightPaint, Paint glowPaint,
                            Paint contactShadowPaint, RectF shadowRect) {

        int base    = player == 1 ? P1_BASE    : P2_BASE;
        int light   = player == 1 ? P1_LIGHT   : P2_LIGHT;
        int dark    = player == 1 ? P1_DARK    : P2_DARK;
        int rim     = player == 1 ? P1_RIM     : P2_RIM;
        int outline = player == 1 ? P1_OUTLINE : P2_OUTLINE;

        float cornerR = rect.width() * 0.12f;

        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03f, rect.height() * 0.12f);
        contactShadowPaint.setColor(Color.argb(75, 20, 10, 3));
        contactShadowPaint.setMaskFilter(
                new BlurMaskFilter(rect.width() * 0.10f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, cornerR, cornerR, contactShadowPaint);

        fillPaint.setShader(new RadialGradient(
                rect.centerX() - rect.width() * 0.10f,
                rect.centerY() - rect.height() * 0.14f,
                Math.max(rect.width(), rect.height()) * 0.70f,
                new int[]{light, base, dark}, new float[]{0f, 0.50f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerR, cornerR, fillPaint);
        fillPaint.setShader(null);

        rimPaint.setStrokeWidth(Math.max(1.5f, rect.width() * 0.05f));
        rimPaint.setColor(outline);
        canvas.drawRoundRect(rect, cornerR, cornerR, rimPaint);

        RectF inner = new RectF(rect);
        inner.inset(rect.width() * 0.06f, rect.height() * 0.06f);
        highlightPaint.setStrokeWidth(Math.max(1f, rect.width() * 0.03f));
        highlightPaint.setColor(rim);
        canvas.drawRoundRect(inner, cornerR * 0.8f, cornerR * 0.8f, highlightPaint);
    }
}
