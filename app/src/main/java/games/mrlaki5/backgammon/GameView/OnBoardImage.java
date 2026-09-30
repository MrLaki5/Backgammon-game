package games.mrlaki5.backgammon.GameView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameView.themes.BoardThemeFactory;
import games.mrlaki5.backgammon.R;

/**
 * Custom View for rendering the backgammon board, checkers, dice, animations, and touch targets.
 * Decomposes rendering and geometry into CheckerRenderer, BoardMetrics, and BoardTouchHandler.
 */
public class OnBoardImage extends androidx.appcompat.widget.AppCompatImageView {

    private static final long MESSAGE_ANIMATION_MS = 260L;

    // Modular components
    private final BoardMetrics boardMetrics = new BoardMetrics();
    private final BoardTouchHandler touchHandler = new BoardTouchHandler();
    private CheckerRenderer checkerRenderer;

    // Legacy fields preserved for subclass / reflection compatibility (e.g. RoyalOnBoardImage)
    protected Paint RedChipPaint;
    protected Paint WhiteChipPaint;
    protected Paint BorderChipPaint;
    protected Paint TextPaint;

    // Board state
    private BoardFieldState[] ChipMatrix;
    private int[] NextMoveArray;
    private String Message = "";
    private int currentTheme = 0;
    private int messagePlayerNum = 1;

    // Moving chip state
    private float MoveChipSize;
    private float MoveChipX = -1;
    private float MoveChipY = -1;
    private int MoveChipPlayer = -1;

    // Dice state
    private final Bitmap[] DiceImages = new Bitmap[4];

    // Animation state
    private final Object messageLock = new Object();
    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private Runnable movePulseRunnable;
    private Runnable messageAnimationRunnable;
    private int movePulseField = -1;
    private boolean movePulseHit = false;
    private float movePulseProgress = 1F;
    private long messageAnimationStart = 0L;
    private float messageAnimationProgress = 1F;
    private boolean messageRollPrompt = false;

    public OnBoardImage(Context context) {
        super(context);
        initOnBoardImage();
    }

