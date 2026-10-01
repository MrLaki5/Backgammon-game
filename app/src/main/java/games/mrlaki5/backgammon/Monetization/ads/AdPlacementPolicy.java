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
