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
 * Draws a Woodland-themed backgammon board entirely on Canvas.
 * No PNG assets required.
 *
 * Layout (fractions of total width):
 *   boardArea  = 86%   — two quadrants + center bar
 *   rightPanel = 14%   — two stacked dice-holder panels
 *
 * Each quadrant: 6 top-triangles (tips ↓) + 6 bottom-triangles (tips ↑),
 * alternating forest-green and birch-cream with copper outlines.
 */
public class WoodlandBoardDrawable extends Drawable {

    // ── Palette ────────────────────────────────────────────────────────────
    private static final int C_FRAME_DARK   = Color.rgb(18,  9,  3);
    private static final int C_FRAME_MID    = Color.rgb(34, 18,  7);
    private static final int C_BOARD_DARK   = Color.rgb(42, 22,  9);
    private static final int C_BOARD_MID    = Color.rgb(58, 31, 13);
    private static final int C_GREEN        = Color.rgb(43, 82, 35);
    private static final int C_GREEN_LIGHT  = Color.rgb(60, 105, 48);
    private static final int C_CREAM        = Color.rgb(232, 220, 185);
    private static final int C_CREAM_DARK   = Color.rgb(195, 178, 138);
    private static final int C_COPPER       = Color.rgb(180, 120, 52);
    private static final int C_COPPER_DIM   = Color.argb(140, 160, 105, 42);
    private static final int C_BAR_DARK     = Color.rgb(22, 11,  4);
    private static final int C_BAR_MID      = Color.rgb(46, 25, 10);
    private static final int C_PANEL_BG     = Color.rgb(38, 20,  8);

    // ── Paints ─────────────────────────────────────────────────────────────
    private final Paint pFill      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGreen     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCream     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutline   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCopper    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPanel     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  path       = new Path();
    private final BoardMetrics bm  = new BoardMetrics();

    public WoodlandBoardDrawable() {
        pFill.setStyle(Paint.Style.FILL);
        pGreen.setStyle(Paint.Style.FILL);
        pCream.setStyle(Paint.Style.FILL);
        pOutline.setStyle(Paint.Style.STROKE);
        pOutline.setStrokeJoin(Paint.Join.MITER);
        pCopper.setStyle(Paint.Style.STROKE);
        pPanel.setStyle(Paint.Style.FILL);
    }

    // ── Main draw ──────────────────────────────────────────────────────────
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

