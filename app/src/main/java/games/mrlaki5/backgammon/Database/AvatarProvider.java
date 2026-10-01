package games.mrlaki5.backgammon.Database;

import games.mrlaki5.backgammon.R;

/**
 * Provides predefined avatar drawables for player profiles.
 * No user photo upload needed — just a selection of built-in icons.
 */
public final class AvatarProvider {

    private AvatarProvider() {}

    /**
     * Predefined avatar resource IDs.
     * These reference drawable resources (vector icons or PNGs).
     * The index in this array is the avatarId stored in PlayerProfile.
     *
     * Note: Using generic placeholder IDs. Replace with actual drawable resources
     * when avatar artwork is ready. For now, the app will use the first available
     * drawable or a colored circle with initials as fallback.
     */
    private static final int[] AVATAR_RESOURCES = {
            R.drawable.ic_avatar_default,   // 0 - Default
            R.drawable.ic_avatar_lion,      // 1 - Lion
            R.drawable.ic_avatar_eagle,     // 2 - Eagle
            R.drawable.ic_avatar_wolf,      // 3 - Wolf
            R.drawable.ic_avatar_crown,     // 4 - Crown
            R.drawable.ic_avatar_shield,    // 5 - Shield
            R.drawable.ic_avatar_flame,     // 6 - Flame
            R.drawable.ic_avatar_star,      // 7 - Star
    };

    /**
     * Returns the drawable resource ID for the given avatar index.
     * Falls back to default if index is out of range.
     */
    public static int getAvatarResource(int avatarId) {
        if (avatarId >= 0 && avatarId < AVATAR_RESOURCES.length) {
            return AVATAR_RESOURCES[avatarId];
        }
        return AVATAR_RESOURCES[0];
    }

    /**
     * Returns the total number of available avatars.
     */
    public static int getAvatarCount() {
        return AVATAR_RESOURCES.length;
    }

    /**
     * Returns all avatar resource IDs (for showing selection grid).
     */
    public static int[] getAllAvatarResources() {
        return AVATAR_RESOURCES.clone();
    }
}
