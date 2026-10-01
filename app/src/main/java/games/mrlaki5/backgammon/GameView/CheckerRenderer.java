package games.mrlaki5.backgammon.GameView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameView.themes.BoardTheme;
import games.mrlaki5.backgammon.GameView.themes.BoardThemeFactory;
import games.mrlaki5.backgammon.R;

/**
 * Handles canvas rendering for checkers, dice, legal move hint markers,
 * game message banners, and move pulse feedback.
 */
public class CheckerRenderer {

    private static final int COLOR_SURFACE_BASE = Color.rgb(17, 37, 50);
    private static final int COLOR_SURFACE_INSET = Color.rgb(11, 26, 36);
    private static final int COLOR_ACCENT_GOLD = Color.rgb(244, 176, 68);
    private static final int COLOR_ACCENT_ORANGE = Color.rgb(224, 104, 14);
    private static final int COLOR_ACCENT_SLATE = Color.rgb(136, 165, 183);
    private static final int COLOR_TEXT_PRIMARY = Color.rgb(247, 239, 213);

    private BoardTheme activeTheme;

    // Paints
    public Paint RedChipPaint;
    public Paint WhiteChipPaint;
    public Paint BorderChipPaint;
    public Paint NextTriangleTransparentPaint;
    public Paint DicePaint;
    public Paint TextFigurePaint;
    public Paint TextPaint;
    public Paint MoveChipShadowPaint;
    public Paint MovePulsePaint;
    public Paint ChipHighlightPaint;
    public Paint ChipRimPaint;
    public Paint MessageOuterBorderPaint;
    public Paint MessageInnerBorderPaint;
    public Paint MessageDiceFillPaint;
    public Paint MessageDiceDotPaint;

    public Paint chipGlowPaint;
    public Paint chipContactShadowPaint;
    public Paint chipSpecPaint;
    public Paint chipBevelPaint;
    public Paint chipAccentPaint;
    public Paint chipDotPaint;

    // Reusable rects
    private final RectF ChipRect = new RectF();
    private final RectF NextTriangleRect = new RectF();
    private final RectF DiceRect = new RectF();
    private final RectF TextFigureRect = new RectF();
    private final RectF chipShadowRect = new RectF();
    private final RectF chipSpecRect = new RectF();

    // Hint bitmaps
    private Bitmap NextTriangleImageTop;
    private Bitmap NextTriangleImageBottom;
    private Bitmap EndBoardImage;

    // Dice cache
    private final Bitmap[][] DiceBitmapCache = new Bitmap[7][2];

