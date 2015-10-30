Unity Android Sensor Plugin
====

Unity plugin for using Android sensors.

Usage
----

See also: [ExampleBehaviour/SensorBehaviour.cs](ExampleBehaviour/SensorBehaviour.cs).

### Add plugin jar file to Unity Assets
Download [AndroidSensorPlugin.jar](library/release/AndroidSensorPlugin.jar) and put it into Unity Assets folder ( `Assets/Plugins/Android/AndroidSensorPlugin.jar` ).

### Add codes to script
Add these codes to your Behaviour script, and they initialize the plugin.
```c#
using UnityEngine;

...

        private AndroidJavaObject plugin;

        void Start ()
        {
#if UNITY_ANDROID
                plugin = new AndroidJavaClass("jp.kshoji.unity.sensor.UnitySensorPlugin").CallStatic<AndroidJavaObject>("getInstance");
#endif
        }

        void OnApplicationQuit ()
        {
#if UNITY_ANDROID
		if (plugin != null) {
			plugin.Call("terminate");
			plugin = null;
		}
#endif
	}
```

To use `accelerometer` sensor, add these lines on the `Start` method.
```c#
#if UNITY_ANDROID
        if (plugin != null) {
                plugin.Call("startSensorListening", "accelerometer");
        }
#endif
```

To get sensor values, add these lines on `Update` method.
```c#
#if UNITY_ANDROID
	if (plugin != null) {
		float[] sensorValue = plugin.Call<float[]>("getSensorValues", "accelerometer");
	}
#endif
```

### Tips
If the app want to read sensor values more frequently, call `setSamplingPeriod` plugin method.
The argument is in `micro seconds`.

```c#
	plugin.Call("setSamplingPeriod", 1000); // refreshes sensor 1 milli second each
```

And then, call `InvokeRepeating(string methodName, float time, float repeatRate)` method on Unity Behaviour's `Start` method.
This makes enable more frequently method calling than game FPS.

```c#
        void Start ()
        {
                ... initializations here ...
                
                // wait 1.0 second, and call `CheckSensor` method at 1000Hz (1/0.001 Hz)
                InvokeRepeating("CheckSensor", 1.0f, 0.001f);
        }

        void CheckSensor ()
        {
#if UNITY_ANDROID
                if (plugin != null) {
                        float[] sensorValue = plugin.Call<float[]>("getSensorValues", "accelerometer");
                }
#endif
        }
```

Available Sensors
----
* accelerometer
* ambienttemperature
* gamerotationvector
* geomagneticrotationvector
* gravity
* gyroscope
* gyroscopeuncalibrated
* heartrate
* light
* linearacceleration
* magneticfield
* magneticfielduncalibrated
* pressure
* proximity
* relativehumidity
* rotationvector
* significantmotion
* stepcounter
* stepdetector
