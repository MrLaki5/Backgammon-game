package games.mrlaki5.backgammon.GameModel;

import android.os.Bundle;

import games.mrlaki5.backgammon.Analysis.TurnRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.royalbackgammon.core.model.BoardField;
import com.royalbackgammon.core.model.GameState;
import com.royalbackgammon.core.model.TurnContext;
import com.royalbackgammon.core.scoring.MatchState;
import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Players.Bot;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

//Model class
public class Model {

    //Keeps the review bounded on very long games
    private static final int MAX_RECORDED_TURNS = 300;

    //Array that represents all fields on board, side board and end board
    //24-white, 25-red side board
    //27-white endBoard, 26-red endBoard
    private BoardFieldState[] BoardFields= new BoardFieldState[28];
    //Array that represents thrown dices
    private DiceThrow[] DiceThrows=new DiceThrow[4];
    //Array of players in game
    private Player[] Players=new Player[2];
    //List of all next moves for current player
    private List<NextJump> NextMoves=null;
    //Current player
    // 2 red, 1 white
    private int CurrentPlayer;
    //Current state of game
    //0: player1 roll dice
    //1: player2 roll dice
    //2: currentPlayer move chips
    //3: currentPlayer roll dices
    private int State;
    //Rule variant of this game
    private Variant variant = Variant.STANDARD;
    //Completed turns this game and checkers moved off the head this turn (running family)
    private int turnsPlayed = 0;
    private int headMovesThisTurn = 0;
    //Acey-deucey: checkers sent back by a hit (index 0 white, 1 red), and the 1-2 bonus state
    private final int[] barHits = new int[2];
    private boolean bonusDoublePending = false;
    private boolean extraTurnPending = false;
    //Challenge games roll from a seed so that everyone gets the same dice; 0 = normal random
    private long diceSeed = 0;
    private int diceRollsUsed = 0;
    private transient java.util.Random seededRandom;
    //Turn-by-turn history for the post-game review (human turns are the ones analysed)
    private final List<TurnRecord> turnHistory = new ArrayList<>();
    private TurnRecord currentTurnRecord;
    //Running score of the match this game belongs to (target 1 = single game)
    private MatchState match = new MatchState(Variant.STANDARD, Collections.emptyList(), 1, 0, 0, 0);

    //Constructor used when loading model from save file
    public Model(){}

    //Constructor used when creating new game
    public Model(Bundle extras, GameActivity activity){
        //Initialize dice throws
        for(int i=0; i<DiceThrows.length; i++){
            DiceThrows[i]=new DiceThrow(0);
            DiceThrows[i].setAlreadyUsed(1);
        }
        DiceThrows[0].setThrowNumber(1);
        DiceThrows[1].setThrowNumber(1);
        //Set current player to white
        CurrentPlayer=1;
        //Load from extras player names and player kinds and create players
        if(extras!=null){
            variant=parseVariant(extras.getString(MenuActivity.EXTRA_VARIANT));
            List<Variant> rotation = extras.getBoolean(MenuActivity.EXTRA_TAVLI, false)
                    ? Variant.TAVLI_ROTATION : Collections.<Variant>emptyList();
            match=new MatchState(variant, rotation,
                    Math.max(1, extras.getInt(MenuActivity.EXTRA_MATCH_TARGET, 1)),
                    extras.getInt(MenuActivity.EXTRA_MATCH_WHITE_SCORE, 0),
                    extras.getInt(MenuActivity.EXTRA_MATCH_RED_SCORE, 0),
                    extras.getInt(MenuActivity.EXTRA_MATCH_GAMES, 0));
            //In a Tavli match the rotation decides which game is being played
            variant=match.currentVariant();
            diceSeed=extras.getLong(MenuActivity.EXTRA_DICE_SEED, 0L);
            String p1Name=extras.getString(MenuActivity.EXTRA_PLAYER1_NAME);
            String p2Name=extras.getString(MenuActivity.EXTRA_PLAYER2_NAME);
            if("Player".equals(extras.getString(MenuActivity.EXTRA_PLAYER1_KIND))){
                Players[0]=new Human(activity, p1Name);
            }
            else{
                Players[0]=new Bot(activity, p1Name, this);
            }
            if("Player".equals(extras.getString(MenuActivity.EXTRA_PLAYER2_KIND))){
                Players[1]=new Human(activity, p2Name);
            }
            else{
                Players[1]=new Bot(activity, p2Name, this);
            }
        }
        //Create board fields with the variant's starting position
        BoardField[] start=GameState.newGame(variant).getBoard();
        for(int i=0; i<BoardFields.length; i++){
            BoardFields[i]=new BoardFieldState(start[i].getChipCount(), start[i].getOwner(), start[i].getPinned());
        }
    }

