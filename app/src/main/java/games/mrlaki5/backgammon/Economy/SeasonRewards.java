package games.mrlaki5.backgammon.Economy;

import android.content.Context;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;

/**
 * The pieces that coins cannot buy.
 *
 * Every other object in this game has a price, and a catalogue where everything
 * has a price is one where nothing was earned: the shelf fills at whatever rate
 * the player is willing to spend, and none of it means anything afterwards. The
 * eight season banners are the answer. They are handed over here and nowhere
 * else — no screen offers them, and the bundle deliberately skips them.
 *
 * The rule is deliberately not "chapter three gives you the Tabriz banner".
 * That would let a player finish the Journey once and walk away with all eight,
 * including seasons that have not opened yet, which is exactly the thing that
 * makes a collection weightless. Instead: finishing a chapter hands over the
 * banner of the season that is open while you finish it. Eight banners means
 * eight seasons of actually being here, which is the only claim a banner makes
 * and the only one worth making.
 */
public final class SeasonRewards {

    private SeasonRewards() {}

    /**
     * Hands over the open season's banner, if this chapter earns one and the
     * player does not already hold it.
     *
     * Chapter one is excluded: it is the tutorial, and a banner handed over
     * before the player has chosen anything is a banner that means nothing.
     *
     * @return the id of the piece granted, or null if nothing was granted.
     */
    public static String grantForStage(Context context, int stageIndex) {
        if (stageIndex < 1) {
            return null;
        }
        Season season = new SeasonManager(context).current();
        String bannerId = season.bannerId();
        if (bannerId == null) {
            return null;
        }
        PlayerProfileManager profile = PlayerProfileManager.getInstance(context);
        if (profile.isItemPurchased(bannerId)) {
            return null;
        }
        profile.addPurchasedItem(bannerId);
        return bannerId;
    }

    /** True when this piece is one that only a finished chapter hands over. */
    public static boolean isEarnedBanner(String itemId) {
        return itemId != null && itemId.startsWith("banner_");
    }
}
