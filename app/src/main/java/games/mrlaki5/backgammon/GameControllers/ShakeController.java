package games.mrlaki5.backgammon.GameControllers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Encapsulates SensorManager, accelerometer detection, and shake threshold logic.
 */
public class ShakeController implements SensorEventListener {

    public interface OnShakeListener {
        void onShakeStarted();
        void onShakeCompleted();
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private OnShakeListener listener;

    private int shakeThreshold = 100;
    private int sampleTime = 100;
    private int diceDelay = 3;

    private long lastUpdate = 0;
    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    private int shakeStarted = 0;
    private int beforeShakeStability = 0;
    private int shakeStability = 0;
    private boolean isListening = false;

    public ShakeController(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            accelerometer = null;
        }
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    public void updateSettings(int threshold, int sampleTimeMs, int delay) {
        this.shakeThreshold = threshold;
        this.sampleTime = sampleTimeMs;
        this.diceDelay = delay;
    }

    public void startListening() {
        if (sensorManager != null && accelerometer != null && !isListening) {
            reset();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            isListening = true;
        }
    }

    public void stopListening() {
        if (sensorManager != null && isListening) {
            sensorManager.unregisterListener(this);
            isListening = false;
        }
    }

    public void reset() {
        shakeStarted = 0;
        beforeShakeStability = 0;
        shakeStability = 0;
        lastUpdate = 0;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    public int getShakeStarted() {
        return shakeStarted;
    }

    public void setShakeStarted(int shakeStarted) {
        this.shakeStarted = shakeStarted;
    }

    public boolean isListening() {
        return isListening;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > sampleTime) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

            if (speed > shakeThreshold) {
                if (shakeStarted == 0 && beforeShakeStability >= diceDelay) {
                    shakeStarted = 1;
                    if (listener != null) {
                        listener.onShakeStarted();
                    }
                } else {
                    beforeShakeStability++;
                }
                shakeStability = 0;
            } else {
                shakeStability++;
                beforeShakeStability = 0;
                if (shakeStability >= diceDelay && shakeStarted == 1) {
                    if (listener != null) {
                        listener.onShakeCompleted();
                    }
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Blank
    }
}
