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
 * Procedural board drawable for the Neon Retro theme.
 * Dark scanline grid (#0D0D0D with 4px scanlines at 15% alpha),
 * alternating 60% opacity neon cyan and neon magenta triangles,
 * #111111 frame with neon cyan glow line.
 */
public class NeonRetroBoardDrawable extends Drawable {

    private static final int C_BG_DARK       = Color.rgb(0x0D, 0x0D, 0x0D);
    private static final int C_FRAME         = Color.rgb(0x11, 0x11, 0x11);
    private static final int C_CYAN          = Color.rgb(0x00, 0xFF, 0xFF);
    private static final int C_MAGENTA       = Color.rgb(0xFF, 0x00, 0xFF);
    // 60% opacity triangles (alpha = 153)
    private static final int C_CYAN_60       = Color.argb(153, 0, 255, 255);
    private static final int C_CYAN_TIP      = Color.argb(200, 0, 200, 255);
    private static final int C_MAGENTA_60    = Color.argb(153, 255, 0, 255);
    private static final int C_MAGENTA_TIP   = Color.argb(200, 200, 0, 255);
    private static final int C_BAR_BG        = Color.rgb(0x08, 0x08, 0x08);
    private static final int C_PANEL_BG      = Color.rgb(0x14, 0x14, 0x14);

    private final Paint pFill        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTriangle    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutline     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGlowLine    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pScanline    = new Paint();
    private final Path  path         = new Path();
    private final BoardMetrics bm    = new BoardMetrics();

    public NeonRetroBoardDrawable() {
        pFill.setStyle(Paint.Style.FILL);
        pTriangle.setStyle(Paint.Style.FILL);
        pOutline.setStyle(Paint.Style.STROKE);
        pOutline.setStrokeJoin(Paint.Join.MITER);
        pGlowLine.setStyle(Paint.Style.STROKE);
        pScanline.setColor(Color.argb(38, 0, 255, 255)); // 15% alpha cyan scanlines
        pScanline.setStrokeWidth(1.0f);
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

        // ── Outer frame (#111111) ──
        pFill.setColor(C_FRAME);
        canvas.drawRect(0, 0, boardW, H, pFill);

        // ── Inner board surface (#0D0D0D), aligned to BoardMetrics ──
        float iL = bm.XBaseLeft, iT = bm.YBaseTop;
        float iR = bm.Width, iB = bm.Height;

        pFill.setColor(C_BG_DARK);
        canvas.drawRect(iL, iT, iR, iB, pFill);

        // ── Scanline effect: drawLine every 4px across inner board ──
        for (float y = iT; y <= iB; y += 4.0f) {
            canvas.drawLine(iL, y, iR, y, pScanline);
        }

        // ── Bar ──
        float barX = iL + bm.LeftX;
        float barW = bm.RightX - bm.LeftX;
        pFill.setColor(C_BAR_BG);
        canvas.drawRect(barX, iT, barX + barW, iB, pFill);

        // ── Triangles ──
        float ptH  = bm.TriangleHeight;
        float outSW = Math.max(1.0f, bm.PaddingXLeft * 0.014f);
        pOutline.setStrokeWidth(outSW);

        drawQuadrantTriangles(canvas, iL, iT, iB, bm.PaddingXLeft, ptH, true);
        drawQuadrantTriangles(canvas, barX + barW, iT, iB, bm.PaddingXRight, ptH, false);

        // Bar neon lines (cyan on left, magenta on right)
        pGlowLine.setStrokeWidth(outSW);
        pGlowLine.setColor(C_CYAN);
        canvas.drawLine(barX, iT, barX, iB, pGlowLine);
        pGlowLine.setColor(C_MAGENTA);
        canvas.drawLine(barX + barW, iT, barX + barW, iB, pGlowLine);

        // Neon cyan outer glow line on frame
        float inset = frameT * 0.28f;
        // Outer glow (thick, translucent)
        pGlowLine.setColor(Color.argb(80, 0, 255, 255));
        pGlowLine.setStrokeWidth(Math.max(3f, frameT * 0.35f));
        canvas.drawRect(inset, inset, boardW - inset, H - inset, pGlowLine);
        // Inner crisp line (thin, full opacity)
        pGlowLine.setColor(C_CYAN);
        pGlowLine.setStrokeWidth(Math.max(1.2f, frameT * 0.14f));
        canvas.drawRect(inset, inset, boardW - inset, H - inset, pGlowLine);

        // ── Right panel column ──
        pFill.setColor(Color.rgb(0x09, 0x09, 0x09));
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

        // Neon panel border with glow
        pGlowLine.setColor(Color.argb(70, 0, 255, 255));
        pGlowLine.setStrokeWidth(pW * 0.06f);
        canvas.drawRoundRect(topPanel, 4f, 4f, pGlowLine);
        pGlowLine.setColor(Color.argb(70, 255, 0, 255));
        canvas.drawRoundRect(botPanel, 4f, 4f, pGlowLine);

        pGlowLine.setColor(C_CYAN);
        pGlowLine.setStrokeWidth(Math.max(1.5f, pW * 0.025f));
        canvas.drawRoundRect(topPanel, 4f, 4f, pGlowLine);
        pGlowLine.setColor(C_MAGENTA);
        canvas.drawRoundRect(botPanel, 4f, 4f, pGlowLine);
    }

    private void drawQuadrantTriangles(Canvas canvas, float qX, float top, float bottom,
                                       float ptW, float ptH, boolean leftQuad) {
        for (int i = 0; i < 6; i++) {
            float x0   = qX + i * ptW;
            float x1   = x0 + ptW;
            float xMid = (x0 + x1) / 2f;

            boolean evenCyan = leftQuad;
            boolean topCyan  = (i % 2 == 0) == evenCyan;

            // Top triangle (tip down)
            drawTriangle(canvas, x0, top, x1, top, xMid, top + ptH,
                    topCyan ? C_CYAN_60 : C_MAGENTA_60,
                    topCyan ? C_CYAN_TIP : C_MAGENTA_TIP,
                    topCyan ? C_CYAN : C_MAGENTA);

            // Bottom triangle (tip up)
            drawTriangle(canvas, x0, bottom, x1, bottom, xMid, bottom - ptH,
                    topCyan ? C_MAGENTA_60 : C_CYAN_60,
                    topCyan ? C_MAGENTA_TIP : C_CYAN_TIP,
                    topCyan ? C_MAGENTA : C_CYAN);
        }
    }

    private void drawTriangle(Canvas canvas, float ax, float ay, float bx, float by,
                              float tipX, float tipY, int baseColor, int tipColor,
                              int outlineColor) {
        path.reset();
        path.moveTo(ax, ay);
        path.lineTo(bx, by);
        path.lineTo(tipX, tipY);
        path.close();

        pTriangle.setShader(new LinearGradient(ax, ay, tipX, tipY,
                baseColor, tipColor, Shader.TileMode.CLAMP));
        canvas.drawPath(path, pTriangle);
        pTriangle.setShader(null);

        pOutline.setColor(outlineColor);
        canvas.drawPath(path, pOutline);
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter cf) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}