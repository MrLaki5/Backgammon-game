package com.royalbackgammon.core.variant

import com.royalbackgammon.core.model.WinType
import kotlinx.serialization.Serializable

/** How the first turn of a game starts after the one-die opening roll decides who moves first. */
enum class OpeningRoll {
    /** Starter plays the two opening dice (international backgammon). */
    PLAY_OPENING_DICE,
    /** Starter rolls both dice again; doubles are possible on the first move (Tavla, Portes). */
    STARTER_REROLLS
}

/** Movement family: decides direction, contact and blocking rules. */
enum class RuleFamily {
    /** Opposite directions, a lone checker can be hit to the bar. */
    HITTING,
    /**
     * Same rotational direction from diagonally opposite heads, no hitting, one checker owns a
     * point, one checker leaves the head per turn, no prime that traps all opponent checkers.
     */
    RUNNING,
    /**
     * Opposite directions from opposite corners; landing on a lone opposing checker pins it
     * under yours instead of hitting. Pinning the mother (last checker on its start) wins double.
     */
    PINNING
}

/** How the running family restricts checkers leaving the starting point. */
enum class HeadRule {
    /** Not a running variant. */
    NONE,
    /** Long Nardy: one checker per turn, two on a first-turn 6-6, 4-4 or 3-3. */
    ONE_PER_TURN,
    /** Fevga: only the leading checker moves until it passes the opponent's starting point. */
    LEAD_MUST_PASS
}

/** Points awarded per [WinType]. */
@Serializable
data class WinScoring(val single: Int, val gammon: Int, val backgammon: Int) {
    fun pointsFor(type: WinType): Int = when (type) {
        WinType.SINGLE -> single
        WinType.GAMMON -> gammon
        WinType.BACKGAMMON -> backgammon
        WinType.MOTHER_PINNED -> gammon
        WinType.DRAW -> 0
    }
}

/** Playable rule variants. */
enum class Variant(
    val family: RuleFamily,
    val openingRoll: OpeningRoll,
    val scoring: WinScoring,
    val cubeAllowed: Boolean,
    val headRule: HeadRule = HeadRule.NONE,
    /** Acey-deucey: every checker starts on the bar and must be entered. */
    val startsOnBar: Boolean = false,
    /** Acey-deucey: a 1-2 roll adds a named double and another roll. */
    val aceyDeuceyRoll: Boolean = false,
    /** Acey-deucey: the loser pays one point per checker still on the board. */
    val scoreByRemainingCheckers: Boolean = false
) {
    STANDARD(RuleFamily.HITTING, OpeningRoll.PLAY_OPENING_DICE, WinScoring(1, 2, 3), cubeAllowed = true),
    TAVLA(RuleFamily.HITTING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 2, 2), cubeAllowed = false),
    PORTES(RuleFamily.HITTING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 2, 2), cubeAllowed = false),
    /** Greek pinning game, second game of Tavli. */
    PLAKOTO(RuleFamily.PINNING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 2, 2), cubeAllowed = false),
    /** Long Nardy: oyn 1, mars 2. */
    NARDY(RuleFamily.RUNNING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 2, 2), cubeAllowed = false,
        headRule = HeadRule.ONE_PER_TURN),
    /** American acey-deucey: enter from the fence, 1-2 grants a named double and an extra roll. */
    ACEY_DEUCEY(RuleFamily.HITTING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 1, 1),
        cubeAllowed = false, startsOnBar = true, aceyDeuceyRoll = true,
        scoreByRemainingCheckers = true),
    /** Fevga, third game of Tavli: the lead checker must pass the opponent's start first. */
    FEVGA(RuleFamily.RUNNING, OpeningRoll.STARTER_REROLLS, WinScoring(1, 2, 2), cubeAllowed = false,
        headRule = HeadRule.LEAD_MUST_PASS);

    companion object {
        /** The three games of a Tavli match, in playing order. */
        @JvmField
        val TAVLI_ROTATION: List<Variant> = listOf(PORTES, PLAKOTO, FEVGA)
    }
}
