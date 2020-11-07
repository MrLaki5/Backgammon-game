package games.mrlaki5.backgammon.GameModel;

import android.os.Bundle;

import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Players.Bot;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

//Model class
public class Model {

    //Array that represents all fields on board, side board and end board
    //24-white, 25-red side board
    //27-white endBoard, 26-red endBoard
    private BoardFieldState[] BoardFields= new BoardFieldState[28];
    //Array that represents thrown dices
    private DiceThrow[] DiceThrows=new DiceThrow[4];
    //Array of players in game
    private Player[] Players=new Player[2];
    //List of all next moves for current player
    private List<NextJump> NextMoves=null;
    //Current player
    // 2 red, 1 white
    private int CurrentPlayer;
    //Current state of game
    //0: player1 roll dice
    //1: player2 roll dice
    //2: currentPlayer move chips
    //3: currentPlayer roll dices
    private int State;

    //Constructor used when loading model from save file
    public Model(){}

    //Constructor used when creating new game
    public Model(Bundle extras, GameActivity activity){
        //Initialize dice throws
        for(int i=0; i<DiceThrows.length; i++){
            DiceThrows[i]=new DiceThrow(0);
            DiceThrows[i].setAlreadyUsed(1);
        }
        DiceThrows[0].setThrowNumber(1);
        DiceThrows[1].setThrowNumber(1);
        //Set current player to white
        CurrentPlayer=1;
        //Load from extras player names and player kinds and create players
        if(extras!=null){
            String p1Name=extras.getString(MenuActivity.EXTRA_PLAYER1_NAME);
            String p2Name=extras.getString(MenuActivity.EXTRA_PLAYER2_NAME);
            if("Player".equals(extras.getString(MenuActivity.EXTRA_PLAYER1_KIND))){
                Players[0]=new Human(activity, p1Name);
            }
            else{
                Players[0]=new Bot(activity, p1Name, this);
            }
            if("Player".equals(extras.getString(MenuActivity.EXTRA_PLAYER2_KIND))){
                Players[1]=new Human(activity, p2Name);
            }
            else{
                Players[1]=new Bot(activity, p2Name, this);
            }
        }
        //Create board fields
        for(int i=0; i<BoardFields.length; i++){
            BoardFields[i]=new BoardFieldState();
        }
        //Initialize board fields with starting chip positions
        //White player
        BoardFields[0].setNumberOfChips(5);
        BoardFields[0].setPlayer(1);
        BoardFields[11].setNumberOfChips(2);
        BoardFields[11].setPlayer(1);
        BoardFields[16].setNumberOfChips(3);
        BoardFields[16].setPlayer(1);
        BoardFields[18].setNumberOfChips(5);
        BoardFields[18].setPlayer(1);
        //Red player
        BoardFields[4].setNumberOfChips(3);
        BoardFields[4].setPlayer(2);
        BoardFields[6].setNumberOfChips(5);
        BoardFields[6].setPlayer(2);
        BoardFields[12].setNumberOfChips(5);
        BoardFields[12].setPlayer(2);
        BoardFields[23].setNumberOfChips(2);
        BoardFields[23].setPlayer(2);
    }

    //Method used to change current player to other one
    public void changeCurrentPlayer(){
        if(CurrentPlayer==1){
            CurrentPlayer=2;
        }
        else{
            CurrentPlayer=1;
        }
    }

    //Getters and setters
    public BoardFieldState[] getBoardFields() {
        return BoardFields;
    }

    public void setBoardFields(BoardFieldState[] boardFields) {
        BoardFields = boardFields;
    }

    public DiceThrow[] getDiceThrows() {
        return DiceThrows;
    }

    public void setDiceThrows(DiceThrow[] diceThrows) {
        this.DiceThrows = diceThrows;
    }

    public Player[] getPlayers() {
        return Players;
    }

    public void setPlayers(Player[] players) {
        Players = players;
    }

    public int getCurrentPlayer() {
        return CurrentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        CurrentPlayer = currentPlayer;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public List<NextJump> getNextMoves() {
        return NextMoves;
    }

    public void setNextMoves(List<NextJump> nextMoves) {
        NextMoves = nextMoves;
    }

    public Player getCurrentObjectPlayer(){
        return Players[CurrentPlayer-1];
    }
}
