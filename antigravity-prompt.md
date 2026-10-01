# Royal Backgammon — Full Implementation Prompt for Antigravity

> **Project**: Royal Backgammon (Android, Java + Kotlin)
> **Package**: `games.mrlaki5.backgammon` (app) + `com.royalbackgammon.core` (game-core module)
> **Build**: Gradle, compileSdk 35, minSdk 21, Java 17
> **Ad SDK**: Tapsell Plus 2.2.4 (Iranian ad network)
> **Stores**: Bazaar, Myket, FOSS (3 product flavors)

---

## CONTEXT — READ BEFORE DOING ANYTHING

This is a backgammon game for Iranian app stores. The codebase has a clean `game-core` Kotlin module (rules, engine, bot AI, ELO) with zero Android dependency — **do not touch game-core unless explicitly told**. The Android layer (`app` module) has significant technical debt. You are implementing a 5-phase overhaul. Execute each phase in order. Do NOT skip phases. Commit after each phase.

### Existing Architecture (do not break)
- `game-core/` — Pure Kotlin: `BackgammonRules`, `GameEngine`, `MoveExecutor`, `BotStrategy`, `EloRating`, `DiceRoller`. Has unit tests. Leave it alone.
- `app/.../Monetization/ads/` — `AdProvider` interface → `TapsellAdProvider` / `StubAdProvider`. `AdManager` handles frequency. `AdConfig` has zone IDs.
- `app/.../Monetization/iap/` — `BillingProvider` interface → `StubBillingProvider`. `PremiumManager` manages ad-removal + theme purchases. `Product` has SKU constants.
- `app/.../Retention/` — `DailyChallenge`, `AchievementManager`, `ReviewPromptManager`.
- `app/.../WinStreakTracker.java`
- `app/.../GameControllers/GameActivity.java` (1,492 lines — God class)
- `app/.../GameControllers/GameTask.java` (extends AsyncTask — deprecated)
- `app/.../GameView/OnBoardImage.java` (1,477 lines — God class)
- `app/.../Analytics/GameAnalytics.java` — singleton facade, use `GameAnalytics.get().trackXxx()` for all new events.

### Key Conventions
- All persistence uses SharedPreferences (separate prefs file per system).
- All analytics go through `GameAnalytics.get()`.
- Ad placement rules are documented in `AdPlacementPolicy.java` — update it when adding placements.
- Strings go in `res/values/strings.xml` (English) AND `res/values-fa/strings.xml` (Farsi). Always add both.
- The app has 4 board themes: Royal (free, index 0), Pop Art (1), Cyberpunk (2), Persian Luxury (3).
- Bot difficulties: Easy (0), Medium (1), Hard (2), Royal (3).

---

## PHASE 1 — Repository Cleanup & Stability (do first)

### Task 1.1: Clean git repository

1. Remove all JVM crash dumps from tracking:
```bash
git rm hs_err_pid12692.log hs_err_pid19196.log hs_err_pid23936.log hs_err_pid24556.log hs_err_pid26232.log hs_err_pid26368.log hs_err_pid7144.log
git rm replay_pid24556.log replay_pid7144.log
```

2. Add to `.gitignore` (append after existing entries):
```
hs_err_pid*
replay_pid*
```

3. Commit: `chore: remove JVM crash dumps from repository and gitignore them`

### Task 1.2: Move hardcoded keys to BuildConfig

1. In `app/build.gradle`, inside `defaultConfig {}`, add:
```groovy
buildConfigField "String", "TAPSELL_APP_KEY", "\"icqpgedfgrrbfflttiglpqbfneimtpltlnbodeaginjtfcreqkthhblofgmcegogkgpeij\""
buildConfigField "String", "TAPSELL_ZONE_INTERSTITIAL", "\"6a8b35a0f34d73758477ec0a\""
buildConfigField "String", "TAPSELL_ZONE_REWARDED", "\"6a8dddf3488ef01a725b3afd\""
```

2. In `BackgammonApp.java`, replace the hardcoded string:
```java
TapsellPlus.initialize(this, BuildConfig.TAPSELL_APP_KEY);
```

3. In `AdConfig.java`, replace:
```java
public static final String ZONE_INTERSTITIAL = BuildConfig.TAPSELL_ZONE_INTERSTITIAL;
public static final String ZONE_REWARDED = BuildConfig.TAPSELL_ZONE_REWARDED;
```

4. Import `BuildConfig` where needed.

### Task 1.3: Remove duplicated constant

In `app/.../Monetization/ads/AdPlacementPolicy.java`, remove:
```java
public static final int INTERSTITIAL_EVERY_N_GAMES = 3;
```
Replace any reference to `AdPlacementPolicy.INTERSTITIAL_EVERY_N_GAMES` with `AdConfig.INTERSTITIAL_EVERY_N_GAMES`. Search the entire project for usages.

### Task 1.4: Extract shared DateUtil

Create `app/src/main/java/games/mrlaki5/backgammon/Util/DateUtil.java`:
```java
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
```

Replace `getDayOfYear()` in both `AchievementManager.java` (line ~157) and `DailyChallenge.java` (line ~150) with `DateUtil.getDayOfYear()`. Delete their private `getDayOfYear()` methods.

### Task 1.5: Migrate GameTask from AsyncTask

This is the most critical change. The file is `app/.../GameControllers/GameTask.java` (234 lines).

**Current state**: Extends `AsyncTask<Void, Void, Void>`. Has int flags `WorkFlag`, `FinishedFlag`, `EndRoutineStarted` without volatile — race condition risk. Uses `Thread.sleep()` for delays.

