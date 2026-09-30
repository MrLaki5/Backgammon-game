package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Procedural renderer for generating styled dice bitmaps for themes.
 */
public final class ThemeDiceRenderer {

    private ThemeDiceRenderer() {}

    public static Bitmap createDiceBitmap(int diceNumber, boolean used, int size,
                                          int faceColor, int pipColor, int borderColor,
                                          Integer glowColor) {
        if (diceNumber < 1 || diceNumber > 6) return null;
        if (size <= 0) size = 128;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float cornerRadius = size * 0.18f;
        RectF diceRect = new RectF(size * 0.04f, size * 0.04f, size * 0.96f, size * 0.96f);

        // Fill face
        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        if (used) {
            int r = (int) (Color.red(faceColor) * 0.6f);
            int g = (int) (Color.green(faceColor) * 0.6f);
            int b = (int) (Color.blue(faceColor) * 0.6f);
            fillPaint.setColor(Color.argb(180, r, g, b));
        } else {
            fillPaint.setColor(faceColor);
        }
        canvas.drawRoundRect(diceRect, cornerRadius, cornerRadius, fillPaint);

        // Optional glow border (e.g. for Neon Retro)
        if (glowColor != null) {
            Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setColor(Color.argb(used ? 40 : 90,
                    Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor)));
            glowPaint.setStrokeWidth(size * 0.09f);
            canvas.drawRoundRect(diceRect, cornerRadius, cornerRadius, glowPaint);
        }

        // Main border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(used ? Color.argb(150, Color.red(borderColor), Color.green(borderColor), Color.blue(borderColor)) : borderColor);
        borderPaint.setStrokeWidth(Math.max(2f, size * 0.05f));
        canvas.drawRoundRect(diceRect, cornerRadius, cornerRadius, borderPaint);

        // Pips
        Paint pipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pipPaint.setStyle(Paint.Style.FILL);
        pipPaint.setColor(used ? Color.argb(150, Color.red(pipColor), Color.green(pipColor), Color.blue(pipColor)) : pipColor);

        float dotRadius = size * 0.082f;
        float c1 = size * 0.28f;
        float c2 = size * 0.50f;
        float c3 = size * 0.72f;

        switch (diceNumber) {
            case 1:
                canvas.drawCircle(c2, c2, dotRadius, pipPaint);
                break;
            case 2:
                canvas.drawCircle(c1, c1, dotRadius, pipPaint);
                canvas.drawCircle(c3, c3, dotRadius, pipPaint);
                break;
            case 3:
                canvas.drawCircle(c1, c1, dotRadius, pipPaint);
                canvas.drawCircle(c2, c2, dotRadius, pipPaint);
                canvas.drawCircle(c3, c3, dotRadius, pipPaint);
                break;
            case 4:
                canvas.drawCircle(c1, c1, dotRadius, pipPaint);
                canvas.drawCircle(c3, c1, dotRadius, pipPaint);
                canvas.drawCircle(c1, c3, dotRadius, pipPaint);
                canvas.drawCircle(c3, c3, dotRadius, pipPaint);
                break;
            case 5:
                canvas.drawCircle(c1, c1, dotRadius, pipPaint);
                canvas.drawCircle(c3, c1, dotRadius, pipPaint);
                canvas.drawCircle(c2, c2, dotRadius, pipPaint);
                canvas.drawCircle(c1, c3, dotRadius, pipPaint);
                canvas.drawCircle(c3, c3, dotRadius, pipPaint);
                break;
            case 6:
                canvas.drawCircle(c1, c1, dotRadius, pipPaint);
                canvas.drawCircle(c3, c1, dotRadius, pipPaint);
                canvas.drawCircle(c1, c2, dotRadius, pipPaint);
                canvas.drawCircle(c3, c2, dotRadius, pipPaint);
                canvas.drawCircle(c1, c3, dotRadius, pipPaint);
                canvas.drawCircle(c3, c3, dotRadius, pipPaint);
                break;
        }

        return bitmap;
    }
}