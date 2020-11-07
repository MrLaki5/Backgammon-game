package games.mrlaki5.backgammon.Players;

import games.mrlaki5.backgammon.GameControllers.GameActivity;

//Implementation of player, real human
public class Human extends Player {

    //Constructor for human
    public Human(GameActivity currGame, String playerName) {
        super(currGame, playerName);
    }

    //Method used when player needs to move chips
    //it has synchronization with UI thread, its called from GameTask
    @Override
    public synchronized void actionMove() {
        //set synchronization flag
        super.setWaitCond(1);
        //activate touch listener so player can move chips
        super.getCurrGame().activateTouchListener();
        //while synchronization flag is set wait until is unset
        //its unset if player moved chips or left the game
        while(super.getWaitCond()==1) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //deactivate touch listener
        super.getCurrGame().deactivateTouchListener();
    }

    //Method used when player needs to roll dices
    //it has synchronization with UI thread, its called from GameTask
    @Override
    public synchronized void actionRoll() {
        //set synchronization flag
        super.setWaitCond(1);
        //activate shake listener so player can roll dices
        super.getCurrGame().activateShakeListener();
        //while synchronization flag is set wait until is unset
        //its unset if player rolled dices or left the game
        while (super.getWaitCond()==1) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //deactivate shake listener
        super.getCurrGame().deactivateShakeListener();
    }
}