**Target**: Replace with an `ExecutorService`-based approach (NOT coroutines yet — the rest of the app is Java and would need too many changes). Keep the same public API that `GameActivity` calls.

Create new `GameTask.java` that:
1. Uses `Executors.newSingleThreadExecutor()` instead of AsyncTask.
2. Replaces `int` flags with `AtomicInteger` (or `volatile boolean`).
3. Replaces `Thread.sleep()` with same pattern but wrapped in try-catch that properly checks the interrupt flag.
4. Keeps the exact same 4-state FSM logic (states 0, 1, 2, 3).
5. Keeps all calls to `gameActivity.onGameFinished()`, `gameActivity.showTurnSwitchAndWait()`, `gameActivity.isTutorialMode()`, `gameActivity.isPassAndPlayMode()`, `gameActivity.playGameFinishedEffect()`.
6. The `synchronized` block around `EndRoutineStarted` must use proper `AtomicInteger.compareAndSet(0, 1)`.
7. Add a `shutdown()` method that calls `executor.shutdownNow()`.

**Critical**: `GameActivity` calls `getWorkFlag()`, `setWorkFlag()`, `getFinishedFlag()`, `getEndRoutineStarted()`, `setEndRoutineStarted()`. Keep these getters/setters but make them thread-safe. Search `GameActivity.java` for every reference to `GameTask` and ensure compatibility.

Commit: `refactor: migrate GameTask from AsyncTask to ExecutorService with thread-safe flags`

---

## PHASE 2 — Coin Economy & Reward System

### Task 2.1: Create CoinManager

Create `app/src/main/java/games/mrlaki5/backgammon/Economy/CoinManager.java`:

```java
package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;

/**
 * Manages the in-game coin economy (Gold Coins).
 * Coins are EARNED through gameplay only — never purchased with real money.
 * Persistence: SharedPreferences. No backend needed.
 */
public class CoinManager {

    private static final String PREFS_NAME = "coin_economy_prefs";
    private static final String KEY_BALANCE = "coin_balance";
    private static final String KEY_TOTAL_EARNED = "total_coins_earned";
    private static final String KEY_TOTAL_SPENT = "total_coins_spent";

    private final SharedPreferences prefs;

    public CoinManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getBalance() {
        return prefs.getInt(KEY_BALANCE, 0);
    }

    /**
     * Awards coins to the player.
     * @param amount positive number of coins
     * @param source identifier for analytics (e.g., "game_win", "daily_challenge", "rewarded_ad")
     * @return new balance
     */
    public int earn(int amount, String source) {
        if (amount <= 0) return getBalance();
        int newBalance = getBalance() + amount;
        int totalEarned = prefs.getInt(KEY_TOTAL_EARNED, 0) + amount;
        prefs.edit()
                .putInt(KEY_BALANCE, newBalance)
                .putInt(KEY_TOTAL_EARNED, totalEarned)
                .apply();
        GameAnalytics.get().trackCoinEarned(amount, source, newBalance);
        return newBalance;
    }

    /**
     * Spends coins. Returns true if successful, false if insufficient balance.
     * @param amount positive number of coins to spend
     * @param item identifier for analytics (e.g., "hint", "undo", "theme_rental")
     */
    public boolean spend(int amount, String item) {
        if (amount <= 0) return false;
        int balance = getBalance();
        if (balance < amount) return false;
        int newBalance = balance - amount;
        int totalSpent = prefs.getInt(KEY_TOTAL_SPENT, 0) + amount;
        prefs.edit()
                .putInt(KEY_BALANCE, newBalance)
                .putInt(KEY_TOTAL_SPENT, totalSpent)
                .apply();
        GameAnalytics.get().trackCoinSpent(amount, item, newBalance);
        return true;
    }

    public boolean canAfford(int amount) {
        return getBalance() >= amount;
    }

    public int getTotalEarned() {
        return prefs.getInt(KEY_TOTAL_EARNED, 0);
    }

    public int getTotalSpent() {
        return prefs.getInt(KEY_TOTAL_SPENT, 0);
    }
}
```

### Task 2.2: Create CoinConfig

Create `app/src/main/java/games/mrlaki5/backgammon/Economy/CoinConfig.java`:

```java
package games.mrlaki5.backgammon.Economy;

/**
 * All coin economy constants. Change values here to rebalance.
 */
public final class CoinConfig {
    private CoinConfig() {}

    // === Earning ===
    public static final int WIN_BASE = 10;
    public static final int WIN_BONUS_MEDIUM = 5;
    public static final int WIN_BONUS_HARD = 10;
    public static final int WIN_BONUS_ROYAL = 20;
    public static final int DAILY_CHALLENGE_COMPLETE = 25;
    public static final int STREAK_MILESTONE_EVERY = 5;    // every N consecutive wins
    public static final int STREAK_MILESTONE_REWARD = 15;
    public static final int ACHIEVEMENT_UNLOCK = 50;
    public static final int ACHIEVEMENT_UNLOCK_MAJOR = 100; // games_100, beat_royal
    public static final int FIRST_GAME_OF_DAY = 5;
    public static final int REWARDED_AD_WATCH = 15;
    public static final int DAILY_CHALLENGE_BONUS_CHEST = 25;  // on top of base 25
    public static final int DOUBLE_REWARD_MULTIPLIER = 2;

    // === Spending ===
    public static final int HINT_COST = 20;
    public static final int UNDO_COST = 30;
    public static final int THEME_RENTAL_COST = 50;       // 24-hour rental
    public static final int AVATAR_FRAME_COST = 100;
    public static final int DICE_SKIN_COST = 150;
    public static final int PROFILE_TITLE_COST = 200;

    // === Daily Login Bonus (7-day cycle) ===
    public static final int[] DAILY_LOGIN_REWARDS = {5, 10, 15, 20, 25, 30, 50};
}
```

