package games.mrlaki5.backgammon.Beans;

public class DiceThrow {

    int ThrowNumber;
    int AlreadyUsed;

    public DiceThrow(int throwNumber) {
        ThrowNumber = throwNumber;
        AlreadyUsed = 0;
    }

    public int getThrowNumber() {
        return ThrowNumber;
    }

    public void setThrowNumber(int throwNumber) {
        ThrowNumber = throwNumber;
    }

    public int getAlreadyUsed() {
        return AlreadyUsed;
    }

    public void setAlreadyUsed(int alreadyUsed) {
        AlreadyUsed = alreadyUsed;
    }
}
