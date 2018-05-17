package games.mrlaki5.backgammon.GameControllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.File;

import games.mrlaki5.backgammon.Beans.DiceThrow;
import games.mrlaki5.backgammon.Beans.NextJump;
import games.mrlaki5.backgammon.MenuActivity;
import games.mrlaki5.backgammon.Model;
import games.mrlaki5.backgammon.ModelLoader;
import games.mrlaki5.backgammon.OnBoardImage;
import games.mrlaki5.backgammon.R;
import games.mrlaki5.backgammon.SettingsActivity;

//Game activity
public class GameActivity extends AppCompatActivity {

    //Player used for all game sounds
    private MediaPlayer mPlayer;
    //String used for synchronization on media player
    private final String mPlayerSem="mPlayer";
    //Current sound volume in which player should play
    private int soundVolume=0;
    //Object for some game related rules
    private GameLogic gameLogic;
    //View
    private OnBoardImage BoardImage;
    //Object for loading model
    private ModelLoader modelLoader;
    //Model (used for storing game state)
    private Model model;
    //Thread in which game turns are run
    private GameTask gameTask;
    //Flag used in onPause and in onStop to know cause of game stoping
    private int pauseDone=0;
    //Time between player turns
    private int timeBetweenTurns=0;
    //Used in touch listener to save starting field of moving chip
    private int MoveFieldSrc;
    //Used in touch listener to save id of chip moving finger
    private int CurrentFingerPointer=-1;
    //sensor manager for shake listener
    private SensorManager sensorManager;
    //sensor for shake listener
    private Sensor sensor;
    //Time of last update in shake listener
    private long lastUpdate=0;
    //x acceleration value in last update in shake listener
    private float last_x=0;
    //y acceleration value in last update in shake listener
    private float last_y=0;
    //z acceleration value in last update in shake listener
    private float last_z=0;
    //Treshold of shake listener
    private int shake_treshold = 100;
    //Time between two updates of shake listener
    private int sample_time;
    //Delay in starting and ending shake
    private int dice_delay;
    //Flag for determination if shake started
    private int shakeStarted=0;
    //Stability counter used for shake start delay
    private int beforeShakeStability=0;
    //Stability counter used for shake end delay
    private int shakeStability=0;