### Task 2.3: Create DailyLoginManager

Create `app/src/main/java/games/mrlaki5/backgammon/Economy/DailyLoginManager.java`:

```java
package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Util.DateUtil;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;

/**
 * 7-day daily login bonus cycle.
 * Day 1: 5 coins → Day 7: 50 coins (star day).
 * Missing a day resets to Day 1.
 */
public class DailyLoginManager {

    private static final String PREFS_NAME = "daily_login_prefs";
    private static final String KEY_LAST_LOGIN_DAY = "last_login_day";
    private static final String KEY_CONSECUTIVE_DAYS = "consecutive_days";
    private static final String KEY_CLAIMED_TODAY = "claimed_today";

    private final SharedPreferences prefs;

    public DailyLoginManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Call on app open. Returns the reward amount if unclaimed today, or 0 if already claimed.
     */
    public int checkAndGetReward() {
        int today = DateUtil.getDayOfYear();
        int lastLogin = prefs.getInt(KEY_LAST_LOGIN_DAY, -1);
        boolean claimedToday = prefs.getBoolean(KEY_CLAIMED_TODAY, false);

        if (today == lastLogin && claimedToday) {
            return 0; // Already claimed
        }

        int consecutive = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0);

        if (today != lastLogin) {
            // Is it the next consecutive day? (allowing 1-day tolerance via day diff)
            if (lastLogin > 0 && (today - lastLogin) == 1) {
                consecutive++;
            } else if (lastLogin > 0) {
                consecutive = 0; // Streak broken
            }

            // Cycle resets after 7 days
            if (consecutive >= CoinConfig.DAILY_LOGIN_REWARDS.length) {
                consecutive = 0;
            }

            prefs.edit()
                    .putInt(KEY_LAST_LOGIN_DAY, today)
                    .putInt(KEY_CONSECUTIVE_DAYS, consecutive)
                    .putBoolean(KEY_CLAIMED_TODAY, false)
                    .apply();
        }

        return CoinConfig.DAILY_LOGIN_REWARDS[consecutive];
    }

    /**
     * Marks today's reward as claimed. Call after showing the reward animation.
     */
    public void claimReward() {
        prefs.edit().putBoolean(KEY_CLAIMED_TODAY, true).apply();
    }

    /** Returns current day index in the 7-day cycle (0-6). */
    public int getCurrentDay() {
        return prefs.getInt(KEY_CONSECUTIVE_DAYS, 0);
    }

    /** Returns true if today's reward has been claimed. */
    public boolean isClaimedToday() {
        int today = DateUtil.getDayOfYear();
        int lastLogin = prefs.getInt(KEY_LAST_LOGIN_DAY, -1);
        return today == lastLogin && prefs.getBoolean(KEY_CLAIMED_TODAY, false);
    }
}
```

### Task 2.4: Create WeeklyChallenge

Create `app/src/main/java/games/mrlaki5/backgammon/Retention/WeeklyChallenge.java`:

