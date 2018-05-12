package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import games.mrlaki5.backgammon.Players.Bot;
import games.mrlaki5.backgammon.Players.Human;
import games.mrlaki5.backgammon.Players.Player;

public class GameActivity extends AppCompatActivity {

    MediaPlayer mPlayer;

    private GameLogic gameLogic;
    private OnBoardImage BoardImage;
    private Model model;
    private GameTask gameTask;

    private List<NextJump> nextMoves=null;


    private int MoveFieldSrc;

    private int CurrentFingerPointer=-1;


    private SensorManager sensorManager;
    private Sensor sensor;

    private long lastUpdate=0;
    private float last_x=0;
    private float last_y=0;
    private float last_z=0;
    private int shake_treshold = 100;
    private int sample_time;
    private int dice_delay;
    private int shakeStarted=0;

    private int beforeShakeStability=0;
    private int shakeStability=0;



    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x_touch= event.getX();
            float y_touch= event.getY();

            gameTask.cancel(true);

            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    int touchedNum=BoardImage.triangleTouched(x_touch,y_touch);
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, x_touch, y_touch);
                    int[] nextMoves1 = new int[28];
                    if(isTouched){

                        if(model.getBoardFields()[touchedNum].getPlayer()==model.getCurrentPlayer()) {
                            nextMoves = gameLogic.calculateMoves(model.getBoardFields(), model.getBoardFields()[touchedNum].getPlayer(), model.getDiceThrows());
                            nextMoves1 = gameLogic.calculateNextMovesForSpecificField(nextMoves, touchedNum);
                            if (nextMoves1 !=null) {
                                model.getBoardFields()[touchedNum].setNumberOfChips(model.getBoardFields()[touchedNum].getNumberOfChips()-1);
                                if(model.getBoardFields()[touchedNum].getNumberOfChips()==0) {
                                    model.getBoardFields()[touchedNum].setPlayer(0);
                                }
                                BoardImage.setNextMoveArray(nextMoves1);
                                MoveFieldSrc=touchedNum;
                                BoardImage.setMoveChip(x_touch, y_touch, model.getCurrentPlayer());
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
                                model.getBoardFields()[MoveFieldSrc].setNumberOfChips(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()+1);
                                if(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()==1){
                                    model.getBoardFields()[MoveFieldSrc].setPlayer(model.getCurrentPlayer());
                                }
                            }
                            else {
                                for (DiceThrow tempThrow : model.getDiceThrows()) {
                                    if (tempThrow.getThrowNumber() == throwNum && tempThrow.getAlreadyUsed() == 0) {
                                        tempThrow.setAlreadyUsed(1);
                                        BoardImage.setDices(model.getDiceThrows());
                                        break;
                                    }
                                }
                                int tmpPlayer=model.getBoardFields()[dstField].getPlayer();
                                if(model.getBoardFields()[dstField].getNumberOfChips()==1 && tmpPlayer!= model.getCurrentPlayer()){
                                    model.getBoardFields()[23+tmpPlayer].setNumberOfChips(model.getBoardFields()[23+tmpPlayer].getNumberOfChips()+1);
                                    if(model.getBoardFields()[23+tmpPlayer].getNumberOfChips()==1){
                                        model.getBoardFields()[23+tmpPlayer].setPlayer(tmpPlayer);
                                    }
                                }
                                else {
                                    model.getBoardFields()[dstField].setNumberOfChips(model.getBoardFields()[dstField].getNumberOfChips() + 1);
                                }
                                if(model.getBoardFields()[dstField].getNumberOfChips()==1){
                                    model.getBoardFields()[dstField].setPlayer(model.getCurrentPlayer());
                                }
                            }

                        }
                        else{
                            model.getBoardFields()[MoveFieldSrc].setNumberOfChips(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()+1);
                            if(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()==1){
                                model.getBoardFields()[MoveFieldSrc].setPlayer(model.getCurrentPlayer());
                            }
                        }
                        nextMoves1 = null;
                        BoardImage.setNextMoveArray(nextMoves1);
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

                        int tempFlag=dice_delay;//(100/(sample_time))*3;

                        if (speed > shake_treshold) {
                            if(shakeStarted==0 && beforeShakeStability>=tempFlag) {
                                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_shake);
                                mPlayer.setLooping(true);
                                mPlayer.start();
                                shakeStarted=1;
                            }
                            else{
                                beforeShakeStability++;
                            }
                            shakeStability=0;
                            //android.widget.Toast.makeText(GameActivity.this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            shakeStability++;
                            beforeShakeStability=0;
                            if(shakeStability>=tempFlag && shakeStarted==1) {
                                beforeShakeStability=0;
                                mPlayer.stop();
                                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_roll);
                                mPlayer.start();
                                model.setDiceThrows(gameLogic.rollDices());
                                BoardImage.setDices(model.getDiceThrows());
                                BoardImage.invalidate();
                                synchronized (model.getCurrentObjectPlayer()){
                                    model.getCurrentObjectPlayer().notifyAll();
                                }
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

    //CREATION OF LISTENERS

    public void activateTouchListener(){
        BoardImage.setOnTouchListener(BoardListener);
    }

    public void deactivateTouchListener(){
        BoardImage.setOnTouchListener(null);
    }

    public void activateShakeListener(){
        shakeStarted=0;
        beforeShakeStability=0;
        lastUpdate=0;
        last_x=0;
        last_y=0;
        last_z=0;
        sensorManager.registerListener(DiceListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void deactivateShakeListener(){
        sensorManager.unregisterListener(DiceListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Bundle extras=getIntent().getExtras();

        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        shake_treshold=preferences.getInt(SettingsActivity.KEY_DICE_TRESHOLD, SettingsActivity.DEF_DICE_TRAESHOLD);
        sample_time=preferences.getInt(SettingsActivity.KEY_TIME_SAMPLE, SettingsActivity.DEF_TIME_SAMPLE);
        dice_delay=preferences.getInt(SettingsActivity.KEY_DICE_SHAKE_DELAY, SettingsActivity.DEF_DICE_SHAKE_DELAY);


        model=new Model(extras, this);
        gameLogic = new GameLogic(model);
        gameTask=new GameTask(model);
        //TEST PART
/*
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
        BoardImage.setChipMatrix(model.getBoardFields());
        BoardImage.setDices(model.getDiceThrows());
        //BoardImage.setNextMoveArray(pomNiz);

        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);
        //BoardImage.setOnTouchListener(null);

        sensorManager=(SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //sensorManager.registerListener(DiceListener, sensor, SensorManager.SENSOR_DELAY_GAME);


        gameTask.execute();
    }
}
