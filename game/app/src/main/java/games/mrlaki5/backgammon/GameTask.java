package games.mrlaki5.backgammon;

import android.os.AsyncTask;

public class GameTask extends AsyncTask<Void, Void, Void> {

    public static long SLEEP_TIME=4000;

    private Model model;

    public GameTask(Model model){
        this.model=model;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(!this.isCancelled()){
            switch(model.getState()){
                case 0:
                    model.getCurrentObjectPlayer().actionRoll();
                    model.setState(1);
                    model.cahngeCurrentPlayer();
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                case 1:
                    model.getCurrentObjectPlayer().actionRoll();
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    model.setState(2);
                    break;
            }
        }
        return null;
    }
}
