# MotionTrace
MotionTrace is a java library that is used to estimate smart phone posture and detect steps of the person that holds the phone.

## Usage
* Install [Eclipse](www.eclipse.org/downloads)
* Install [Android SDK](http://developer.android.com/sdk/index.html)
* Install [Android Development Tools](http://developer.android.com/sdk/installing/installing-adt.html)
* Clone this repository
* Create an empty workspace in Eclipse
* Import all project in this repository to Eclipse
* Compile and use

Project `MotionTrace` provides all features, and `MotionTraceApp` is a demo or example to show how to use MotionTrace.

## Features
MotionTrace provides estimaters that input raw data of smartphone sensors and output estimation value.

### PostureEstimator
Inputs raw data of accelerometer, magnetometer and gyroscope, calculates posture of the smart phone,
provides methods converting global coordinate system to local coordinate system of the phone, and vise versa.

Here's some example codes:
```Java
// Initialization
  PostureEstimator pe = new PostureEstimator();
  ...
// In sensor reading event handler
  switch (event.sensor.getType()) {
  case Sensor.TYPE_ACCELEROMETER:
    pe.inputRawData(DataType.TYPE_ACC, rawdata, timestamp);
    break;
  case Sensor.TYPE_MAGNETIC_FIELD:
  	pe.inputRawData(DataType.TYPE_MAG, rawdata, timestamp);
  	break;
  case Sensor.TYPE_GYROSCOPE:
  	pe.inputRawData(DataType.TYPE_GYR, rawdata, timestamp);
  	break;
  }
  ...
// When posture is used
  // Convert direction from global to local coordinate system
  double[] xaxisLocal = pe.getRawData(new double[] { 20, 0, 0 });
  // Convert direction from local to global coordinate system
  double[] yaxisGlobal = pe.getPosturedData(new double[] { 0, 20, 0 });
  // Get gravity value
  double gravity = pe.getGravityValue();
  // Get the rotation matrix that transforms local vectors to global vectors
  Matrix matrix = pe.getLocalToWorldMatrix();
```
### Step Estimator
Inputs data in global coordinate system and estimates step count, walk direction and stride length.

Example:
```Java
// Initialization
  PostureEstimator pe = new PostureEstimator();
  StepEstimator se = new StepEstimator();
  ...
// In sensor reading event handler
  switch (event.sensor.getType()) {
  case Sensor.TYPE_ACCELEROMETER:
    pe.inputRawData(DataType.TYPE_ACC, rawdata, timestamp);
    double[] postured = pe.getPosturedData(rawdata);
    se.setGravity(pe.getGravityValue());
    se.inputPosturedData(DataType.TYPE_ACC, postured, event.timestamp);
    break;
  case Sensor.TYPE_MAGNETIC_FIELD:
  	pe.inputRawData(DataType.TYPE_MAG, rawdata, timestamp);
  	break;
  case Sensor.TYPE_GYROSCOPE:
  	pe.inputRawData(DataType.TYPE_GYR, rawdata, timestamp);
  	break;
  }
  ...
// When step is used
  // Get step count
  double step = se.getStepCount();
  // Get walk direction, may return null
  double[] direction = se.getDirection();
  // Get stride length, input height of the person
  double length = se.getEstimatedStrideLength(1.8);
```

### DevicePositionClassifier
Inputs raw data of sensors, output where the smart phone is (static, carrying, swing or other).

```Java
// Initialization and event handler is the same as PostureEstimator, skip.
  ...
// Usage:
  int state = dpc.getState();  // 0 for static, 1 for carrying, 2 for swing, 3 for other
```

### Calibrator
Inputs raw data, output the zero position and scale of different axises. Each calibrator is only for one sensor.

```Java
// Initialization
  Calibrator c = new Calibrator(DataType.TYPE_MAG);
  ...
// In sensor event
  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
    c.inputRawData(DataType.TYPE_MAG, rawdata, timestamp);
  }
  ...
// Usage:
  // Get zero point of the sensor
  double[] zero = c.getZero();
  // Get scale of each axis
  double[] scale = c.getScale();
```

## License
[Herbix Simple License](LICENSE)
