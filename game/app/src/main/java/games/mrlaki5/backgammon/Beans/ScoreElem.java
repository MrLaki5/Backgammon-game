package games.mrlaki5.backgammon.Beans;

//Class used for list representation of scores
public class ScoreElem {

    //Name of player1
    private String Player1Name;
    //Name of player2
    private String Player2Name;
    //Number of player1 wins
    private int Player1Score;
    //Number of player2 wins
    private int Player2Score;

    //Constructor used for loading new score
    public ScoreElem(String Player1Name, String Player2Name, int Player1Score, int Player2Score){
        this.Player1Name=Player1Name;
        this.Player2Name=Player2Name;
        this.Player1Score=Player1Score;
        this.Player2Score=Player2Score;
    }

    //Method used to check if row from db belongs to this score
    //(player names are same) and if they are add result to score
    //return true if row belongs to score, false if it doesn't
    public boolean checkAndAdd(String name1, String name2, int score1, int score2){
        if(Player1Name.equals(name1) && Player2Name.equals(name2)){
            Player1Score+=score1;
            Player2Score+=score2;
            return true;
        }
        if(Player2Name.equals(name1) && Player1Name.equals(name2)){
            Player1Score+=score2;
            Player2Score+=score1;
            return true;
        }
        return false;
    }

    //Getters and setters
    public String getPlayer1Name() {
        return Player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        Player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return Player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        Player2Name = player2Name;
    }

    public int getPlayer1Score() {
        return Player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        Player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return Player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        Player2Score = player2Score;
    }
}
