package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import games.mrlaki5.backgammon.GameView.BoardMetrics;

/**
 * Procedural board drawable for the Ancient Egypt theme.
 * Sandy gradient, repeating diamond hieroglyphic motifs, burnt sienna and papyrus green triangles,
 * dark wood frame with gold diagonal hieroglyph stripe.
 */
public class AncientEgyptBoardDrawable extends Drawable {

    private static final int C_SAND_LIGHT   = Color.rgb(0xD4, 0xA8, 0x43);
    private static final int C_SAND_DARK    = Color.rgb(0x8B, 0x69, 0x14);
    private static final int C_BURNT_SIENNA = Color.rgb(0x8B, 0x25, 0x00);
    private static final int C_SIENNA_LIGHT = Color.rgb(0xAF, 0x38, 0x0C);
    private static final int C_PAPYRUS_GRN  = Color.rgb(0x2E, 0x59, 0x02);
    private static final int C_GRN_LIGHT    = Color.rgb(0x44, 0x78, 0x08);
    private static final int C_WOOD_FRAME   = Color.rgb(0x3E, 0x1C, 0x00);
    private static final int C_WOOD_BAR     = Color.rgb(0x2E, 0x14, 0x00);
    private static final int C_GOLD         = Color.rgb(0xDA, 0xA5, 0x20);
    private static final int C_GOLD_DIM     = Color.argb(140, 0xDA, 0xA5, 0x20);
    private static final int C_PANEL_BG     = Color.rgb(0x32, 0x18, 0x04);

    private final Paint pFill      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTriangle  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutline   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGold      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPattern   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  path       = new Path();
    private final Path  diamondPath = new Path();
    private final BoardMetrics bm  = new BoardMetrics();

