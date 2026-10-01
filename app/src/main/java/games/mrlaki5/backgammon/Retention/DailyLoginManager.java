package games.mrlaki5.backgammon.Retention;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;

public class DailyLoginManager {

    private static final String PREFS        = "daily_login_prefs";
    private static final String KEY_LAST_DAY = "last_login_day_of_year";
    private static final String KEY_LAST_YEAR= "last_login_year";
    private static final String KEY_STREAK   = "login_streak";

    public static class LoginResult {
        public final boolean isNewDay;
        public final int streak;       // 1-indexed (1 = first day)
        public final int coinsEarned;
        public LoginResult(boolean isNewDay, int streak, int coinsEarned) {
            this.isNewDay = isNewDay;
            this.streak = streak;
            this.coinsEarned = coinsEarned;
        }
    }

    /** Call once from MenuActivity.onResume(). Returns LoginResult. */
    public static LoginResult checkDailyLogin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        java.util.Calendar now = java.util.Calendar.getInstance();
        int todayDay  = now.get(java.util.Calendar.DAY_OF_YEAR);
        int todayYear = now.get(java.util.Calendar.YEAR);

        int lastDay  = prefs.getInt(KEY_LAST_DAY,  -1);
        int lastYear = prefs.getInt(KEY_LAST_YEAR, -1);
        int streak   = prefs.getInt(KEY_STREAK,     0);

        boolean sameDay  = (lastDay == todayDay  && lastYear == todayYear);
        boolean prevDay  = isPreviousDay(lastDay, lastYear, todayDay, todayYear);

        if (sameDay) {
            return new LoginResult(false, streak, 0);
        }

        // New day
        streak = prevDay ? Math.min(streak + 1, 7) : 1; // reset if gap > 1 day
        int bonusIdx = Math.max(0, Math.min(streak - 1, CoinConfig.DAILY_LOGIN_BONUS.length - 1));
        int coins = CoinConfig.DAILY_LOGIN_BONUS[bonusIdx];

        prefs.edit()
            .putInt(KEY_LAST_DAY,  todayDay)
            .putInt(KEY_LAST_YEAR, todayYear)
            .putInt(KEY_STREAK,    streak)
            .apply();

        CoinManager cm = new CoinManager(context);
        cm.earn(coins, "daily_login_day_" + streak);

        return new LoginResult(true, streak, coins);
    }

    private static boolean isPreviousDay(int lastDay, int lastYear, int todayDay, int todayYear) {
        if (lastYear == -1) return false;
        if (todayYear == lastYear) {
            return todayDay == lastDay + 1;
        }
        // Year rollover: Dec 31 → Jan 1
        if (todayYear == lastYear + 1 && todayDay == 1) {
            java.util.Calendar prev = java.util.Calendar.getInstance();
            prev.set(java.util.Calendar.YEAR, lastYear);
            int daysInLastYear = prev.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
            return lastDay == daysInLastYear;
        }
        return false;
    }
}