```java
package games.mrlaki5.backgammon.Retention;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Util.DateUtil;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;

/**
 * Weekly challenge system. Three concurrent challenges per week.
 * Resets every Monday (start of ISO week).
 *
 * Challenges:
 * 1. "Player of the Week" — Complete all 7 daily challenges
 * 2. "Weekly Warrior" — Win 15 games
 * 3. "Endurance" — Complete 20 games without quitting
 */
public class WeeklyChallenge {

    private static final String PREFS_NAME = "weekly_challenge_prefs";
    private static final String KEY_LAST_WEEK = "last_week";
    private static final String KEY_DAILY_CHALLENGES_DONE = "daily_done";
    private static final String KEY_WINS = "weekly_wins";
    private static final String KEY_GAMES_NO_QUIT = "weekly_games_no_quit";
    private static final String KEY_CHALLENGE_1_CLAIMED = "c1_claimed";
    private static final String KEY_CHALLENGE_2_CLAIMED = "c2_claimed";
    private static final String KEY_CHALLENGE_3_CLAIMED = "c3_claimed";

    public static final int DAILY_TARGET = 7;
    public static final int WINS_TARGET = 15;
    public static final int ENDURANCE_TARGET = 20;
    public static final int REWARD_PLAYER_OF_WEEK = 100;
    public static final int REWARD_WARRIOR = 75;
    public static final int REWARD_ENDURANCE = 75;

    private final SharedPreferences prefs;

    public WeeklyChallenge(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        refreshIfNewWeek();
    }

    private void refreshIfNewWeek() {
        int currentWeek = DateUtil.getWeekOfYear();
        int lastWeek = prefs.getInt(KEY_LAST_WEEK, -1);
        if (currentWeek != lastWeek) {
            prefs.edit()
                    .putInt(KEY_LAST_WEEK, currentWeek)
                    .putInt(KEY_DAILY_CHALLENGES_DONE, 0)
                    .putInt(KEY_WINS, 0)
                    .putInt(KEY_GAMES_NO_QUIT, 0)
                    .putBoolean(KEY_CHALLENGE_1_CLAIMED, false)
                    .putBoolean(KEY_CHALLENGE_2_CLAIMED, false)
                    .putBoolean(KEY_CHALLENGE_3_CLAIMED, false)
                    .apply();
        }
    }

    public void onDailyChallengeCompleted() {
        int val = prefs.getInt(KEY_DAILY_CHALLENGES_DONE, 0) + 1;
        prefs.edit().putInt(KEY_DAILY_CHALLENGES_DONE, val).apply();
    }

    public void onGameCompleted(boolean won) {
        SharedPreferences.Editor editor = prefs.edit();
        if (won) {
            editor.putInt(KEY_WINS, prefs.getInt(KEY_WINS, 0) + 1);
        }
        editor.putInt(KEY_GAMES_NO_QUIT, prefs.getInt(KEY_GAMES_NO_QUIT, 0) + 1);
        editor.apply();
    }

    public void onGameAbandoned() {
        prefs.edit().putInt(KEY_GAMES_NO_QUIT, 0).apply();
    }

    // --- Progress getters ---
    public int getDailyChallengesDone() { return prefs.getInt(KEY_DAILY_CHALLENGES_DONE, 0); }
    public int getWeeklyWins() { return prefs.getInt(KEY_WINS, 0); }
    public int getGamesNoQuit() { return prefs.getInt(KEY_GAMES_NO_QUIT, 0); }

    public boolean isChallenge1Complete() { return getDailyChallengesDone() >= DAILY_TARGET; }
    public boolean isChallenge2Complete() { return getWeeklyWins() >= WINS_TARGET; }
    public boolean isChallenge3Complete() { return getGamesNoQuit() >= ENDURANCE_TARGET; }

    public boolean isChallenge1Claimed() { return prefs.getBoolean(KEY_CHALLENGE_1_CLAIMED, false); }
    public boolean isChallenge2Claimed() { return prefs.getBoolean(KEY_CHALLENGE_2_CLAIMED, false); }
    public boolean isChallenge3Claimed() { return prefs.getBoolean(KEY_CHALLENGE_3_CLAIMED, false); }

    public void claimChallenge1() { prefs.edit().putBoolean(KEY_CHALLENGE_1_CLAIMED, true).apply(); }
    public void claimChallenge2() { prefs.edit().putBoolean(KEY_CHALLENGE_2_CLAIMED, true).apply(); }
    public void claimChallenge3() { prefs.edit().putBoolean(KEY_CHALLENGE_3_CLAIMED, true).apply(); }
}
```

### Task 2.5: Add analytics methods to GameAnalytics

Open `app/.../Analytics/GameAnalytics.java`. Add these tracking methods (follow the existing pattern of the class — they should delegate to the underlying AnalyticsProvider):

```java
// Coin economy
public void trackCoinEarned(int amount, String source, int newBalance) { ... }
public void trackCoinSpent(int amount, String item, int newBalance) { ... }
public void trackDailyLoginClaimed(int day, int amount) { ... }
public void trackWeeklyChallengeCompleted(String challengeId) { ... }
public void trackRewardedAdWatched(String placement) { ... }
public void trackStreakSaved() { ... }
public void trackDoubleRewardClaimed() { ... }
```

Each should call `provider.logEvent(eventName, params)` with appropriate parameters. Check how existing methods like `trackAchievementUnlocked` and `trackMissionCompleted` are implemented and follow the same pattern.

### Task 2.6: Wire CoinManager into existing game flow

In `GameActivity.java`, in the `onGameFinished()` method (or wherever game completion is handled):

1. Instantiate or get `CoinManager` (make it a field, initialized in `onCreate`).
2. After a WIN:
   ```java
   int coins = CoinConfig.WIN_BASE;
   // difficulty is the AI difficulty index (0-3)
   if (difficulty == 1) coins += CoinConfig.WIN_BONUS_MEDIUM;
   else if (difficulty == 2) coins += CoinConfig.WIN_BONUS_HARD;
   else if (difficulty == 3) coins += CoinConfig.WIN_BONUS_ROYAL;
   int newBalance = coinManager.earn(coins, "game_win");
   ```
3. Check first game of day:
   ```java
   int today = DateUtil.getDayOfYear();
   if (today != prefs.getInt("last_game_day", -1)) {
       coinManager.earn(CoinConfig.FIRST_GAME_OF_DAY, "first_game_of_day");
       prefs.edit().putInt("last_game_day", today).apply();
   }
   ```
4. On streak milestones (check after `winStreakTracker.recordWin()`):
   ```java
   int streak = winStreakTracker.getCurrentStreak();
   if (streak > 0 && streak % CoinConfig.STREAK_MILESTONE_EVERY == 0) {
       coinManager.earn(CoinConfig.STREAK_MILESTONE_REWARD, "streak_milestone");
   }
   ```

### Task 2.7: Wire coins into AchievementManager

In `AchievementManager.java`, modify the `unlock()` method to accept a `CoinManager`:

```java
private void unlock(String achievementId, CoinManager coinManager) {
    if (!isUnlocked(achievementId)) {
        prefs.edit().putBoolean("unlocked_" + achievementId, true).apply();
        GameAnalytics.get().trackAchievementUnlocked(achievementId);
        if (coinManager != null) {
            boolean isMajor = "games_100".equals(achievementId) || "beat_royal".equals(achievementId);
            int reward = isMajor ? CoinConfig.ACHIEVEMENT_UNLOCK_MAJOR : CoinConfig.ACHIEVEMENT_UNLOCK;
            coinManager.earn(reward, "achievement_" + achievementId);
        }
    }
}
```