        // ── Outer frame (dark walnut gradient) ──
        pFill.setShader(new LinearGradient(0, 0, 0, H,
                C_FRAME_DARK, C_FRAME_MID, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, boardW, H, pFill);
        pFill.setShader(null);

        // ── Inner board surface (aligned to BoardMetrics so checkers sit on the triangles) ──
        float iL = bm.XBaseLeft, iT = bm.YBaseTop;
        float iR = bm.Width, iB = bm.Height;
        float iW = iR - iL, iH = iB - iT;
        pFill.setShader(new LinearGradient(iL, 0, iR, 0,
                C_BOARD_MID, C_BOARD_DARK, Shader.TileMode.CLAMP));
        canvas.drawRect(iL, iT, iR, iB, pFill);
        pFill.setShader(null);

        // ── Bar ──
        float barX = iL + bm.LeftX;
        float barW = bm.RightX - bm.LeftX;
        pFill.setShader(new LinearGradient(barX, 0, barX + barW, 0,
                C_BAR_DARK, C_BAR_MID, Shader.TileMode.CLAMP));
        canvas.drawRect(barX, iT, barX + barW, iB, pFill);
        pFill.setShader(null);

        // ── Triangles ──
        float ptH  = bm.TriangleHeight;
        float outSW = Math.max(1.0f, bm.PaddingXLeft * 0.014f);
        pOutline.setStrokeWidth(outSW);
        pOutline.setColor(C_COPPER);

        // Gradient fills for triangles
        // (reapplied per triangle for richer look)
        drawQuadrantTriangles(canvas, iL,   iT, iB, bm.PaddingXLeft, ptH, true);
        drawQuadrantTriangles(canvas, barX + barW, iT, iB, bm.PaddingXRight, ptH, false);

        // Bar copper edge lines
        pCopper.setColor(C_COPPER);
        pCopper.setStrokeWidth(outSW * 0.85f);
        canvas.drawLine(barX, iT, barX, iB, pCopper);
        canvas.drawLine(barX + barW, iT, barX + barW, iB, pCopper);

        // Frame copper inlay
        float inset = frameT * 0.28f;
        pCopper.setColor(C_COPPER_DIM);
        pCopper.setStrokeWidth(Math.max(1.5f, frameT * 0.20f));
        canvas.drawRect(inset, inset, boardW - inset, H - inset, pCopper);

        // ── Right panel column ──
        // Column background
        pFill.setColor(Color.rgb(20, 10, 4));
        canvas.drawRect(boardW, 0, W, H, pFill);

        // Two stacked panels
        float pM = W * 0.008f;
        float pX = boardW + pM;
        float pW = panelColW - pM * 2f;
        float pGap = H * 0.018f;
        float midY = H / 2f;

        RectF topPanel = new RectF(pX, frameT, pX + pW, midY - pGap);
        RectF botPanel = new RectF(pX, midY + pGap, pX + pW, H - frameT);

        pPanel.setColor(C_PANEL_BG);
        canvas.drawRoundRect(topPanel, 4f, 4f, pPanel);
        canvas.drawRoundRect(botPanel, 4f, 4f, pPanel);

        pCopper.setColor(C_COPPER);
        pCopper.setStrokeWidth(Math.max(1.5f, pW * 0.038f));
        canvas.drawRoundRect(topPanel, 4f, 4f, pCopper);
        canvas.drawRoundRect(botPanel, 4f, 4f, pCopper);

        // Inner panel inlay
        RectF topInner = new RectF(topPanel); topInner.inset(pW * 0.09f, pW * 0.09f);
        RectF botInner = new RectF(botPanel); botInner.inset(pW * 0.09f, pW * 0.09f);
        pCopper.setColor(C_COPPER_DIM);
        pCopper.setStrokeWidth(Math.max(1f, pW * 0.022f));
        canvas.drawRoundRect(topInner, 3f, 3f, pCopper);
        canvas.drawRoundRect(botInner, 3f, 3f, pCopper);
    }

    // ── Triangle drawing ───────────────────────────────────────────────────
    private void drawQuadrantTriangles(Canvas canvas,
                                        float qX, float top, float bottom,
                                        float ptW, float ptH, boolean leftQuad) {
        float qH = bottom - top;
        for (int i = 0; i < 6; i++) {
            float x0   = qX + i * ptW;
            float x1   = x0 + ptW;
            float xMid = (x0 + x1) / 2f;

            // Color rule: left quad → even=green; right quad → even=cream
            boolean evenGreen = leftQuad;
            boolean topGreen  = (i % 2 == 0) == evenGreen;

            // Top triangle (tip ↓)
            drawTriangle(canvas, x0, top, x1, top, xMid, top + ptH,
                    topGreen ? C_GREEN : C_CREAM,
                    topGreen ? C_GREEN_LIGHT : C_CREAM_DARK);

            // Bottom triangle (tip ↑)
            drawTriangle(canvas, x0, bottom, x1, bottom, xMid, bottom - ptH,
                    topGreen ? C_CREAM : C_GREEN,
                    topGreen ? C_CREAM_DARK : C_GREEN_LIGHT);
        }
    }

    private void drawTriangle(Canvas canvas,
                               float ax, float ay, float bx, float by,
                               float tipX, float tipY,
                               int baseColor, int tipColor) {
        path.reset();
        path.moveTo(ax, ay);
        path.lineTo(bx, by);
        path.lineTo(tipX, tipY);
        path.close();

        // Gradient from base edge to tip
        pGreen.setShader(new LinearGradient(ax, ay, tipX, tipY,
                baseColor, tipColor, Shader.TileMode.CLAMP));
        canvas.drawPath(path, pGreen);
        pGreen.setShader(null);

        canvas.drawPath(path, pOutline);
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter cf) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}
