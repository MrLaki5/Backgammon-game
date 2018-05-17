package games.mrlaki5.backgammon.Beans;

//Class for representation of possible next jump
public class NextJump {

    //Dice thrown number
    int JumpNumber;
    //Starting jump field
    int SrcField;
    //Destination jump field
    int DstField;

    //Constructor used when calculating next jumps
    public NextJump(int jumpNumber, int srcField, int dstField) {
        JumpNumber = jumpNumber;
        SrcField = srcField;
        DstField = dstField;
    }

    //Getters and setters
    public int getJumpNumber() {
        return JumpNumber;
    }

    public void setJumpNumber(int jumpNumber) {
        JumpNumber = jumpNumber;
    }

    public int getSrcField() {
        return SrcField;
    }

    public void setSrcField(int srcField) {
        SrcField = srcField;
    }

    public int getDstField() {
        return DstField;
    }

    public void setDstField(int dstField) {
        DstField = dstField;
    }
}