Update all calls to `unlock()` to pass the CoinManager. Add `CoinManager` as a constructor parameter or add a setter.

### Task 2.8: Wire coins into DailyChallenge

In `DailyChallenge.java`, in `onGameCompleted()`, where challenge completion is detected (the block that sets `KEY_CHALLENGE_COMPLETED` to true), add:

```java
if (coinManager != null) {
    coinManager.earn(CoinConfig.DAILY_CHALLENGE_COMPLETE, "daily_challenge");
}
```

Add CoinManager as a field with a setter, same pattern as AchievementManager.

### Task 2.9: Add coin-related strings

Add to `res/values/strings.xml`:
```xml
<!-- Coin Economy -->
<string name="coins">Coins</string>
<string name="coin_balance">%1$d coins</string>
<string name="coin_earned">+%1$d coins</string>
<string name="coin_spent">-%1$d coins</string>
<string name="daily_login_title">Daily Bonus</string>
<string name="daily_login_claim">Claim</string>
<string name="daily_login_claimed">Claimed!</string>
<string name="daily_login_day">Day %1$d</string>
<string name="free_coins">Free Coins</string>
<string name="watch_ad_for_coins">Watch video for %1$d coins</string>
<string name="double_reward">Double Coins</string>
<string name="double_reward_desc">Watch a video to double your reward!</string>
<string name="save_streak">Save Streak</string>
<string name="save_streak_desc">Watch a video to keep your %1$d win streak!</string>
<string name="bonus_chest">Bonus Chest</string>
<string name="bonus_chest_desc">Watch a video for %1$d bonus coins!</string>
<string name="hint_choice_title">Get Hint</string>
<string name="hint_use_coins">Use %1$d coins</string>
<string name="hint_watch_video">Watch video (free)</string>
<string name="insufficient_coins">Not enough coins</string>
<string name="weekly_challenge">Weekly Challenge</string>
<string name="weekly_player">Player of the Week</string>
<string name="weekly_warrior">Weekly Warrior</string>
<string name="weekly_endurance">Endurance</string>
<string name="weekly_progress">%1$d / %2$d</string>
<string name="undo_move">Undo Move</string>
<string name="coin_shop">Shop</string>
<string name="avatar_frames">Avatar Frames</string>
<string name="dice_skins">Dice Skins</string>
<string name="profile_titles">Titles</string>
<string name="theme_rental">Rent Theme (24h)</string>
```

Add the Farsi translations to `res/values-fa/strings.xml`:
```xml
<!-- Coin Economy -->
<string name="coins">سکه</string>
<string name="coin_balance">%1$d سکه</string>
<string name="coin_earned">+%1$d سکه</string>
<string name="coin_spent">-%1$d سکه</string>
<string name="daily_login_title">پاداش روزانه</string>
<string name="daily_login_claim">دریافت</string>
<string name="daily_login_claimed">دریافت شد!</string>
<string name="daily_login_day">روز %1$d</string>
<string name="free_coins">سکه رایگان</string>
<string name="watch_ad_for_coins">تماشای ویدئو برای %1$d سکه</string>
<string name="double_reward">سکه دوبرابر</string>
<string name="double_reward_desc">تماشای ویدئو برای دوبرابر شدن پاداش!</string>
<string name="save_streak">حفظ رکورد</string>
<string name="save_streak_desc">تماشای ویدئو برای حفظ رکورد %1$d بردت!</string>
<string name="bonus_chest">صندوق بونوس</string>
<string name="bonus_chest_desc">تماشای ویدئو برای %1$d سکه بونوس!</string>
<string name="hint_choice_title">راهنمایی</string>
<string name="hint_use_coins">استفاده از %1$d سکه</string>
<string name="hint_watch_video">تماشای ویدئو (رایگان)</string>
<string name="insufficient_coins">سکه کافی نیست</string>
<string name="weekly_challenge">چالش هفتگی</string>
<string name="weekly_player">بازیکن هفته</string>
<string name="weekly_warrior">جنگجوی هفته</string>
<string name="weekly_endurance">استقامت</string>
<string name="weekly_progress">%1$d / %2$d</string>
<string name="undo_move">برگشت حرکت</string>
<string name="coin_shop">فروشگاه</string>
<string name="avatar_frames">فریم آواتار</string>
<string name="dice_skins">طرح تاس</string>
<string name="profile_titles">عنوان‌ها</string>
<string name="theme_rental">اجاره تم (۲۴ ساعت)</string>
```

Commit: `feat: add coin economy system with CoinManager, DailyLoginManager, WeeklyChallenge`

---

## PHASE 3 — Expanded Rewarded Ads

### Task 3.1: Create RewardedAdPlacement enum

Create `app/src/main/java/games/mrlaki5/backgammon/Monetization/ads/RewardedAdPlacement.java`:

```java
package games.mrlaki5.backgammon.Monetization.ads;

/**
 * All rewarded ad placement points in the app.
 * Each has its own frequency cap and cooldown.
 */
public enum RewardedAdPlacement {
    HINT("hint", 3, 0, 0),                    // 3 per game, no daily cap, no cooldown
    FREE_COINS("free_coins", 0, 3, 15 * 60),  // 3 per day, 15 min cooldown (seconds)
    DOUBLE_REWARD("double_reward", 0, 0, 0),   // no cap (naturally limited to wins)
    SAVE_STREAK("save_streak", 0, 1, 0),       // 1 per day
    BONUS_CHEST("bonus_chest", 0, 1, 0);       // 1 per day (1 daily challenge)

    public final String id;
    public final int maxPerGame;       // 0 = unlimited within game
    public final int maxPerDay;        // 0 = unlimited per day
    public final int cooldownSeconds;  // 0 = no cooldown

    RewardedAdPlacement(String id, int maxPerGame, int maxPerDay, int cooldownSeconds) {
        this.id = id;
        this.maxPerGame = maxPerGame;
        this.maxPerDay = maxPerDay;
        this.cooldownSeconds = cooldownSeconds;
    }
}
```

