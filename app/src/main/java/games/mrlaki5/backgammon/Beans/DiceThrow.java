package games.mrlaki5.backgammon.Beans;

//Class for representation of thrown dice
public class DiceThrow {

    //Thrown dice number
    int ThrowNumber;
    //Did this throw already been used for jumping
    int AlreadyUsed;

    //Constructor used when loading new game
    public DiceThrow(int throwNumber) {
        ThrowNumber = throwNumber;
        AlreadyUsed = 0;
    }

    //Constructor used when loading state from file
    public DiceThrow(int throwNumber, int alreadyUsed){
        this.ThrowNumber=throwNumber;
        this.AlreadyUsed=alreadyUsed;
    }

    //Getters and setters
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
