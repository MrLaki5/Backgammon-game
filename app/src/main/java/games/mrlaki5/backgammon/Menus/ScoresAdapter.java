package games.mrlaki5.backgammon.Menus;

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
import games.mrlaki5.backgammon.R;

//Adapter used for printing score ArrayList to ListView
public class ScoresAdapter extends ArrayAdapter<ScoreElem>{

    //ScoresActivity context
    private Context mContext;
    //ArrayList with scores
    private List<ScoreElem> myList;

    //Constructor
    public ScoresAdapter(@NonNull Context context, ArrayList<ScoreElem> list) {
        //Constructor of ArrayAdapter. Resource is set in getView so now is passed 0
        super(context, 0, list);
        this.mContext=context;
        this.myList=list;
    }

    //Method called for showing data from score ArrayList to view
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Load layout for score list view element
        View listItem=convertView;
        if(listItem==null){
            listItem= LayoutInflater.from(mContext).inflate(R.layout.score_list, parent,
                    false);
        }
        //Get current element from arrayList
        ScoreElem scoreElem= myList.get(position);
        //Set data to newly created view
        ((TextView) listItem.findViewById(R.id.sListP1Name)).setText(scoreElem.getPlayer1Name());
        ((TextView) listItem.findViewById(R.id.sListP2Name)).setText(scoreElem.getPlayer2Name());
        String centerText="";
        centerText=""+scoreElem.getPlayer1Score()+":"+scoreElem.getPlayer2Score();
        ((TextView) listItem.findViewById(R.id.sListDummuText)).setText(centerText);
        return listItem;
    }
}
