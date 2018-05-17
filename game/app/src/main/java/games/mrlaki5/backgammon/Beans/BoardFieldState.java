package games.mrlaki5.backgammon.Beans;

//class for representation of every field on board
public class BoardFieldState {

    //Number of chips on field
    int NumberOfChips;  //0-No chips
    //Player whos chips are on board
    int Player; //0-Nobody, 1-Player1, 2-Player2

    //Constructor used when loading new game
    public BoardFieldState() {
        NumberOfChips=0;
        Player=0;
    }

    //Constructor used when loading state from file
    public BoardFieldState(int NumberOfChips, int Player){
        this.NumberOfChips=NumberOfChips;
        this.Player=Player;
    }

    //Getters and setters
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
