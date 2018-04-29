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

    }

    public void setChipMatrix(int [][]chips){
        this.ChipMatrix=chips;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint=new Paint();
        paint.setStrokeWidth(10);

        float width=canvas.getWidth();
        float height=canvas.getHeight();
        float x=width/26;
        float y=0;

        //canvas.drawLine(width,y,width, y+520, paint);
        //canvas.drawLine(0,y,0, y+520, paint);

        //canvas.drawLine(width/2,y,width/2, y+520, paint);
        float leftX=width*0.450f;
        float rightX=width*0.550f;
        //canvas.drawLine(leftX,y,leftX, y+520, paint);
        //canvas.drawLine(rightX,y,rightX, y+520, paint);

        float paddingXLeft=leftX/6;
        float paddingXRight=(width-rightX)/6;
        x=paddingXLeft/2;


        float triangleHeight=height*0.4f;
        float realTrinagleHeight=triangleHeight;

        float currPading=paddingXLeft;

        if(ChipMatrix!=null) {
            for (int i = 0; i < 24; i++) {
                if(i==6){
                    x=rightX+paddingXRight/2;
                    currPading=paddingXRight;
                }
                if(i==12){
                    x=paddingXLeft/2;
                    currPading=paddingXLeft;
                    y=height;
                    triangleHeight=0-triangleHeight;
                }
                if(i==18){
                    x=rightX+paddingXRight/2;
                    currPading=paddingXRight;
                }
                //canvas.drawLine(x,y,x, y+triangleHeight, paint);
                if (ChipMatrix[i][0] > 0) {
                    float xChipStart=x-currPading*0.35f;
                    float xChipEnd=x+currPading*0.35f;
                    float yChipStart=y;
                    float chipSize=Math.abs(xChipStart-xChipEnd);
                    float heightPadding=0F;
                    if((chipSize*ChipMatrix[i][0]>realTrinagleHeight) && (ChipMatrix[i][0]>1)){
                        heightPadding=(chipSize*ChipMatrix[i][0]-realTrinagleHeight)/(ChipMatrix[i][0]-1);
                    }
                    float yChipEnd=chipSize;
                    if(i>=12){
                        yChipEnd=y-yChipEnd;
                    }
                    Paint localPaint=new Paint();
                    Paint outerPaint=new Paint();
                    outerPaint.setStyle(Paint.Style.STROKE);
                    outerPaint.setStrokeWidth(chipSize*0.09F);

                    if(ChipMatrix[i][1]==1){
                        localPaint.setColor(Color.WHITE);
                    }
                    else{
                        localPaint.setColor(Color.rgb(212, 31, 38));
                    }
                    for(int j=0; j<ChipMatrix[i][0]; j++) {
                        RectF rect = new RectF(xChipStart, yChipStart, xChipEnd, yChipEnd);
                        canvas.drawOval(rect, localPaint);
                        canvas.drawOval(rect, outerPaint);
                        if(i>=12){
                            yChipStart=yChipEnd+heightPadding;
                            yChipEnd=yChipEnd-chipSize+heightPadding;
                        }
                        else{
                            yChipStart=yChipEnd-heightPadding;
                            yChipEnd=yChipEnd+(chipSize-heightPadding);
                        }
                    }

                }
                x+=currPading;
            }
        }
    }
}
