package games.mrlaki5.backgammon.Util;

import java.util.Calendar;

public final class DateUtil {
    private DateUtil() {}

    /** Returns a unique day identifier: YEAR*1000 + DAY_OF_YEAR */
    public static int getDayOfYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR);
    }

    /** Returns a unique week identifier: YEAR*100 + WEEK_OF_YEAR */
    public static int getWeekOfYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.WEEK_OF_YEAR);
    }
}
