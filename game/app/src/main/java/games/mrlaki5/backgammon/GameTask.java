package games.mrlaki5.backgammon;

import android.os.AsyncTask;

public class GameTask extends AsyncTask<Void, Void, Void> {

    public static long SLEEP_TIME=4000;
    private int WorkFlag=1;

    private Model model;
    private GameLogic gameLogic;

    public GameTask(Model model, GameLogic gamLogic){
        this.model=model;
        this.gameLogic=gamLogic;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(WorkFlag==1){
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
                    if(model.getDiceThrows()[0].getThrowNumber()>=model.getDiceThrows()[1].getThrowNumber()){
                        model.setCurrentPlayer(1);
                    }
                    else{
                        model.setCurrentPlayer(2);
                    }
                    model.setState(2);
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(), model.getCurrentPlayer(), model.getDiceThrows()));
                    if(!model.getNextMoves().isEmpty()) {
                        model.getCurrentObjectPlayer().actionMove();
                    }
                    model.cahngeCurrentPlayer();
                    model.setState(3);
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    model.getCurrentObjectPlayer().actionRoll();
                    model.setState(2);
                    break;
            }
        }
        return null;
    }
}
