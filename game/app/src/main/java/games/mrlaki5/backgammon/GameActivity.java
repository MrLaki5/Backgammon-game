package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.Intent;
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



    private MediaPlayer mPlayer;
    private final String mPlayerSem="mPlayer";

    private GameLogic gameLogic;
    private OnBoardImage BoardImage;
    private ModelLoader modelLoader;
    private Model model;
    private GameTask gameTask;




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
                    if(isTouched){
                        if(model.getBoardFields()[touchedNum].getPlayer()==model.getCurrentPlayer()) {
                            int[] currNextMoves;
                            currNextMoves = gameLogic.calculateNextMovesForSpecificField(model.getNextMoves(), touchedNum);
                            if (currNextMoves !=null) {
                                model.getBoardFields()[touchedNum].setNumberOfChips(model.getBoardFields()[touchedNum].getNumberOfChips()-1);
                                if(model.getBoardFields()[touchedNum].getNumberOfChips()==0) {
                                    model.getBoardFields()[touchedNum].setPlayer(0);
                                }
                                BoardImage.setNextMoveArray(currNextMoves);
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
                            for (NextJump tempJump: model.getNextMoves()) {
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
                                model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(), model.getCurrentPlayer(), model.getDiceThrows()));
                            }

                        }
                        else{
                            model.getBoardFields()[MoveFieldSrc].setNumberOfChips(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()+1);
                            if(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()==1){
                                model.getBoardFields()[MoveFieldSrc].setPlayer(model.getCurrentPlayer());
                            }
                        }
                        BoardImage.setNextMoveArray(null);
                        if(model.getNextMoves().isEmpty()){
                            synchronized (model.getCurrentObjectPlayer()){
                                model.getCurrentObjectPlayer().setWaitCond(0);
                                model.getCurrentObjectPlayer().notifyAll();
                            }
                        }
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
                                setMPlayer(1);
                                shakeStarted=1;
                            }
                            else{
                                beforeShakeStability++;
                            }
                            shakeStability=0;
                        }
                        else{
                            shakeStability++;
                            beforeShakeStability=0;
                            if(shakeStability>=tempFlag && shakeStarted==1) {
                                shakeStarted=2;
                                setMPlayer(2);
                                model.setDiceThrows(gameLogic.rollDices());
                                BoardImage.setDices(model.getDiceThrows());
                                BoardImage.invalidate();
                                synchronized (model.getCurrentObjectPlayer()){
                                    model.getCurrentObjectPlayer().setWaitCond(0);
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
        int timeBTUrns=preferences.getInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS, SettingsActivity.DEF_TIME_BETWEEN_TURNS);

        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        modelLoader=new ModelLoader();
        model=modelLoader.loadModel(extras, this);
        gameLogic = new GameLogic(model);
        gameTask=new GameTask(model, gameLogic, BoardImage, timeBTUrns*1000, this);


        BoardImage.setChipMatrix(model.getBoardFields());
        BoardImage.setDices(model.getDiceThrows());

        BoardImage.invalidate();
        BoardImage.setOnTouchListener(BoardListener);

        sensorManager=(SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gameTask.execute();
    }

    @Override
    public void onBackPressed() {
        leaveMethod();
        Intent data= new Intent();
        setResult(MenuActivity.GAME_PRESSED_BACK, data);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //leaveMethod();
    }

    public void leaveMethod(){
        if(gameTask!=null){
            gameTask.setWorkFlag(0);

            synchronized (model.getCurrentObjectPlayer()) {
                model.getCurrentObjectPlayer().setWaitCond(0);
                model.getCurrentObjectPlayer().notifyAll();
            }
            //gameTask.cancel(true);
            clearMPlayer();
            sensorManager.unregisterListener(DiceListener);
            int tempFlag=0;
            synchronized (gameTask) {
                if(gameTask.getEndRoutineStarted()==0){
                    gameTask.setEndRoutineStarted(1);
                    tempFlag=1;
                }
                while (gameTask.getFinishedFlag() != 1) {
                    try {
                        gameTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(tempFlag==1) {
                modelLoader.saveModel(model, this);
            }
        }
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public OnBoardImage getBoardImage() {
        return BoardImage;
    }

    public void setBoardImage(OnBoardImage boardImage) {
        BoardImage = boardImage;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void clearMPlayer(){
        synchronized (mPlayerSem){
            if(mPlayer!=null){
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer=null;
            }
        }
    }

    public void setMPlayer(int SongNum){
        synchronized (mPlayerSem){
            if(mPlayer!=null){
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer=null;
            }
            if(SongNum==1){
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_shake);
                mPlayer.setLooping(true);
                mPlayer.start();
            }
            else{
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_roll);
                mPlayer.start();
            }
        }
    }

}
