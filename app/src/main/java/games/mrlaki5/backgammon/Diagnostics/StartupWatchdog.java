package games.mrlaki5.backgammon.Diagnostics;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Debug aid for "app isn't responding" reports: pings the main thread once a second and, when a
 * ping goes unanswered for too long, logs the main thread's stack trace. The blocking call is then
 * named directly in logcat (tag {@value #TAG}) instead of having to be guessed.
 */
public final class StartupWatchdog {

    public static final String TAG = "ANR-WATCHDOG";

    private static final long PING_INTERVAL_MS = 1000L;
    private static final long STALL_THRESHOLD_MS = 4000L;

    private static Thread watcher;

    private StartupWatchdog() {}

    /** Starts one watchdog thread; further calls do nothing. */
    public static synchronized void start() {
        if (watcher != null) {
            return;
        }
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final AtomicLong lastReply = new AtomicLong(System.currentTimeMillis());
        final AtomicLong lastReport = new AtomicLong(0L);

        watcher = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                mainHandler.post(() -> lastReply.set(System.currentTimeMillis()));
                try {
                    Thread.sleep(PING_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                long stalledFor = System.currentTimeMillis() - lastReply.get();
                if (stalledFor < STALL_THRESHOLD_MS) {
                    continue;
                }
                // Report each stall once, then wait for the main thread to answer again
                if (lastReply.get() == lastReport.get()) {
                    continue;
                }
                lastReport.set(lastReply.get());
                Log.e(TAG, "Main thread blocked for " + stalledFor + "ms; stack trace:");
                for (StackTraceElement frame : Looper.getMainLooper().getThread().getStackTrace()) {
                    Log.e(TAG, "    at " + frame);
                }
            }
        }, "startup-watchdog");
        watcher.setDaemon(true);
        watcher.start();
    }
}
