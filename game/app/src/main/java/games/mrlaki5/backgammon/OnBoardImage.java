package games.mrlaki5.backgammon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class OnBoardImage extends android.support.v7.widget.AppCompatImageView {

    private int[][] ChipMatrix;

    private Paint RedChipPaint;
    private Paint WhiteChipPaint;
    private Paint BorderChipPaint;
    private RectF ChipRect;

    private float Width;
    private float Height;
    private float LeftX;
    private float RightX;
    private float PaddingXLeft;
    private float PaddingXRight;
    private float TriangleHeight;

    public OnBoardImage(Context context) {
        super(context);
        initOnBoardImage();
    }

    public OnBoardImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initOnBoardImage();
    }

    public OnBoardImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initOnBoardImage();
    }

    private void initOnBoardImage(){
        //create color for red chips
        RedChipPaint=new Paint();
        RedChipPaint.setColor(Color.rgb(212, 31, 38));
        //create color for white chips
        WhiteChipPaint=new Paint();
        WhiteChipPaint.setColor(Color.WHITE);
        //create color for border of chips
        BorderChipPaint= new Paint();
        BorderChipPaint.setStyle(Paint.Style.STROKE);
        //create rect that will be used for drawing chips
        ChipRect=new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Take width and height of canvas
        Width=w;
        Height=h;
        //Width of middle wood border is 10% of board border
        //find width of left and right size of board
        LeftX=Width*0.450f;
        RightX=Width*0.550f;
        //There is 12 triangles on left and right side, and 6 of them coun in width
        //Count width of left side and right side triangles
        PaddingXLeft=LeftX/6;
        PaddingXRight=(Width-RightX)/6;
        //Calculate triangles height about 40% of boards height
        TriangleHeight=Height*0.4f;
    }

    public synchronized void setChipMatrix(int [][]chips){
        this.ChipMatrix=chips;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Set starting coordinates to first triangles middle line
        float x=PaddingXLeft/2;
        float y=0;
        //padding to next triangles middle line
        float currPading=PaddingXLeft;
        //if matrix exists draw chips
        if(ChipMatrix!=null) {
            synchronized (this) {
                for (int i = 0; i < 24; i++) {
                    //after first six triangles jump to right top side of board
                    if (i == 6) {
                        //set right top side board coordinates
                        x = RightX + PaddingXRight / 2;
                        //set padding for right side of board
                        currPading = PaddingXRight;
                    }
                    //after first 12 triangles jump to left bottom side of board
                    if (i == 12) {
                        //set coordinates for right bottom side of board
                        x = PaddingXLeft / 2;
                        y = Height;
                        //set padding for left side of board
                        currPading = PaddingXLeft;
                    }
                    //after first 18 triangles jump to right bottom side of board
                    if (i == 18) {
                        //set coordinates for left bottom side of board
                        x = RightX + PaddingXRight / 2;
                        //set padding for right side of board
                        currPading = PaddingXRight;
                    }
                    //if there is any chips on current triangle
                    if (ChipMatrix[i][0] > 0) {
                        //calculate x coordinates for triangle where chip is drawn
                        float xChipStart = x - currPading * 0.35f;
                        float xChipEnd = x + currPading * 0.35f;
                        //calculate size of chip
                        float chipSize = Math.abs(xChipStart - xChipEnd);
                        //calculate padding in chip center (needed if there is more chips
                        //in triangle then triangles height is)
                        float heightPadding = 0F;
                        if ((chipSize * ChipMatrix[i][0] > TriangleHeight) && (ChipMatrix[i][0] > 1)) {
                            heightPadding = (chipSize * ChipMatrix[i][0] - TriangleHeight) / (ChipMatrix[i][0] - 1);
                        }
                        //calculate y coordinates for triangle where chip is drawn
                        float yChipStart = y;
                        float yChipEnd = chipSize;
                        if (i >= 12) {
                            yChipEnd = y - yChipEnd;
                        }
                        //set up width of border on chips
                        BorderChipPaint.setStrokeWidth(chipSize * 0.09F);
                        //set up color of chips, depending on player
                        Paint localPaint = null;
                        if (ChipMatrix[i][1] == 1) {
                            localPaint = WhiteChipPaint;
                        } else {
                            localPaint = RedChipPaint;
                        }
                        //go through all chips on triangle and draw them
                        for (int j = 0; j < ChipMatrix[i][0]; j++) {
                            //set up coordinates for drawing current chip
                            ChipRect.set(xChipStart, yChipStart, xChipEnd, yChipEnd);
                            //draw chip
                            canvas.drawOval(ChipRect, localPaint);
                            //draw border for chip
                            canvas.drawOval(ChipRect, BorderChipPaint);
                            //move y coordinates for drawing next chip on same triangle
                            if (i >= 12) {
                                yChipStart = yChipEnd + heightPadding;
                                yChipEnd = yChipEnd - chipSize + heightPadding;
                            } else {
                                yChipStart = yChipEnd - heightPadding;
                                yChipEnd = yChipEnd + (chipSize - heightPadding);
                            }
                        }

                    }
                    //move to next triangle
                    x += currPading;
                }
            }
        }
    }

    public int triangleTouched(float touch_x, float touch_y){
        //check if coordinates are in top row of triangles
        if(touch_y<TriangleHeight){
            //check if coordinates are in top right board
            if(touch_x>RightX){
                //go through triangles and find touched one
                float TriangleBorder=PaddingXRight+RightX;
                int currTriangle=6;
                while(TriangleBorder<touch_x){
                    currTriangle++;
                    TriangleBorder+=PaddingXLeft;
                }
                return currTriangle;
            }
            else{
                //check if coordinates are in top left board
                if(touch_x<LeftX){
                    //go through triangles and find touched one
                    float TriangleBorder=PaddingXLeft;
                    int currTriangle=0;
                    while(TriangleBorder<touch_x){
                        currTriangle++;
                        TriangleBorder+=PaddingXLeft;
                    }
                    return currTriangle;
                }
            }
        }
        else{
            //check if coordinates are in bottom row of triangles
            if ((Height-TriangleHeight)<touch_y){
                //check if coordinates are in bottom right board
                if(touch_x>RightX){
                    //go through triangles and find touched one
                    float TriangleBorder=PaddingXRight+RightX;
                    int currTriangle=18;
                    while(TriangleBorder<touch_x){
                        currTriangle++;
                        TriangleBorder+=PaddingXLeft;
                    }
                    return currTriangle;
                }
                else{
                    //check if coordinates are in bottom left board
                    if(touch_x<LeftX){
                        //go through triangles and find touched one
                        float TriangleBorder=PaddingXLeft;
                        int currTriangle=12;
                        while(TriangleBorder<touch_x){
                            currTriangle++;
                            TriangleBorder+=PaddingXLeft;
                        }
                        return currTriangle;
                    }
                }
            }
        }
        //return -1 if none of triangles are clicked
        return -1;
    }

    public synchronized boolean chipPTouched(int trianglePosition, float touch_x, float touch_y){
        if(trianglePosition<0 || trianglePosition>23){
            return false;
        }
        float chipSize=0f;
        //check if coordinates are in right board
        if(touch_x>RightX){
            chipSize=PaddingXRight*0.7f;
        }
        else{
            //check if coordinates are in left board
            if(touch_x<LeftX){
                chipSize=PaddingXLeft*0.7f;
            }
            else{
                return false;
            }
        }
        float chipsY=ChipMatrix[trianglePosition][0]*chipSize;
        //check if coordinates are in top row of triangles
        if(touch_y<TriangleHeight){
            if(touch_y<=chipsY){
                return true;
            }
        }
        else{
            //check if coordinates are in bottom row of triangles
            if ((Height-TriangleHeight)<touch_y){
                if((Height-chipsY)<=touch_y){
                    return true;
                }
            }
        }
        return false;
    }
}
