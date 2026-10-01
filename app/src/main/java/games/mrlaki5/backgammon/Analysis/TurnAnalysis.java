package games.mrlaki5.backgammon.Analysis;

/** Grade of one played turn, with the best play the engine found. */
public class TurnAnalysis {

    public enum Grade { EXCELLENT, GOOD, MISTAKE, BLUNDER }

    private final int turnNumber;
    private final String diceLabel;
    private final String playedNotation;
    private final String bestNotation;
    private final Grade grade;

    public TurnAnalysis(int turnNumber, String diceLabel, String playedNotation,
                        String bestNotation, Grade grade) {
        this.turnNumber = turnNumber;
        this.diceLabel = diceLabel;
        this.playedNotation = playedNotation;
        this.bestNotation = bestNotation;
        this.grade = grade;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getDiceLabel() {
        return diceLabel;
    }

    public String getPlayedNotation() {
        return playedNotation;
    }

    public String getBestNotation() {
        return bestNotation;
    }

    public Grade getGrade() {
        return grade;
    }

    /** True when the played turn matched the engine's choice closely enough to show no advice. */
    public boolean isBestPlay() {
        return playedNotation.equals(bestNotation);
    }
}
