# QA Test Checklist — Royal Backgammon v2.0.0

Run all tests before each release. Mark with ✓ when passing.

---

## 1. VS Bot (Regression)

- [ ] Start new game vs Easy bot — complete full game
- [ ] Start new game vs Medium bot — complete full game
- [ ] Start new game vs Hard bot — complete full game
- [ ] Start new game vs Royal bot — complete full game
- [ ] Verify dice roll works (shake + button)
- [ ] Verify legal move highlights are correct
- [ ] Verify hitting opponent checker sends to bar
- [ ] Verify bar checker must move first
- [ ] Verify bearing off works (exact + overshoot)
- [ ] Verify doubles give 4 moves
- [ ] Verify win detection and results screen
- [ ] Verify game save/continue works (exit mid-game, resume)
- [ ] Test all 4 board themes during gameplay
- [ ] Verify sound effects (move, hit, dice, win)
- [ ] Verify music volume control works

## 2. Pass & Play

- [ ] Start two-player game from menu
- [ ] Verify player names are customizable
- [ ] Verify turn-switch overlay appears between turns
- [ ] Verify "I'm Ready" button dismisses overlay
- [ ] Verify board rotates 180° for Player 2
- [ ] Verify board rotates back to 0° for Player 1
- [ ] Complete a full game without crash
- [ ] Verify correct player wins and is recorded
- [ ] Verify back button works mid-game (no hang/deadlock)
- [ ] Test with default names and with custom names

## 3. Online (when backend is connected)

- [ ] Anonymous sign-in succeeds
- [ ] Random matchmaking: enter queue
- [ ] Random matchmaking: match found, game starts
- [ ] Private room: create room, get invite code
- [ ] Private room: second player joins with code
- [ ] Game sync: moves appear on opponent's device
- [ ] Dice sync: both players see same dice values
- [ ] Reconnect: disconnect WiFi, reconnect within 60s — game resumes
- [ ] Reconnect: timeout after 60s — game forfeits
- [ ] Resign: player resigns, opponent notified
- [ ] Anti-cheat: invalid move from opponent is rejected
- [ ] ELO updated after online game

## 4. Leaderboard & Profile

- [ ] Scores page shows 3 tabs (Week/Month/All Time)
- [ ] After playing a game, player appears in leaderboard
- [ ] ELO rating changes after win/loss
- [ ] "Match History" button shows legacy scores
- [ ] Leaderboard shows correct rank, avatar, name, ELO
- [ ] Empty state shows "No games played yet" message

## 5. Monetization — Ads

- [ ] Interstitial does NOT show during gameplay
- [ ] Interstitial shows after every 3rd game completion
- [ ] Interstitial does NOT show if "Remove Ads" purchased
- [ ] Rewarded ad available for hint (button visible)
- [ ] After watching rewarded ad, hint/reward is granted
- [ ] No banner ads anywhere in the app
- [ ] Ad frequency resets correctly

## 6. Monetization — IAP

- [ ] "Remove Ads" purchase works (test with StubBillingProvider)
- [ ] After "Remove Ads" purchase, interstitials stop
- [ ] Theme purchase unlocks the theme
- [ ] Locked themes show lock indicator
- [ ] Restore purchases works on fresh install
- [ ] Purchase state persists across app restart

## 7. Analytics & Crash Reporting

- [ ] `game_started` event fires on game begin (check Logcat with StubAnalytics)
- [ ] `game_completed` event fires with correct mode/winner/duration
- [ ] `ad_shown` event fires when ad is displayed
- [ ] `iap_purchase_success` fires on purchase
- [ ] `matchmaking_started/matched/abandoned` fire correctly
- [ ] `app_open` fires on app launch
- [ ] Intentional crash (testCrash) appears in crash dashboard
- [ ] Non-fatal exception reporting works

## 8. UI & Localization

- [ ] Switch to Farsi — all strings display correctly
- [ ] Switch to English — all strings display correctly
- [ ] RTL layout correct in Farsi mode
- [ ] All dialogs fit on screen (landscape)
- [ ] Menu buttons have correct animations
- [ ] Tutorial mode works end-to-end

## 9. Build Variants

- [ ] `assembleFossDebug` builds successfully
- [ ] `assembleBazaarDebug` builds successfully
- [ ] `assembleMyketDebug` builds successfully
- [ ] `assembleBazaarRelease` builds successfully (with signing)
- [ ] `assembleMyketRelease` builds successfully (with signing)
- [ ] APK sizes are reasonable (< 15MB)
- [ ] ProGuard doesn't strip needed classes

## 10. Device Compatibility

- [ ] Runs on Android 5.0 (API 21) emulator
- [ ] Runs on Android 14 (API 34) device
- [ ] Landscape orientation locked correctly
- [ ] No memory leaks on repeated game starts (monitor with Profiler)
- [ ] No ANR on game start/end

---

## Test Commands

```bash
# Unit tests (game-core)
./gradlew :game-core:test

# Full debug build (all flavors)
./gradlew assembleFossDebug

# Release build (requires signing config)
./gradlew assembleBazaarRelease
./gradlew assembleMyketRelease

# Run instrumented tests
./gradlew connectedFossDebugAndroidTest
```
