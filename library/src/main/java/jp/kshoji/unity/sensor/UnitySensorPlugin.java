package jp.kshoji.unity.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sensors plugin for Unity
 *
 * @author K.Shoji
 */
public final class UnitySensorPlugin {

    /**
     * Obtains {@link UnitySensorPlugin} instance
     * Called by Unity
     *
     * @return {@link UnitySensorPlugin} instance
     */
    public static UnitySensorPlugin getInstance() {
        // double check synchronization
        UnitySensorPlugin result = instance;
        if (result == null) {
            synchronized (SINGLETON_LOCK) {
                result = instance;
                if (result == null) {
                    result = new UnitySensorPlugin();
                    result.sensorManager = (SensorManager) UnityPlayer.currentActivity.getSystemService(Context.SENSOR_SERVICE);
                    instance = result;
                }
            }
        }

        return result;
    }

    /**
     * Get if the sensor is valid
     * Called by Unity
     *
     * @param sensorKind sensor kind @see {@link Sensors}
     * @return true if the sensor exist
     */

    public boolean hasSensor(String sensorKind) {

        Sensors specifiedSensor;
        try {
            specifiedSensor = Sensors.valueOf(sensorKind);
        } catch (IllegalArgumentException e) {
            // valueOf failed
            Log.e(getClass().getName(), "Bad sensor type: " + sensorKind + ", available types: " + Arrays.toString(Sensors.values()));
            return false;
        }

       if( sensorManager.getDefaultSensor( specifiedSensor.getSensorType() ) == null) {
              return false;
       }

        return true;
    }
    
    /**
     * Terminates instance
     * Must be called on App's last use.
     * Called by Unity
     */
    public void terminate() {
        Log.d(this.getClass().getName(), "Terminating UnitySensorPlugin.");
        for (SensorEventListener eventListener : eventListeners.values()) {
            sensorManager.unregisterListener(eventListener);
        }

        eventListeners.clear();
    }

    /**
     * Sets sensor sampling period in micro seconds.
     * Called by Unity
     *
     * @param samplingPeriodUs sampling period @see {@link SensorManager#registerListener(SensorEventListener, Sensor, int)}
     */
    public void setSamplingPeriod(int samplingPeriodUs) {
        samplingPeriod = samplingPeriodUs;
        for (UnitySensorEventListener eventListener : eventListeners.values()) {
            sensorManager.unregisterListener(eventListener);

            for (Sensor sensor : eventListener.getSensors()) {
                sensorManager.registerListener(eventListener, sensor, samplingPeriod);
            }
        }
    }

    /**
     * Obtains current sensor values
     * Called by Unity
     *
     * @param sensorKind sensor kind @see {@link Sensors}
     * @return latest sensor values
     */
    public float[] getSensorValues(String sensorKind) {
        UnitySensorEventListener sensorEventListener = eventListeners.get(convertSensorKind(sensorKind));
        if (sensorEventListener == null) {
            return null;
        }

        return sensorEventListener.getLastValues();
    }

    /**
     * Start to listen sensor
     * Called by Unity
     *
     * @param sensorKind sensor kind @see {@link Sensors}
     */
    public void startSensorListening(String sensorKind) {
        addSensorEventListener(sensorKind, null, null);
    }

    /**
     * Add sensor listener with callback
     * Called by Unity
     *
     * @param sensorKind sensor kind @see {@link Sensors}
     * @param gameObjectName Unity game object name
     * @param methodName Unity Behaviour's method name for callback
     */
    public void addSensorEventListener(String sensorKind, String gameObjectName, String methodName) {
        String sensorKindConverted = convertSensorKind(sensorKind);

        UnitySensorEventListener sensorEventListener = eventListeners.get(sensorKindConverted);

        if (sensorEventListener == null) {
            Sensors specifiedSensor;
            try {
                specifiedSensor = Sensors.valueOf(sensorKindConverted);
            } catch (IllegalArgumentException e) {
                // valueOf failed
                Log.e(getClass().getName(), "Bad sensor type: " + sensorKind + ", available types: " + Arrays.toString(Sensors.values()));
                return;
            }

            List<Sensor> sensors = sensorManager.getSensorList(specifiedSensor.getSensorType());
            sensorEventListener = new UnitySensorEventListener(sensors);
            for (Sensor sensor : sensors) {
                sensorManager.registerListener(sensorEventListener, sensor, samplingPeriod);
            }
            eventListeners.put(sensorKindConverted, sensorEventListener);
        }

        if (gameObjectName != null && methodName != null) {
            sensorEventListener.addUnityCallback(new UnityCallback(gameObjectName, methodName));
        }
    }

