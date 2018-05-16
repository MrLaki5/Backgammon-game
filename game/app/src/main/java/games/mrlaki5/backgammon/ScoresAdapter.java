package games.mrlaki5.backgammon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Beans.ScoreElem;

public class ScoresAdapter extends ArrayAdapter<ScoreElem>{

    private Context mContext;
    private List<ScoreElem> myList;

    public ScoresAdapter(@NonNull Context context, ArrayList<ScoreElem> list) {
        super(context, 0, list);
        this.mContext=context;
        this.myList=list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem=convertView;
        if(listItem==null){
            listItem= LayoutInflater.from(mContext).inflate(R.layout.score_list, parent, false);
        }
        ScoreElem scoreElem= myList.get(position);
        ((TextView) listItem.findViewById(R.id.sListP1Name)).setText(scoreElem.getPlayer1Name());
        ((TextView) listItem.findViewById(R.id.sListP2Name)).setText(scoreElem.getPlayer2Name());
        String centerText="";
        centerText=""+scoreElem.getPlayer1Score()+":"+scoreElem.getPlayer2Score();
        ((TextView) listItem.findViewById(R.id.sListDummuText)).setText(centerText);
        return listItem;
    }
}
