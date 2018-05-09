package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

import games.mrlaki5.backgammon.Beans.BoardFieldState;
import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;

public class GameActivity extends AppCompatActivity {

    private BoardFieldState[] BoardFields= new BoardFieldState[28];   // 2 red, 1 white 24-white, 25-red side board
                                                                      // 27-white endBoard, 26-red endBoard
    private DiceThrow[] diceThrows=new DiceThrow[4];

    private GameLogic gameLogic;
    private OnBoardImage BoardImage;

    private List<NextJump> nextMoves=null;
    private int [] NextMoves=new int[28];

    private int CurrentPlayer;
    private int MoveFieldSrc;

    private int CurrentFingerPointer=-1;


    private SensorManager sensorManager;
    Sensor sensor;

    private long lastUpdate=0;
    private float last_x=0;
    private float last_y=0;
    private float last_z=0;
    private int shake_treshold = 100;
    private int sample_time;
    private int shakeStarted=0;

    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x_touch= event.getX();
            float y_touch= event.getY();
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    int touchedNum=BoardImage.triangleTouched(x_touch,y_touch);
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, x_touch, y_touch);
                    if(isTouched){

                        if(BoardFields[touchedNum].getPlayer()==CurrentPlayer) {
                            nextMoves = gameLogic.calculateMoves(BoardFields, BoardFields[touchedNum].getPlayer(), diceThrows);
                            NextMoves = gameLogic.calculateNextMovesForSpecificField(nextMoves, touchedNum);
                            if (NextMoves!=null) {
                                BoardFields[touchedNum].setNumberOfChips(BoardFields[touchedNum].getNumberOfChips()-1);
                                if(BoardFields[touchedNum].getNumberOfChips()==0) {
                                    BoardFields[touchedNum].setPlayer(0);
                                }
                                BoardImage.setNextMoveArray(NextMoves);
                                MoveFieldSrc=touchedNum;
                                BoardImage.setMoveChip(x_touch, y_touch, CurrentPlayer);
                                BoardImage.invalidate();
                                CurrentFingerPointer=event.getPointerId(0);
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    for(int i=0; i<event.getPointerCount(); i++){
                        if(event.getPointerId(i)==CurrentFingerPointer){
                            x_touch=event.getX(i);
                            y_touch=event.getY(i);
                            break;
                        }
                    }
                    if(BoardImage.moveMoveChip(x_touch, y_touch)) {
                        BoardImage.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    if(CurrentFingerPointer==-1){
                        break;
                    }
                    int tempFlag=1;
                    for(int i=0; i<event.getPointerCount(); i++){
                        if(event.getPointerId(i)==CurrentFingerPointer && event.getActionIndex()!=i){
                            tempFlag=0;
                            break;
                        }
                    }
                    if(tempFlag==0){
                        break;
                    }
                    else{
                        CurrentFingerPointer=-1;
                    }
                    x_touch=BoardImage.getXMovPos();
                    y_touch=BoardImage.getYMovPos();
                    if(BoardImage.unsetMoveChip()) {
                        int dstField = BoardImage.triangleTouched(x_touch,y_touch);
                        if(dstField!=-1) {
                            int throwNum = 0;
                            for (NextJump tempJump: nextMoves) {
                                if(tempJump.getSrcField()==MoveFieldSrc && tempJump.getDstField()==dstField){
                                    throwNum=tempJump.getJumpNumber();
                                    break;
                                }
                            }
                            if(throwNum==0){
                                BoardFields[MoveFieldSrc].setNumberOfChips(BoardFields[MoveFieldSrc].getNumberOfChips()+1);
                                if(BoardFields[MoveFieldSrc].getNumberOfChips()==1){
                                    BoardFields[MoveFieldSrc].setPlayer(CurrentPlayer);
                                }
                            }
                            else {
                                for (DiceThrow tempThrow : diceThrows) {
                                    if (tempThrow.getThrowNumber() == throwNum && tempThrow.getAlreadyUsed() == 0) {
                                        tempThrow.setAlreadyUsed(1);
                                        BoardImage.setDices(diceThrows);
                                        break;
                                    }
                                }
                                int tmpPlayer=BoardFields[dstField].getPlayer();
                                if(BoardFields[dstField].getNumberOfChips()==1 && tmpPlayer!=CurrentPlayer){
                                    BoardFields[23+tmpPlayer].setNumberOfChips(BoardFields[23+tmpPlayer].getNumberOfChips()+1);
                                    if(BoardFields[23+tmpPlayer].getNumberOfChips()==1){
                                        BoardFields[23+tmpPlayer].setPlayer(tmpPlayer);
                                    }
                                }
                                else {
                                    BoardFields[dstField].setNumberOfChips(BoardFields[dstField].getNumberOfChips() + 1);
                                }
                                if(BoardFields[dstField].getNumberOfChips()==1){
                                    BoardFields[dstField].setPlayer(CurrentPlayer);
                                }
                            }

                        }
                        else{
                            BoardFields[MoveFieldSrc].setNumberOfChips(BoardFields[MoveFieldSrc].getNumberOfChips()+1);
                            if(BoardFields[MoveFieldSrc].getNumberOfChips()==1){
                                BoardFields[MoveFieldSrc].setPlayer(CurrentPlayer);
                            }
                        }
                        NextMoves = null;
                        BoardImage.setNextMoveArray(NextMoves);
                        BoardImage.invalidate();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private SensorEventListener DiceListener= new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    long curTime = System.currentTimeMillis();
                    // only allow one update every 100ms.
                    if ((curTime - lastUpdate) > sample_time) {
                        long diffTime = (curTime - lastUpdate);
                        lastUpdate = curTime;

                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                        if (speed > shake_treshold) {
                            shakeStarted=1;
                            android.widget.Toast.makeText(GameActivity.this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(shakeStarted==1){
                                shakeStarted=0;
                                diceThrows=gameLogic.rollDices();
                                BoardImage.setDices(diceThrows);
                                BoardImage.invalidate();
                            }
                        }
                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        gameLogic = new GameLogic();

        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        shake_treshold=preferences.getInt("sensor_sensibility", 400);
        sample_time=preferences.getInt("sample_time", 100);

        for(int i=0; i<diceThrows.length; i++){
            diceThrows[i]=new DiceThrow(0);
            diceThrows[i].setAlreadyUsed(1);
        }

        diceThrows[0].setThrowNumber(3);
        diceThrows[0].setAlreadyUsed(0);
        diceThrows[1].setThrowNumber(6);
        diceThrows[1].setAlreadyUsed(0);
        diceThrows[2].setThrowNumber(3);
        diceThrows[2].setAlreadyUsed(0);
        diceThrows[3].setThrowNumber(6);
        diceThrows[3].setAlreadyUsed(0);


        CurrentPlayer=2;

        for(int i=0; i<BoardFields.length; i++){
            BoardFields[i]=new BoardFieldState();
        }

        BoardFields[0].setNumberOfChips(5);
        BoardFields[0].setPlayer(1);

        BoardFields[11].setNumberOfChips(2);
        BoardFields[11].setPlayer(1);

        BoardFields[16].setNumberOfChips(3);
        BoardFields[16].setPlayer(1);

        BoardFields[18].setNumberOfChips(5);
        BoardFields[18].setPlayer(1);


        BoardFields[4].setNumberOfChips(3);
        BoardFields[4].setPlayer(2);

        BoardFields[6].setNumberOfChips(5);
        BoardFields[6].setPlayer(2);

        BoardFields[12].setNumberOfChips(5);
        BoardFields[12].setPlayer(2);

        BoardFields[23].setNumberOfChips(2);
        BoardFields[23].setPlayer(2);

/*
        //TEST PART

        BoardFields[24].setNumberOfChips(1);
        BoardFields[24].setPlayer(1);

        BoardFields[25].setNumberOfChips(1);
        BoardFields[25].setPlayer(2);

        BoardFields[26].setNumberOfChips(3);
        BoardFields[26].setPlayer(2);

        BoardFields[27].setNumberOfChips(10);
        BoardFields[27].setPlayer(1);

        int []pomNiz=new int[28];
        pomNiz[26]=1;
        pomNiz[27]=1;
*/

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        BoardImage.setChipMatrix(BoardFields);
        BoardImage.setDices(diceThrows);
        //BoardImage.setNextMoveArray(pomNiz);

        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);

        sensorManager=(SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(DiceListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }
}
