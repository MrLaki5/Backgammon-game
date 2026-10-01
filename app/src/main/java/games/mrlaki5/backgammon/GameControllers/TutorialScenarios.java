package games.mrlaki5.backgammon.GameControllers;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameModel.Model;

/**
 * Scripted board states for each tutorial step.
 * Each step sets up a board where the player needs to do exactly ONE thing.
 * After they do it, the next step loads a new board.
 *
 * Flow:
 *   Step 0 (INTRO): Show intro text, player taps "Next" button
 *   Step 1 (ROLL): Board ready, dice unrolled → player rolls → auto-advance
 *   Step 2 (MOVE): Dice pre-rolled, clear move available → player moves → auto-advance
 *   Step 3 (HIT): Red blot in path, die set to hit → player hits → auto-advance
 *   Step 4 (BAR): White on bar, die set to re-enter → player enters → auto-advance
 *   Step 5 (BEAR_OFF): All home, die set to bear off → player bears off → auto-advance
 *   Step 6 (DONE): Congratulations, auto-dismiss
 */
public final class TutorialScenarios {

    public static final int STEP_INTRO = 0;
    public static final int STEP_ROLL = 1;
    public static final int STEP_MOVE = 2;
    public static final int STEP_HIT = 3;
    public static final int STEP_BAR = 4;
    public static final int STEP_BEAR_OFF = 5;
    public static final int STEP_DONE = 6;
    public static final int TOTAL_STEPS = 7;

    private TutorialScenarios() {}

    /**
     * Sets up the board for the given tutorial step.
     * Always sets currentPlayer = 1 (White = human learner).
     */
    public static void applyScenario(Model model, int step) {
        clearBoard(model);
        model.setCurrentPlayer(1);

        switch (step) {
            case STEP_INTRO:
            case STEP_ROLL:
                // Standard starting position, dice not yet rolled
                setupStandard(model);
                setDiceEmpty(model);
                model.setState(3); // State: roll dice
                break;

            case STEP_MOVE:
                // Simple board with dice already rolled (6, 1)
                // White at matrix 0 (real 12) can move 6 to matrix 6 (real 6... wait)
                // Let's use: White at matrix 18 (real 19), die=4 → real 23 = matrix 22 (empty)
                setupForMove(model);
                setDice(model, 4, 0);
                markDiceUsed(model, 1); // only first die active
                model.setState(2); // State: move
                break;

            case STEP_HIT:
                // Red blot 3 away from White checker
                // White at matrix 5 (real 7), die=3, target matrix 2 (real 10) has 1 Red
                setupForHit(model);
                setDice(model, 3, 0);
                markDiceUsed(model, 1);
                model.setState(2);
                break;

            case STEP_BAR:
                // White has 1 checker on bar (index 24), die=3
                // Target: real 3 = matrix 9 (must be empty)
                setupForBar(model);
                setDice(model, 3, 0);
                markDiceUsed(model, 1);
                model.setState(2);
                break;

            case STEP_BEAR_OFF:
                // All White in home, 1 checker at real 24 (matrix 23), die=1 → bear off
                setupForBearOff(model);
                setDice(model, 1, 0);
                markDiceUsed(model, 1);
                model.setState(2);
                break;

            case STEP_DONE:
                // Just show message, don't change board
                break;
        }
    }

    // ========== Board Setups ==========

    private static void setupStandard(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        // White (player 1) - standard start
        b[0].setNumberOfChips(5);  b[0].setPlayer(1);
        b[11].setNumberOfChips(2); b[11].setPlayer(1);
        b[16].setNumberOfChips(3); b[16].setPlayer(1);
        b[18].setNumberOfChips(5); b[18].setPlayer(1);
        // Red (player 2) - standard start
        b[4].setNumberOfChips(3);  b[4].setPlayer(2);
        b[6].setNumberOfChips(5);  b[6].setPlayer(2);
        b[12].setNumberOfChips(5); b[12].setPlayer(2);
        b[23].setNumberOfChips(2); b[23].setPlayer(2);
    }

