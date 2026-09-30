package games.mrlaki5.backgammon.Menus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * The small, slow movements that make a still screen feel like a made object.
 *
 * A menu that never moves reads as flat however well it is drawn, because real
 * gold never sits still: the light slides across it as you turn it in your hand.
 * That is what these do — nothing here is decoration for its own sake, and
 * nothing runs fast enough to compete with what the player came to do.
 *
 * All of it is cancelled when the view leaves the window, so nothing keeps a
 * dead activity alive.
 */
public final class RoyalMotion {

    /** Long enough that the sweep reads as a highlight, not a loading bar. */
    private static final long SHIMMER_TRAVEL_MS = 1150L;
    private static final long SHIMMER_REST_MS = 3400L;

    private static final long BREATH_MS = 2600L;
    private static final float BREATH_SCALE = 1.012F;

    private RoyalMotion() {}

    /**
     * Slides a highlight across a surface, pauses, and does it again. The glint
     * view is expected to sit inside a clipping parent, wider than it is tall.
     */
    public static void shimmer(final View glint) {
        glint.post(() -> {
            final View parent = (View) glint.getParent();
            if (parent == null || parent.getWidth() == 0) {
                return;
            }
            final float from = -glint.getWidth();
            final float to = parent.getWidth();
            glint.setTranslationX(from);
            glint.setVisibility(View.VISIBLE);

            final ObjectAnimator sweep = ObjectAnimator.ofFloat(glint, View.TRANSLATION_X, from, to);
            sweep.setDuration(SHIMMER_TRAVEL_MS);
            sweep.setInterpolator(new AccelerateDecelerateInterpolator());
            sweep.setStartDelay(SHIMMER_REST_MS);
            sweep.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (glint.isAttachedToWindow()) {
                        sweep.setStartDelay(SHIMMER_REST_MS);
                        sweep.start();
                    }
                }
            });
            cancelWhenDetached(glint, sweep);
            sweep.start();
        });
    }

    /**
     * A breath, not a pulse: just enough scale for the eye to notice the button
     * is the live one, far too little to read as an animation.
     */
    public static void breathe(final View view) {
        ValueAnimator breath = ValueAnimator.ofFloat(1F, BREATH_SCALE);
        breath.setDuration(BREATH_MS);
        breath.setRepeatCount(ValueAnimator.INFINITE);
        breath.setRepeatMode(ValueAnimator.REVERSE);
        breath.setInterpolator(new AccelerateDecelerateInterpolator());
        breath.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
        });
        cancelWhenDetached(view, breath);
        breath.start();
    }

    /** Settles a view in from slightly above, after a delay. */
    public static void settleIn(View view, long delayMs) {
        view.setAlpha(0F);
        view.setTranslationY(-12F);
        view.animate()
                .alpha(1F)
                .translationY(0F)
                .setStartDelay(delayMs)
                .setDuration(420L)
                .setInterpolator(new DecelerateInterpolator(1.6F))
                .start();
    }

    private static void cancelWhenDetached(View view, final Animator animator) {
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                animator.removeAllListeners();
                animator.cancel();
            }
        });
    }
}
