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
 * Ancient Egypt theme:
 * Sandy board with burnt sienna and papyrus green triangles, cedar wood frame with gold hieroglyph accents.
 * Checkers: Player 1 = Lapis Lazuli gradient (#1F4E8C), Player 2 = Gold gradient (#DAA520).
 * Dice: Papyrus-colored (#F5DEB3) face, dark brown pips, gold border.
 */
public class AncientEgyptTheme implements BoardTheme {

    // Player 1 — Lapis Lazuli (#1F4E8C gradient)
    private static final int P1_BASE      = Color.rgb(0x1F, 0x4E, 0x8C);
    private static final int P1_LIGHT     = Color.rgb(0x3B, 0x72, 0xBA);
    private static final int P1_DARK      = Color.rgb(0x0E, 0x2A, 0x54);
    private static final int P1_RIM       = Color.rgb(0xDA, 0xA5, 0x20); // Gold rim
    private static final int P1_OUTLINE   = Color.rgb(0x0A, 0x1A, 0x30);
    private static final int P1_HIGHLIGHT = Color.rgb(0x90, 0xCA, 0xF9);
    private static final int P1_ACCENT    = Color.rgb(0xFF, 0xD7, 0x00);

    // Player 2 — Egyptian Gold (#DAA520 gradient)
    private static final int P2_BASE      = Color.rgb(0xDA, 0xA5, 0x20);
    private static final int P2_LIGHT     = Color.rgb(0xF5, 0xD7, 0x7F);
    private static final int P2_DARK      = Color.rgb(0x99, 0x65, 0x15);
    private static final int P2_RIM       = Color.rgb(0x8B, 0x69, 0x14);
    private static final int P2_OUTLINE   = Color.rgb(0x4A, 0x30, 0x00);
    private static final int P2_HIGHLIGHT = Color.rgb(0xFF, 0xF8, 0xDC);
    private static final int P2_ACCENT    = Color.rgb(0x1F, 0x4E, 0x8C); // Lapis accent

    // Dice colors
    private static final int DICE_FACE   = Color.rgb(0xF5, 0xDE, 0xB3); // Papyrus
    private static final int DICE_PIP    = Color.rgb(0x4A, 0x2E, 0x12); // Dark brown
    private static final int DICE_BORDER = Color.rgb(0xDA, 0xA5, 0x20); // Gold

    @Override
    public int getThemeId() {
        return GamePreferences.THEME_ANCIENT_EGYPT;
    }

    @Override
    public int getBackgroundDrawableRes() {
        return 0; // programmatic
    }

    @Override
    public Drawable createBackgroundDrawable() {
        return new AncientEgyptBoardDrawable();
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

        // Shadow
        shadowRect.set(rect);
        shadowRect.offset(rect.width() * 0.04f, rect.height() * 0.07f);
        contactShadowPaint.setColor(Color.argb(85, 30, 20, 5));
        contactShadowPaint.setMaskFilter(new BlurMaskFilter(r * 0.16f, BlurMaskFilter.Blur.NORMAL));
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

        // Gold rim
        RectF rimRing = new RectF(rect);
        rimRing.inset(rect.width() * 0.065f, rect.width() * 0.065f);
        highlightPaint.setStrokeWidth(Math.max(1.5f, rect.width() * 0.035f));
        highlightPaint.setColor(rim);
        canvas.drawOval(rimRing, highlightPaint);

        // Inner jewel ring
        RectF accentRing = new RectF(rect);
        accentRing.inset(rect.width() * 0.19f, rect.width() * 0.19f);
        accentPaint.setStrokeWidth(Math.max(1f, rect.width() * 0.024f));
        accentPaint.setColor(Color.argb(130, Color.red(accent), Color.green(accent), Color.blue(accent)));
        canvas.drawOval(accentRing, accentPaint);

        // Specular highlight
        specRect.set(rect);
        specRect.inset(rect.width() * 0.30f, rect.height() * 0.32f);
        specRect.offset(-rect.width() * 0.10f, -rect.height() * 0.12f);
        specPaint.setShader(new RadialGradient(
                specRect.centerX(), specRect.centerY(),
                specRect.width() * 0.55f,
                new int[]{Color.argb(75, Color.red(highlight), Color.green(highlight), Color.blue(highlight)),
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
        contactShadowPaint.setColor(Color.argb(70, 30, 20, 5));
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