    public OnBoardImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initOnBoardImage();
    }

    public OnBoardImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initOnBoardImage();
    }

    private void initOnBoardImage() {
        Message = getContext().getString(R.string.initial_game_message);
        checkerRenderer = new CheckerRenderer(getContext());

        RedChipPaint = checkerRenderer.RedChipPaint;
        WhiteChipPaint = checkerRenderer.WhiteChipPaint;
        BorderChipPaint = checkerRenderer.BorderChipPaint;
        TextPaint = checkerRenderer.TextPaint;
    }

    /** Set the active board theme so chip rendering can adapt. */
    public void setBoardTheme(int themeId) {
        this.currentTheme = themeId;
        if (checkerRenderer != null) {
            checkerRenderer.setTheme(BoardThemeFactory.getTheme(themeId));
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boardMetrics.update(w, h);
    }

    private void startMessageAnimation() {
        if (messageAnimationRunnable != null) {
            animationHandler.removeCallbacks(messageAnimationRunnable);
        }
        messageAnimationStart = System.currentTimeMillis();
        messageAnimationProgress = 0F;
        messageAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - messageAnimationStart;
                messageAnimationProgress = Math.min(1F, elapsed / (float) MESSAGE_ANIMATION_MS);
                postInvalidateOnAnimation();
                if (messageAnimationProgress < 1F) {
                    animationHandler.postDelayed(this, 16L);
                } else {
                    messageAnimationRunnable = null;
                }
            }
        };
        animationHandler.post(messageAnimationRunnable);
    }

    public synchronized void setChipMatrix(BoardFieldState[] chips) {
        this.ChipMatrix = chips;
    }

    public synchronized void setNextMoveArray(int[] moves) {
        this.NextMoveArray = moves;
    }

    public void setMoveChip(float x, float y, int player) {
        if (!((x - (MoveChipSize / 2F)) >= boardMetrics.XBaseLeft)) {
            MoveChipX = boardMetrics.XBaseLeft + MoveChipSize / 2F;
        } else {
            if (!((x + (MoveChipSize / 2F)) <= (boardMetrics.RealWidth - boardMetrics.XBaseRight))) {
                MoveChipX = boardMetrics.RealWidth - boardMetrics.XBaseRight - MoveChipSize / 2F;
            } else {
                MoveChipX = x;
            }
        }
        if (!((y - (MoveChipSize / 2f)) >= boardMetrics.YBaseTop)) {
            MoveChipY = boardMetrics.YBaseTop + MoveChipSize / 2F;
        } else {
            if (!((y + (MoveChipSize / 2f)) <= boardMetrics.Height)) {
                MoveChipY = boardMetrics.Height - MoveChipSize / 2F;
            } else {
                MoveChipY = y;
            }
        }
        MoveChipPlayer = player;
    }

    public boolean unsetMoveChip() {
        if (MoveChipX != -1 && MoveChipY != -1) {
            MoveChipX = -1;
            MoveChipY = -1;
            MoveChipPlayer = -1;
            return true;
        }
        return false;
    }

    public float getXMovPos() {
        return MoveChipX;
    }

    public float getYMovPos() {
        return MoveChipY;
    }

    public boolean moveMoveChip(float x, float y) {
        if (MoveChipX != -1 && MoveChipY != -1) {
            if ((x - (MoveChipSize / 2f)) >= boardMetrics.XBaseLeft
                    && (x + (MoveChipSize / 2f)) <= (boardMetrics.RealWidth - boardMetrics.XBaseRight)) {
                MoveChipX = x;
            }
            if ((y - (MoveChipSize / 2f)) >= boardMetrics.YBaseTop
                    && (y + (MoveChipSize / 2f)) <= boardMetrics.Height) {
                MoveChipY = y;
            }
            return true;
        }
        return false;
    }

    public void playMoveFeedback(final int destinationField, final boolean hit) {
        if (destinationField < 0 || destinationField >= boardMetrics.FieldCenterX.length) {
            return;
        }
        if (movePulseRunnable != null) {
            animationHandler.removeCallbacks(movePulseRunnable);
        }
        movePulseField = destinationField;
        movePulseHit = hit;
        movePulseProgress = 0F;
        movePulseRunnable = new Runnable() {
            @Override
            public void run() {
                movePulseProgress += 0.12F;
                if (movePulseProgress >= 1F) {
                    movePulseField = -1;
                    movePulseRunnable = null;
                    postInvalidateOnAnimation();
                    return;
                }
                postInvalidateOnAnimation();
                animationHandler.postDelayed(this, 16L);
            }
        };
        animationHandler.post(movePulseRunnable);
    }

    public void setDices(DiceThrow[] diceThrows) {
        synchronized (DiceImages) {
            for (int i = 0; i < diceThrows.length; i++) {
                if (diceThrows[i].getThrowNumber() == 0) {
                    DiceImages[i] = null;
                    continue;
                }
                int diceNumber = diceThrows[i].getThrowNumber();
                boolean used = diceThrows[i].getAlreadyUsed() == 1;
                DiceImages[i] = checkerRenderer.getCachedDiceBitmap(getResources(), diceNumber, used);
            }
        }
    }

    public void setMessage(String text, int playerNum) {
        setMessage(text, playerNum, false);
    }

    public void setMessage(String text, int playerNum, boolean rollPrompt) {
        synchronized (messageLock) {
            if (!Message.equals(text) || messageRollPrompt != rollPrompt) {
                Message = text;
                messageRollPrompt = rollPrompt;
                startMessageAnimation();
            }
            messagePlayerNum = playerNum;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Draw top banner text
        synchronized (messageLock) {
            checkerRenderer.drawMessageBanner(canvas, boardMetrics, Message,
                    messageRollPrompt, messageAnimationProgress, messagePlayerNum,
                    getWidth(), getHeight());
        }

        // 2. Draw dice
        synchronized (DiceImages) {
            checkerRenderer.drawDices(canvas, boardMetrics, DiceImages);
        }

        // 3. Draw checkers and hints
        if (ChipMatrix != null) {
            synchronized (this) {
                checkerRenderer.drawCheckersAndHints(canvas, boardMetrics, ChipMatrix, NextMoveArray);
            }

            // 4. Draw moving chip
            checkerRenderer.drawMovingChip(canvas, MoveChipX, MoveChipY, MoveChipSize, MoveChipPlayer);

            // 5. Draw move pulse feedback
            if (movePulseField != -1) {
                checkerRenderer.drawMovePulse(canvas, boardMetrics, movePulseField,
                        movePulseHit, movePulseProgress, MoveChipSize);
            }
        }
    }

    public int triangleTouched(float touchX, float touchY) {
        return touchHandler.triangleTouched(touchX, touchY, boardMetrics);
    }

    public synchronized boolean chipPTouched(int trianglePosition, float touchX, float touchY) {
        boolean touched = touchHandler.chipPTouched(trianglePosition, touchX, touchY,
                ChipMatrix, boardMetrics);
        if (touched) {
            MoveChipSize = touchHandler.getLastMoveChipSize();
        }
        return touched;
    }

    public BoardMetrics getBoardMetrics() {
        return boardMetrics;
    }
}
