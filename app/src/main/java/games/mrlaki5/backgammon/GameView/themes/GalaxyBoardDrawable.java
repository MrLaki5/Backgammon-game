package games.mrlaki5.backgammon.GameView.themes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

import games.mrlaki5.backgammon.GameView.BoardMetrics;

/**
 * Procedural board drawable for the Galaxy theme.
 * Deep space radial gradient, seeded starfield, indigo/purple triangles with star-shimmer details.
 */
public class GalaxyBoardDrawable extends Drawable {

    private static final int C_BG_CENTER    = Color.rgb(0x0A, 0x0A, 0x2E);
    private static final int C_BG_EDGE      = Color.rgb(0x00, 0x00, 0x00);
    private static final int C_INDIGO       = Color.rgb(0x1A, 0x23, 0x7E);
    private static final int C_INDIGO_LIGHT = Color.rgb(0x28, 0x35, 0x93);
    private static final int C_PURPLE       = Color.rgb(0x4A, 0x14, 0x8C);
    private static final int C_PURPLE_LIGHT = Color.rgb(0x6A, 0x1B, 0x9A);
    private static final int C_FRAME        = Color.rgb(0x1C, 0x1C, 0x3A);
    private static final int C_GOLD         = Color.rgb(0xFF, 0xD7, 0x00);
    private static final int C_GOLD_DIM     = Color.argb(130, 0xD4, 0xAF, 0x37);
    private static final int C_BAR_DARK     = Color.rgb(0x08, 0x08, 0x18);
    private static final int C_BAR_MID      = Color.rgb(0x18, 0x18, 0x30);
    private static final int C_PANEL_BG     = Color.rgb(0x10, 0x10, 0x28);

    private final Paint pFill      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTriangle  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutline   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGold      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStar      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pShimmer   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  path       = new Path();
    private final Path  shimmerPath = new Path();
    private final BoardMetrics bm  = new BoardMetrics();

    // 28 seeded star coordinates (fractions of board width and height)
    private static final float[] STAR_X = new float[28];
    private static final float[] STAR_Y = new float[28];
    private static final float[] STAR_R = new float[28];
    private static final int[]   STAR_A = new int[28];

    static {
        Random rng = new Random(42007L);
        for (int i = 0; i < 28; i++) {
            STAR_X[i] = 0.03f + rng.nextFloat() * 0.94f;
            STAR_Y[i] = 0.05f + rng.nextFloat() * 0.90f;
            STAR_R[i] = 0.8f + rng.nextFloat() * 1.8f;
            STAR_A[i] = 130 + rng.nextInt(126);
        }
    }

    public GalaxyBoardDrawable() {
        pFill.setStyle(Paint.Style.FILL);
        pTriangle.setStyle(Paint.Style.FILL);
        pOutline.setStyle(Paint.Style.STROKE);
        pOutline.setStrokeJoin(Paint.Join.MITER);
        pGold.setStyle(Paint.Style.STROKE);
        pStar.setStyle(Paint.Style.FILL);
        pShimmer.setStyle(Paint.Style.FILL_AND_STROKE);
        pShimmer.setColor(Color.argb(160, 255, 255, 255));
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

        // ── Outer frame (dark metallic #1C1C3A) ──
        pFill.setColor(C_FRAME);
        canvas.drawRect(0, 0, boardW, H, pFill);

        // ── Inner board surface (deep space radial gradient: #0A0A2E center to #000000 edge) ──
        float iL = bm.XBaseLeft, iT = bm.YBaseTop;
        float iR = bm.Width, iB = bm.Height;
        float iW = iR - iL, iH = iB - iT;
        float cx = iL + iW / 2f, cy = iT + iH / 2f;
        float gradR = Math.max(iW, iH) * 0.72f;

        pFill.setShader(new RadialGradient(cx, cy, gradR,
                C_BG_CENTER, C_BG_EDGE, Shader.TileMode.CLAMP));
        canvas.drawRect(iL, iT, iR, iB, pFill);
        pFill.setShader(null);

        // ── Seeded Stars in board space ──
        for (int i = 0; i < STAR_X.length; i++) {
            float sx = iL + STAR_X[i] * iW;
            float sy = iT + STAR_Y[i] * iH;
            pStar.setColor(Color.argb(STAR_A[i], 255, 255, 255));
            canvas.drawCircle(sx, sy, STAR_R[i], pStar);
        }

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
        pOutline.setColor(C_GOLD_DIM);

        drawQuadrantTriangles(canvas, iL, iT, iB, bm.PaddingXLeft, ptH, true);
        drawQuadrantTriangles(canvas, barX + barW, iT, iB, bm.PaddingXRight, ptH, false);

        // Bar edge lines
        pGold.setColor(C_GOLD);
        pGold.setStrokeWidth(outSW * 0.85f);
        canvas.drawLine(barX, iT, barX, iB, pGold);
        canvas.drawLine(barX + barW, iT, barX + barW, iB, pGold);

        // Thin gold outline on frame
        float inset = frameT * 0.28f;
        pGold.setColor(C_GOLD);
        pGold.setStrokeWidth(Math.max(1.5f, frameT * 0.18f));
        canvas.drawRect(inset, inset, boardW - inset, H - inset, pGold);

        // ── Right panel column ──
        pFill.setColor(Color.rgb(0x06, 0x06, 0x14));
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

            boolean evenIndigo = leftQuad;
            boolean topIndigo  = (i % 2 == 0) == evenIndigo;

            // Top triangle (tip down)
            drawTriangle(canvas, x0, top, x1, top, xMid, top + ptH,
                    topIndigo ? C_INDIGO : C_PURPLE,
                    topIndigo ? C_INDIGO_LIGHT : C_PURPLE_LIGHT,
                    top + ptH, i % 3 == 0);

            // Bottom triangle (tip up)
            drawTriangle(canvas, x0, bottom, x1, bottom, xMid, bottom - ptH,
                    topIndigo ? C_PURPLE : C_INDIGO,
                    topIndigo ? C_PURPLE_LIGHT : C_INDIGO_LIGHT,
                    bottom - ptH, i % 3 == 1);
        }
    }

    private void drawTriangle(Canvas canvas, float ax, float ay, float bx, float by,
                              float tipX, float tipY, int baseColor, int tipColor,
                              float shimmerY, boolean hasShimmer) {
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

        // Subtle star-shimmer 4-point star path at tip
        if (hasShimmer) {
            float sSize = Math.abs(bx - ax) * 0.16f;
            shimmerPath.reset();
            shimmerPath.moveTo(tipX, shimmerY - sSize);
            shimmerPath.quadTo(tipX, shimmerY, tipX + sSize, shimmerY);
            shimmerPath.quadTo(tipX, shimmerY, tipX, shimmerY + sSize);
            shimmerPath.quadTo(tipX, shimmerY, tipX - sSize, shimmerY);
            shimmerPath.quadTo(tipX, shimmerY, tipX, shimmerY - sSize);
            shimmerPath.close();
            canvas.drawPath(shimmerPath, pShimmer);
        }
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter cf) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}