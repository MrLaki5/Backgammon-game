package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.Bitmap;
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
 * Neon Retro theme:
 * 80s synthwave/cyberpunk aesthetic with scanlines, neon cyan & magenta triangles.
 * Checkers: Player 1 = Neon Blue (#0080FF), Player 2 = Neon Orange (#FF6600).
 * Dice: Black face, neon green (#39FF14) pips, cyan glow border.
 */
public class NeonRetroTheme implements BoardTheme {

    // Player 1 — Neon Blue (#0080FF)
    private static final int P1_BASE      = Color.rgb(0x00, 0x80, 0xFF);
    private static final int P1_LIGHT     = Color.rgb(0x66, 0xB2, 0xFF);
    private static final int P1_DARK      = Color.rgb(0x00, 0x40, 0x99);
    private static final int P1_RIM       = Color.rgb(0x00, 0xFF, 0xFF); // Cyan rim
    private static final int P1_OUTLINE   = Color.rgb(0x00, 0x20, 0x50);
    private static final int P1_HIGHLIGHT = Color.rgb(0xE0, 0xF7, 0xFA);
    private static final int P1_GLOW      = Color.rgb(0x00, 0x80, 0xFF);

    // Player 2 — Neon Orange (#FF6600)
    private static final int P2_BASE      = Color.rgb(0xFF, 0x66, 0x00);
    private static final int P2_LIGHT     = Color.rgb(0xFF, 0x99, 0x4D);
    private static final int P2_DARK      = Color.rgb(0xB3, 0x36, 0x00);
    private static final int P2_RIM       = Color.rgb(0xFF, 0xD7, 0x00); // Golden yellow rim
    private static final int P2_OUTLINE   = Color.rgb(0x50, 0x15, 0x00);
    private static final int P2_HIGHLIGHT = Color.rgb(0xFF, 0xF3, 0xE0);
    private static final int P2_GLOW      = Color.rgb(0xFF, 0x66, 0x00);

    // Dice colors
    private static final int DICE_FACE   = Color.rgb(0x00, 0x00, 0x00);
    private static final int DICE_PIP    = Color.rgb(0x39, 0xFF, 0x14); // Neon green
    private static final int DICE_BORDER = Color.rgb(0x00, 0xFF, 0xFF); // Cyan
    private static final int DICE_GLOW   = Color.rgb(0x00, 0xFF, 0xFF);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_NEON_RETRO;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return 0; // programmatic
    }

    @Override
    public Drawable createBackgroundDrawable() {
        return new NeonRetroBoardDrawable();
    }

    @Override
    public Bitmap createDiceBitmap(int diceNumber, boolean used, int size) {
        return ThemeDiceRenderer.createDiceBitmap(diceNumber, used, size,
                DICE_FACE, DICE_PIP, DICE_BORDER, DICE_GLOW);
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
        int glow      = player == 1 ? P1_GLOW      : P2_GLOW;

        float cx = rect.centerX(), cy = rect.centerY();
        float r  = Math.max(rect.width(), rect.height()) / 2f;

        // Vivid neon outer glow
        glowPaint.setColor(Color.argb(100, Color.red(glow), Color.green(glow), Color.blue(glow)));
        glowPaint.setMaskFilter(new BlurMaskFilter(r * 0.45f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(rect, glowPaint);

        // Dark contact shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.03f, rect.height() * 0.06f);
        contactShadowPaint.setColor(Color.argb(90, 0, 0, 0));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(r * 0.15f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // Body gradient
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

        // Neon glowing rim
        RectF rimRing = new RectF(rect);
        rimRing.inset(rect.width() * 0.065f, rect.width() * 0.065f);
        highlightPaint.setStrokeWidth(Math.max(1.5f, rect.width() * 0.035f));
        highlightPaint.setColor(rim);
        canvas.drawOval(rimRing, highlightPaint);

        // Inner neon circuit ring
        RectF innerRing = new RectF(rect);
        innerRing.inset(rect.width() * 0.18f, rect.width() * 0.18f);
        accentPaint.setStrokeWidth(Math.max(1.2f, rect.width() * 0.024f));
        accentPaint.setColor(Color.argb(180, Color.red(rim), Color.green(rim), Color.blue(rim)));
        canvas.drawOval(innerRing, accentPaint);

        // Specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30f, rect.height() * 0.32f);
        specRect.offset(-rect.width() * 0.10f, -rect.height() * 0.12f);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55f,
                new int[]{Color.argb(85, Color.red(highlight), Color.green(highlight), Color.blue(highlight)),
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
        shadowRect.offset(rect.width() * 0.03f, rect.height() * 0.10f);
        contactShadowPaint.setColor(Color.argb(70, 0, 0, 0));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(rect.width() * 0.10f, BlurMaskFilter.Blur.NORMAL));
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