    /**
     * Move scenario: White has a checker that can clearly move.
     * White at matrix 18 (real 19), die = 4 → target real 23 = matrix 22 (empty).
     */
    private static void setupForMove(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        // White checkers
        b[18].setNumberOfChips(5); b[18].setPlayer(1); // real 19 — source
        b[16].setNumberOfChips(5); b[16].setPlayer(1); // real 17
        b[0].setNumberOfChips(5);  b[0].setPlayer(1);  // real 12
        // Red away from target area
        b[6].setNumberOfChips(8);  b[6].setPlayer(2);
        b[12].setNumberOfChips(7); b[12].setPlayer(2);
        // matrix 22 (target, real 23) is EMPTY — clear destination
    }

    /**
     * Hit scenario: Red has a single blot exactly where White lands with die=3.
     * White at matrix 5 (real 7) + die 3 = real 10 = matrix 2.
     * Red blot at matrix 2 (1 checker, player 2).
     */
    private static void setupForHit(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        // White
        b[5].setNumberOfChips(3);  b[5].setPlayer(1);  // real 7 — source
        b[0].setNumberOfChips(5);  b[0].setPlayer(1);  // real 12
        b[18].setNumberOfChips(4); b[18].setPlayer(1); // real 19
        b[16].setNumberOfChips(3); b[16].setPlayer(1); // real 17
        // Red BLOT at matrix 2 (real 10) — exactly 3 away from White at real 7
        b[2].setNumberOfChips(1);  b[2].setPlayer(2);  // *** BLOT ***
        // Red safe elsewhere
        b[12].setNumberOfChips(7); b[12].setPlayer(2);
        b[6].setNumberOfChips(7);  b[6].setPlayer(2);
    }

    /**
     * Bar scenario: White has 1 checker on bar (index 24).
     * Die = 3, so White enters at real 3 = matrix 9.
     * matrix 9 must be empty or have max 1 Red.
     */
    private static void setupForBar(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        // White on bar
        b[24].setNumberOfChips(1); b[24].setPlayer(1); // BAR
        // Rest of White
        b[18].setNumberOfChips(5); b[18].setPlayer(1);
        b[16].setNumberOfChips(5); b[16].setPlayer(1);
        b[0].setNumberOfChips(4);  b[0].setPlayer(1);
        // Red — keep matrix 9 EMPTY (that's where White will enter)
        b[12].setNumberOfChips(8); b[12].setPlayer(2);
        b[6].setNumberOfChips(7);  b[6].setPlayer(2);
    }

    /**
     * Bear-off scenario: All White in home board.
     * 1 checker at real 24 (matrix 23), die = 1 → bears off.
     * 14 others already borne off (index 27).
     */
    private static void setupForBearOff(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        // 14 White already borne off
        b[27].setNumberOfChips(14); b[27].setPlayer(1);
        // Last checker at matrix 23 (real 24)
        b[23].setNumberOfChips(1);  b[23].setPlayer(1);
        // Red somewhere far
        b[0].setNumberOfChips(10);  b[0].setPlayer(2);
        b[6].setNumberOfChips(5);   b[6].setPlayer(2);
    }

    // ========== Helpers ==========

    private static void clearBoard(Model model) {
        BoardFieldState[] b = model.getBoardFields();
        for (int i = 0; i < b.length; i++) {
            b[i].setNumberOfChips(0);
            b[i].setPlayer(0);
        }
    }

    private static void setDice(Model model, int d1, int d2) {
        DiceThrow[] dice = model.getDiceThrows();
        dice[0] = new DiceThrow(d1);
        dice[1] = new DiceThrow(d2 > 0 ? d2 : 0);
        dice[2] = new DiceThrow(0);
        dice[3] = new DiceThrow(0);
        if (d2 <= 0) dice[1].setAlreadyUsed(1);
        dice[2].setAlreadyUsed(1);
        dice[3].setAlreadyUsed(1);
    }

    private static void setDiceEmpty(Model model) {
        DiceThrow[] dice = model.getDiceThrows();
        for (int i = 0; i < 4; i++) {
            dice[i] = new DiceThrow(0);
            dice[i].setAlreadyUsed(1);
        }
        dice[0].setThrowNumber(1);
        dice[1].setThrowNumber(1);
    }

    /** Marks dice slots beyond index `activeCount` as used. */
    private static void markDiceUsed(Model model, int activeCount) {
        DiceThrow[] dice = model.getDiceThrows();
        for (int i = activeCount; i < 4; i++) {
            dice[i].setAlreadyUsed(1);
        }
    }
}
