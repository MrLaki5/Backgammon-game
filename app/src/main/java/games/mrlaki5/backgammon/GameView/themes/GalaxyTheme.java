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
 * Galaxy theme:
 * Deep space board with starry shimmer, dark metallic frame with thin gold outline.
 * Checkers: Player 1 = silver gradient (#C0C0C0 to #808080), Player 2 = gold gradient (#FFD700 to #C8860A).
 * Dice: dark #1A1A2E face, white pips, gold (#FFD700) border.
 */
public class GalaxyTheme implements BoardTheme {

    // Player 1 — Silver gradient (#C0C0C0 to #808080)
    private static final int P1_BASE      = Color.rgb(0xC0, 0xC0, 0xC0);
    private static final int P1_LIGHT     = Color.rgb(0xE8, 0xE8, 0xE8);
    private static final int P1_DARK      = Color.rgb(0x80, 0x80, 0x80);
    private static final int P1_RIM       = Color.rgb(0xA0, 0xA0, 0xA0);
    private static final int P1_OUTLINE   = Color.rgb(0x40, 0x40, 0x40);
    private static final int P1_HIGHLIGHT = Color.rgb(0xFF, 0xFF, 0xFF);
    private static final int P1_ACCENT    = Color.rgb(0x80, 0xD8, 0xFF); // icy cyan accent

    // Player 2 — Gold gradient (#FFD700 to #C8860A)
    private static final int P2_BASE      = Color.rgb(0xFF, 0xD7, 0x00);
    private static final int P2_LIGHT     = Color.rgb(0xFF, 0xEB, 0x80);
    private static final int P2_DARK      = Color.rgb(0xC8, 0x86, 0x0A);
    private static final int P2_RIM       = Color.rgb(0xB8, 0x73, 0x0A);
    private static final int P2_OUTLINE   = Color.rgb(0x50, 0x30, 0x00);
    private static final int P2_HIGHLIGHT = Color.rgb(0xFF, 0xFF, 0xD0);
    private static final int P2_ACCENT    = Color.rgb(0xBA, 0x68, 0xC8); // cosmic purple accent

    // Dice colors
    private static final int DICE_FACE   = Color.rgb(0x1A, 0x1A, 0x2E);
    private static final int DICE_PIP    = Color.rgb(0xFF, 0xFF, 0xFF);
    private static final int DICE_BORDER = Color.rgb(0xFF, 0xD7, 0x00);

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_GALAXY;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return 0; // programmatic
    }

    @Override
    public Drawable createBackgroundDrawable() {
        return new GalaxyBoardDrawable();
    }

    @Override
    public Bitmap createDiceBitmap(int diceNumber, boolean used, int size) {
        return ThemeDiceRenderer.createDiceBitmap(diceNumber, used, size,
                DICE_FACE, DICE_PIP, DICE_BORDER, null);
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

        // Subtle cosmic glow
        glowPaint.setColor(Color.argb(55, Color.red(accent), Color.green(accent), Color.blue(accent)));
        glowPaint.setMaskFilter(new BlurMaskFilter(r * 0.35f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(rect, glowPaint);

        // Drop shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.04f, rect.height() * 0.07f);
        contactShadowPaint.setColor(Color.argb(85, 5, 5, 20));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(r * 0.16f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawOval(shadowRect, contactShadowPaint);

        // Body radial gradient
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

        // Metallic rim
        RectF rimRing = new RectF(rect);
        rimRing.inset(rect.width() * 0.065f, rect.width() * 0.065f);
        highlightPaint.setStrokeWidth(Math.max(1.5f, rect.width() * 0.035f));
        highlightPaint.setColor(rim);
        canvas.drawOval(rimRing, highlightPaint);

        // Inner cosmic accent ring
        RectF accentRing = new RectF(rect);
        accentRing.inset(rect.width() * 0.18f, rect.width() * 0.18f);
        accentPaint.setStrokeWidth(Math.max(1f, rect.width() * 0.024f));
        accentPaint.setColor(Color.argb(120, Color.red(accent), Color.green(accent), Color.blue(accent)));
        canvas.drawOval(accentRing, accentPaint);

        // Specular star shine
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30f, rect.height() * 0.32f);
        specRect.offset(-rect.width() * 0.10f, -rect.height() * 0.12f);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55f,
                new int[]{Color.argb(80, Color.red(highlight), Color.green(highlight), Color.blue(highlight)),
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
        contactShadowPaint.setColor(Color.argb(70, 5, 5, 20));
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