    //Method used to change current player to other one
    public void changeCurrentPlayer(){
        if(CurrentPlayer==1){
            CurrentPlayer=2;
        }
        else{
            CurrentPlayer=1;
        }
    }

    //Getters and setters
    public BoardFieldState[] getBoardFields() {
        return BoardFields;
    }

    public void setBoardFields(BoardFieldState[] boardFields) {
        BoardFields = boardFields;
    }

    public DiceThrow[] getDiceThrows() {
        return DiceThrows;
    }

    public void setDiceThrows(DiceThrow[] diceThrows) {
        this.DiceThrows = diceThrows;
    }

    public Player[] getPlayers() {
        return Players;
    }

    public void setPlayers(Player[] players) {
        Players = players;
    }

    public int getCurrentPlayer() {
        return CurrentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        CurrentPlayer = currentPlayer;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public List<NextJump> getNextMoves() {
        return NextMoves;
    }

    public void setNextMoves(List<NextJump> nextMoves) {
        NextMoves = nextMoves;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    //Called when the current player's turn ends, before switching player
    public void onTurnEnded(){
        flushTurnRecord();
        turnsPlayed++;
        headMovesThisTurn=0;
        bonusDoublePending=false;
        extraTurnPending=false;
    }

    //Called when the current player is about to move, and again after an undo
    public void beginTurnRecord(){
        currentTurnRecord=new TurnRecord(variant, CurrentPlayer, BoardFields, DiceThrows);
    }

    public void recordMove(NextJump move){
        if(currentTurnRecord!=null){
            currentTurnRecord.addMove(move);
        }
    }

    //Stores the turn that was just played; also called when the game ends mid-turn
    public void flushTurnRecord(){
        if(currentTurnRecord!=null && !currentTurnRecord.getMoves().isEmpty()
                && turnHistory.size()<MAX_RECORDED_TURNS){
            turnHistory.add(currentTurnRecord);
        }
        currentTurnRecord=null;
    }

    public List<TurnRecord> getTurnHistory() {
        return turnHistory;
    }

    public TurnContext getTurnContext(){
        return new TurnContext(headMovesThisTurn, turnsPlayed < 2, getHitsOnBar(CurrentPlayer));
    }

    public int getHitsOnBar(int player){
        return barHits[player == 1 ? 0 : 1];
    }

    public void setHitsOnBar(int player, int value){
        barHits[player == 1 ? 0 : 1] = Math.max(0, value);
    }

    public boolean isBonusDoublePending() {
        return bonusDoublePending;
    }

    public void setBonusDoublePending(boolean pending) {
        this.bonusDoublePending = pending;
    }

    public boolean isExtraTurnPending() {
        return extraTurnPending;
    }

    public void setExtraTurnPending(boolean pending) {
        this.extraTurnPending = pending;
    }

    public int getTurnsPlayed() {
        return turnsPlayed;
    }

    public void setTurnsPlayed(int turnsPlayed) {
        this.turnsPlayed = turnsPlayed;
    }

    public int getHeadMovesThisTurn() {
        return headMovesThisTurn;
    }

    public void setHeadMovesThisTurn(int headMovesThisTurn) {
        this.headMovesThisTurn = headMovesThisTurn;
    }

    public long getDiceSeed() {
        return diceSeed;
    }

    public void setDiceSeed(long diceSeed) {
        this.diceSeed = diceSeed;
        this.seededRandom = null;
    }

    public int getDiceRollsUsed() {
        return diceRollsUsed;
    }

    public void setDiceRollsUsed(int diceRollsUsed) {
        this.diceRollsUsed = diceRollsUsed;
        this.seededRandom = null;
    }

    /** Next die value: seeded and reproducible in challenge games, random otherwise. */
    public int nextDieValue() {
        if (diceSeed == 0) {
            return (int) (Math.random() * 6) + 1;
        }
        if (seededRandom == null) {
            seededRandom = new java.util.Random(diceSeed);
            for (int i = 0; i < diceRollsUsed; i++) {
                seededRandom.nextInt(6);
            }
        }
        diceRollsUsed++;
        return seededRandom.nextInt(6) + 1;
    }

    public MatchState getMatch() {
        return match;
    }

    public void setMatch(MatchState match) {
        this.match = match;
    }

    //Unknown or missing names fall back to STANDARD (old saves, old intents)
    public static Variant parseVariant(String name){
        if(name==null){
            return Variant.STANDARD;
        }
        try{
            return Variant.valueOf(name);
        } catch (IllegalArgumentException e){
            return Variant.STANDARD;
        }
    }

    public Player getCurrentObjectPlayer(){
        return Players[CurrentPlayer-1];
    }
}
