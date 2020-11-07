package games.mrlaki5.backgammon.GameControllers;

import android.content.Intent;
import android.os.AsyncTask;

import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GameView.OnBoardImage;

//Class for game thread
public class GameTask extends AsyncTask<Void, Void, Void> {

    //Time between two turns
    private long sleep_time;
    //Flag for work, when set off thread will finish
    private int WorkFlag=1;
    //Flag turned on when thread finishes execution
    private int FinishedFlag=0;
    //Flag for synchronization of end routines
    private int EndRoutineStarted=0;
    //Game activity context
    private GameActivity gameActivity;
    //Model of game state
    private Model model;
    //Object of game logic
    private GameLogic gameLogic;
    //View
    private OnBoardImage onBoardImage;

    //Constructor
    public GameTask(Model model, GameLogic gamLogic, OnBoardImage onBoardImage, long sleep_time,
                    GameActivity gameActivity){
        this.model=model;
        this.gameLogic=gamLogic;
        this.onBoardImage=onBoardImage;
        this.sleep_time=sleep_time;
        this.gameActivity=gameActivity;
        if(this.sleep_time==0){
            this.sleep_time=1;
        }
    }

    //Method for writing text on view
    private void writeMessage(String Text){
        //Set text on view
        onBoardImage.setMessage(model.getCurrentObjectPlayer().getPlayerName() +
                ", " +Text, model.getCurrentPlayer());
        //Invalidate view, its redrawn
        onBoardImage.postInvalidate();
    }

    //Run method
    @Override
    protected Void doInBackground(Void... voids) {
        //While working flag is on thread runs
        while(WorkFlag==1){
            //Depending on state go
            switch(model.getState()){
                //State 0: player1 throw one dice
                case 0:
                    //Set message
                    writeMessage("roll dice");
                    //Call method on object player to roll dice
                    model.getCurrentObjectPlayer().actionRoll();
                    //Check if thread should finish
                    if(WorkFlag==0){
                        break;
                    }
                    //Change state to State 1
                    model.setState(1);
                    //Change current player
                    model.changeCurrentPlayer();
                    //Wait time between turns
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                //State 1: player2 throw one dice
                case 1:
                    //Set message
                    writeMessage("roll dice");
                    //Call method in object player to roll dice
                    model.getCurrentObjectPlayer().actionRoll();
                    //Check if thread should finish
                    if(WorkFlag==0){
                        break;
                    }
                    //Check which player got higher number, that one plays first
                    if(model.getDiceThrows()[0].getThrowNumber()>=
                            model.getDiceThrows()[1].getThrowNumber()){
                        model.setCurrentPlayer(1);
                    }
                    else{
                        model.setCurrentPlayer(2);
                    }
                    //Change state to State 3
                    model.setState(2);
                    //Wait time between turns
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                //State 2: currentPlayer move chips
                case 2:
                    //Calculate all next moves for current player
                    model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                            model.getCurrentPlayer(), model.getDiceThrows()));
                    //If there are next moves
                    if(!model.getNextMoves().isEmpty()) {
                        //Write message
                        writeMessage("move chips");
                        //Call method in object player to move chips
                        model.getCurrentObjectPlayer().actionMove();
                        //Check if thread should finish
                        if(WorkFlag==0){
                            break;
                        }
                    }
                    //Check if current player finished game
                    if(gameLogic.getCurrPlayerFinished()!=0){
                        synchronized (this) {
                            //Check end routine synchronization flag
                            if(EndRoutineStarted==0) {
                                //Set end routine synchronization flag
                                EndRoutineStarted=1;
                                //Set working flag to 0
                                WorkFlag = 0;
                                //Create end result intent
                                Intent data = new Intent();
                                //Add player1 name to intent
                                data.putExtra(MenuActivity.EXTRA_PLAYER1_NAME,
                                        model.getPlayers()[0].getPlayerName());
                                //Add player2 name to intent
                                data.putExtra(MenuActivity.EXTRA_PLAYER2_NAME,
                                        model.getPlayers()[1].getPlayerName());
                                //Add wining player to intent
                                data.putExtra(MenuActivity.EXTRA_WINING_PLAYER,
                                        gameLogic.getCurrPlayerFinished());
                                gameActivity.setResult(MenuActivity.GAME_ENDED_OK, data);
                                //Finish game activity (this will start onPause and onStop)
                                gameActivity.finish();
                            }
                        }
                        break;
                    }
                    //Change current player
                    model.changeCurrentPlayer();
                    //Set state to State 3
                    model.setState(3);
                    //Wait time between turns
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                //State 3: currentPlayer roll dices
                case 3:
                    //Set message
                    writeMessage("roll dices");
                    //Call method in object player to roll dice
                    model.getCurrentObjectPlayer().actionRoll();
                    //Check if thread should finish
                    if(WorkFlag==0){
                        break;
                    }
                    //Change state to State 2
                    model.setState(2);
                    break;
            }
        }
        //Set finished flag and notify if UI thread is waiting for it
        synchronized (this) {
            FinishedFlag = 1;
            this.notifyAll();
        }
        return null;
    }

    //Getters and setters
    public int getWorkFlag() {
        return WorkFlag;
    }

    public void setWorkFlag(int workFlag) {
        WorkFlag = workFlag;
    }

    public int getFinishedFlag() {
        return FinishedFlag;
    }

    public void setFinishedFlag(int finishedFlag) {
        FinishedFlag = finishedFlag;
    }

    public int getEndRoutineStarted() {
        return EndRoutineStarted;
    }

    public void setEndRoutineStarted(int endRoutineStarted) {
        EndRoutineStarted = endRoutineStarted;
    }
}
