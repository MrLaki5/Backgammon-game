package games.mrlaki5.backgammon.Economy;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.R;

/**
 * The crafted goods: checkers, dice and titles.
 *
 * These live here rather than inside the shop screen because two screens need
 * the same list — the shop, to sell them, and the treasury, to show which ones
 * were missed. A season names its pieces by id, so both screens have to agree on
 * what those ids mean, and the only way to guarantee that is one definition.
 */
public final class SeasonCatalogue {

    private SeasonCatalogue() {}

    public static List<ShopItem> all(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.addAll(boards(context));
        items.addAll(checkers(context));
        items.addAll(dice(context));
        items.addAll(effects(context));
        items.addAll(banners(context));
        items.addAll(frames(context));
        items.addAll(sounds(context));
        items.addAll(titles(context));
        return items;
    }

    /** Every piece in one season's set, in the order the bundle page lists them. */
    public static List<ShopItem> ofSeason(Context context, Season season) {
        List<ShopItem> set = new ArrayList<>();
        for (String id : season.itemIds()) {
            ShopItem item = byId(context, id);
            if (item != null) {
                set.add(item);
            }
        }
        return set;
    }

    public static ShopItem byId(Context context, String id) {
        for (ShopItem item : all(context)) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * What the pieces of a set would cost bought one at a time.
     *
     * Earn-only pieces contribute nothing, because they are not for sale at any
     * price and counting them would inflate the saving the bundle claims.
     */
    public static int separatePrice(Context context, Season season) {
        int total = 0;
        for (ShopItem item : ofSeason(context, season)) {
            if (item.isPurchasable()) {
                total += item.price;
            }
        }
        return total;
    }

    /**
     * The playing surface. A board is the largest thing on screen during a
     * game, so it is the piece a set is actually judged on — and the catalogue
     * had none at all.
     */
    public static List<ShopItem> boards(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(board(context, "board_khatam", R.string.board_khatam_title,
                R.string.board_khatam_desc, R.string.board_khatam_story,
                3200, ShopItem.Rarity.EPIC, R.drawable.board_khatam));
        items.add(board(context, "board_mina", R.string.board_mina_title,
                R.string.board_mina_desc, R.string.board_mina_story,
                3600, ShopItem.Rarity.EPIC, R.drawable.board_mina));
        items.add(board(context, "board_nacre", R.string.board_nacre_title,
                R.string.board_nacre_desc, R.string.board_nacre_story,
                3000, ShopItem.Rarity.EPIC, R.drawable.board_nacre));
        items.add(board(context, "board_monabbat", R.string.board_monabbat_title,
                R.string.board_monabbat_desc, R.string.board_monabbat_story,
                3800, ShopItem.Rarity.EPIC, R.drawable.board_monabbat));
        items.add(board(context, "board_haftrang", R.string.board_haftrang_title,
                R.string.board_haftrang_desc, R.string.board_haftrang_story,
                3400, ShopItem.Rarity.EPIC, R.drawable.board_haftrang));
        items.add(board(context, "board_pateh", R.string.board_pateh_title,
                R.string.board_pateh_desc, R.string.board_pateh_story,
                3100, ShopItem.Rarity.EPIC, R.drawable.board_pateh));
        items.add(board(context, "board_melileh", R.string.board_melileh_title,
                R.string.board_melileh_desc, R.string.board_melileh_story,
                4200, ShopItem.Rarity.LEGENDARY, R.drawable.board_melileh));
        items.add(board(context, "board_zari", R.string.board_zari_title,
                R.string.board_zari_desc, R.string.board_zari_story,
                3500, ShopItem.Rarity.EPIC, R.drawable.board_zari));
        return items;
    }

    private static ShopItem board(Context c, String id, int title, int desc, int story,
                                  int price, ShopItem.Rarity rarity, int art) {
        return ShopItem.crafted(id, c.getString(title), c.getString(desc),
                c.getString(story), price, ShopItem.Category.BOARD, rarity, art,
                0, ShopItem.Acquisition.BUY, "");
    }

    /** What the dice throw off when they land. */
    public static List<ShopItem> effects(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(effect(context, "effect_enamel_dust", R.string.effect_enamel_dust_title,
                R.string.effect_enamel_dust_desc, R.string.effect_enamel_dust_story,
                1400, R.drawable.effect_enamel_dust));
        items.add(effect(context, "effect_turquoise_spark", R.string.effect_turquoise_spark_title,
                R.string.effect_turquoise_spark_desc, R.string.effect_turquoise_spark_story,
                1600, R.drawable.effect_turquoise_spark));
        items.add(effect(context, "effect_pearl_ripple", R.string.effect_pearl_ripple_title,
                R.string.effect_pearl_ripple_desc, R.string.effect_pearl_ripple_story,
                1300, R.drawable.effect_pearl_ripple));
        items.add(effect(context, "effect_agate_ember", R.string.effect_agate_ember_title,
                R.string.effect_agate_ember_desc, R.string.effect_agate_ember_story,
                1700, R.drawable.effect_agate_ember));
        items.add(effect(context, "effect_lapis_night", R.string.effect_lapis_night_title,
                R.string.effect_lapis_night_desc, R.string.effect_lapis_night_story,
                1500, R.drawable.effect_lapis_night));
        items.add(effect(context, "effect_madder_thread", R.string.effect_madder_thread_title,
                R.string.effect_madder_thread_desc, R.string.effect_madder_thread_story,
                1200, R.drawable.effect_madder_thread));
        items.add(effect(context, "effect_filigree_glint", R.string.effect_filigree_glint_title,
                R.string.effect_filigree_glint_desc, R.string.effect_filigree_glint_story,
                1900, R.drawable.effect_filigree_glint));
        items.add(effect(context, "effect_saffron_haze", R.string.effect_saffron_haze_title,
                R.string.effect_saffron_haze_desc, R.string.effect_saffron_haze_story,
                1600, R.drawable.effect_saffron_haze));
        return items;
    }

    private static ShopItem effect(Context c, String id, int title, int desc, int story,
                                   int price, int art) {
        return ShopItem.crafted(id, c.getString(title), c.getString(desc),
                c.getString(story), price, ShopItem.Category.DICE_EFFECT,
                ShopItem.Rarity.RARE, art, 0, ShopItem.Acquisition.BUY, "");
    }

    /**
     * Banners. One per season, and none of them has a price.
     *
     * This is the half of the catalogue that gives the other half its meaning:
     * a shelf where everything can be bought is a shelf where nothing was
     * earned, so the banner of each season is only ever handed over for
     * finishing that season's Journey chapter.
     */
    public static List<ShopItem> banners(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(banner(context, "banner_isfahan", R.string.banner_isfahan_title,
                R.string.banner_isfahan_desc, R.string.banner_isfahan_story,
                R.drawable.banner_isfahan, EARN_CHAPTER_ISFAHAN));
        items.add(banner(context, "banner_neyshabur", R.string.banner_neyshabur_title,
                R.string.banner_neyshabur_desc, R.string.banner_neyshabur_story,
                R.drawable.banner_neyshabur, EARN_CHAPTER_NEYSHABUR));
        items.add(banner(context, "banner_harbour", R.string.banner_harbour_title,
                R.string.banner_harbour_desc, R.string.banner_harbour_story,
                R.drawable.banner_harbour, EARN_CHAPTER_HARBOUR));
        items.add(banner(context, "banner_caravan", R.string.banner_caravan_title,
                R.string.banner_caravan_desc, R.string.banner_caravan_story,
                R.drawable.banner_caravan, EARN_CHAPTER_CARAVAN));
        items.add(banner(context, "banner_shiraz", R.string.banner_shiraz_title,
                R.string.banner_shiraz_desc, R.string.banner_shiraz_story,
                R.drawable.banner_shiraz, "chapter_shiraz"));
        items.add(banner(context, "banner_kerman", R.string.banner_kerman_title,
                R.string.banner_kerman_desc, R.string.banner_kerman_story,
                R.drawable.banner_kerman, "chapter_kerman"));
        items.add(banner(context, "banner_tabriz", R.string.banner_tabriz_title,
                R.string.banner_tabriz_desc, R.string.banner_tabriz_story,
                R.drawable.banner_tabriz, "chapter_tabriz"));
        items.add(banner(context, "banner_yazd", R.string.banner_yazd_title,
                R.string.banner_yazd_desc, R.string.banner_yazd_story,
                R.drawable.banner_yazd, "chapter_yazd"));
        return items;
    }

    /** The Journey chapters that hand over each season's banner. */
    public static final String EARN_CHAPTER_ISFAHAN   = "chapter_isfahan";
    public static final String EARN_CHAPTER_NEYSHABUR = "chapter_neyshabur";
    public static final String EARN_CHAPTER_HARBOUR   = "chapter_harbour";
    public static final String EARN_CHAPTER_CARAVAN   = "chapter_caravan";

    private static ShopItem banner(Context c, String id, int title, int desc, int story,
                                   int art, String earnKey) {
        return ShopItem.earned(id, c.getString(title), c.getString(desc),
                c.getString(story), ShopItem.Category.BANNER,
                ShopItem.Rarity.EPIC, art, earnKey);
    }

    /**
     * Sound sets. The one product in the catalogue that cannot be shown, so its
     * card carries a struck mark rather than a picture of nothing, and its
     * description does the work a picture would.
     */
    public static List<ShopItem> sounds(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(sound(context, "sound_isfahan", R.string.sound_isfahan_title,
                R.string.sound_isfahan_desc, R.string.sound_isfahan_story,
                900, R.drawable.sound_isfahan));
        items.add(sound(context, "sound_neyshabur", R.string.sound_neyshabur_title,
                R.string.sound_neyshabur_desc, R.string.sound_neyshabur_story,
                1000, R.drawable.sound_neyshabur));
        items.add(sound(context, "sound_harbour", R.string.sound_harbour_title,
                R.string.sound_harbour_desc, R.string.sound_harbour_story,
                850, R.drawable.sound_harbour));
        items.add(sound(context, "sound_caravan", R.string.sound_caravan_title,
                R.string.sound_caravan_desc, R.string.sound_caravan_story,
                1000, R.drawable.sound_caravan));
        items.add(sound(context, "sound_shiraz", R.string.sound_shiraz_title,
                R.string.sound_shiraz_desc, R.string.sound_shiraz_story,
                1100, R.drawable.sound_shiraz));
        items.add(sound(context, "sound_kerman", R.string.sound_kerman_title,
                R.string.sound_kerman_desc, R.string.sound_kerman_story,
                950, R.drawable.sound_kerman));
        items.add(sound(context, "sound_tabriz", R.string.sound_tabriz_title,
                R.string.sound_tabriz_desc, R.string.sound_tabriz_story,
                1300, R.drawable.sound_tabriz));
        items.add(sound(context, "sound_yazd", R.string.sound_yazd_title,
                R.string.sound_yazd_desc, R.string.sound_yazd_story,
                1050, R.drawable.sound_yazd));
        return items;
    }

    private static ShopItem sound(Context c, String id, int title, int desc, int story,
                                  int price, int art) {
        return ShopItem.crafted(id, c.getString(title), c.getString(desc),
                c.getString(story), price, ShopItem.Category.SOUND_SET,
                ShopItem.Rarity.RARE, art, 0, ShopItem.Acquisition.BUY, "");
    }

    /** Frames for the player's face, named for a season rather than a metal tier. */
    public static List<ShopItem> frames(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(frame(context, "frame_isfahan", R.string.frame_isfahan_title,
                R.string.frame_isfahan_desc, R.string.frame_isfahan_story,
                1200, R.drawable.frame_isfahan));
        items.add(frame(context, "frame_neyshabur", R.string.frame_neyshabur_title,
                R.string.frame_neyshabur_desc, R.string.frame_neyshabur_story,
                1400, R.drawable.frame_neyshabur));
        items.add(frame(context, "frame_harbour", R.string.frame_harbour_title,
                R.string.frame_harbour_desc, R.string.frame_harbour_story,
                1100, R.drawable.frame_harbour));
        items.add(frame(context, "frame_caravan", R.string.frame_caravan_title,
                R.string.frame_caravan_desc, R.string.frame_caravan_story,
                1500, R.drawable.frame_caravan));
        items.add(frame(context, "frame_shiraz", R.string.frame_shiraz_title,
                R.string.frame_shiraz_desc, R.string.frame_shiraz_story,
                1300, R.drawable.frame_shiraz));
        items.add(frame(context, "frame_kerman", R.string.frame_kerman_title,
                R.string.frame_kerman_desc, R.string.frame_kerman_story,
                1000, R.drawable.frame_kerman));
        items.add(frame(context, "frame_tabriz", R.string.frame_tabriz_title,
                R.string.frame_tabriz_desc, R.string.frame_tabriz_story,
                1700, R.drawable.frame_tabriz));
        items.add(frame(context, "frame_yazd", R.string.frame_yazd_title,
                R.string.frame_yazd_desc, R.string.frame_yazd_story,
                1400, R.drawable.frame_yazd));
        return items;
    }

    private static ShopItem frame(Context c, String id, int title, int desc, int story,
                                  int price, int art) {
        return ShopItem.crafted(id, c.getString(title), c.getString(desc),
                c.getString(story), price, ShopItem.Category.AVATAR_FRAME,
                ShopItem.Rarity.RARE, art, 0, ShopItem.Acquisition.BUY, "");
    }

    /**
     * Checker sets, named for what a real set is made of.
     *
     * A bronze/silver/gold ladder belongs to every mobile game; walnut, camel
     * bone, Isfahan khatam and Neyshabur turquoise belong to this one.
     */
    public static List<ShopItem> checkers(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(ShopItem.crafted("checkers_walnut",
                context.getString(R.string.checkers_walnut_title),
                context.getString(R.string.checkers_walnut_desc), 0,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.COMMON,
                R.drawable.checkers_walnut, 0));
        items.add(ShopItem.crafted("checkers_bone",
                context.getString(R.string.checkers_bone_title),
                context.getString(R.string.checkers_bone_desc), 300,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.COMMON,
                R.drawable.checkers_bone, 0));
        items.add(ShopItem.crafted("checkers_khatam",
                context.getString(R.string.checkers_khatam_title),
                context.getString(R.string.checkers_khatam_desc), 800,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.RARE,
                R.drawable.checkers_khatam, 15));
        items.add(ShopItem.crafted("checkers_nacre",
                context.getString(R.string.checkers_nacre_title),
                context.getString(R.string.checkers_nacre_desc), 1100,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.RARE,
                R.drawable.checkers_nacre, 25));
        items.add(ShopItem.crafted("checkers_turquoise",
                context.getString(R.string.checkers_turquoise_title),
                context.getString(R.string.checkers_turquoise_desc), 2000,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.EPIC,
                R.drawable.checkers_turquoise, 50));
        items.add(ShopItem.crafted("checkers_agate",
                context.getString(R.string.checkers_agate_title),
                context.getString(R.string.checkers_agate_desc), 2800,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.EPIC,
                R.drawable.checkers_agate, 80));
        items.add(ShopItem.crafted("checkers_gold",
                context.getString(R.string.checkers_gold_title),
                context.getString(R.string.checkers_gold_desc), 6000,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.LEGENDARY,
                R.drawable.checkers_gold, 200));
        items.add(ShopItem.crafted("checkers_lajvard",
                context.getString(R.string.checkers_lajvard_title),
                context.getString(R.string.checkers_lajvard_desc),
                context.getString(R.string.checkers_lajvard_story), 2200,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.EPIC,
                R.drawable.checkers_lajvard, 45, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("checkers_copper",
                context.getString(R.string.checkers_copper_title),
                context.getString(R.string.checkers_copper_desc),
                context.getString(R.string.checkers_copper_story), 900,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.RARE,
                R.drawable.checkers_copper, 18, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("checkers_melileh",
                context.getString(R.string.checkers_melileh_title),
                context.getString(R.string.checkers_melileh_desc),
                context.getString(R.string.checkers_melileh_story), 3000,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.EPIC,
                R.drawable.checkers_melileh, 90, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("checkers_saffron",
                context.getString(R.string.checkers_saffron_title),
                context.getString(R.string.checkers_saffron_desc),
                context.getString(R.string.checkers_saffron_story), 2500,
                ShopItem.Category.CHECKERS, ShopItem.Rarity.EPIC,
                R.drawable.checkers_saffron, 60, ShopItem.Acquisition.BUY, ""));
        return items;
    }

    /** Dice, cut from the stones a real pair is cut from. */
    public static List<ShopItem> dice(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(ShopItem.crafted("dice_bone",
                context.getString(R.string.dice_bone_title),
                context.getString(R.string.dice_bone_desc), 0,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.COMMON,
                R.drawable.dice_set_bone, 0));
        items.add(ShopItem.crafted("dice_walnut",
                context.getString(R.string.dice_walnut_title),
                context.getString(R.string.dice_walnut_desc), 250,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.COMMON,
                R.drawable.dice_set_walnut, 0));
        items.add(ShopItem.crafted("dice_ebony",
                context.getString(R.string.dice_ebony_title),
                context.getString(R.string.dice_ebony_desc), 700,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.RARE,
                R.drawable.dice_set_ebony, 12));
        items.add(ShopItem.crafted("dice_turquoise",
                context.getString(R.string.dice_turquoise_title),
                context.getString(R.string.dice_turquoise_desc), 1800,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC,
                R.drawable.dice_set_turquoise, 40));
        items.add(ShopItem.crafted("dice_agate",
                context.getString(R.string.dice_agate_title),
                context.getString(R.string.dice_agate_desc), 2400,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC,
                R.drawable.dice_set_agate, 60));
        items.add(ShopItem.crafted("dice_gold",
                context.getString(R.string.dice_gold_title),
                context.getString(R.string.dice_gold_desc), 5000,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.LEGENDARY,
                R.drawable.dice_set_gold, 150));
        items.add(ShopItem.crafted("dice_lajvard",
                context.getString(R.string.dice_lajvard_title),
                context.getString(R.string.dice_lajvard_desc),
                context.getString(R.string.dice_lajvard_story), 2000,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC,
                R.drawable.dice_set_lajvard, 35, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("dice_copper",
                context.getString(R.string.dice_copper_title),
                context.getString(R.string.dice_copper_desc),
                context.getString(R.string.dice_copper_story), 800,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.RARE,
                R.drawable.dice_set_copper, 14, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("dice_melileh",
                context.getString(R.string.dice_melileh_title),
                context.getString(R.string.dice_melileh_desc),
                context.getString(R.string.dice_melileh_story), 2600,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC,
                R.drawable.dice_set_melileh, 75, ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("dice_saffron",
                context.getString(R.string.dice_saffron_title),
                context.getString(R.string.dice_saffron_desc),
                context.getString(R.string.dice_saffron_story), 2200,
                ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC,
                R.drawable.dice_set_saffron, 55, ShopItem.Acquisition.BUY, ""));
        return items;
    }

    /**
     * Titles in the game's own language rather than a rank ladder — what
     * backgammon players actually call each other.
     */
    public static List<ShopItem> titles(Context context) {
        List<ShopItem> items = new ArrayList<>();
        items.add(ShopItem.permanent("title_none",
                context.getString(R.string.title_none_title),
                context.getString(R.string.title_none_desc), 0,
                ShopItem.Category.TITLE, ShopItem.Rarity.COMMON, ""));
        items.add(ShopItem.withUnlock("title_doubler",
                context.getString(R.string.title_doubler_title),
                context.getString(R.string.title_doubler_desc), 400,
                ShopItem.Category.TITLE, ShopItem.Rarity.COMMON, "", 10));
        items.add(ShopItem.withUnlock("title_prime",
                context.getString(R.string.title_prime_title),
                context.getString(R.string.title_prime_desc), 900,
                ShopItem.Category.TITLE, ShopItem.Rarity.RARE, "", 25));
        items.add(ShopItem.withUnlock("title_gammon",
                context.getString(R.string.title_gammon_title),
                context.getString(R.string.title_gammon_desc), 1500,
                ShopItem.Category.TITLE, ShopItem.Rarity.RARE, "", 40));
        items.add(ShopItem.withUnlock("title_plakoto",
                context.getString(R.string.title_plakoto_title),
                context.getString(R.string.title_plakoto_desc), 2200,
                ShopItem.Category.TITLE, ShopItem.Rarity.EPIC, "", 70));
        items.add(ShopItem.withUnlock("title_unbeaten",
                context.getString(R.string.title_unbeaten_title),
                context.getString(R.string.title_unbeaten_desc), 4000,
                ShopItem.Category.TITLE, ShopItem.Rarity.LEGENDARY, "", 150));
        items.add(ShopItem.crafted("title_backgame",
                context.getString(R.string.title_backgame_title),
                context.getString(R.string.title_backgame_desc),
                context.getString(R.string.title_backgame_story), 1800,
                ShopItem.Category.TITLE, ShopItem.Rarity.EPIC, 0, 55,
                ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("title_anchor",
                context.getString(R.string.title_anchor_title),
                context.getString(R.string.title_anchor_desc),
                context.getString(R.string.title_anchor_story), 1300,
                ShopItem.Category.TITLE, ShopItem.Rarity.RARE, 0, 30,
                ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("title_blitz",
                context.getString(R.string.title_blitz_title),
                context.getString(R.string.title_blitz_desc),
                context.getString(R.string.title_blitz_story), 2600,
                ShopItem.Category.TITLE, ShopItem.Rarity.EPIC, 0, 85,
                ShopItem.Acquisition.BUY, ""));
        items.add(ShopItem.crafted("title_bearoff",
                context.getString(R.string.title_bearoff_title),
                context.getString(R.string.title_bearoff_desc),
                context.getString(R.string.title_bearoff_story), 3200,
                ShopItem.Category.TITLE, ShopItem.Rarity.LEGENDARY, 0, 120,
                ShopItem.Acquisition.BUY, ""));
        return items;
    }
}