    public AncientEgyptBoardDrawable() {
        pFill.setStyle(Paint.Style.FILL);
        pTriangle.setStyle(Paint.Style.FILL);
        pOutline.setStyle(Paint.Style.STROKE);
        pOutline.setStrokeJoin(Paint.Join.MITER);
        pGold.setStyle(Paint.Style.STROKE);
        pPattern.setStyle(Paint.Style.STROKE);
        pPattern.setColor(Color.argb(32, 0x3E, 0x1C, 0x00));
        pPattern.setStrokeWidth(1.2f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        float W = b.width();
        float H = b.height();
        if (W <= 0 || H <= 0) return;

        bm.update((int) W, (int) H);
        float frameT    = Math.max(10f, W * 0.011f);
        float boardW    = bm.Width + bm.XBaseRight;
        float panelColW = W - boardW;

        // ── Outer frame (dark wood #3E1C00) ──
        pFill.setColor(C_WOOD_FRAME);
        canvas.drawRect(0, 0, boardW, H, pFill);

        // ── Gold hieroglyph stripe on frame (repeating diagonal lines) ──
        pGold.setColor(C_GOLD_DIM);
        pGold.setStrokeWidth(1.5f);
        float step = frameT * 1.5f;
        // Top and bottom borders
        for (float x = 0; x < boardW; x += step) {
            canvas.drawLine(x, 0, x + frameT, frameT, pGold);
            canvas.drawLine(x, H - frameT, x + frameT, H, pGold);
        }

        // ── Inner board surface (sandy gradient #D4A843 to #8B6914) ──
        float iL = bm.XBaseLeft, iT = bm.YBaseTop;
        float iR = bm.Width, iB = bm.Height;
        float iW = iR - iL, iH = iB - iT;

        pFill.setShader(new LinearGradient(iL, iT, iR, iB,
                C_SAND_LIGHT, C_SAND_DARK, Shader.TileMode.CLAMP));
        canvas.drawRect(iL, iT, iR, iB, pFill);
        pFill.setShader(null);

        // ── Subtle hieroglyph geometric pattern (repeating diamonds across middle row) ──
        float diaSize = iW * 0.024f;
        float diaY = iT + iH * 0.5f;
        for (float dx = iL + diaSize; dx < iR - diaSize; dx += diaSize * 2.8f) {
            diamondPath.reset();
            diamondPath.moveTo(dx, diaY - diaSize);
            diamondPath.lineTo(dx + diaSize, diaY);
            diamondPath.lineTo(dx, diaY + diaSize);
            diamondPath.lineTo(dx - diaSize, diaY);
            diamondPath.close();
            canvas.drawPath(diamondPath, pPattern);
        }

        // ── Bar ──
        float barX = iL + bm.LeftX;
        float barW = bm.RightX - bm.LeftX;
        pFill.setColor(C_WOOD_BAR);
        canvas.drawRect(barX, iT, barX + barW, iB, pFill);

        // ── Triangles ──
        float ptH  = bm.TriangleHeight;
        float outSW = Math.max(1.0f, bm.PaddingXLeft * 0.014f);
        pOutline.setStrokeWidth(outSW);
        pOutline.setColor(C_GOLD);

        drawQuadrantTriangles(canvas, iL, iT, iB, bm.PaddingXLeft, ptH, true);
        drawQuadrantTriangles(canvas, barX + barW, iT, iB, bm.PaddingXRight, ptH, false);

        // Bar gold borders
        pGold.setColor(C_GOLD);
        pGold.setStrokeWidth(outSW * 0.9f);
        canvas.drawLine(barX, iT, barX, iB, pGold);
        canvas.drawLine(barX + barW, iT, barX + barW, iB, pGold);

        // Frame inner border line
        float inset = frameT * 0.28f;
        pGold.setStrokeWidth(Math.max(1.5f, frameT * 0.18f));
        canvas.drawRect(inset, inset, boardW - inset, H - inset, pGold);

        // ── Right panel column ──
        pFill.setColor(Color.rgb(0x22, 0x0E, 0x00));
        canvas.drawRect(boardW, 0, W, H, pFill);

        float pM = W * 0.008f;
        float pX = boardW + pM;
        float pW = panelColW - pM * 2f;
        float pGap = H * 0.018f;
        float midY = H / 2f;

        RectF topPanel = new RectF(pX, frameT, pX + pW, midY - pGap);
        RectF botPanel = new RectF(pX, midY + pGap, pX + pW, H - frameT);

        pFill.setColor(C_PANEL_BG);
        canvas.drawRoundRect(topPanel, 4f, 4f, pFill);
        canvas.drawRoundRect(botPanel, 4f, 4f, pFill);

        pGold.setColor(C_GOLD);
        pGold.setStrokeWidth(Math.max(1.5f, pW * 0.035f));
        canvas.drawRoundRect(topPanel, 4f, 4f, pGold);
        canvas.drawRoundRect(botPanel, 4f, 4f, pGold);
    }

    private void drawQuadrantTriangles(Canvas canvas, float qX, float top, float bottom,
                                       float ptW, float ptH, boolean leftQuad) {
        for (int i = 0; i < 6; i++) {
            float x0   = qX + i * ptW;
            float x1   = x0 + ptW;
            float xMid = (x0 + x1) / 2f;

            boolean evenSienna = leftQuad;
            boolean topSienna  = (i % 2 == 0) == evenSienna;

            // Top triangle (tip down)
            drawTriangle(canvas, x0, top, x1, top, xMid, top + ptH,
                    topSienna ? C_BURNT_SIENNA : C_PAPYRUS_GRN,
                    topSienna ? C_SIENNA_LIGHT : C_GRN_LIGHT);

            // Bottom triangle (tip up)
            drawTriangle(canvas, x0, bottom, x1, bottom, xMid, bottom - ptH,
                    topSienna ? C_PAPYRUS_GRN : C_BURNT_SIENNA,
                    topSienna ? C_GRN_LIGHT : C_SIENNA_LIGHT);
        }
    }

    private void drawTriangle(Canvas canvas, float ax, float ay, float bx, float by,
                              float tipX, float tipY, int baseColor, int tipColor) {
        path.reset();
        path.moveTo(ax, ay);
        path.lineTo(bx, by);
        path.lineTo(tipX, tipY);
        path.close();

        pTriangle.setShader(new LinearGradient(ax, ay, tipX, tipY,
                baseColor, tipColor, Shader.TileMode.CLAMP));
        canvas.drawPath(path, pTriangle);
        pTriangle.setShader(null);

        canvas.drawPath(path, pOutline);
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter cf) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}