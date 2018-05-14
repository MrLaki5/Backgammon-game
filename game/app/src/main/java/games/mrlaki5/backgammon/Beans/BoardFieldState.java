package games.mrlaki5.backgammon.Beans;

public class BoardFieldState {

    //0-No chips
    int NumberOfChips;
    //0-Nobody, 1-Player1, 2-Player2;
    int Player;

    public BoardFieldState() {
        NumberOfChips=0;
        Player=0;
    }

    public BoardFieldState(int NumberOfChips, int Player){
        this.NumberOfChips=NumberOfChips;
        this.Player=Player;
    }

    public int getNumberOfChips() {
        return NumberOfChips;
    }

    public void setNumberOfChips(int numberOfChips) {
        NumberOfChips = numberOfChips;
    }

    public int getPlayer() {
        return Player;
    }

    public void setPlayer(int player) {
        Player = player;
    }
}
