package games.mrlaki5.backgammon.GameView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Random;

import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.R;

/**
 * Royal visual layer for the legacy board renderer.
 * It preserves the tested board geometry and game logic while applying the new palette.
 */
public class RoyalOnBoardImage extends OnBoardImage {

    private Paint turquoiseChipPaint;
    private Paint ivoryChipPaint;
    private Paint chipBorderPaint;
    private Paint messagePaint;
    private final Handler diceAnimationHandler=new Handler(Looper.getMainLooper());
    private final Random diceRandom=new Random();
    private Runnable diceAnimationRunnable;
    private boolean diceInitialized=false;
    private boolean lastDiceWereUsed=true;
    private int lastFirstDie=0;
    private int lastSecondDie=0;

    public RoyalOnBoardImage(Context context) {
        super(context);
        applyRoyalPalette();
    }

    public RoyalOnBoardImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyRoyalPalette();
    }

    public RoyalOnBoardImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyRoyalPalette();
    }

    private Paint getLegacyPaint(String fieldName) throws Exception {
        Field field=OnBoardImage.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Paint) field.get(this);
    }

    private void applyRoyalPalette() {
        try {
            turquoiseChipPaint=getLegacyPaint("RedChipPaint");
            ivoryChipPaint=getLegacyPaint("WhiteChipPaint");
            chipBorderPaint=getLegacyPaint("BorderChipPaint");
            messagePaint=getLegacyPaint("TextPaint");

            turquoiseChipPaint.setColor(Color.rgb(136, 165, 183));
            ivoryChipPaint.setColor(Color.rgb(224, 104, 14));
            chipBorderPaint.setColor(Color.rgb(244, 176, 68));
            chipBorderPaint.setStrokeWidth(3F);
            messagePaint.setColor(Color.rgb(244, 176, 68));
            messagePaint.setShadowLayer(3F, 2F, 2F, Color.rgb(11, 26, 36));
        } catch (Exception ignored) {
            //If a future legacy renderer changes its fields, the original palette remains usable.
        }
    }

    private DiceThrow[] createDisplayDice(int first, int second) {
        DiceThrow[] displayDice=new DiceThrow[4];
        displayDice[0]=new DiceThrow(first);
        displayDice[1]=new DiceThrow(second);
        displayDice[2]=new DiceThrow(0);
        displayDice[3]=new DiceThrow(0);
        displayDice[2].setAlreadyUsed(1);
        displayDice[3].setAlreadyUsed(1);
        return displayDice;
    }

    private void showDiceImmediately(final int first, final int second) {
        super.setDices(createDisplayDice(first, second));
        postInvalidateOnAnimation();
    }

    private void animateToDice(final int first, final int second) {
        if(diceAnimationRunnable!=null){
            diceAnimationHandler.removeCallbacks(diceAnimationRunnable);
        }

        diceAnimationRunnable=new Runnable() {
            private int frame=0;

            @Override
            public void run() {
                if(frame<8){
                    int animationFirst=diceRandom.nextInt(6)+1;
                    int animationSecond=diceRandom.nextInt(6)+1;
                    showDiceImmediately(animationFirst, animationSecond);
                    frame++;
                    diceAnimationHandler.postDelayed(this, 42L + (frame * 6L));
                }
                else{
                    showDiceImmediately(first, second);
                    diceAnimationRunnable=null;
                }
            }
        };
        diceAnimationHandler.post(diceAnimationRunnable);
    }

    /**
     * Keeps four logical throws for doubles in the model, but always renders two physical dice.
     * Used dice remain complete instead of switching to the legacy transparent *d assets.
     */
    @Override
    public void setDices(DiceThrow[] diceThrows) {
        if(diceThrows==null || diceThrows.length<2){
            return;
        }

        final int first=diceThrows[0].getThrowNumber();
        final int second=diceThrows[1].getThrowNumber();
        boolean currentDiceAreUsed=diceThrows[0].getAlreadyUsed()==1
                || diceThrows[1].getAlreadyUsed()==1;
        boolean valuesChanged=first!=lastFirstDie || second!=lastSecondDie;
        boolean shouldAnimate=diceInitialized && !currentDiceAreUsed
                && (valuesChanged || lastDiceWereUsed);

        lastFirstDie=first;
        lastSecondDie=second;
        lastDiceWereUsed=currentDiceAreUsed;

        if(!diceInitialized){
            diceInitialized=true;
            showDiceImmediately(first, second);
        }
        else if(shouldAnimate){
            animateToDice(first, second);
        }
        else{
            if(diceAnimationRunnable!=null){
                diceAnimationHandler.removeCallbacks(diceAnimationRunnable);
                diceAnimationRunnable=null;
            }
            showDiceImmediately(first, second);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if(diceAnimationRunnable!=null){
            diceAnimationHandler.removeCallbacks(diceAnimationRunnable);
            diceAnimationRunnable=null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void setMessage(String text, int playerNum) {
        setMessage(text, playerNum, false);
    }

    @Override
    public void setMessage(String text, int playerNum, boolean rollPrompt) {
        int separator=text.indexOf(", ");
        if(separator>0){
            text=getContext().getString(R.string.current_turn_message,
                    text.substring(0, separator), text.substring(separator + 2));
        }
        super.setMessage(text, playerNum, rollPrompt);
        if(messagePaint!=null){
            messagePaint.setColor(playerNum==1
                    ? Color.rgb(244, 176, 68)
                    : Color.rgb(247, 239, 213));
        }
    }
}

