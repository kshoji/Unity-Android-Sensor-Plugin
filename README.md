Unity Android Sensor Plugin
====

Unity plugin for using Android sensors.

Usage
----

See also: ./ExampleBehaviour directory.

### Add plugin jar file
Put ./library/release/AndroidSensorPlugin.jar to Unity Assets ( Assets/Plugins/Android/AndroidSensorPlugin.jar )

### Add code to script
Add these codes to your Behaviour script, and this initializes the plugin.
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
