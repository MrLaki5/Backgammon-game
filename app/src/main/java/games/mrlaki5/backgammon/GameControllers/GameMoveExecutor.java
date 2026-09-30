package games.mrlaki5.backgammon.GameControllers;

import com.royalbackgammon.core.logic.BackgammonRules;
import com.royalbackgammon.core.model.GameState;
import com.royalbackgammon.core.variant.RuleFamily;
import com.royalbackgammon.core.variant.Variant;

import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameModel.Model;

// Applies checker moves to the model without depending on Android UI classes.
public class GameMoveExecutor {
    private final Model model;

    public static class MoveResult {
        private final boolean applied;
        private final int sourceField;
        private final int destinationField;
        private final boolean hit;

        private MoveResult(boolean applied, int sourceField, int destinationField, boolean hit) {
            this.applied = applied;
            this.sourceField = sourceField;
            this.destinationField = destinationField;
            this.hit = hit;
        }

        public boolean isApplied() {
            return applied;
        }

        public int getSourceField() {
            return sourceField;
        }

        public int getDestinationField() {
            return destinationField;
        }

        public boolean isHit() {
            return hit;
        }
    }

    public GameMoveExecutor(Model model) {
        this.model = model;
    }

    public boolean applyPickedUpMove(int srcField, int dstField, List<NextJump> legalMoves) {
        return applyPickedUpMoveWithResult(srcField, dstField, legalMoves).isApplied();
    }

    public MoveResult applyPickedUpMoveWithResult(int srcField, int dstField,
                                                  List<NextJump> legalMoves) {
        NextJump jump = findMove(srcField, dstField, legalMoves);
        if (jump == null) {
            restorePickedUpChecker(srcField);
            return new MoveResult(false, srcField, dstField, false);
        }

        countHeadMove(srcField);
        countBarEntry(srcField);
        releasePinnedIfUncovered(srcField);
        model.recordMove(jump);
        consumeDice(jump.getJumpNumber());
        boolean hit = placeChecker(dstField);
        return new MoveResult(true, srcField, dstField, hit);
    }

    public MoveResult applyMove(NextJump jump) {
        BoardFieldState[] board = model.getBoardFields();
        int src = jump.getSrcField();

        board[src].setNumberOfChips(board[src].getNumberOfChips() - 1);
        if (board[src].getNumberOfChips() == 0) {
            board[src].setPlayer(0);
        }
        releasePinnedIfUncovered(src);

        countHeadMove(src);
        countBarEntry(src);
        model.recordMove(jump);
        consumeDice(jump.getJumpNumber());
        boolean hit = placeChecker(jump.getDstField());
        return new MoveResult(true, src, jump.getDstField(), hit);
    }

    private NextJump findMove(int srcField, int dstField, List<NextJump> legalMoves) {
        for (NextJump jump : legalMoves) {
            if (jump.getSrcField() == srcField && jump.getDstField() == dstField) {
                return jump;
            }
        }
        return null;
    }

    private void restorePickedUpChecker(int srcField) {
        BoardFieldState[] board = model.getBoardFields();
        board[srcField].setNumberOfChips(board[srcField].getNumberOfChips() + 1);
        if (board[srcField].getNumberOfChips() == 1) {
            board[srcField].setPlayer(model.getCurrentPlayer());
        }
    }

    // Acey-deucey: a checker coming in off the bar clears one of the hits waiting there
    private void countBarEntry(int srcField) {
        if (model.getVariant().getStartsOnBar()
                && srcField == GameState.barIndex(model.getCurrentPlayer())) {
            model.setHitsOnBar(model.getCurrentPlayer(),
                    model.getHitsOnBar(model.getCurrentPlayer()) - 1);
        }
    }

    // Running family: track checkers leaving the head for the one-per-turn rule
    private void countHeadMove(int srcField) {
        Variant variant = model.getVariant();
        if (variant.getFamily() == RuleFamily.RUNNING
                && srcField == BackgammonRules.headIndex(model.getCurrentPlayer(), variant)) {
            model.setHeadMovesThisTurn(model.getHeadMovesThisTurn() + 1);
        }
    }

    // Plakoto: once the stack above a pinned checker is gone, that checker is free again
    private void releasePinnedIfUncovered(int field) {
        BoardFieldState state = model.getBoardFields()[field];
        if (state.getNumberOfChips() == 0 && state.getPinnedPlayer() != 0) {
            state.setNumberOfChips(1);
            state.setPlayer(state.getPinnedPlayer());
            state.setPinnedPlayer(0);
        }
    }

    private void consumeDice(int throwNumber) {
        for (DiceThrow dice : model.getDiceThrows()) {
            if (dice.getThrowNumber() == throwNumber && dice.getAlreadyUsed() == 0) {
                dice.setAlreadyUsed(1);
                return;
            }
        }
    }

    private boolean placeChecker(int dstField) {
        BoardFieldState[] board = model.getBoardFields();
        int player = model.getCurrentPlayer();
        int destinationPlayer = board[dstField].getPlayer();
        boolean lone = board[dstField].getNumberOfChips() == 1 && destinationPlayer != player;
        if (lone && model.getVariant().getFamily() == RuleFamily.PINNING) {
            board[dstField].setPinnedPlayer(destinationPlayer);
            board[dstField].setPlayer(player);
            return false;
        }
        boolean hit = lone;

        if (hit) {
            model.setHitsOnBar(destinationPlayer, model.getHitsOnBar(destinationPlayer) + 1);
            int bar = 23 + destinationPlayer;
            board[bar].setNumberOfChips(board[bar].getNumberOfChips() + 1);
            board[bar].setPlayer(destinationPlayer);
        } else {
            board[dstField].setNumberOfChips(board[dstField].getNumberOfChips() + 1);
        }

        if (board[dstField].getNumberOfChips() == 1) {
            board[dstField].setPlayer(player);
        }
        return hit;
    }
}