### Task 3.2: Create RewardedAdTracker

Create `app/src/main/java/games/mrlaki5/backgammon/Monetization/ads/RewardedAdTracker.java`:

```java
package games.mrlaki5.backgammon.Monetization.ads;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Util.DateUtil;

/**
 * Tracks rewarded ad usage per placement to enforce frequency caps.
 * Global limit: max 5 rewarded ads per day across ALL placements.
 * Global cooldown: 60 seconds between any two rewarded ads.
 */
public class RewardedAdTracker {

    private static final String PREFS_NAME = "rewarded_ad_tracker_prefs";
    private static final String KEY_TOTAL_TODAY = "total_today";
    private static final String KEY_LAST_AD_TIME = "last_ad_time";
    private static final String KEY_LAST_DAY = "last_day";
    private static final int MAX_TOTAL_PER_DAY = 5;
    private static final int GLOBAL_COOLDOWN_MS = 60_000;

    private final SharedPreferences prefs;

    public RewardedAdTracker(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        refreshIfNewDay();
    }

    private void refreshIfNewDay() {
        int today = DateUtil.getDayOfYear();
        if (today != prefs.getInt(KEY_LAST_DAY, -1)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_LAST_DAY, today);
            editor.putInt(KEY_TOTAL_TODAY, 0);
            for (RewardedAdPlacement p : RewardedAdPlacement.values()) {
                editor.putInt("count_" + p.id, 0);
            }
            editor.apply();
        }
    }

    /**
     * Returns true if the given placement can show a rewarded ad right now.
     */
    public boolean canShow(RewardedAdPlacement placement) {
        refreshIfNewDay();

        // Global daily limit
        if (prefs.getInt(KEY_TOTAL_TODAY, 0) >= MAX_TOTAL_PER_DAY) return false;

        // Global cooldown
        long lastTime = prefs.getLong(KEY_LAST_AD_TIME, 0);
        if (System.currentTimeMillis() - lastTime < GLOBAL_COOLDOWN_MS) return false;

        // Per-placement daily limit
        if (placement.maxPerDay > 0) {
            int count = prefs.getInt("count_" + placement.id, 0);
            if (count >= placement.maxPerDay) return false;
        }

        // Per-placement cooldown
        if (placement.cooldownSeconds > 0) {
            long lastPlacement = prefs.getLong("last_" + placement.id, 0);
            if (System.currentTimeMillis() - lastPlacement < placement.cooldownSeconds * 1000L) return false;
        }

        return true;
    }

    /**
     * Records that a rewarded ad was shown for the given placement.
     * Call AFTER the ad is successfully shown and reward granted.
     */
    public void recordShow(RewardedAdPlacement placement) {
        long now = System.currentTimeMillis();
        prefs.edit()
                .putInt(KEY_TOTAL_TODAY, prefs.getInt(KEY_TOTAL_TODAY, 0) + 1)
                .putLong(KEY_LAST_AD_TIME, now)
                .putInt("count_" + placement.id, prefs.getInt("count_" + placement.id, 0) + 1)
                .putLong("last_" + placement.id, now)
                .apply();
    }

    /** Returns how many times the placement was used today. */
    public int getUsageToday(RewardedAdPlacement placement) {
        refreshIfNewDay();
        return prefs.getInt("count_" + placement.id, 0);
    }

    /** Returns remaining global daily rewarded ads. */
    public int getRemainingToday() {
        refreshIfNewDay();
        return MAX_TOTAL_PER_DAY - prefs.getInt(KEY_TOTAL_TODAY, 0);
    }
}
```

### Task 3.3: Update AdPlacementPolicy documentation

Replace the entire content of `AdPlacementPolicy.java` with:

```java
package games.mrlaki5.backgammon.Monetization.ads;

/**
 * Documents and enforces ad placement rules — v2.0.
 *
 * INTERSTITIAL (between games):
 * ┌────────────────────────────────────────────────────────┐
 * │ • After game ends, before results screen               │
 * │ • Max frequency: 1 per N completed games (see AdConfig)│
 * │ • Never during gameplay                                │
 * │ • Disabled by "Remove Ads" IAP                         │
 * └────────────────────────────────────────────────────────┘
 *
 * REWARDED VIDEO (5 placements, all opt-in):
 * ┌────────────────────────────────────────────────────────┐
 * │ 1. HINT — during game, max 3/game, coin alternative   │
 * │ 2. FREE_COINS — main menu, 3/day, 15min cooldown      │
 * │ 3. DOUBLE_REWARD — after win, doubles coin reward      │
 * │ 4. SAVE_STREAK — after loss w/ streak≥3, 1/day        │
 * │ 5. BONUS_CHEST — after daily challenge, 1/day          │
 * ├────────────────────────────────────────────────────────┤
 * │ GLOBAL: max 5 rewarded/day, 60s cooldown between any  │
 * │ "Remove Ads" does NOT disable rewarded (user benefits) │
 * └────────────────────────────────────────────────────────┘
 *
 * FORBIDDEN:
 * ✗ Banner ads anywhere
 * ✗ Any ad mid-turn or mid-game
 * ✗ Forced ad viewing before game starts
 * ✗ Any ad that interrupts active gameplay
 *
 * If ad fails to load → fall back to coin payment, never block the feature.
 */
public final class AdPlacementPolicy {
    private AdPlacementPolicy() {}

    public static final boolean REWARDED_IS_OPT_IN = true;
    public static final boolean BANNERS_ALLOWED = false;
}
```