    public CheckerRenderer(Context context) {
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();

        activeTheme = BoardThemeFactory.getTheme(0);

        RedChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RedChipPaint.setColor(COLOR_ACCENT_SLATE);

        WhiteChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        WhiteChipPaint.setColor(COLOR_ACCENT_ORANGE);

        BorderChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BorderChipPaint.setStyle(Paint.Style.STROKE);

        NextTriangleTransparentPaint = new Paint();
        NextTriangleTransparentPaint.setAlpha(150);

        NextTriangleImageTop = BitmapFactory.decodeResource(res, R.drawable.triangle_up);
        NextTriangleImageBottom = BitmapFactory.decodeResource(res, R.drawable.triangle_down);
        EndBoardImage = BitmapFactory.decodeResource(res, R.drawable.square);

        DicePaint = new Paint();

        TextFigurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TextFigurePaint.setColor(Color.argb(222, 17, 37, 50));

        TextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TextPaint.setColor(COLOR_ACCENT_GOLD);
        TextPaint.setShadowLayer(5.0F, 1.5F, 2.5F, COLOR_SURFACE_INSET);
        TextPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        TextPaint.setFakeBoldText(true);
        TextPaint.setLetterSpacing(0.04F);
        TextPaint.setTextSkewX(0F);

        MoveChipShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MoveChipShadowPaint.setColor(Color.argb(132, 0, 0, 0));

        MovePulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MovePulsePaint.setStyle(Paint.Style.STROKE);
        MovePulsePaint.setStrokeWidth(4F);

        ChipHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ChipHighlightPaint.setStyle(Paint.Style.STROKE);
        ChipHighlightPaint.setColor(Color.argb(120, 255, 255, 255));

        ChipRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ChipRimPaint.setStyle(Paint.Style.STROKE);

        MessageOuterBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MessageOuterBorderPaint.setStyle(Paint.Style.STROKE);
        MessageOuterBorderPaint.setColor(COLOR_ACCENT_GOLD);

        MessageInnerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MessageInnerBorderPaint.setStyle(Paint.Style.STROKE);
        MessageInnerBorderPaint.setColor(COLOR_SURFACE_INSET);

        MessageDiceFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MessageDiceFillPaint.setStyle(Paint.Style.FILL);
        MessageDiceFillPaint.setColor(COLOR_TEXT_PRIMARY);

        MessageDiceDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MessageDiceDotPaint.setStyle(Paint.Style.FILL);
        MessageDiceDotPaint.setColor(COLOR_SURFACE_BASE);

        chipGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipContactShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipSpecPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipBevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipBevelPaint.setStyle(Paint.Style.STROKE);
        chipAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipAccentPaint.setStyle(Paint.Style.STROKE);
        chipDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setTheme(BoardTheme theme) {
        if (theme != null) {
            if (this.activeTheme != theme) {
                for (int d = 0; d <= 6; d++) {
                    DiceBitmapCache[d][0] = null;
                    DiceBitmapCache[d][1] = null;
                }
            }
            this.activeTheme = theme;
        }
    }

    public BoardTheme getActiveTheme() {
        return activeTheme;
    }

    public void drawMessageBanner(Canvas canvas, BoardMetrics m, String message,
                                  boolean messageRollPrompt, float messageAnimationProgress,
                                  int playerNum, int width, int height) {
        if (m == null || message == null || message.isEmpty()) return;

        TextPaint.setTextSize(m.TextSize);
        TextPaint.setColor(playerNum == 1 ? COLOR_ACCENT_GOLD : COLOR_TEXT_PRIMARY);

        float easedProgress = 1F - ((1F - messageAnimationProgress) * (1F - messageAnimationProgress));
        int savedAlpha = canvas.saveLayerAlpha(0F, 0F, width, height, (int) (185F + (70F * easedProgress)));
        canvas.scale(0.96F + (0.04F * easedProgress),
                0.96F + (0.04F * easedProgress), m.TextXCoordinate, m.TextYCoordinate);

        float textWidth = TextPaint.measureText(message);
        float glyphSize = messageRollPrompt ? m.TextSize * 0.82F : 0F;
        float glyphGap = messageRollPrompt ? m.TextSize * 0.34F : 0F;
        float contentWidth = textWidth + glyphSize + glyphGap;
        float horizontalPadding = m.TextSize * 0.65F;
        float verticalPadding = m.TextSize * 0.42F;

        float boxLeft = Math.max(m.XBaseLeft, m.TextXCoordinate - (contentWidth / 2F) - horizontalPadding);
        float boxRight = Math.min(m.RealWidth - m.XBaseRight, m.TextXCoordinate + (contentWidth / 2F) + horizontalPadding);
        float contentX = boxLeft + ((boxRight - boxLeft - contentWidth) / 2F);
        float textX = contentX + glyphSize + glyphGap;

        TextFigureRect.set(boxLeft,
                m.TextYCoordinate - m.TextSize - verticalPadding,
                boxRight,
                m.TextYCoordinate + verticalPadding);

        canvas.drawRoundRect(TextFigureRect, m.TextSize * 0.58F, m.TextSize * 0.58F, TextFigurePaint);
        MessageOuterBorderPaint.setStrokeWidth(Math.max(2F, m.TextSize * 0.08F));
        canvas.drawRoundRect(TextFigureRect, m.TextSize * 0.58F, m.TextSize * 0.58F, MessageOuterBorderPaint);

        RectF innerFrame = new RectF(TextFigureRect);
        float innerInset = Math.max(3F, m.TextSize * 0.16F);
        innerFrame.inset(innerInset, innerInset);
        MessageInnerBorderPaint.setStrokeWidth(Math.max(1F, m.TextSize * 0.035F));
        canvas.drawRoundRect(innerFrame, m.TextSize * 0.42F, m.TextSize * 0.42F, MessageInnerBorderPaint);

        if (messageRollPrompt) {
            drawMessageDiceGlyph(canvas, contentX + (glyphSize / 2F),
                    m.TextYCoordinate - (m.TextSize * 0.32F), glyphSize);
        }

        canvas.drawText(message, textX, m.TextYCoordinate, TextPaint);
        canvas.restoreToCount(savedAlpha);
    }

    private void drawMessageDiceGlyph(Canvas canvas, float centerX, float centerY, float size) {
        RectF diceRect = new RectF(centerX - size / 2F, centerY - size / 2F,
                centerX + size / 2F, centerY + size / 2F);
        canvas.drawRoundRect(diceRect, size * 0.20F, size * 0.20F, MessageDiceFillPaint);
        MessageInnerBorderPaint.setStrokeWidth(Math.max(1F, size * 0.07F));
        canvas.drawRoundRect(diceRect, size * 0.20F, size * 0.20F, MessageInnerBorderPaint);

        float dotRadius = size * 0.075F;
        float offset = size * 0.22F;
        canvas.drawCircle(centerX - offset, centerY - offset, dotRadius, MessageDiceDotPaint);
        canvas.drawCircle(centerX, centerY, dotRadius, MessageDiceDotPaint);
        canvas.drawCircle(centerX + offset, centerY + offset, dotRadius, MessageDiceDotPaint);
    }

    public void drawDices(Canvas canvas, BoardMetrics m, Bitmap[] diceImages) {
        if (m == null || diceImages == null) return;

        float yDice = (diceImages[2] == null || diceImages[3] == null)
                ? m.DiceYStartTwo : m.DiceYStartFour;

        for (Bitmap diceImage : diceImages) {
            if (diceImage != null) {
                DiceRect.set(m.DiceXStart, yDice, m.DiceXEnd, yDice + m.DiceSize);
                canvas.drawBitmap(diceImage, null, DiceRect, DicePaint);
                yDice += m.DiceSize + m.DicePadding;
            }
        }
    }

    public void drawCheckersAndHints(Canvas canvas, BoardMetrics m, BoardFieldState[] chipMatrix,
                                     int[] nextMoveArray) {
        if (m == null || chipMatrix == null) return;

        float y = m.YBaseTop;
        float currPadding = m.PaddingXLeft;
        Bitmap currentNextImage = NextTriangleImageTop;
        boolean drawEndBoard = false;

        for (int i = 0; i < chipMatrix.length; i++) {
            float x = m.FieldCenterX[i];

            if (i == 6) {
                currPadding = m.PaddingXRight;
            }
            if (i == 12) {
                y = m.Height;
                currPadding = m.PaddingXLeft;
                currentNextImage = NextTriangleImageBottom;
            }
            if (i == 18) {
                currPadding = m.PaddingXRight;
            }
            if (i == 24) {
                y = m.YBaseTop;
                currPadding = m.PaddingXLeft;
            }
            if (i == 25) {
                y = m.Height;
                currPadding = m.PaddingXLeft;
            }
            if (i == 26) {
                drawEndBoard = true;
                y = m.YBaseTop;
                currentNextImage = EndBoardImage;
            }
            if (i == 27) {
                drawEndBoard = true;
                x = m.EndBoardMidX;
                y = m.Height;
            }

            // Draw checkers on field
            int visibleChips = chipMatrix[i].getVisibleChips();
            if (visibleChips > 0) {
                float xChipStart = x - currPadding * 0.35f;
                float xChipEnd = x + currPadding * 0.35f;
                float chipSize = Math.abs(xChipStart - xChipEnd);

                float heightPadding = 0F;
                float yChipStart;
                float yChipEnd;

                if (!drawEndBoard) {
                    if ((chipSize * visibleChips > m.TriangleHeight) && (visibleChips > 1)) {
                        heightPadding = (chipSize * visibleChips - m.TriangleHeight)
                                / (visibleChips - 1);
                    }
                    yChipStart = y;
                    yChipEnd = chipSize + y;
                    if (i >= 12 && i != 24) {
                        yChipEnd = y - chipSize;
                    }
                } else {
                    yChipStart = y;
                    yChipEnd = m.EndChipHeight + y;
                    if (i == 27) {
                        yChipEnd = y - m.EndChipHeight;
                    }
                }

                BorderChipPaint.setStrokeWidth(chipSize * 0.09F);
                int pinnedPlayer = chipMatrix[i].getPinnedPlayer();

                for (int j = 0; j < visibleChips; j++) {
                    // A pinned checker sits at the base, under the pinning stack
                    int chipPlayer = (j == 0 && pinnedPlayer != 0) ? pinnedPlayer : chipMatrix[i].getPlayer();
                    Paint localPaint = (chipPlayer == 1) ? WhiteChipPaint : RedChipPaint;
                    if (i < 12 || i == 24 || i == 26) {
                        ChipRect.set(xChipStart, yChipStart, xChipEnd, yChipEnd);
                    } else {
                        ChipRect.set(xChipStart, yChipEnd, xChipEnd, yChipStart);
                    }

                    if (!drawEndBoard) {
                        activeTheme.drawChip(canvas, ChipRect, chipPlayer,
                                localPaint, ChipRimPaint, MoveChipShadowPaint,
                                ChipHighlightPaint, chipSpecPaint, chipBevelPaint,
                                chipAccentPaint, chipDotPaint, chipGlowPaint, chipContactShadowPaint,
                                chipShadowRect, chipSpecRect);

                        if (i >= 12 && i != 24) {
                            yChipStart = yChipEnd + heightPadding;
                            yChipEnd = yChipEnd - chipSize + heightPadding;
                        } else {
                            yChipStart = yChipEnd - heightPadding;
                            yChipEnd = yChipEnd + (chipSize - heightPadding);
                        }
                    } else {
                        activeTheme.drawEndChip(canvas, ChipRect, chipPlayer,
                                localPaint, ChipRimPaint, MoveChipShadowPaint,
                                ChipHighlightPaint, chipGlowPaint, chipContactShadowPaint,
                                chipShadowRect);

                        if (i == 27) {
                            yChipStart = yChipEnd;
                            yChipEnd = yChipEnd - m.EndChipHeight;
                        } else {
                            yChipStart = yChipEnd;
                            yChipEnd = yChipEnd + m.EndChipHeight;
                        }
                    }
                }
            }

            // Draw next step hint (green triangle/square)
            float tempHintWidth = !drawEndBoard ? currPadding / 2F : currPadding * 0.35F;
            if (nextMoveArray != null && i != 24 && i != 25 && nextMoveArray[i] != 0) {
                if (i < 12 || i == 26) {
                    NextTriangleRect.set(x - tempHintWidth, m.YBaseTop,
                            x + tempHintWidth, m.TriangleHeight + m.YBaseTop);
                } else {
                    NextTriangleRect.set(x - tempHintWidth, m.Height - m.TriangleHeight,
                            x + tempHintWidth, m.Height);
                }
                canvas.drawBitmap(currentNextImage, null, NextTriangleRect, NextTriangleTransparentPaint);
            }
        }
    }

    public void drawMovingChip(Canvas canvas, float x, float y, float size, int player) {
        if (x == -1 || y == -1 || size <= 0 || player <= 0) return;

        Paint localPaint = (player == 1) ? WhiteChipPaint : RedChipPaint;
        canvas.drawCircle(x + size * 0.08F, y + size * 0.12F, size / 2f, MoveChipShadowPaint);
        ChipRect.set(x - size / 2F, y - size / 2F, x + size / 2F, y + size / 2F);

        activeTheme.drawChip(canvas, ChipRect, player,
                localPaint, ChipRimPaint, MoveChipShadowPaint,
                ChipHighlightPaint, chipSpecPaint, chipBevelPaint,
                chipAccentPaint, chipDotPaint, chipGlowPaint, chipContactShadowPaint,
                chipShadowRect, chipSpecRect);
    }

    public void drawMovePulse(Canvas canvas, BoardMetrics m, int pulseField, boolean pulseHit,
                              float pulseProgress, float moveChipSize) {
        if (m == null || pulseField < 0 || pulseField >= m.FieldCenterX.length) return;

        float pulseX = m.FieldCenterX[pulseField];
        float pulseY = (pulseField < 12 || pulseField == 24 || pulseField == 26)
                ? m.YBaseTop + Math.max(moveChipSize, m.EndChipHeight)
                : m.Height - Math.max(moveChipSize, m.EndChipHeight);

        float radius = (moveChipSize * 0.45F) + (moveChipSize * 0.45F * pulseProgress);
        int alpha = (int) (180F * (1F - pulseProgress));
        MovePulsePaint.setColor(pulseHit
                ? Color.argb(alpha, 244, 176, 68)
                : Color.argb(alpha, 136, 165, 183));
        MovePulsePaint.setStrokeWidth(Math.max(3F, moveChipSize * 0.08F));
        canvas.drawCircle(pulseX, pulseY, radius, MovePulsePaint);
    }

    public Bitmap getCachedDiceBitmap(Resources res, int diceNumber, boolean used) {
        if (diceNumber < 1 || diceNumber > 6) return null;
        int usedIdx = used ? 1 : 0;
        if (DiceBitmapCache[diceNumber][usedIdx] == null) {
            if (activeTheme != null) {
                Bitmap customDice = activeTheme.createDiceBitmap(diceNumber, used, 128);
                if (customDice != null) {
                    DiceBitmapCache[diceNumber][usedIdx] = customDice;
                    return customDice;
                }
            }
            DiceBitmapCache[diceNumber][usedIdx] = BitmapFactory.decodeResource(res,
                    diceResourceId(diceNumber, used));
        }
        return DiceBitmapCache[diceNumber][usedIdx];
    }

    private int diceResourceId(int diceNumber, boolean used) {
        switch (diceNumber) {
            case 1: return used ? R.drawable.dice1d : R.drawable.dice1;
            case 2: return used ? R.drawable.dice2d : R.drawable.dice2;
            case 3: return used ? R.drawable.dice3d : R.drawable.dice3;
            case 4: return used ? R.drawable.dice4d : R.drawable.dice4;
            case 5: return used ? R.drawable.dice5d : R.drawable.dice5;
            case 6: return used ? R.drawable.dice6d : R.drawable.dice6;
            default: return R.drawable.dice1;
        }
    }
}
