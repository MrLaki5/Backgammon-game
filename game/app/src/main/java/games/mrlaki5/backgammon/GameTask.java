package games.mrlaki5.backgammon;

import android.os.AsyncTask;

public class GameTask extends AsyncTask<Void, Void, Void> {

    public static long SLEEP_TIME=0001;
    private int WorkFlag=1;

    private Model model;
    private GameLogic gameLogic;
    private OnBoardImage onBoardImage;

    public GameTask(Model model, GameLogic gamLogic, OnBoardImage onBoardImage){
        this.model=model;
        this.gameLogic=gamLogic;
        this.onBoardImage=onBoardImage;
    }

    protected void writeMessage(String Text){
        onBoardImage.setMessage(model.getCurrentObjectPlayer().getPlayerName() +
                ", " +Text, model.getCurrentPlayer());
        onBoardImage.postInvalidate();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(WorkFlag==1){
            switch(model.getState()){
                case 0:
                    writeMessage("roll dice");
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
                    writeMessage("roll dice");
                    model.getCurrentObjectPlayer().actionRoll();
                    if(model.getDiceThrows()[0].getThrowNumber()>=
                            model.getDiceThrows()[1].getThrowNumber()){
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
                    model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                            model.getCurrentPlayer(), model.getDiceThrows()));
                    if(!model.getNextMoves().isEmpty()) {
                        writeMessage("move chips");
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
                    writeMessage("roll dices");
                    model.getCurrentObjectPlayer().actionRoll();
                    model.setState(2);
                    break;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        onBoardImage.invalidate();
    }
}
