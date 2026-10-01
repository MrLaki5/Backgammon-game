# Analytics Events — Royal Backgammon

## Architecture

```
Game Code → GameAnalytics (singleton facade) → AnalyticsProvider (interface) → FirebaseAnalyticsProvider → Firebase SDK
```

- `StubAnalyticsProvider` logs to Logcat (debug/test builds)
- `FirebaseAnalyticsProvider` sends to Firebase Analytics (production)
- No game logic code directly references any SDK

---

## App Lifecycle Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `app_open` | — | `BackgammonApp.initializeAnalytics()` | App opened (any launch) |
| `first_launch` | — | `BackgammonApp.initializeAnalytics()` | First ever app open (one-time) |
| `session_start` | — | `BackgammonApp.initializeAnalytics()` | Session begins |
| `session_end` | `duration_sec` | `GameAnalytics.trackSessionEnd()` | Session ends |

---

## Tutorial Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `tutorial_started` | — | `GameActivity.trackGameStartedEvent()` | Tutorial mode entered |
| `tutorial_step` | `step` | `GameActivity.trackTutorialStepCompleted()` | Tutorial step completed |
| `tutorial_completed` | — | `GameActivity.trackTutorialStepCompleted()` | All tutorial steps done |
| `tutorial_skipped` | `step` | (future: skip button) | Tutorial exited early |

---

## Game Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `game_started` | `mode`, `difficulty`, `theme` | `GameActivity.trackGameStartedEvent()` | New game begins |
| `game_completed` | `mode`, `winner`, `duration_sec`, `games_played`, `elo_before`, `elo_after` | `MenuActivity.onActivityResult()` | Game finishes normally |
| `game_won` | `mode`, `difficulty`, `duration_sec` | (future: enhanced result) | Player wins |
| `game_lost` | `mode`, `difficulty`, `duration_sec` | (future: enhanced result) | Player loses |
| `game_abandoned` | `mode`, `difficulty`, `duration_sec` | `GameActivity` pause→quit | Player quits mid-game |
| `rematch_clicked` | `mode`, `difficulty` | (future: result screen) | Rematch button tapped |
| `rematch_started` | `mode`, `difficulty` | (future: rematch flow) | Rematch game begins |
| `rematch_completed` | `mode`, `difficulty`, `winner` | (future: rematch flow) | Rematch game ends |
| `game_restarted` | `mode`, `difficulty` | `GameActivity.restartGame()` | Game restarted from pause |

---

## Ad Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `ad_requested` | `ad_type`, `placement` | `TapsellAdProvider.loadAd()` | Ad request sent to SDK |
| `ad_loaded` | `ad_type`, `placement` | `TapsellAdProvider` callback | Ad loaded and ready |
| `ad_failed` | `ad_type`, `placement`, `error` | `TapsellAdProvider` callback | Ad failed to load/show |
| `ad_shown` | `ad_type`, `placement`, `game_number` | `TapsellAdProvider.showAd()` | Ad displayed to user |
| `ad_clicked` | `ad_type`, `placement` | `TapsellAdProvider` callback | User clicked ad |
| `rewarded_ad_started` | `placement`, `reward_type` | Hint button flow | Rewarded ad started |
| `rewarded_ad_completed` | `placement`, `reward_type` | `TapsellAdProvider` callback | Rewarded ad watched fully |
| `rewarded_ad_failed` | `placement`, `error` | `TapsellAdProvider` callback | Rewarded ad failed |

---

## UX Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `menu_play_clicked` | `mode` | `MenuActivity.openPlayOptions()`, `openPassAndPlay()` | Play button tapped |
| `tutorial_clicked` | — | `MenuActivity.startTutorial()` | Tutorial button tapped |
| `settings_opened` | — | `MenuActivity.OpenSettings()` | Settings opened |
| `scores_opened` | — | `MenuActivity.scores()` | Scores/leaderboard opened |
| `theme_selected` | `selected_theme` | Single player dialog / P&P dialog | Theme chosen |
| `difficulty_selected` | `selected_difficulty` | Single player dialog | Difficulty chosen |
| `language_changed` | `language` | `MenuActivity.toggleLanguage()` | Language switched |

---

## Monetization Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `purchase_started` | `sku`, `store` | `PremiumManager` | Purchase flow initiated |
| `purchase_success` | `sku`, `store` | `PremiumManager` callback | Purchase completed |
| `purchase_failed` | `sku`, `store`, `error` | `PremiumManager` callback | Purchase failed |
| `remove_ads_clicked` | — | (future: settings/UI) | Remove ads button tapped |
| `theme_purchase_clicked` | `selected_theme` | (future: theme store) | Theme purchase attempted |

---

## Retention Events

| Event | Parameters | Trigger Location | Description |
|-------|-----------|-----------------|-------------|
| `daily_reward_opened` | `day_streak` | (future: daily reward UI) | Daily reward screen opened |
| `daily_reward_claimed` | `day_streak`, `reward_id` | (future: daily reward) | Reward claimed |
| `mission_started` | `mission_id` | (future: missions system) | Mission activated |
| `mission_completed` | `mission_id` | (future: missions system) | Mission completed |
| `achievement_unlocked` | `achievement_id` | (future: achievements) | Achievement earned |

---

## Parameter Reference

| Parameter | Type | Values |
|-----------|------|--------|
| `mode` | String | `vs_bot`, `pass_and_play`, `tutorial`, `online` |
| `difficulty` | String | `easy`, `medium`, `hard`, `royal` |
| `theme` | String | `royal`, `pop_art`, `cyberpunk`, `luxury` |
| `winner` | String | `player`, `opponent`, `player1`, `player2` |
| `duration_sec` | Long | Game duration in seconds |
| `games_played` | Int | Total games played in session |
| `elo_before` | Int | ELO rating before game |
| `elo_after` | Int | ELO rating after game |
| `ad_type` | String | `interstitial`, `rewarded` |
| `placement` | String | `post_game`, `hint`, `daily_reward` |
| `game_number` | Int | Nth game in current session |
| `reward_type` | String | `hint`, `extra_move`, `theme_trial` |
| `error` | String | Error message/code |
| `step` | Int | Tutorial step number (0-indexed) |
| `language` | String | `en`, `fa` |
| `sku` | String | Product ID |
| `store` | String | `bazaar`, `myket`, `foss` |
| `day_streak` | Int | Consecutive days |
| `reward_id` | String | Reward identifier |
| `mission_id` | String | Mission identifier |
| `achievement_id` | String | Achievement identifier |

---

## Implementation Status

| Category | Status | Notes |
|----------|--------|-------|
| App lifecycle | ✅ Active | Tracked in BackgammonApp |
| Tutorial | ✅ Active | start/step tracked, skip pending |
| Game | ✅ Active | started/completed/abandoned/restarted |
| Ads | 🔄 Pending | Will be wired in PHASE 2 with TapsellAdProvider |
| UX | ✅ Active | All menu actions tracked |
| Monetization | 🔄 Pending | Wired when real billing added |
| Retention | 🔄 Pending | Wired when daily/achievements built |

---

## Firebase Configuration

- Provider: `FirebaseAnalyticsProvider.java`
- Requires: `app/google-services.json` (from Firebase Console)
- Falls back to `StubAnalyticsProvider` if Firebase unavailable
- Debug: Events visible in Firebase DebugView with `adb shell setprop debug.firebase.analytics.app com.royalbackgammon.offline`
