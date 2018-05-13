package games.mrlaki5.backgammon;

import android.os.AsyncTask;

public class GameTask extends AsyncTask<Void, Void, Void> {

    public long sleep_time;
    private int WorkFlag=1;

    private Model model;
    private GameLogic gameLogic;
    private OnBoardImage onBoardImage;

    public GameTask(Model model, GameLogic gamLogic, OnBoardImage onBoardImage, long sleep_time){
        this.model=model;
        this.gameLogic=gamLogic;
        this.onBoardImage=onBoardImage;
        this.sleep_time=sleep_time;
        if(this.sleep_time==0){
            this.sleep_time=1;
        }
    }

    public int getWorkFlag() {
        return WorkFlag;
    }

    public void setWorkFlag(int workFlag) {
        WorkFlag = workFlag;
    }

    private void writeMessage(String Text){
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
                    if(WorkFlag==0){
                        break;
                    }
                    model.setState(1);
                    model.cahngeCurrentPlayer();
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                case 1:
                    writeMessage("roll dice");
                    model.getCurrentObjectPlayer().actionRoll();
                    if(WorkFlag==0){
                        break;
                    }
                    if(model.getDiceThrows()[0].getThrowNumber()>=
                            model.getDiceThrows()[1].getThrowNumber()){
                        model.setCurrentPlayer(1);
                    }
                    else{
                        model.setCurrentPlayer(2);
                    }
                    model.setState(2);
                    try {
                        Thread.sleep(sleep_time);
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
                        if(WorkFlag==0){
                            break;
                        }
                    }
                    model.cahngeCurrentPlayer();
                    model.setState(3);
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    writeMessage("roll dices");
                    model.getCurrentObjectPlayer().actionRoll();
                    if(WorkFlag==0){
                        break;
                    }
                    model.setState(2);
                    break;
            }
        }
        return null;
    }
}
