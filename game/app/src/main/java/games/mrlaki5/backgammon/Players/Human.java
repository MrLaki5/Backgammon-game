package games.mrlaki5.backgammon.Players;

import games.mrlaki5.backgammon.GameActivity;

public class Human extends Player {

    private int WaitCond;

    public Human(GameActivity currGame, String playerName) {
        super(currGame, playerName);
    }

    public int getWaitCond() {
        return WaitCond;
    }

    public void setWaitCond(int waitCond) {
        WaitCond = waitCond;
    }

    @Override
    public synchronized void actionMove() {
        super.getCurrGame().activateTouchListener();
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.getCurrGame().deactivateTouchListener();
    }

    @Override
    public synchronized void actionRoll() {
        super.getCurrGame().activateShakeListener();
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.getCurrGame().deactivateShakeListener();
    }
}