    /**
     * Sensors enumerator
     */
    private enum Sensors {
        accelerometer(android.hardware.Sensor.TYPE_ACCELEROMETER),
        ambienttemperature(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE),
        gamerotationvector(android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR),
        geomagneticrotationvector(android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR),
        gravity(android.hardware.Sensor.TYPE_GRAVITY),
        gyroscope(android.hardware.Sensor.TYPE_GYROSCOPE),
        gyroscopeuncalibrated(android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
        heartrate(android.hardware.Sensor.TYPE_HEART_RATE),
        light(android.hardware.Sensor.TYPE_LIGHT),
        linearacceleration(android.hardware.Sensor.TYPE_LINEAR_ACCELERATION),
        magneticfield(android.hardware.Sensor.TYPE_MAGNETIC_FIELD),
        magneticfielduncalibrated(android.hardware.Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED),
        pressure(android.hardware.Sensor.TYPE_PRESSURE),
        proximity(android.hardware.Sensor.TYPE_PROXIMITY),
        relativehumidity(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY),
        rotationvector(android.hardware.Sensor.TYPE_ROTATION_VECTOR),
        significantmotion(android.hardware.Sensor.TYPE_SIGNIFICANT_MOTION),
        stepcounter(android.hardware.Sensor.TYPE_STEP_COUNTER),
        stepdetector(android.hardware.Sensor.TYPE_STEP_DETECTOR),
        ;

        private int sensorType;

        Sensors(int sensorType) {
            this.sensorType = sensorType;
        }

        int getSensorType() {
            return sensorType;
        }
    }

    /**
     * Stores callback information
     */
    private static class UnityCallback {
        private final String gameObjectName;
        private final String methodName;

        UnityCallback(String gameObjectName, String methodName) {
            this.gameObjectName = gameObjectName;
            this.methodName = methodName;
        }

        public String getGameObjectName() {
            return gameObjectName;
        }

        public String getMethodName() {
            return methodName;
        }

        @Override
        public int hashCode() {
            return (gameObjectName + methodName).hashCode();
        }
    }

    /**
     * for event listening
     */
    private int samplingPeriod = SensorManager.SENSOR_DELAY_GAME;
    private SensorManager sensorManager;
    private Map<String, UnitySensorEventListener> eventListeners = new HashMap<>();

    /**
     * for singleton pattern
     */
    private static final Object SINGLETON_LOCK = new Object();
    private static volatile UnitySensorPlugin instance;
    private UnitySensorPlugin() {
        // not to be called
    }

    /**
     * Converts sensorKind to enum value string.
     *
     * @param sensorKind sensor kind string
     * @return string for enum
     */
    private String convertSensorKind(String sensorKind) {
        return sensorKind.toLowerCase(Locale.US).replaceAll("_", "");
    }

    /**
     * SensorEventListener for Unity
     */
    private static class UnitySensorEventListener implements SensorEventListener {
        private float[] lastValues;
        private final List<Sensor> sensors;

        private final Set<UnityCallback> unityCallbacks = new HashSet<>();

        public UnitySensorEventListener(List<Sensor> sensors) {
            this.sensors = sensors;
        }

        @Override
        public final void onSensorChanged(SensorEvent event) {
            lastValues = event.values.clone();
            for (UnityCallback unityCallback : unityCallbacks) {
                UnityPlayer.UnitySendMessage(unityCallback.getGameObjectName(), unityCallback.getMethodName(), Arrays.toString(event.values));
            }
        }

        @Override
        public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public float[] getLastValues() {
            return lastValues;
        }

        public void addUnityCallback(UnityCallback callback) {
            synchronized (unityCallbacks) {
                unityCallbacks.add(callback);
            }
        }

        public List<Sensor> getSensors() {
            return sensors;
        }
    }
}
