package games.mrlaki5.backgammon.Diagnostics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Cheap online check. The game is offline-first: unless the system reports a network that
 * actually reaches the internet, the ad and analytics SDKs are not started at all, so they
 * cannot stall the app waiting for a server.
 *
 * "Connected" is not enough. On a filtered or throttled connection the device is connected
 * while no request completes, which is precisely the case that used to hang startup. From
 * API 23 the platform's own validation probe answers that question, and it is the answer
 * this method reports.
 */
public final class Connectivity {

    private Connectivity() {}

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager manager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = manager.getActiveNetwork();
                if (network == null) {
                    return false;
                }
                NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Throwable t) {
            // Never let a connectivity check break the game
            return false;
        }
    }
}
