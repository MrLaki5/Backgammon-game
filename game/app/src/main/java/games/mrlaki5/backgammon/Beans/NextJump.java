package games.mrlaki5.backgammon.Beans;

public class NextJump {

    int JumpNumber;
    int SrcField;
    int DstField;

    public NextJump(int jumpNumber, int srcField, int dstField) {
        JumpNumber = jumpNumber;
        SrcField = srcField;
        DstField = dstField;
    }

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