    //Touch listener activated when human needs to move chips
    private View.OnTouchListener BoardListener= new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Get current touch x and y coordinates
            float x_touch= event.getX();
            float y_touch= event.getY();
            //which touch action is done
            switch(event.getActionMasked()) {
                //First finger is put on screen
                case MotionEvent.ACTION_DOWN:
                    //Find a number of currently touched triangle
                    int touchedNum=BoardImage.triangleTouched(x_touch,y_touch);
                    //Find if chips on that triangle are touched
                    boolean isTouched=BoardImage.chipPTouched(touchedNum, x_touch, y_touch);
                    if(isTouched){
                        //Check if touched chips are from current player
                        if(model.getBoardFields()[touchedNum].getPlayer()
                                ==model.getCurrentPlayer()) {
                            int[] currNextMoves;
                            //Calculate moves for currently touched chip
                            currNextMoves = gameLogic.calculateNextMovesForSpecificField(
                                    model.getNextMoves(), touchedNum);
                            if (currNextMoves !=null) {
                                //Lower number of chips from touched triangle
                                model.getBoardFields()[touchedNum].setNumberOfChips(
                                        model.getBoardFields()[touchedNum].getNumberOfChips()-1);
                                //If there are no left chips, remove player from triangle
                                if(model.getBoardFields()[touchedNum].getNumberOfChips()==0) {
                                    model.getBoardFields()[touchedNum].setPlayer(0);
                                }
                                //Update view with hints where can picked chip move
                                BoardImage.setNextMoveArray(currNextMoves);
                                //Set source field of moving field
                                MoveFieldSrc=touchedNum;
                                //Update view, set moving chip
                                BoardImage.setMoveChip(x_touch, y_touch, model.getCurrentPlayer());
                                //Invalidate view to draw again
                                BoardImage.invalidate();
                                //Save current finger id
                                CurrentFingerPointer=event.getPointerId(0);
                            }
                        }
                    }
                    break;
                //Some finger moved
                case MotionEvent.ACTION_MOVE:
                    //Check if moving finger is chip finger
                    for(int i=0; i<event.getPointerCount(); i++){
                        if(event.getPointerId(i)==CurrentFingerPointer){
                            x_touch=event.getX(i);
                            y_touch=event.getY(i);
                            break;
                        }
                    }
                    //Move chip to finger position, update view
                    if(BoardImage.moveMoveChip(x_touch, y_touch)) {
                        BoardImage.invalidate();
                    }
                    break;
                //Some finger left screen
                case MotionEvent.ACTION_POINTER_UP:
                //Last finger left screen
                case MotionEvent.ACTION_UP:
                    //Check if chip finger didnt left already (==-1)
                    if(CurrentFingerPointer==-1){
                        break;
                    }
                    //Check if chip finger is leaving now
                    int tempFlag=1;
                    for(int i=0; i<event.getPointerCount(); i++){
                        if(event.getPointerId(i)==CurrentFingerPointer
                                && event.getActionIndex()!=i){
                            tempFlag=0;
                            break;
                        }
                    }
                    //If it is not break, if it is set it to -1
                    if(tempFlag==0){
                        break;
                    }
                    else{
                        CurrentFingerPointer=-1;
                    }
                    //Get x and y chip coordinates from view
                    x_touch=BoardImage.getXMovPos();
                    y_touch=BoardImage.getYMovPos();
                    //Unset move chip
                    if(BoardImage.unsetMoveChip()) {
                        //Calculate destination triangle
                        int dstField = BoardImage.triangleTouched(x_touch,y_touch);
                        if(dstField!=-1) {
                            //Find in nextMoves if exists combination with source and destination
                            //fields
                            int throwNum = 0;
                            for (NextJump tempJump: model.getNextMoves()) {
                                if(tempJump.getSrcField()==MoveFieldSrc &&
                                        tempJump.getDstField()==dstField){
                                    throwNum=tempJump.getJumpNumber();
                                    break;
                                }
                            }
                            //If it doesnt find combination of source and destination, return chip
                            if(throwNum==0){
                                //Return one chip to source field
                                model.getBoardFields()[MoveFieldSrc].setNumberOfChips(
                                        model.getBoardFields()[MoveFieldSrc].getNumberOfChips()+1);
                                //If there was no chips on source field set player of field
                                if(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()==1){
                                    model.getBoardFields()[MoveFieldSrc].setPlayer(
                                            model.getCurrentPlayer());
                                }
                            }
                            //If it did find combination of source and destination
                            else {
                                //Find DiceThrow that is used and set it used
                                for (DiceThrow tempThrow : model.getDiceThrows()) {
                                    if (tempThrow.getThrowNumber() == throwNum &&
                                            tempThrow.getAlreadyUsed() == 0) {
                                        tempThrow.setAlreadyUsed(1);
                                        BoardImage.setDices(model.getDiceThrows());
                                        break;
                                    }
                                }
                                //Get player of destination filed
                                int tmpPlayer=model.getBoardFields()[dstField].getPlayer();
                                //If there is one chip on destination field and player is different
                                //from current playing (eat chip)
                                if(model.getBoardFields()[dstField].getNumberOfChips()==1 &&
                                        tmpPlayer!= model.getCurrentPlayer()){
                                    //Add one chip to side board
                                    model.getBoardFields()[23+tmpPlayer].setNumberOfChips(
                                        model.getBoardFields()[23+tmpPlayer].getNumberOfChips()+1);
                                    //If its first chip on side board add player too
                                    if(model.getBoardFields()[23+tmpPlayer].getNumberOfChips()==1){
                                        model.getBoardFields()[23+tmpPlayer].setPlayer(tmpPlayer);
                                    }
                                    //But dont remove chip from destination just change its player
                                }
                                else {
                                    //If there is no other player chip, add one chip to
                                    // destination field
                                    model.getBoardFields()[dstField].setNumberOfChips(
                                        model.getBoardFields()[dstField].getNumberOfChips() + 1);
                                }
                                //If number of chips on destination field is 1 set field player
                                // to current
                                if(model.getBoardFields()[dstField].getNumberOfChips()==1){
                                    model.getBoardFields()[dstField].setPlayer(
                                    model.getCurrentPlayer());
                                }
                                //Calculate next moves after chip move
                                model.setNextMoves(gameLogic.calculateMoves(model.getBoardFields(),
                                        model.getCurrentPlayer(), model.getDiceThrows()));
                            }

                        }
                        else{
                            //If it doesnt find destination field, return chip
                            model.getBoardFields()[MoveFieldSrc].setNumberOfChips(
                                    model.getBoardFields()[MoveFieldSrc].getNumberOfChips()+1);
                            //If there was no chips on source field set player of field
                            if(model.getBoardFields()[MoveFieldSrc].getNumberOfChips()==1){
                                model.getBoardFields()[MoveFieldSrc].setPlayer(
                                model.getCurrentPlayer());
                            }
                        }
                        //Update view, remove hints
                        BoardImage.setNextMoveArray(null);
                        //If there are no more next moves return to GameTask and continue turns
                        if(model.getNextMoves().isEmpty()){
                            synchronized (model.getCurrentObjectPlayer()){
                                model.getCurrentObjectPlayer().setWaitCond(0);
                                model.getCurrentObjectPlayer().notifyAll();
                            }
                        }
                        //Draw view again
                        BoardImage.invalidate();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    //Shake listener activated when human needs to roll dices
    private SensorEventListener DiceListener= new SensorEventListener() {

        //Is called sensor reacts to change of values
        @Override
        public void onSensorChanged(SensorEvent event) {
            //Find type of sensor reacting
            switch(event.sensor.getType()){
                //If its accelerometer
                case Sensor.TYPE_ACCELEROMETER:
                    //Get current time
                    long curTime = System.currentTimeMillis();
                    //Only allow one update every sample_time
                    if ((curTime - lastUpdate) > sample_time) {
                        long diffTime = (curTime - lastUpdate);
                        //Update lastupdate time to current
                        lastUpdate = curTime;
                        //Get current x,y,z values
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        //Calculate speed
                        float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
                        //If speed is greater then Trashold
                        if (speed > shake_treshold) {
                            //If Shake didnt started yet and before shake delay is done
                            if(shakeStarted==0 && beforeShakeStability>=dice_delay) {
                                //Start roll dice sound
                                setMPlayer(1);
                                //Set shakeStarted flag
                                shakeStarted=1;
                            }
                            else{
                                //If delay is not over decrease it
                                beforeShakeStability++;
                            }
                            //Set after shake stability to zero ass long as speed is more then
                            // trashold (reset delay)
                            shakeStability=0;
                        }
                        //If speed is lower then trashold
                        else{
                            //Decrease after shake stability
                            shakeStability++;
                            //While shake speed is not more then trashold set before shake stability
                            //to zero (reset delay)
                            beforeShakeStability=0;
                            //If end delay is over and shake started, end shake
                            if(shakeStability>=dice_delay && shakeStarted==1) {
                                //Set shake flag so it cant detect again
                                shakeStarted=2;
                                //Play throw dice sound
                                setMPlayer(2);
                                //Update model with new dice rolls
                                model.setDiceThrows(gameLogic.rollDices());
                                //Update view with new dice rolls
                                BoardImage.setDices(model.getDiceThrows());
                                //Invalidate view so it can be drawn again
                                BoardImage.invalidate();
                                //Roll is over, call GameTask so it can continue turns
                                synchronized (model.getCurrentObjectPlayer()){
                                    model.getCurrentObjectPlayer().setWaitCond(0);
                                    model.getCurrentObjectPlayer().notifyAll();
                                }
                            }
                        }
                        //Update last x, y, z values
                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Blank
        }
    };

    //Method called when activating touch listener
    public void activateTouchListener(){
        BoardImage.setOnTouchListener(BoardListener);
    }

    //Method called when deactivating touch listener
    public void deactivateTouchListener(){
        BoardImage.setOnTouchListener(null);
    }

    //Method called when activating shake listener
    public void activateShakeListener(){
        //Reset all important values to starting
        shakeStarted=0;
        beforeShakeStability=0;
        lastUpdate=0;
        last_x=0;
        last_y=0;
        last_z=0;
        //Register listener
        sensorManager.registerListener(DiceListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    //Method called when deactivating shake listener
    public void deactivateShakeListener(){
        sensorManager.unregisterListener(DiceListener);
    }

    //Method called on creation of GameActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        //Get sent extras from menu activity (they dont exist if game is continued,
        // only if its new game)
        Bundle extras=getIntent().getExtras();
        //Get values of shared preferences (game settings parameters)
        SharedPreferences preferences = getSharedPreferences("Settings", 0);
        //Get shake treshold value
        shake_treshold=preferences.getInt(SettingsActivity.KEY_DICE_TRESHOLD,
                SettingsActivity.DEF_DICE_TRAESHOLD);
        //Get time value between two shake sensor events
        sample_time=preferences.getInt(SettingsActivity.KEY_TIME_SAMPLE,
                SettingsActivity.DEF_TIME_SAMPLE);
        //Get value of shake delays
        dice_delay=preferences.getInt(SettingsActivity.KEY_DICE_SHAKE_DELAY,
                SettingsActivity.DEF_DICE_SHAKE_DELAY);
        //Get value of time between turns in game
        timeBetweenTurns=preferences.getInt(SettingsActivity.KEY_TIME_BETWEEN_TURNS,
                SettingsActivity.DEF_TIME_BETWEEN_TURNS);
        //Get value of sound volume
        soundVolume=preferences.getInt(SettingsActivity.KEY_SOUND_VOLUME,
                SettingsActivity.DEF_SOUND_VOLUME);
        //Get View
        BoardImage=((OnBoardImage)findViewById(R.id.boardImage) );
        //Create model loader
        modelLoader=new ModelLoader();
        //Build model
        model=modelLoader.loadModel(extras, this);
        //Create game logics
        gameLogic = new GameLogic(model);
        //Create game task
        gameTask=new GameTask(model, gameLogic, BoardImage,
                timeBetweenTurns*1000, this);
        //Update view with chip matrix from model
        BoardImage.setChipMatrix(model.getBoardFields());
        //Update view with dice throws from model
        BoardImage.setDices(model.getDiceThrows());
        //Invalidate view, it is drawn
        BoardImage.invalidate();
        //Get sensor manager
        sensorManager=(SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        //Get sensor
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Start game thread
        gameTask.execute();
    }

    //Method called when back button is pressed
    @Override
    public void onBackPressed() {
        //Call leave method to close stuff
        leaveMethod();
        //Create result intent
        Intent data= new Intent();
        setResult(MenuActivity.GAME_PRESSED_BACK, data);
        super.onBackPressed();
    }

    //Method called after onCreate, before onResume. Must be called if on closing onStop was called
    @Override
    protected void onStart() {
        super.onStart();
        //If pause was done it means that game was closed with onStop, so model was loaded from file
        //and save file must be deleted
        if(pauseDone==1){
            File file=new File(GameActivity.this.getFilesDir().getAbsolutePath(),
                    MenuActivity.GAME_CONTINUE_SAVE_FILE_NAME);
            file.delete();
        }
    }

    //Method called after onStart. Must be called if on closing onPause was called
    @Override
    protected void onResume() {
        super.onResume();
        //If onPause was called which shut down game thread, so new one must be created and started
        if(pauseDone==1){
            //Flag reset
            pauseDone=0;
            //New game thread created
            gameTask=new GameTask(model, gameLogic, BoardImage,
                    timeBetweenTurns*1000, this);
            //New game thread started
            gameTask.execute();
        }
    }

    //Method called on pausing activity
    @Override
    protected void onPause() {
        if(gameTask!=null) {
            //Set work flag in game thread to 0
            gameTask.setWorkFlag(0);
            //Synchronize end of activity with setting endRoutineStarted to 1
            //which will stop oll other endActivity attempts
            synchronized (gameTask) {
                if (gameTask.getEndRoutineStarted() == 0) {
                    gameTask.setEndRoutineStarted(1);
                    pauseDone=1;
                }
            }
            //If current pause ending activity is first one
            if(pauseDone==1){
                //Shut down game thread if it was waiting
                synchronized (model.getCurrentObjectPlayer()) {
                    model.getCurrentObjectPlayer().setWaitCond(0);
                    model.getCurrentObjectPlayer().notifyAll();
                }
                //Stop sound player
                clearMPlayer();
                //Unregister shake listener if it was registered
                sensorManager.unregisterListener(DiceListener);
            }
        }
        super.onPause();
    }

    //Method called on stop activity, after onPause activity
    @Override
    protected void onStop() {
        //If current pause ending activity is first one
        if(pauseDone==1){
            synchronized (gameTask){
                //Wait until game thread is finished
                while (gameTask.getFinishedFlag() != 1) {
                    try {
                        gameTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //Save model to file
            modelLoader.saveModel(model, this);
        }
        super.onStop();
    }

    //Method used on back pressed to shut down all resources
    public void leaveMethod(){
        if(gameTask!=null){
            //Set work flag in game thread to 0
            gameTask.setWorkFlag(0);
            //Shut down game thread if it was waiting
            synchronized (model.getCurrentObjectPlayer()) {
                model.getCurrentObjectPlayer().setWaitCond(0);
                model.getCurrentObjectPlayer().notifyAll();
            }
            //Stop sound player
            clearMPlayer();
            //Unregister shake listener if it was registered
            sensorManager.unregisterListener(DiceListener);
            int tempFlag=0;
            //Synchronize end of activity with setting endRoutineStarted to 1
            //which will stop oll other endActivity attempts
            synchronized (gameTask) {
                if(gameTask.getEndRoutineStarted()==0){
                    gameTask.setEndRoutineStarted(1);
                    tempFlag=1;
                }
                //Wait until game thread is finished
                while (gameTask.getFinishedFlag() != 1) {
                    try {
                        gameTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //if back pressed is first end of activity save model to file
            if(tempFlag==1) {
                modelLoader.saveModel(model, this);
            }
        }
    }

    //Method for stopping and clearing sound player
    public void clearMPlayer(){
        //Synchronize on sound player string
        synchronized (mPlayerSem){
            //if sound player exists stop it and delete it
            if(mPlayer!=null){
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer=null;
            }
        }
    }

    //Method for playing sounds on media player
    //SongNum= 1:diceShake, 2:diceRoll
    public void setMPlayer(int SongNum){
        //Synchronize on sound player string
        synchronized (mPlayerSem){
            //if sound player exists stop it and delete it
            if(mPlayer!=null){
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer=null;
            }
            if(SongNum==1){
                //Play dice_shake sound
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_shake);
                //Calculate volume
                final float volume = (float) (1 -
                        (Math.log(SettingsActivity.MAX_SOUND_VOLUME - soundVolume)
                                / Math.log(SettingsActivity.MAX_SOUND_VOLUME)));
                //Set volume
                mPlayer.setVolume(volume, volume);
                //Set sound looping so it doesnt end until its turned off
                mPlayer.setLooping(true);
                //Play sound
                mPlayer.start();
            }
            else{
                //Play dice_roll sound
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dice_roll);
                //Calculate volume
                final float volume = (float) (1 -
                        (Math.log(SettingsActivity.MAX_SOUND_VOLUME - soundVolume)
                                / Math.log(SettingsActivity.MAX_SOUND_VOLUME)));
                //Set volume
                mPlayer.setVolume(volume, volume);
                //Play sound
                mPlayer.start();
            }
        }
    }

    //Getters and setters
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
}
