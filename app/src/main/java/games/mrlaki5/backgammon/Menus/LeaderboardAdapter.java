package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import games.mrlaki5.backgammon.Database.AvatarProvider;
import games.mrlaki5.backgammon.Database.PlayerProfile;
import games.mrlaki5.backgammon.R;

/**
 * Adapter for the leaderboard ListView. Shows rank, avatar, name, games, and ELO.
 */
public class LeaderboardAdapter extends ArrayAdapter<PlayerProfile> {

    private final Context context;
    private final List<PlayerProfile> profiles;

    public LeaderboardAdapter(@NonNull Context context, List<PlayerProfile> profiles) {
        super(context, 0, profiles);
        this.context = context;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false);
        }

        PlayerProfile profile = profiles.get(position);

        // Rank
        TextView rank = view.findViewById(R.id.leaderRank);
        rank.setText("#" + (position + 1));

        // Avatar
        ImageView avatar = view.findViewById(R.id.leaderAvatar);
        avatar.setImageResource(AvatarProvider.getAvatarResource(profile.getAvatarId()));

        // Name
        TextView name = view.findViewById(R.id.leaderName);
        name.setText(profile.getDisplayName());

        // Games + win rate
        TextView games = view.findViewById(R.id.leaderGames);
        String gamesText = context.getString(R.string.leaderboard_games_format,
                profile.getTotalGames(), profile.getWinRate());
        games.setText(gamesText);

        // ELO
        TextView elo = view.findViewById(R.id.leaderElo);
        elo.setText(String.valueOf(profile.getElo()));

        return view;
    }
}
