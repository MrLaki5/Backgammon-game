package games.mrlaki5.backgammon.Analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameLogic;
import games.mrlaki5.backgammon.GameControllers.GameMoveExecutor;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.Players.BotMoveStrategy;

/**
 * Grades the turns a player actually played against every legal alternative, scoring each
 * resulting position with the engine's strongest evaluation. Runs entirely on the device.
 */
public class GameAnalyzer {

    /** Upper bound on the full-turn plays explored per turn (doubles can branch widely). */
    private static final int MAX_PLAYS_PER_TURN = 600;
    private static final double EXCELLENT_RATIO = 0.02;
    private static final double GOOD_RATIO = 0.12;
    private static final double MISTAKE_RATIO = 0.35;

    private final BotMoveStrategy evaluator = new BotMoveStrategy();
    private final int difficulty;

    public GameAnalyzer() {
        this(GamePreferences.BOT_ROYAL);
    }

    public GameAnalyzer(int difficulty) {
        this.difficulty = difficulty;
    }

    /** Grades every turn [player] played in [history], oldest first. */
    public List<TurnAnalysis> analyze(List<TurnRecord> history, int player) {
        List<TurnAnalysis> result = new ArrayList<>();
        int turnNumber = 0;
        for (TurnRecord record : history) {
            if (record.getPlayer() != player || record.getMoves().isEmpty()) {
                continue;
            }
            turnNumber++;
            TurnAnalysis analysis = analyzeTurn(record, turnNumber);
            if (analysis != null) {
                result.add(analysis);
            }
        }
        return result;
    }

    /** Share of turns played excellently or well, 0–100; 100 when there is nothing to grade. */
    public static int accuracy(List<TurnAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return 100;
        }
        int good = 0;
        for (TurnAnalysis a : analyses) {
            if (a.getGrade() == TurnAnalysis.Grade.EXCELLENT || a.getGrade() == TurnAnalysis.Grade.GOOD) {
                good++;
            }
        }
        return Math.round((good * 100f) / analyses.size());
    }

    private TurnAnalysis analyzeTurn(TurnRecord record, int turnNumber) {
        List<List<NextJump>> plays = enumeratePlays(record);
        if (plays.size() < 2) {
            return null;  // forced turn: nothing to judge
        }

        double best = -Double.MAX_VALUE;
        double worst = Double.MAX_VALUE;
        List<NextJump> bestPlay = plays.get(0);
        for (List<NextJump> play : plays) {
            double score = score(record, play);
            if (score > best) {
                best = score;
                bestPlay = play;
            }
            if (score < worst) {
                worst = score;
            }
        }

        double played = score(record, record.getMoves());
        double range = best - worst;
        TurnAnalysis.Grade grade;
        if (range <= 1e-6) {
            grade = TurnAnalysis.Grade.EXCELLENT;
        } else {
            double ratio = (best - played) / range;
            if (ratio <= EXCELLENT_RATIO) grade = TurnAnalysis.Grade.EXCELLENT;
            else if (ratio <= GOOD_RATIO) grade = TurnAnalysis.Grade.GOOD;
            else if (ratio <= MISTAKE_RATIO) grade = TurnAnalysis.Grade.MISTAKE;
            else grade = TurnAnalysis.Grade.BLUNDER;
        }

        return new TurnAnalysis(turnNumber, diceLabel(record), notation(record, record.getMoves()),
                notation(record, bestPlay), grade);
    }

    private double score(TurnRecord record, List<NextJump> play) {
        Model model = modelFor(record);
        GameMoveExecutor executor = new GameMoveExecutor(model);
        for (NextJump move : play) {
            executor.applyMove(move);
        }
        return evaluator.evaluatePosition(model, record.getPlayer(), difficulty);
    }

    /** All legal full-turn plays from the recorded position, de-duplicated by resulting position. */
    private List<List<NextJump>> enumeratePlays(TurnRecord record) {
        List<List<NextJump>> plays = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        expand(modelFor(record), new ArrayList<NextJump>(), plays, seen);
        return plays;
    }

    private void expand(Model model, List<NextJump> played, List<List<NextJump>> plays,
                        Set<String> seen) {
        if (plays.size() >= MAX_PLAYS_PER_TURN) {
            return;
        }
        GameLogic logic = new GameLogic(model);
        List<NextJump> moves = logic.calculateMoves(model.getBoardFields(),
                model.getCurrentPlayer(), model.getDiceThrows());
        if (moves.isEmpty()) {
            if (!played.isEmpty() && seen.add(signature(model))) {
                plays.add(new ArrayList<>(played));
            }
            return;
        }
        for (NextJump move : moves) {
            Model next = copyModel(model);
            new GameMoveExecutor(next).applyMove(move);
            played.add(move);
            expand(next, played, plays, seen);
            played.remove(played.size() - 1);
        }
    }

    private static String signature(Model model) {
        StringBuilder sb = new StringBuilder();
        for (BoardFieldState field : model.getBoardFields()) {
            sb.append(field.getNumberOfChips()).append(',').append(field.getPlayer())
                    .append(',').append(field.getPinnedPlayer()).append(';');
        }
        return sb.toString();
    }

    private Model modelFor(TurnRecord record) {
        Model model = new Model();
        model.setVariant(record.getVariant());
        model.setBoardFields(record.copyStartBoard());
        model.setDiceThrows(record.copyStartDice());
        model.setCurrentPlayer(record.getPlayer());
        model.setState(2);
        return model;
    }

    private Model copyModel(Model source) {
        Model copy = new Model();
        copy.setVariant(source.getVariant());
        copy.setBoardFields(copyBoard(source.getBoardFields()));
        copy.setDiceThrows(copyDice(source.getDiceThrows()));
        copy.setCurrentPlayer(source.getCurrentPlayer());
        copy.setState(source.getState());
        copy.setTurnsPlayed(source.getTurnsPlayed());
        copy.setHeadMovesThisTurn(source.getHeadMovesThisTurn());
        return copy;
    }

    private static BoardFieldState[] copyBoard(BoardFieldState[] source) {
        BoardFieldState[] copy = new BoardFieldState[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].copy();
        }
        return copy;
    }

    private static DiceThrow[] copyDice(DiceThrow[] source) {
        DiceThrow[] copy = new DiceThrow[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new DiceThrow(source[i].getThrowNumber(), source[i].getAlreadyUsed());
        }
        return copy;
    }

    private static String diceLabel(TurnRecord record) {
        DiceThrow[] dice = record.copyStartDice();
        return dice[0].getThrowNumber() + "-" + dice[1].getThrowNumber();
    }

    /** Moves in "from/to" notation using the player's own point numbers, "off" for bear-offs. */
    private static String notation(TurnRecord record, List<NextJump> play) {
        GameLogic logic = new GameLogic(record.getVariant());
        StringBuilder sb = new StringBuilder();
        for (NextJump move : play) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(point(logic, move.getSrcField(), record.getPlayer()))
                    .append('/')
                    .append(point(logic, move.getDstField(), record.getPlayer()));
        }
        return sb.toString();
    }

    private static String point(GameLogic logic, int field, int player) {
        int real = logic.calculateRealPosition(field, player);
        if (real == 100) return "off";
        if (real == 0) return "bar";
        return String.valueOf(real);
    }
}
