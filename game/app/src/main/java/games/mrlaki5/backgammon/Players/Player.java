package games.mrlaki5.backgammon.Players;

import games.mrlaki5.backgammon.GameControllers.GameActivity;

//Abstract class for player representation
public abstract class Player {

    //Context of current game activity (used for playing sounds...)
    private GameActivity CurrGame;
    //Name of this player
    private String PlayerName;
    //Used for synchronization between UI thread (GameActivity) and GameTask
    private int WaitCond;

    //Constructor for player
    public Player(GameActivity currGame, String playerName) {
        CurrGame = currGame;
        PlayerName = playerName;
    }

    //Getters and setters
    public int getWaitCond() {
        return WaitCond;
    }

    public void setWaitCond(int waitCond) {
        WaitCond = waitCond;
    }

    public String getPlayerName() {
        return PlayerName;
    }

    public GameActivity getCurrGame() {
        return CurrGame;
    }

    public void setCurrGame(GameActivity currGame) {
        CurrGame = currGame;
    }

    public void setPlayerName(String playerName) {
        PlayerName = playerName;
    }

    //Abstract methods
    //method called when player needs to move chips
    public abstract void actionMove();

    //method called when player needs to roll dices
    public abstract void actionRoll();
}
