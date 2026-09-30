package games.mrlaki5.backgammon.Players;

import com.royalbackgammon.core.model.GameResult;
import com.royalbackgammon.core.model.WinType;
import com.royalbackgammon.core.logic.BackgammonRules;
import com.royalbackgammon.core.logic.PositionMapper;
import com.royalbackgammon.core.variant.RuleFamily;
import com.royalbackgammon.core.variant.Variant;

import java.util.List;
import java.util.Random;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameLogic;
import games.mrlaki5.backgammon.GameControllers.GameMoveExecutor;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.GamePreferences;

public class BotMoveStrategy {
    private static final RollOutcome[] ROLL_OUTCOMES = createRollOutcomes();

    public NextJump chooseMove(Model model, List<NextJump> moves, int difficulty, Random random) {
        if (moves == null || moves.isEmpty()) {
            return null;
        }

        NextJump best = moves.get(0);
        double bestScore = -Double.MAX_VALUE;
        SearchProfile profile = profileFor(difficulty);
        for (NextJump move : moves) {
            Model afterMove = copyModel(model);
            new GameMoveExecutor(afterMove).applyMove(move);
            SearchBudget budget = new SearchBudget(profile.nodeBudget);
            double score = continueTurnOrRoll(afterMove, model.getCurrentPlayer(),
                    profile.lookaheadRolls, profile, random, budget);
            score += tacticalMoveBonus(model, move, difficulty);
            score += random.nextDouble() * profile.noise;
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }

    /** Acey-deucey: names the double that leaves the best position after playing it out. */
    public int chooseBonusDouble(Model model, int difficulty, Random random) {
        SearchProfile profile = profileFor(difficulty);
        int bestValue = 6;
        double bestScore = -Double.MAX_VALUE;
        for (int value = 1; value <= 6; value++) {
            Model copy = copyModel(model);
            copy.setDiceThrows(dice(value, value));
            GameLogic logic = new GameLogic(copy);
            GameMoveExecutor executor = new GameMoveExecutor(copy);
            while (true) {
                List<NextJump> moves = logic.calculateMoves(copy.getBoardFields(),
                        copy.getCurrentPlayer(), copy.getDiceThrows());
                if (moves.isEmpty()) break;
                NextJump move = chooseMove(copy, moves, difficulty, random);
                executor.applyMove(move);
            }
            double score = evaluateBoard(copy, model.getCurrentPlayer(), profile);
            if (score > bestScore) {
                bestScore = score;
                bestValue = value;
            }
        }
        return bestValue;
    }

    private double continueTurnOrRoll(Model model, int rootPlayer, int rollsRemaining,
                                      SearchProfile profile, Random random,
                                      SearchBudget budget) {
        if (!budget.tryVisit()) {
            return evaluateBoard(model, rootPlayer, profile);
        }
        GameLogic logic = new GameLogic(model);
        List<NextJump> moves = logic.calculateMoves(model.getBoardFields(),
                model.getCurrentPlayer(), model.getDiceThrows());
        if (!moves.isEmpty()) {
            boolean maximizing = model.getCurrentPlayer() == rootPlayer;
            double best = maximizing ? -Double.MAX_VALUE : Double.MAX_VALUE;
            for (NextJump move : moves) {
                Model next = copyModel(model);
                new GameMoveExecutor(next).applyMove(move);
                double score = continueTurnOrRoll(next, rootPlayer, rollsRemaining,
                        profile, random, budget);
                best = maximizing ? Math.max(best, score) : Math.min(best, score);
            }
            return best;
        }

        if (rollsRemaining <= 0 || logic.whatPartOfGame(model.getBoardFields(), rootPlayer) == 2
                || logic.whatPartOfGame(model.getBoardFields(), opponentOf(rootPlayer)) == 2) {
            return evaluateBoard(model, rootPlayer, profile);
        }

        Model nextTurn = copyModel(model);
        nextTurn.onTurnEnded();
        nextTurn.changeCurrentPlayer();
        return expectedRollValue(nextTurn, rootPlayer, rollsRemaining - 1, profile, random,
                budget);
    }

    private double expectedRollValue(Model model, int rootPlayer, int rollsRemaining,
                                     SearchProfile profile, Random random,
                                     SearchBudget budget) {
        double expected = 0.0;
        for (RollOutcome outcome : ROLL_OUTCOMES) {
            Model rolled = copyModel(model);
            rolled.setDiceThrows(dice(outcome.first, outcome.second));
            expected += outcome.probability * continueTurnOrRoll(rolled, rootPlayer,
                    rollsRemaining, profile, random, budget);
        }
        return expected;
    }

    private double scoreSingleMove(Model model, NextJump move, int difficulty) {
        Model copy = copyModel(model);
        double tacticalBonus = tacticalMoveBonus(model, move, difficulty);
        new GameMoveExecutor(copy).applyMove(move);
        SearchProfile profile = profileFor(difficulty);
        return evaluateBoard(copy, model.getCurrentPlayer(), profile)
                - evaluateBoard(model, model.getCurrentPlayer(), profile) * 0.72
                + move.getJumpNumber() * 1.4
                + tacticalBonus;
    }

    private double tacticalMoveBonus(Model model, NextJump move, int difficulty) {
        BoardFieldState[] board = model.getBoardFields();
        int player = model.getCurrentPlayer();
        int opponent = player == 1 ? 2 : 1;
        int dst = move.getDstField();
        double bonus = 0.0;
        if (dst == 26 || dst == 27) {
            bonus += difficulty == GamePreferences.BOT_ROYAL ? 190.0 : 135.0;
        }
        if (board[dst].getPlayer() == opponent && board[dst].getNumberOfChips() == 1) {
            bonus += difficulty >= GamePreferences.BOT_HARD ? 165.0 : 95.0;
        }
        if (board[move.getSrcField()].getNumberOfChips() == 1) {
            bonus += 32.0;
        }
        return bonus;
    }

    /** Position score for [player] at [difficulty]; used by the post-game analyzer. */
    public double evaluatePosition(Model model, int player, int difficulty) {
        return evaluateBoard(model, player, profileFor(difficulty));
    }

    private double evaluateBoard(Model model, int player, SearchProfile profile) {
        if (model.getVariant().getFamily() == RuleFamily.RUNNING) {
            return evaluateRunning(model, player, profile)
                    - evaluateRunning(model, opponentOf(player), profile) * 0.9;
        }
        BoardFieldState[] board = model.getBoardFields();
        int opponent = player == 1 ? 2 : 1;
        GameLogic logic = new GameLogic(model);
        double score = model.getVariant().getFamily() == RuleFamily.PINNING
                ? pinningTerms(model, player, logic, profile) : 0.0;

        for (int i = 0; i < board.length; i++) {
            int chips = board[i].getNumberOfChips();
            int owner = board[i].getPlayer();
            if (chips <= 0 || owner == 0) {
                continue;
            }
            int sign = owner == player ? 1 : -1;
            int real = normalizedRealPosition(logic, i, owner);
            boolean home = real >= 19 && real <= 24;
            boolean bar = (owner == 1 && i == 24) || (owner == 2 && i == 25);
            boolean borneOff = (owner == 1 && i == 27) || (owner == 2 && i == 26);

            if (borneOff) score += sign * chips * profile.borneOffWeight;
            else if (bar) score -= sign * chips * profile.barPenalty;
            else score += sign * chips * real * profile.progressWeight;

            if (!borneOff && !bar) {
                if (chips == 1) score -= sign * blotPenalty(board, i, owner, profile);
                if (chips == 2) score += sign * profile.pointWeight;
                if (chips >= 3) score += sign * Math.min(profile.stackLimit,
                        profile.pointWeight * 0.45 + chips * 4.0);
                if (home) score += sign * chips * profile.homeWeight;
            }
        }

        score += madePointRun(board, player, logic) * profile.primeWeight;
        score -= madePointRun(board, opponent, logic) * profile.opponentPrimeWeight;
        return score;
    }

    /**
     * Plakoto extras on top of the hitting evaluation: a decisive mother result, pinned checkers
     * (still progressing, but frozen and costly like a hit), and the value of pinning early.
     */
    private double pinningTerms(Model model, int player, GameLogic logic, SearchProfile profile) {
        GameResult result = logic.calculateResult();
        if (result != null) {
            if (result.getWinType() == WinType.DRAW) return 0.0;
            return result.getWinner() == player ? 100000.0 : -100000.0;
        }
        BoardFieldState[] board = model.getBoardFields();
        double score = 0.0;
        for (int i = 0; i < 24; i++) {
            int pinned = board[i].getPinnedPlayer();
            if (pinned == 0) continue;
            int sign = pinned == player ? -1 : 1;
            int real = logic.calculateRealPosition(i, pinned);
            // Pinned deep in the pinner's home is worst: it will wait there for a long time
            score += sign * (profile.barPenalty + (25 - real) * profile.progressWeight * 0.5);
            score -= sign * real * profile.progressWeight;
        }
        return score;
    }

    /**
     * Running-family score for [side]: race progress, borne-off and home checkers, points held in
     * front of the opponent's rearmost checker (longest run weighted most), and a late-head penalty.
     */
    private double evaluateRunning(Model model, int side, SearchProfile profile) {
        BoardFieldState[] board = model.getBoardFields();
        Variant variant = model.getVariant();
        int opponent = opponentOf(side);
        int borneOffIndex = side == 1 ? 27 : 26;
        double score = board[borneOffIndex].getNumberOfChips() * profile.borneOffWeight;

        int opponentRearmost = 25;
        for (int i = 0; i < 24; i++) {
            int chips = board[i].getNumberOfChips();
            if (chips <= 0) continue;
            if (board[i].getPlayer() == side) {
                int real = PositionMapper.toReal(i, side, variant);
                score += chips * real * profile.progressWeight;
                if (real >= 19) score += chips * profile.homeWeight;
            } else if (board[i].getPlayer() == opponent) {
                opponentRearmost = Math.min(opponentRearmost,
                        PositionMapper.toReal(i, opponent, variant));
            }
        }

        int held = 0;
        int run = 0;
        int longestRun = 0;
        for (int oppReal = opponentRearmost + 1; oppReal <= 24; oppReal++) {
            int field = PositionMapper.toMatrix(oppReal, opponent, variant);
            if (board[field].getPlayer() == side && board[field].getNumberOfChips() > 0) {
                held++;
                run++;
                longestRun = Math.max(longestRun, run);
            } else {
                run = 0;
            }
        }
        score += held * profile.pointWeight * 0.35 + longestRun * longestRun * profile.primeWeight * 0.6;

        int head = BackgammonRules.headIndex(side, variant);
        if (board[head].getPlayer() == side && model.getTurnsPlayed() > 12) {
            score -= board[head].getNumberOfChips() * profile.pointWeight * 0.25;
        }
        return score;
    }

    private double blotPenalty(BoardFieldState[] board, int field, int owner,
                               SearchProfile profile) {
        int opponent = owner == 1 ? 2 : 1;
        GameLogic logic = new GameLogic(new Model());
        int real = logic.calculateRealPosition(field, owner);
        double penalty = profile.blotPenalty;
        for (int i = 0; i < 26; i++) {
            if (board[i].getPlayer() != opponent || board[i].getNumberOfChips() <= 0) {
                continue;
            }
            int oppRealAgainstOwner = 25 - logic.calculateRealPosition(i, opponent);
            int distance = real - oppRealAgainstOwner;
            if (distance >= 1 && distance <= 6) {
                penalty += (7 - distance) * profile.directShotPenalty;
            }
        }
        return penalty;
    }

    private int normalizedRealPosition(GameLogic logic, int field, int owner) {
        int real = logic.calculateRealPosition(field, owner);
        if (real == 100) {
            return 25;
        }
        return real;
    }

    private int madePointRun(BoardFieldState[] board, int player, GameLogic logic) {
        int best = 0;
        int current = 0;
        for (int real = 1; real <= 24; real++) {
            int field = logic.calculateMatrixPosition(real, player);
            if (board[field].getPlayer() == player && board[field].getNumberOfChips() >= 2) {
                current++;
                best = Math.max(best, current);
            }
            else {
                current = 0;
            }
        }
        return best;
    }

    private Model copyModel(Model source) {
        Model copy = new Model();
        BoardFieldState[] board = new BoardFieldState[source.getBoardFields().length];
        for (int i = 0; i < board.length; i++) {
            BoardFieldState field = source.getBoardFields()[i];
            board[i] = field.copy();
        }
        copy.setBoardFields(board);
        copy.setCurrentPlayer(source.getCurrentPlayer());
        copy.setState(source.getState());
        copy.setDiceThrows(copyDice(source.getDiceThrows()));
        copy.setVariant(source.getVariant());
        copy.setTurnsPlayed(source.getTurnsPlayed());
        copy.setHeadMovesThisTurn(source.getHeadMovesThisTurn());
        return copy;
    }

    private DiceThrow[] copyDice(DiceThrow[] source) {
        DiceThrow[] dice = new DiceThrow[source.length];
        for (int i = 0; i < source.length; i++) {
            dice[i] = new DiceThrow(source[i].getThrowNumber(), source[i].getAlreadyUsed());
        }
        return dice;
    }

    private DiceThrow[] dice(int first, int second) {
        DiceThrow[] dice = new DiceThrow[4];
        dice[0] = new DiceThrow(first);
        dice[1] = new DiceThrow(second);
        if (first == second) {
            dice[2] = new DiceThrow(first);
            dice[3] = new DiceThrow(first);
        }
        else {
            dice[2] = new DiceThrow(0, 1);
            dice[3] = new DiceThrow(0, 1);
        }
        return dice;
    }

    private int opponentOf(int player) {
        return player == 1 ? 2 : 1;
    }

    private SearchProfile profileFor(int difficulty) {
        switch (difficulty) {
            case GamePreferences.BOT_EASY:
                return new SearchProfile(0, 70.0, 82.0, 62.0, 1.8,
                        10.0, 12.0, 22.0, 3.0, 3.0, 5.0, 1.5, 80);
            case GamePreferences.BOT_HARD:
                return new SearchProfile(1, 2.0, 116.0, 105.0, 3.4,
                        36.0, 40.0, 50.0, 8.5, 15.0, 13.0, 6.0, 1800);
            case GamePreferences.BOT_ROYAL:
                return new SearchProfile(1, 0.35, 126.0, 118.0, 3.8,
                        44.0, 48.0, 58.0, 10.0, 20.0, 15.0, 8.0, 3600);
            case GamePreferences.BOT_MEDIUM:
            default:
                return new SearchProfile(1, 7.0, 106.0, 92.0, 3.0,
                        26.0, 32.0, 42.0, 6.5, 10.0, 10.0, 4.5, 520);
        }
    }

    private static RollOutcome[] createRollOutcomes() {
        RollOutcome[] outcomes = new RollOutcome[21];
        int index = 0;
        for (int first = 1; first <= 6; first++) {
            for (int second = first; second <= 6; second++) {
                outcomes[index++] = new RollOutcome(first, second,
                        first == second ? 1.0 / 36.0 : 2.0 / 36.0);
            }
        }
        return outcomes;
    }

    private static final class SearchProfile {
        final int lookaheadRolls;
        final double noise;
        final double borneOffWeight;
        final double barPenalty;
        final double progressWeight;
        final double blotPenalty;
        final double pointWeight;
        final double stackLimit;
        final double homeWeight;
        final double primeWeight;
        final double opponentPrimeWeight;
        final double directShotPenalty;
        final int nodeBudget;

        SearchProfile(int lookaheadRolls, double noise, double borneOffWeight,
                      double barPenalty, double progressWeight, double blotPenalty,
                      double pointWeight, double stackLimit, double homeWeight,
                      double primeWeight, double opponentPrimeWeight,
                      double directShotPenalty, int nodeBudget) {
            this.lookaheadRolls = lookaheadRolls;
            this.noise = noise;
            this.borneOffWeight = borneOffWeight;
            this.barPenalty = barPenalty;
            this.progressWeight = progressWeight;
            this.blotPenalty = blotPenalty;
            this.pointWeight = pointWeight;
            this.stackLimit = stackLimit;
            this.homeWeight = homeWeight;
            this.primeWeight = primeWeight;
            this.opponentPrimeWeight = opponentPrimeWeight;
            this.directShotPenalty = directShotPenalty;
            this.nodeBudget = nodeBudget;
        }
    }

    private static final class SearchBudget {
        private int remainingNodes;

        SearchBudget(int remainingNodes) {
            this.remainingNodes = remainingNodes;
        }

        boolean tryVisit() {
            if (remainingNodes <= 0) {
                return false;
            }
            remainingNodes--;
            return true;
        }
    }

    private static final class RollOutcome {
        final int first;
        final int second;
        final double probability;

        RollOutcome(int first, int second, double probability) {
            this.first = first;
            this.second = second;
            this.probability = probability;
        }
    }
}
