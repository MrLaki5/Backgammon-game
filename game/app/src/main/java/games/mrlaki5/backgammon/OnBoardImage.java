package games.mrlaki5.backgammon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import games.mrlaki5.backgammon.Beans.BoardFieldState;

public class OnBoardImage extends android.support.v7.widget.AppCompatImageView {

    //Chips matrix (with number of chips on triangle [0] and player [1] (1-white, 2-red)), length:24
    private BoardFieldState[] ChipMatrix;
    //Array with hints for next move (1-there is hint, 0- no hint), length:24
    private int[] NextMoveArray;

    //Paint for red chips
    private Paint RedChipPaint;
    //Paint for white chips
    private Paint WhiteChipPaint;
    //Paint for boreder of chips
    private Paint BorderChipPaint;
    //Paint for next move hint triangle transparency
    private Paint NextTriangleTransparentPaint;
    //Rect in which chip is drawn
    private RectF ChipRect;
    //Image for top row hints
    private Bitmap NextTriangleImageTop;
    //Image for bottom row hints
    private Bitmap NextTriangleImageBottom;
    //Rect in which hint is drawn
    private RectF NextTriangleRect;
    //Image for end board hints
    private Bitmap EndBoardImage;

    //Width of board
    private float Width;
    //Full width of board
    private float RealWidth;
    //x coordinate of endBoards middle line
    private float EndBoardMidX;
    //Height of board
    private float Height;
    //Width of left side of board
    private float LeftX;
    //Width of right size of board
    private float RightX;
    //Padding of left side triangles
    private float PaddingXLeft;
    //Padding of right side triangles
    private float PaddingXRight;
    //Height of triangles
    private float TriangleHeight;
    //Height of end chip
    private float EndChipHeight;

    //Size of moving chip
    private float MoveChipSize;
    //Coordinates of moving chip (-1 if not used)
    private float MoveChipX=-1;
    private float MoveChipY=-1;
    //Player of moving chip (1-white, 2-red)
    private int MoveChipPlayer=-1;


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