### Task 3.4: Wire rewarded ad placements into GameActivity

This is the integration work. In `GameActivity.java`, add fields:

```java
private RewardedAdTracker rewardedAdTracker;
private CoinManager coinManager;
```

Initialize both in `onCreate`.

**Hint button** — Modify the existing hint button click handler. Instead of directly calling `adManager.showRewardedAd()`, show a bottom sheet dialog offering two choices:
1. "Use 20 coins" → `coinManager.spend(20, "hint")` → show hint
2. "Watch video (free)" → check `rewardedAdTracker.canShow(HINT)` → `adManager.showRewardedAd()` → on reward → show hint + `rewardedAdTracker.recordShow(HINT)`

If coins insufficient AND ad not available, disable the hint button.

**Game Over dialog** — In `onGameFinished()`:
- After WIN: show "×2 Coins — Watch Video" button. On tap → `adManager.showRewardedAd()` → on reward → `coinManager.earn(coinsEarned, "double_reward")` + `rewardedAdTracker.recordShow(DOUBLE_REWARD)`.
- After LOSS with streak ≥ 3: show "Save Streak — Watch Video" button. On tap → `adManager.showRewardedAd()` → on reward → restore streak via `winStreakTracker` (add a `restoreStreak(int)` method) + `rewardedAdTracker.recordShow(SAVE_STREAK)`.
- After daily challenge complete: show "Bonus Chest" button. On tap → ad → `coinManager.earn(DAILY_CHALLENGE_BONUS_CHEST, "bonus_chest")`.

**These buttons should only appear if `rewardedAdTracker.canShow(placement)` returns true.**

### Task 3.5: Add "Free Coins" button to MenuActivity

In the main menu (`MenuActivity`), add a small button/icon for "Free Coins". On tap:
1. Check `rewardedAdTracker.canShow(FREE_COINS)`.
2. If yes → show rewarded ad → on reward → `coinManager.earn(CoinConfig.REWARDED_AD_WATCH, "free_coins")`.
3. Update the coin balance display.

Also display the current coin balance in the menu header.

### Task 3.6: Add WinStreakTracker.restoreStreak()

In `WinStreakTracker.java`, add:
```java
/**
 * Restores the streak to a specific value (used by streak-save rewarded ad).
 */
public void restoreStreak(int value) {
    prefs.edit().putInt(KEY_CURRENT_STREAK, value).apply();
}
```

Commit: `feat: add 5 rewarded ad placements with frequency tracking and coin integration`

---

## PHASE 4 — Refactor God Classes

### Task 4.1: Extract ShakeController from GameActivity

Create `app/.../GameControllers/ShakeController.java`:
- Move all `SensorManager`, `SensorEventListener`, shake detection logic, shake threshold, shake duration preferences.
- Public interface: `ShakeController(Context, OnShakeListener)`, `start()`, `stop()`.
- `OnShakeListener` callback: `onShake()`.
- In GameActivity, replace inline shake code with `ShakeController` instance.

### Task 4.2: Extract TutorialController from GameActivity

Create `app/.../GameControllers/TutorialController.java`:
- Move all tutorial-related fields: `tutorialMode`, `tutorialIntroBlocking`, `tutorialStep`, `tutorialMessages`, tutorial panel views.
- Move all tutorial methods: showing tutorial steps, advancing, loading scenarios.
- Public API: `TutorialController(GameActivity, views...)`, `isTutorialMode()`, `advance()`, `getCurrentStep()`.

### Task 4.3: Extract GameOverHandler from GameActivity

Create `app/.../GameControllers/GameOverHandler.java`:
- Move the `onGameFinished()` method and game-over dialog construction.
- Move result recording (database write), ad showing, achievement/challenge/streak updates.
- Move the rematch logic.
- Takes `CoinManager`, `AdManager`, `AchievementManager`, `DailyChallenge`, `WinStreakTracker`, `WeeklyChallenge`, `ReviewPromptManager`, `RewardedAdTracker` as constructor dependencies.

### Task 4.4: Extract PauseMenuHandler from GameActivity

Create `app/.../GameControllers/PauseMenuHandler.java`:
- Move pause dialog creation, volume controls within pause, resume/restart/quit logic.
- Public API: `show()`, `dismiss()`, `isPaused()`.

### Task 4.5: Extract PassAndPlayManager from GameActivity

Create `app/.../GameControllers/PassAndPlayManager.java`:
- Move pass-and-play turn switching overlay, `showTurnSwitchAndWait()`, player swap logic.
- Public API: `isPassAndPlayMode()`, `showTurnSwitchAndWait(String playerName, int playerNum)`.

### Task 4.6: Extract BoardTheme from OnBoardImage

Create `app/.../GameView/themes/BoardTheme.java` (interface):
```java
public interface BoardTheme {
    int getBoardColor();
    int getPointLightColor();
    int getPointDarkColor();
    int getBarColor();
    int getBorderColor();
    int getCheckerPlayer1Color();
    int getCheckerPlayer2Color();
    int getHighlightColor();
    int getTextColor();
    int getDiceColor();
    // ... all color getters currently hardcoded in OnBoardImage
}
```

