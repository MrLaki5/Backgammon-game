package games.mrlaki5.backgammon.GameModel;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.GameControllers.GameActivity;
import games.mrlaki5.backgammon.GameModel.Model;
import games.mrlaki5.backgammon.MenuActivity;
import games.mrlaki5.backgammon.Players.Bot;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

public class ModelLoader {

    public Model loadModel(Bundle extras, GameActivity activity){
        Model model=null;

        File file=new File(activity.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);

        if(!file.exists()){
            model= new Model(extras, activity);
        }
        else{
            model=new Model();
            BufferedReader in=null;
            String line=null;
            try{
                int i=0;
                String[] data;
                in = new BufferedReader(new FileReader(file));

                String p1Name="";
                String p2Name="";
                Player[] players=new Player[2];
                BoardFieldState[] BoardFields= new BoardFieldState[28];
                DiceThrow[] DiceThrows=new DiceThrow[4];

                while((line=in.readLine()) != null){
                    switch (i){
                        case 0:
                            p1Name=line;
                            break;
                        case 1:
                            p2Name=line;
                            break;
                        case 2:
                            data=line.split(" ");
                            if(data[0].equals("1")){
                                players[0]=new Human(activity, p1Name);
                            }
                            else{
                                players[0]=new Bot(activity, p1Name, model);
                            }
                            if(data[1].equals("1")){
                                players[1]=new Human(activity, p2Name);
                            }
                            else{
                                players[1]=new Bot(activity, p2Name, model);
                            }
                            model.setPlayers(players);
                            break;
                        case 3:
                            data=line.split(" ");
                            for(int j=0; j<BoardFields.length; j++){
                                String[] dataTemp=data[j].split(",");
                                int numOfChips=Integer.parseInt(dataTemp[0]);
                                int player=Integer.parseInt(dataTemp[1]);
                                BoardFields[j]=new BoardFieldState(numOfChips, player);
                            }
                            model.setBoardFields(BoardFields);
                            break;
                        case 4:
                            data=line.split(" ");
                            for(int j=0; j<DiceThrows.length; j++){
                                String[] dataTemp=data[j].split(",");
                                int throwNum=Integer.parseInt(dataTemp[0]);
                                int alreadyUsed=Integer.parseInt(dataTemp[1]);
                                DiceThrows[j]=new DiceThrow(throwNum, alreadyUsed);
                            }
                            model.setDiceThrows(DiceThrows);
                            break;
                        case 5:
                            data=line.split(" ");
                            int currentPlayer=Integer.parseInt(data[0]);
                            int state=Integer.parseInt(data[1]);
                            model.setCurrentPlayer(currentPlayer);
                            model.setState(state);
                            break;
                        default:
                            break;
                    }
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try{
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            file=new File(activity.getFilesDir().getAbsolutePath(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
            file.delete();

        }
        return model;
    }

    public void saveModel(Model model, GameActivity activity){
        File file=new File(activity.getFilesDir(), MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
        PrintWriter out=null;
        try{
            out=new PrintWriter(file);
            String tempOut=model.getPlayers()[0].getPlayerName()+"\n";
            out.append(tempOut);

            tempOut=model.getPlayers()[1].getPlayerName()+"\n";
            out.append(tempOut);

            tempOut="";
            if(model.getPlayers()[0] instanceof Human){
                tempOut+="1 ";
            }
            else{
                tempOut+="2 ";
            }
            if(model.getPlayers()[1] instanceof Human){
                tempOut+="1"+"\n";
            }
            else{
                tempOut+="2"+"\n";
            }
            out.append(tempOut);

            tempOut="";
            BoardFieldState[] BoardFields=model.getBoardFields();
            for(int j=0; j<BoardFields.length; j++){
                tempOut+=BoardFields[j].getNumberOfChips()+","+BoardFields[j].getPlayer()+" ";
            }
            tempOut+="\n";
            out.append(tempOut);

            tempOut="";
            DiceThrow[] DiceThrows=model.getDiceThrows();
            for(int j=0; j<DiceThrows.length; j++){
                tempOut+=DiceThrows[j].getThrowNumber()+","+DiceThrows[j].getAlreadyUsed()+" ";
            }
            tempOut+="\n";
            out.append(tempOut);

            tempOut=""+model.getCurrentPlayer()+" "+model.getState()+"\n";
            out.append(tempOut);

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
}
