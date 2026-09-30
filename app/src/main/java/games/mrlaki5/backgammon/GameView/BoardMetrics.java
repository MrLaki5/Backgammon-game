package games.mrlaki5.backgammon.GameView;

/**
 * Holds calculated spatial dimensions, coordinate offsets, and hit-testing metrics for the board.
 */
public class BoardMetrics {
    public float YBaseTop;
    public float XBaseLeft;
    public float XBaseRight;
    public float Width;
    public float RealWidth;
    public float Height;
    public float LeftX;
    public float RightX;
    public float PaddingXLeft;
    public float PaddingXRight;
    public float TriangleHeight;
    public float EndBoardMidX;
    public float EndChipHeight;
    public float DiceSize;
    public float DicePadding;
    public float DiceXCenter;
    public float DiceXStart;
    public float DiceXEnd;
    public float DiceYStartTwo;
    public float DiceYStartFour;
    public float TextXCoordinate;
    public float TextYCoordinate;
    public float TextSize;
    public final float[] FieldCenterX = new float[28];

    public void update(int w, int h) {
        YBaseTop = h * 0.107F;
        XBaseLeft = w * 0.04F;
        XBaseRight = w * 0.05F;
        Width = w * 0.8735F;
        Height = h * 0.966F;
        RealWidth = w;

        LeftX = (Width - YBaseTop) * 0.4565f;
        RightX = (Width - YBaseTop) * 0.566f;

        PaddingXLeft = LeftX / 6f;
        PaddingXRight = ((Width - XBaseLeft) - RightX) / 6f;

        TriangleHeight = (Height - YBaseTop) * 0.39f;
        EndBoardMidX = Width + (PaddingXRight * 3f / 4f);
        calculateFieldCenters();

        EndChipHeight = TriangleHeight / 15f;

        DiceSize = XBaseRight * 0.8F;
        DicePadding = DiceSize * 0.4F;

        DiceXCenter = RealWidth - XBaseRight / 2F;
        DiceXStart = DiceXCenter - DiceSize / 2F;
        DiceXEnd = DiceXCenter + DiceSize / 2F;

        DiceYStartTwo = YBaseTop + ((Height - YBaseTop) / 2F) - (DicePadding / 2F) - DiceSize;
        DiceYStartFour = YBaseTop + ((Height - YBaseTop) / 2F) - ((DicePadding * 3 / 2F) / 2F) - DiceSize * 2F;

        TextXCoordinate = w / 2F;
        TextYCoordinate = YBaseTop * 0.68F;
        TextSize = Math.max(22F, YBaseTop * 0.44F);
    }

    private void calculateFieldCenters() {
        for (int i = 0; i < 6; i++) {
            FieldCenterX[i] = XBaseLeft + PaddingXLeft * (i + 0.5F);
            FieldCenterX[i + 6] = XBaseLeft + RightX + PaddingXRight * (i + 0.5F);
            FieldCenterX[i + 12] = FieldCenterX[i];
            FieldCenterX[i + 18] = FieldCenterX[i + 6];
        }
        FieldCenterX[24] = XBaseLeft + (Width - XBaseLeft) / 2F;
        FieldCenterX[25] = FieldCenterX[24];
        FieldCenterX[26] = EndBoardMidX;
        FieldCenterX[27] = EndBoardMidX;
    }
}