Create 4 implementations: `RoyalTheme`, `PopArtTheme`, `CyberpunkTheme`, `LuxuryTheme`.
Move ALL color constants from `OnBoardImage` into these classes.

In `OnBoardImage`, replace all hardcoded color references with `theme.getXxxColor()`.

### Task 4.7: Extract CheckerRenderer from OnBoardImage

Create `app/.../GameView/CheckerRenderer.java`:
- Move all checker drawing logic (circle drawing, pip dots, selected state, animation).
- Takes `Canvas`, `BoardTheme`, dimensions.
- Public: `drawChecker(Canvas, float x, float y, int player, boolean selected, boolean highlighted)`.

### Task 4.8: Extract BoardTouchHandler from OnBoardImage

Create `app/.../GameView/BoardTouchHandler.java`:
- Move touch coordinate → board position translation logic.
- Move hit testing for checkers, points, bar, bear-off areas.
- Public: `resolveTouch(float x, float y) → BoardPosition`.

After all extractions, `GameActivity.java` should be ~400-600 lines and `OnBoardImage.java` should be ~600-800 lines.

Commit: `refactor: decompose GameActivity and OnBoardImage into focused components`

---

## PHASE 5 — New Features

### Task 5.1: Activate ELO display

The `EloRating.kt` in game-core already works. Wire it:

1. Create `app/.../PlayerProfile/PlayerProfileManager.java`:
   - SharedPreferences-based.
   - Stores: `elo_rating` (default 1200), `total_games`, `avatar_frame`, `dice_skin`, `profile_title`.
   - `updateAfterGame(boolean won, int opponentRating)` → calls `EloRating.calculate()`.

2. In `GameOverHandler`, after game completion, call `playerProfileManager.updateAfterGame()`.
   - For bot opponents, assign ELO: Easy=800, Medium=1100, Hard=1400, Royal=1700.
   - Show ELO change (delta) in game over dialog: "+12 ELO" or "-8 ELO" with green/red color.

3. In `MenuActivity`, show current ELO under player name.

### Task 5.2: Build CoinShopActivity

Create a new Activity `app/.../Menus/CoinShopActivity.java` with layout `activity_coin_shop.xml`:
- Grid layout showing purchasable items.
- Categories: Avatar Frames, Dice Skins, Profile Titles, Theme Rentals.
- Each item shows: icon/preview, name, price in coins, "Buy" button or "Owned" badge.
- On purchase: `coinManager.spend(cost, itemId)` → unlock item in `PlayerProfileManager`.
- Add "Shop" button to MenuActivity.

### Task 5.3: Build DailyLoginActivity (or Dialog)

Show a dialog/bottom sheet on app open (via `DailyLoginManager.checkAndGetReward()`):
- Show 7-day calendar row. Highlight current day. Checkmarks on claimed days.
- "Claim" button → `coinManager.earn(reward, "daily_login")` → `dailyLoginManager.claimReward()`.
- Coin animation (coins flying to balance display).
- Day 7 gets a star/special animation.

### Task 5.4: Redesign Game Over Dialog

Replace the current AlertDialog with a custom layout (`dialog_game_over.xml`):
- Winner name + "Wins!" header
- Duration display
- Coins earned row: "+10 coins" (with difficulty bonus breakdown)
- ELO change: "+12" (green) or "-8" (red)
- Win streak display with fire emoji for streak ≥ 3
- Daily challenge progress bar (if active)
- Buttons row:
  - "×2 Coins" (rewarded ad — only shown when available and after win)
  - "Save Streak" (rewarded ad — only shown after loss with streak ≥ 3)
  - "Rematch"
  - "Main Menu"
  - "Change Settings"

### Task 5.5: Add Undo feature

In `GameActivity`, add an "Undo" button visible during the player's move phase (state 2):
- On tap: check `coinManager.canAfford(CoinConfig.UNDO_COST)`.
- If yes → `coinManager.spend(UNDO_COST, "undo")` → restore board state to before last move.
- This requires saving board state snapshot before each move. Add a `BoardSnapshot` field that's updated in the move execution flow.
- Max 1 undo per turn.

Commit: `feat: add ELO display, coin shop, daily login bonus, redesigned game over, undo feature`

---

## GENERAL RULES FOR ALL PHASES

1. **Test after each task** — At minimum, ensure the project compiles. Run existing unit tests in `game-core`.
2. **Never modify `game-core/`** unless the task explicitly says to.
3. **Follow existing code style** — Java classes use `camelCase` fields, `PascalCase` classes. Kotlin follows Kotlin conventions.
4. **All new SharedPreferences** get their own prefs file name (not default prefs). Follow existing pattern.
5. **All new Analytics events** go through `GameAnalytics.get()`.
6. **All new strings** must be added to BOTH `values/strings.xml` (English) and `values-fa/strings.xml` (Farsi).
7. **Maintain backward compatibility** — existing save data must not be lost. Never rename SharedPreferences keys that are already in production.
8. **AdManager.showRewardedAd()** is the ONLY way to show a rewarded ad. Always check `isRewardedAdReady()` before showing the button, and preload via `preloadAds()`.
9. **Keep ads non-intrusive** — All rewarded ads are opt-in. Interstitials only between games. Never mid-gameplay.
10. **Import statements** — Only import what you use. No wildcard imports.
