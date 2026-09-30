package games.mrlaki5.backgammon.GameControllers;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drop-in replacement for the deprecated GameTask (AsyncTask).
 * Uses a single-thread ExecutorService for AI computation and
 * a main-thread Handler for UI callbacks. All shared flags are AtomicBoolean.
 */
public class GameTaskExecutor {

    public interface GameTaskCallback {
        /** Called on main thread before background work starts. */
        void onPreExecute();
        /** Called on background thread. Return the computed result. */
        Object doInBackground();
        /** Called on main thread with the result from doInBackground(). */
        void onPostExecute(Object result);
        /** Called on main thread if cancel() was called before completion. */
        void onCancelled();
    }

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "GameAI-Thread");
                t.setDaemon(true);
                return t;
            });

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Future<?> future;

    public void execute(GameTaskCallback callback) {
        cancelled.set(false);
        // onPreExecute runs on main thread synchronously before submission
        mainHandler.post(callback::onPreExecute);
        future = executor.submit(() -> {
            Object result = null;
            try {
                result = callback.doInBackground();
            } catch (Exception e) {
                // swallow — treat as cancellation
                cancelled.set(true);
            }
            final Object finalResult = result;
            final boolean wasCancelled = cancelled.get();
            mainHandler.post(() -> {
                if (wasCancelled) {
                    callback.onCancelled();
                } else {
                    callback.onPostExecute(finalResult);
                }
            });
        });
    }

    public void cancel() {
        cancelled.set(true);
        if (future != null) {
            future.cancel(true);
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }
}
