package games.mrlaki5.backgammon.Players;

import games.mrlaki5.backgammon.GameActivity;

public class Human extends Player {

    public Human(GameActivity currGame, String playerName) {
        super(currGame, playerName);
    }



    @Override
    public synchronized void actionMove() {
        super.setWaitCond(1);
        super.getCurrGame().activateTouchListener();
        while(super.getWaitCond()==1) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.getCurrGame().deactivateTouchListener();
    }

    @Override
    public synchronized void actionRoll() {
        super.setWaitCond(1);
        super.getCurrGame().activateShakeListener();
        while (super.getWaitCond()==1) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.getCurrGame().deactivateShakeListener();
    }
}