    //Method used for initialization
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
        //create transparent paint for next move green triangles
        NextTriangleTransparentPaint=new Paint();
        NextTriangleTransparentPaint.setAlpha(150);
        //load green hint images
        NextTriangleImageTop= BitmapFactory.decodeResource(getResources(), R.drawable.triangle_up);
        NextTriangleImageBottom= BitmapFactory.decodeResource(getResources(), R.drawable.triangle_down);
        EndBoardImage= BitmapFactory.decodeResource(getResources(), R.drawable.square);
        //create rect that will be used for drawing triangles for next moves
        NextTriangleRect=new RectF();
    }

    //Method called when size of board changes (is called on creation of view)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Take width and height of canvas
        Width=w*0.909f;
        Height=h;
        RealWidth=w;
        //Width of middle wood border is 10% of board border
        //find width of left and right size of board
        LeftX=Width*0.450f;
        RightX=Width*0.550f;
        //There is 12 triangles on left and right side, and 6 of them coun in width
        //Count width of left side and right side triangles
        PaddingXLeft=LeftX/6;
        PaddingXRight=(Width-RightX)/6;
        //Calculate triangles height about 40% of boards height
        TriangleHeight=Height*0.39f;
        //Calculate end boards middle line on x coordinate
        EndBoardMidX=Width+(PaddingXRight*3/4);
        //Calculate height of end chips
        EndChipHeight=TriangleHeight/15;
    }

    //Method for setting chip matrix
    public synchronized void setChipMatrix(BoardFieldState []chips){
        this.ChipMatrix=chips;
    }

    //Method for setting next move hints
    public synchronized void setNextMoveArray(int []moves){
        this.NextMoveArray=moves;
    }

    //Method for setting coordinates and player of moving chip
    public void setMoveChip(float x, float y, int player){
        MoveChipX=x;
        MoveChipY=y;
        MoveChipPlayer=player;
    }

    //Method for unseting moving chip
    public boolean unsetMoveChip(){
        if(MoveChipX!=-1 && MoveChipY!=-1) {
            MoveChipX = -1;
            MoveChipY = -1;
            MoveChipPlayer = -1;
            return true;
        }
        return false;
    }

    //Method for moving the selected chip
    public boolean moveMoveChip(float x, float y){
        if(MoveChipX!=-1 && MoveChipY!=-1) {
            if((x-(MoveChipSize/2))>=0 && (x+(MoveChipSize/2))<=RealWidth) {
                MoveChipX = x;
            }
            if((y-(MoveChipSize/2))>=0 && (y+(MoveChipSize/2))<=Height){
                MoveChipY = y;
            }
            return true;
        }
        return false;
    }

    //Method for drawing on canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

       /* Paint paint =new Paint();
        paint.setStrokeWidth(5);
        float tempX=this.getWidth()*0.963f;
        tempX=this.Width+(this.PaddingXRight*3/4);


        canvas.drawLine(tempX, 0, tempX, this.TriangleHeight, paint);
        RectF tempRect=new RectF();
        tempRect.set(tempX-PaddingXRight*(1/4f), 0, tempX+PaddingXRight*(1/4f), TriangleHeight);
        Bitmap square=BitmapFactory.decodeResource(getResources(), R.drawable.square);
        canvas.drawBitmap(square, null, tempRect, NextTriangleTransparentPaint);

        tempRect.set(tempX-PaddingXRight*(1/4f), Height-TriangleHeight, tempX+PaddingXRight*(1/4f), Height);
        square=BitmapFactory.decodeResource(getResources(), R.drawable.square);
        canvas.drawBitmap(square, null, tempRect, NextTriangleTransparentPaint);*/

        //Set starting coordinates to first triangles middle line
        float x=PaddingXLeft/2;
        float y=0;
        //padding to next triangles middle line
        float currPading=PaddingXLeft;
        //load current image for next step triangle
        Bitmap currentNextImage=NextTriangleImageTop;
        //if matrix exists draw chips
        if(ChipMatrix!=null) {
            boolean drawEndBoard=false;
            synchronized (this) {
                for (int i = 0; i < ChipMatrix.length; i++) {
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
                        //change next move triangle image
                        currentNextImage=NextTriangleImageBottom;
                    }
                    //after first 18 triangles jump to right bottom side of board
                    if (i == 18) {
                        //set coordinates for left bottom side of board
                        x = RightX + PaddingXRight / 2;
                        //set padding for right side of board
                        currPading = PaddingXRight;
                    }
                    //sideboard white chips (on up middle border)
                    if(i==24){
                        x=Width/2;
                        y=0;
                        currPading = PaddingXLeft;
                    }
                    //sideboard red chips (on down middle border)
                    if(i==25){
                        x=Width/2;
                        y=Height;
                        currPading = PaddingXLeft;
                    }
                    if(i==26){
                        drawEndBoard=true;
                        x=EndBoardMidX;
                        y=0;
                        currentNextImage=EndBoardImage;
                    }
                    if(i==27){
                        drawEndBoard=true;
                        x=EndBoardMidX;
                        y=Height;
                    }
                    //if there is any chips on current triangle
                    if (ChipMatrix[i].getNumberOfChips() > 0) {
                        //calculate x coordinates for rect where chip is drawn
                        float xChipStart = x - currPading * 0.35f;
                        float xChipEnd = x + currPading * 0.35f;
                        //calculate size of chip
                        float chipSize = Math.abs(xChipStart - xChipEnd);

                        float heightPadding = 0F;
                        float yChipStart=0F;
                        float yChipEnd=0F;
                        if (!drawEndBoard) {
                            //calculate padding in chip center (needed if there is more chips
                            //in triangle then triangles height is)
                            if ((chipSize * ChipMatrix[i].getNumberOfChips() > TriangleHeight) && (ChipMatrix[i].getNumberOfChips() > 1)) {
                                heightPadding = (chipSize * ChipMatrix[i].getNumberOfChips() - TriangleHeight) / (ChipMatrix[i].getNumberOfChips() - 1);
                            }
                            //calculate y coordinates for rect where chip is drawn
                            yChipStart = y;
                            yChipEnd = chipSize;
                            if (i >= 12 && i!=24) {
                                yChipEnd = y - yChipEnd;
                            }
                        }
                        else{
                            //calculate y coordinates for rect where end chip is drawn
                            yChipStart = y;
                            yChipEnd = EndChipHeight;
                            if (i==27) {
                                yChipEnd = y - yChipEnd;
                            }
                        }
                        //set up width of border on chips
                        BorderChipPaint.setStrokeWidth(chipSize * 0.09F);
                        //set up color of chips, depending on player
                        Paint localPaint = null;
                        if (ChipMatrix[i].getPlayer() == 1) {
                            localPaint = WhiteChipPaint;
                        } else {
                            localPaint = RedChipPaint;
                        }
                        //go through all chips on triangle and draw them
                        for (int j = 0; j < ChipMatrix[i].getNumberOfChips(); j++) {
                            //set up coordinates for drawing current chip
                            ChipRect.set(xChipStart, yChipStart, xChipEnd, yChipEnd);
                            if(!drawEndBoard) {
                                //draw chip
                                canvas.drawOval(ChipRect, localPaint);
                                //draw border for chip
                                canvas.drawOval(ChipRect, BorderChipPaint);
                                //move y coordinates for drawing next chip on same triangle
                                if (i >= 12 && i != 24) {
                                    yChipStart = yChipEnd + heightPadding;
                                    yChipEnd = yChipEnd - chipSize + heightPadding;
                                } else {
                                    yChipStart = yChipEnd - heightPadding;
                                    yChipEnd = yChipEnd + (chipSize - heightPadding);
                                }
                            }
                            else{
                                //draw end chip
                                canvas.drawRect(ChipRect, localPaint);
                                //draw border for end chip
                                canvas.drawRect(ChipRect, BorderChipPaint);
                                if (i == 27) {
                                    yChipStart = yChipEnd;
                                    yChipEnd = yChipEnd - EndChipHeight;
                                } else {
                                    yChipStart = yChipEnd;
                                    yChipEnd = yChipEnd + EndChipHeight;
                                }
                            }
                        }

                    }
                    //padding for calculating hints width
                    float tempHintWidth=0F;
                    if(!drawEndBoard){
                        tempHintWidth=currPading/2F;
                    }
                    else{
                        tempHintWidth=currPading*0.35F;
                    }
                    //check if next step hint (green triangle) is needed over triangle i
                    if(NextMoveArray!=null &&  i!=24 && i!=25 && NextMoveArray[i]!=0){
                        //calculate coordinates for green triangle
                        if(i<12 || i==26) {
                            //if on top row then y from 0 to triangleHeight
                            NextTriangleRect.set(x - tempHintWidth, 0, x + tempHintWidth, TriangleHeight);
                        }
                        else{
                            //if on bottom row then y from height-triangleHeight to height
                            NextTriangleRect.set(x - tempHintWidth, Height-TriangleHeight, x + tempHintWidth, Height);
                        }
                        //draw green triangle
                        canvas.drawBitmap(currentNextImage, null, NextTriangleRect, NextTriangleTransparentPaint);
                    }
                    //move to next triangle
                    x += currPading;
                }
            }
            //check if there is moving chip
            if(MoveChipX!=-1 && MoveChipY!=-1){
                Paint localPaint=null;
                //find paint for chip
                if(MoveChipPlayer==1){
                    localPaint=WhiteChipPaint;
                }
                else{
                    localPaint=RedChipPaint;
                }
                //draw moving chip
                canvas.drawCircle(MoveChipX, MoveChipY, MoveChipSize/2, localPaint);
                //draw border for moving chip
                canvas.drawCircle(MoveChipX, MoveChipY, MoveChipSize/2, BorderChipPaint);
            }
        }
    }

    //Method for checking which triangle is touched. (if none ret -1)
    public int triangleTouched(float touch_x, float touch_y){
        //check if coordinates are in top row of triangles
        if(touch_y<TriangleHeight){
            //check if coordinates are in top right board
            if(touch_x>RightX){
                //check if end game position
                if(touch_x>Width){
                    float tempXStart=EndBoardMidX-(PaddingXLeft*0.35F);
                    float tempXEnd=EndBoardMidX+(PaddingXLeft*0.35F);
                    if((touch_x>=tempXStart) && (touch_x<=tempXEnd)){
                        return 26;
                    }
                }
                else {
                    //go through triangles and find touched one
                    float TriangleBorder = PaddingXRight + RightX;
                    int currTriangle = 6;
                    while (TriangleBorder < touch_x) {
                        currTriangle++;
                        TriangleBorder += PaddingXLeft;
                    }
                    return currTriangle;
                }
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
                else{
                    float xChipStart = (Width/2) - PaddingXLeft * 0.35f;
                    float xChipEnd = (Width/2) + PaddingXLeft * 0.35f;
                    if(touch_x>=xChipStart && touch_x<=xChipEnd){
                        return 24;
                    }
                }
            }
        }
        else{
            //check if coordinates are in bottom row of triangles
            if ((Height-TriangleHeight)<touch_y){
                //check if coordinates are in bottom right board
                if(touch_x>RightX){
                    //check if end game position
                    if(touch_x>Width){
                        float tempXStart=EndBoardMidX-(PaddingXLeft*0.35F);
                        float tempXEnd=EndBoardMidX+(PaddingXLeft*0.35F);
                        if((touch_x>=tempXStart) && (touch_x<=tempXEnd)){
                            return 27;
                        }
                    }
                    else {
                        //go through triangles and find touched one
                        float TriangleBorder = PaddingXRight + RightX;
                        int currTriangle = 18;
                        while (TriangleBorder < touch_x) {
                            currTriangle++;
                            TriangleBorder += PaddingXLeft;
                        }
                        return currTriangle;
                    }
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
                    else{
                        float xChipStart = (Width/2) - PaddingXLeft * 0.35f;
                        float xChipEnd = (Width/2) + PaddingXLeft * 0.35f;
                        if(touch_x>=xChipStart && touch_x<=xChipEnd){
                            return 25;
                        }
                    }
                }
            }
        }
        //return -1 if none of triangles are clicked
        return -1;
    }

    //Method for checking if chip is touched inside touched triangle
    public synchronized boolean chipPTouched(int trianglePosition, float touch_x, float touch_y){
        //incorrect input
        if(trianglePosition<0 || trianglePosition>25){
            return false;
        }
        //calculate chip side depending on board side
        float chipSize=0f;
        //check if coordinates are in right board
        if(touch_x>RightX){
            chipSize=PaddingXRight*0.7f;
        }
        else{
            chipSize=PaddingXLeft*0.7f;
        }
        //find how much does group of chips take in specific triangle
        float chipsY=ChipMatrix[trianglePosition].getNumberOfChips()*chipSize;
        //check if coordinates are in top row of triangles
        if(touch_y<TriangleHeight){
            //check if it is in chip range in specific triangle
            if(touch_y<=chipsY){
                MoveChipSize=chipSize;
                return true;
            }
        }
        else{
            //check if coordinates are in bottom row of triangles
            if ((Height-TriangleHeight)<touch_y){
                //check if it is in chip range in specific triangle
                if((Height-chipsY)<=touch_y){
                    MoveChipSize=chipSize;
                    return true;
                }
            }
        }
        return false;
    }
}
