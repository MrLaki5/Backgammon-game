package games.mrlaki5.backgammon.GameModel;

import com.royalbackgammon.core.model.GameState;
import com.royalbackgammon.core.scoring.MatchState;
import com.royalbackgammon.core.variant.Variant;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** The save format must survive a round trip and still read files written by older versions. */
public class ModelLoaderTest {

    @Test
    public void roundTripKeepsVariantMatchPinsAndCounters() throws Exception {
        Model original = sampleModel();
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);
        new ModelLoader().writeModel(original, out);
        out.flush();

        Model loaded = new Model();
        new ModelLoader().readModel(loaded, new BufferedReader(new StringReader(buffer.toString())), null);

        assertEquals(Variant.PLAKOTO, loaded.getVariant());
        assertEquals(2, loaded.getCurrentPlayer());
        assertEquals(2, loaded.getState());
        assertEquals(7, loaded.getTurnsPlayed());
        assertEquals(1, loaded.getHeadMovesThisTurn());
        assertEquals(2, loaded.getHitsOnBar(1));
        assertEquals(0, loaded.getHitsOnBar(2));
        assertTrue(loaded.isBonusDoublePending());
        assertTrue(loaded.isExtraTurnPending());
        assertEquals(4242L, loaded.getDiceSeed());
        assertEquals(9, loaded.getDiceRollsUsed());
        assertEquals(3, loaded.getBoardFields()[8].getNumberOfChips());
        assertEquals(1, loaded.getBoardFields()[8].getPlayer());
        assertEquals(2, loaded.getBoardFields()[8].getPinnedPlayer());
        assertEquals(5, loaded.getDiceThrows()[0].getThrowNumber());
        assertEquals(1, loaded.getDiceThrows()[0].getAlreadyUsed());

        MatchState match = loaded.getMatch();
        assertEquals(5, match.getTargetPoints());
        assertEquals(3, match.getWhiteScore());
        assertEquals(1, match.getRedScore());
        assertEquals(2, match.getGamesPlayed());
        assertEquals(Variant.TAVLI_ROTATION, match.getRotation());
        assertEquals(Variant.FEVGA, match.currentVariant());
    }

    @Test
    public void readsOldSaveWithoutVariantMatchOrPins() throws Exception {
        StringBuilder board = new StringBuilder();
        for (int i = 0; i < 28; i++) {
            board.append(i == 0 ? "5,1 " : "0,0 ");
        }
        String legacy = "Me\nBot\n1 2\n" + board.toString().trim()
                + "\n3,0 4,0 0,1 0,1\n1 2\n";

        Model loaded = new Model();
        new ModelLoader().readModel(loaded, new BufferedReader(new StringReader(legacy)), null);

        assertEquals(Variant.STANDARD, loaded.getVariant());
        assertEquals(1, loaded.getMatch().getTargetPoints());
        assertTrue(loaded.getMatch().getRotation().isEmpty());
        assertEquals(0, loaded.getTurnsPlayed());
        assertEquals(0, loaded.getHitsOnBar(1));
        assertTrue(!loaded.isBonusDoublePending());
        assertEquals(0L, loaded.getDiceSeed());
        assertEquals(5, loaded.getBoardFields()[0].getNumberOfChips());
        assertEquals(0, loaded.getBoardFields()[0].getPinnedPlayer());
    }

    private static Model sampleModel() {
        Model model = new Model();
        model.setPlayers(new Player[]{new Human(null, "Me"), new Human(null, "You")});
        BoardFieldState[] board = new BoardFieldState[28];
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardFieldState(0, 0);
        }
        board[GameState.WHITE_BEAR_OFF] = new BoardFieldState(2, 1);
        board[8] = new BoardFieldState(3, 1, 2);
        model.setBoardFields(board);
        model.setDiceThrows(new DiceThrow[]{new DiceThrow(5, 1), new DiceThrow(3, 0),
                new DiceThrow(0, 1), new DiceThrow(0, 1)});
        model.setCurrentPlayer(2);
        model.setState(2);
        model.setVariant(Variant.PLAKOTO);
        model.setTurnsPlayed(7);
        model.setHeadMovesThisTurn(1);
        model.setHitsOnBar(1, 2);
        model.setBonusDoublePending(true);
        model.setDiceSeed(4242L);
        model.setDiceRollsUsed(9);
        model.setExtraTurnPending(true);
        model.setMatch(new MatchState(Variant.PORTES, Variant.TAVLI_ROTATION, 5, 3, 1, 2));
        return model;
    }

    @Test
    public void singleVariantMatchStaysSingleVariant() throws Exception {
        Model model = sampleModel();
        model.setMatch(new MatchState(Variant.NARDY, Collections.emptyList(), 3, 1, 0, 1));
        model.setVariant(Variant.NARDY);
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);
        new ModelLoader().writeModel(model, out);
        out.flush();

        Model loaded = new Model();
        new ModelLoader().readModel(loaded, new BufferedReader(new StringReader(buffer.toString())), null);
        assertTrue(loaded.getMatch().getRotation().isEmpty());
        assertEquals(Variant.NARDY, loaded.getMatch().currentVariant());
    }
}
