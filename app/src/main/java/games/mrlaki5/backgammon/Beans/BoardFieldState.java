package games.mrlaki5.backgammon.Beans;

//class for representation of every field on board
public class BoardFieldState {

    //Number of chips on field
    int NumberOfChips;  //0-No chips
    //Player whos chips are on board
    int Player; //0-Nobody, 1-Player1, 2-Player2
    //Player whose single checker is pinned under this stack (Plakoto), 0 if none
    int PinnedPlayer;

    //Constructor used when loading new game
    public BoardFieldState() {
        NumberOfChips=0;
        Player=0;
    }

    //Constructor used when loading state from file
    public BoardFieldState(int NumberOfChips, int Player){
        this(NumberOfChips, Player, 0);
    }

    public BoardFieldState(int NumberOfChips, int Player, int PinnedPlayer){
        this.NumberOfChips=NumberOfChips;
        this.Player=Player;
        this.PinnedPlayer=PinnedPlayer;
    }

    public BoardFieldState copy(){
        return new BoardFieldState(NumberOfChips, Player, PinnedPlayer);
    }

    //Checkers drawn on this field: the stack plus a pinned checker underneath
    public int getVisibleChips(){
        return NumberOfChips + (PinnedPlayer != 0 ? 1 : 0);
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

    public int getPinnedPlayer() {
        return PinnedPlayer;
    }

    public void setPinnedPlayer(int pinnedPlayer) {
        PinnedPlayer = pinnedPlayer;
    }
}
