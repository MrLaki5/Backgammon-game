package games.mrlaki5.backgammon.GameModel;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.royalbackgammon.core.scoring.MatchState;
import com.royalbackgammon.core.variant.Variant;

import java.util.Collections;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Players.Bot;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

//Class used for model storing and loading
public class ModelLoader {

    //Method used for loading model from file
    public Model loadModel(Bundle extras, GameActivity activity){
        Model model=null;
        //Open saving file
        File file=new File(activity.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        //If file doesn't exist create new model (new game creation)
        if(!file.exists()){
            model= new Model(extras, activity);
        }
        //If file exists load model from it
        else{
            //Create empty model
            model=new Model();
            BufferedReader in=null;
            try{
                in = new BufferedReader(new FileReader(file));
                readModel(model, in, activity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Close input buffer
            finally {
                try{
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Load file and delete it (when continue starts save gets deleted)
            file=new File(activity.getFilesDir().getAbsolutePath(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
            file.delete();

        }
        return model;
    }

    //Method used for saving model state to file
    public void saveModel(Model model, GameActivity activity){
        File file=new File(activity.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        PrintWriter out=null;
        try{
            out=new PrintWriter(file);
            writeModel(model, out);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Reads a saved model from [in]. [activity] may be null when no Player objects are needed
     * (unit tests); otherwise Human/Bot players are created for line 2.
     */
    public void readModel(Model model, BufferedReader in, GameActivity activity) throws IOException {
        int i=0;
        String line;
        String[] data;
        String p1Name="";
        String p2Name="";
        Player[] players=new Player[2];
        BoardFieldState[] BoardFields= new BoardFieldState[28];
        DiceThrow[] DiceThrows=new DiceThrow[4];
        while((line=in.readLine()) != null){
            switch (i){
                //Line 0: player1 name
                case 0:
                    p1Name=line;
                    break;
                //Line 1: player2 name
                case 1:
                    p2Name=line;
                    break;
                //Line 2: player kinds=player1Kind player2Kind
                case 2:
                    data=line.split(" ");
                    if(activity!=null){
                        players[0]="1".equals(data[0]) ? new Human(activity, p1Name)
                                : new Bot(activity, p1Name, model);
                        players[1]="1".equals(data[1]) ? new Human(activity, p2Name)
                                : new Bot(activity, p2Name, model);
                        model.setPlayers(players);
                    }
                    break;
                //Line 3: board fields=F0NumChip,F0Player[,F0Pinned] ...
                case 3:
                    data=line.split(" ");
                    for(int j=0; j<BoardFields.length; j++){
                        String[] dataTemp=data[j].split(",");
                        int numOfChips=Integer.parseInt(dataTemp[0]);
                        int player=Integer.parseInt(dataTemp[1]);
                        int pinned=dataTemp.length>2 ? Integer.parseInt(dataTemp[2]) : 0;
                        BoardFields[j]=new BoardFieldState(numOfChips, player, pinned);
                    }
                    model.setBoardFields(BoardFields);
                    break;
                //Line 4: dice throws=Throw0Num,Throw0Used ...
                case 4:
                    data=line.split(" ");
                    for(int j=0; j<DiceThrows.length; j++){
                        String[] dataTemp=data[j].split(",");
                        DiceThrows[j]=new DiceThrow(Integer.parseInt(dataTemp[0]),
                                Integer.parseInt(dataTemp[1]));
                    }
                    model.setDiceThrows(DiceThrows);
                    break;
                //Line 5: current player and game state
                case 5:
                    data=line.split(" ");
                    model.setCurrentPlayer(Integer.parseInt(data[0]));
                    model.setState(Integer.parseInt(data[1]));
                    break;
                //Line 6: variant name (absent in saves from older versions)
                case 6:
                    model.setVariant(Model.parseVariant(line.trim()));
                    break;
                //Line 7: match=target whiteScore redScore gamesPlayed [tavliFlag]
                case 7:
                    data=line.trim().split(" ");
                    if(data.length>=4){
                        boolean tavli=data.length>4 && "1".equals(data[4]);
                        model.setMatch(new MatchState(model.getVariant(),
                                tavli ? Variant.TAVLI_ROTATION : Collections.<Variant>emptyList(),
                                Math.max(1, Integer.parseInt(data[0])),
                                Integer.parseInt(data[1]), Integer.parseInt(data[2]),
                                Integer.parseInt(data[3])));
                    }
                    break;
                //Line 8: turn counters=turnsPlayed headMovesThisTurn
                case 8:
                    data=line.trim().split(" ");
                    if(data.length==2){
                        model.setTurnsPlayed(Integer.parseInt(data[0]));
                        model.setHeadMovesThisTurn(Integer.parseInt(data[1]));
                    }
                    break;
                //Line 10 (challenge games): diceSeed diceRollsUsed
                case 10:
                    data=line.trim().split(" ");
                    if(data.length==2){
                        model.setDiceSeed(Long.parseLong(data[0]));
                        model.setDiceRollsUsed(Integer.parseInt(data[1]));
                    }
                    break;
                //Line 9 (acey-deucey): whiteHitsOnBar redHitsOnBar bonusPending extraTurnPending
                case 9:
                    data=line.trim().split(" ");
                    if(data.length==4){
                        model.setHitsOnBar(1, Integer.parseInt(data[0]));
                        model.setHitsOnBar(2, Integer.parseInt(data[1]));
                        model.setBonusDoublePending("1".equals(data[2]));
                        model.setExtraTurnPending("1".equals(data[3]));
                    }
                    break;
                default:
                    break;
            }
            i++;
        }
    }

    /** Writes the model in the save-file format. */
    public void writeModel(Model model, PrintWriter out) {
        out.append(model.getPlayers()[0].getPlayerName()).append("\n");
        out.append(model.getPlayers()[1].getPlayerName()).append("\n");
        out.append(model.getPlayers()[0] instanceof Human ? "1 " : "2 ");
        out.append(model.getPlayers()[1] instanceof Human ? "1\n" : "2\n");

        StringBuilder fields=new StringBuilder();
        for (BoardFieldState field : model.getBoardFields()) {
            fields.append(field.getNumberOfChips()).append(",").append(field.getPlayer())
                    .append(",").append(field.getPinnedPlayer()).append(" ");
        }
        out.append(fields.toString().trim()).append("\n");

        StringBuilder dice=new StringBuilder();
        for (DiceThrow die : model.getDiceThrows()) {
            dice.append(die.getThrowNumber()).append(",").append(die.getAlreadyUsed()).append(" ");
        }
        out.append(dice.toString().trim()).append("\n");

        out.append(model.getCurrentPlayer()+" "+model.getState()+"\n");
        out.append(model.getVariant().name()).append("\n");
        MatchState match=model.getMatch();
        out.append(match.getTargetPoints()+" "+match.getWhiteScore()+" "+match.getRedScore()+" "
                +match.getGamesPlayed()+" "+(match.getRotation().isEmpty() ? "0" : "1")+"\n");
        out.append(model.getTurnsPlayed()+" "+model.getHeadMovesThisTurn()+"\n");
        out.append(model.getHitsOnBar(1)+" "+model.getHitsOnBar(2)+" "
                +(model.isBonusDoublePending() ? "1" : "0")+" "
                +(model.isExtraTurnPending() ? "1" : "0")+"\n");
        out.append(model.getDiceSeed()+" "+model.getDiceRollsUsed()+"\n");
    }
}
