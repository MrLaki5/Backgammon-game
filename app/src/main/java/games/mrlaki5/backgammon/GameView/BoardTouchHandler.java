package games.mrlaki5.backgammon.GameView;

import games.mrlaki5.backgammon.Beans.BoardFieldState;

/**
 * Handles touch coordinate to board triangle resolution and checker hit testing.
 */
public class BoardTouchHandler {

    private float lastMoveChipSize = 0F;

    public float getLastMoveChipSize() {
        return lastMoveChipSize;
    }

    /**
     * Resolves touch coordinates to board triangle/field index (0..27), or -1 if no triangle touched.
     */
    public int triangleTouched(float touchX, float touchY, BoardMetrics m) {
        if (m == null) return -1;

        // Top row
        if ((touchY < (m.TriangleHeight + m.YBaseTop)) && (touchY >= m.YBaseTop)) {
            if (touchX > (m.RightX + m.XBaseLeft)) {
                // Top-right end board
                if (touchX > m.Width) {
                    // The whole tray column is a valid drop target
                    return 26;
                } else {
                    // Top-right triangles (6..11)
                    float triangleBorder = m.PaddingXRight + m.RightX + m.XBaseLeft;
                    int currTriangle = 6;
                    while (triangleBorder < touchX) {
                        currTriangle++;
                        triangleBorder += m.PaddingXRight;
                    }
                    return currTriangle;
                }
            } else {
                // Top-left
                if (touchX < m.LeftX + m.XBaseLeft) {
                    // Top-left triangles (0..5)
                    float triangleBorder = m.PaddingXLeft + m.XBaseLeft;
                    int currTriangle = 0;
                    while (triangleBorder < touchX) {
                        currTriangle++;
                        triangleBorder += m.PaddingXLeft;
                    }
                    return currTriangle;
                } else {
                    // Top sideboard (bar for white, field 24)
                    float xChipStart = ((m.Width - m.XBaseLeft) / 2f) + m.XBaseLeft - m.PaddingXLeft * 0.35f;
                    float xChipEnd = ((m.Width - m.XBaseLeft) / 2f) + m.XBaseLeft + m.PaddingXLeft * 0.35f;
                    if (touchX >= xChipStart && touchX <= xChipEnd) {
                        return 24;
                    }
                }
            }
        } else {
            // Bottom row
            if ((m.Height - m.TriangleHeight) < touchY) {
                if (touchX > (m.RightX + m.XBaseLeft)) {
                    // Bottom-right end board
                    if (touchX > m.Width) {
                        // The whole tray column is a valid drop target
                        return 27;
                    } else {
                        // Bottom-right triangles (18..23)
                        float triangleBorder = m.PaddingXRight + m.RightX + m.XBaseLeft;
                        int currTriangle = 18;
                        while (triangleBorder < touchX) {
                            currTriangle++;
                            triangleBorder += m.PaddingXRight;
                        }
                        return currTriangle;
                    }
                } else {
                    // Bottom-left
                    if (touchX < (m.LeftX + m.XBaseLeft)) {
                        // Bottom-left triangles (12..17)
                        float triangleBorder = m.PaddingXLeft + m.XBaseLeft;
                        int currTriangle = 12;
                        while (triangleBorder < touchX) {
                            currTriangle++;
                            triangleBorder += m.PaddingXLeft;
                        }
                        return currTriangle;
                    } else {
                        // Bottom sideboard (bar for red, field 25)
                        float xChipStart = ((m.Width - m.XBaseLeft) / 2f) + m.XBaseLeft - m.PaddingXLeft * 0.35f;
                        float xChipEnd = ((m.Width - m.XBaseLeft) / 2f) + m.XBaseLeft + m.PaddingXLeft * 0.35f;
                        if (touchX >= xChipStart && touchX <= xChipEnd) {
                            return 25;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Checks whether a checker on trianglePosition was touched.
     */
    public boolean chipPTouched(int trianglePosition, float touchX, float touchY,
                               BoardFieldState[] chipMatrix, BoardMetrics m) {
        if (m == null || chipMatrix == null || trianglePosition < 0 || trianglePosition > 25) {
            return false;
        }

        int numberOfChips = chipMatrix[trianglePosition].getVisibleChips();
        if (numberOfChips <= 0) {
            return false;
        }

        float chipSize;
        if (touchX > (m.RightX + m.XBaseLeft)) {
            chipSize = m.PaddingXRight * 0.7f;
        } else {
            chipSize = m.PaddingXLeft * 0.7f;
        }

        float touchTolerance = chipSize * 0.45f;
        float chipsHeight = numberOfChips * chipSize;

        boolean isTopTriangle =
                touchY >= (m.YBaseTop - touchTolerance)
                        && touchY <= (m.YBaseTop + m.TriangleHeight + touchTolerance);

        if (isTopTriangle) {
            float touchEnd = m.YBaseTop + chipsHeight + touchTolerance;
            if (touchY <= touchEnd) {
                lastMoveChipSize = chipSize;
                return true;
            }
        }

        boolean isBottomTriangle =
                touchY >= (m.Height - m.TriangleHeight - touchTolerance)
                        && touchY <= (m.Height + touchTolerance);

        if (isBottomTriangle) {
            float touchStart = m.Height - chipsHeight - touchTolerance;
            if (touchY >= touchStart) {
                lastMoveChipSize = chipSize;
                return true;
            }
        }

        return false;
    }
}
