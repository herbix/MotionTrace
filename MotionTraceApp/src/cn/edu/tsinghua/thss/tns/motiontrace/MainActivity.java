package cn.edu.tsinghua.thss.tns.motiontrace;

import cn.edu.tsinghua.thss.tns.motiontrace.motion.DevicePositionClassifier;
import cn.edu.tsinghua.thss.tns.motiontrace.motion.StepEstimator;
import cn.edu.tsinghua.thss.tns.motiontrace.posture.PostureEstimator;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity {

	public double[] values = new double[3];
	public double[] values2 = new double[3];
	public double[] values3 = new double[3];

	public double[] values4 = new double[3];
	
	public double step = 0;
	public double stepLength = 0;
	public double stepTime = 0;

    private MainView view;
	private SensorEventListener sensorEventListener;

	private SensorManager sensorManager;
	
	private PostureEstimator pe = new PostureEstimator();
	public StepEstimator se = new StepEstimator();
	public DevicePositionClassifier dpc = new DevicePositionClassifier();

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view = new MainView(this));
		initListeners();
    }

	private void initListeners() {
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorEventListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) { }

			@Override
			public void onSensorChanged(SensorEvent event) {
				double[] raw = floatArrToDoubleArr(event.values);
				double[] postured;
				
				switch (event.sensor.getType()) {
				
				case Sensor.TYPE_ACCELEROMETER:
					pe.inputRawData(DataType.TYPE_ACC, raw, event.timestamp);
					dpc.inputRawData(DataType.TYPE_ACC, raw, event.timestamp);
					postured = pe.getPosturedData(raw);
					
					se.setGravity(pe.getGravityValue());
					se.inputPosturedData(DataType.TYPE_ACC, postured, event.timestamp);
					step = se.getStepCount();
					stepTime = se.getLastStepTime() / 1e9;
					
					double[] direction = se.getDirection();
					if (direction == null) {
						direction = se.getStepDirection();
						if (direction == null) {
							direction = new double[2];
						}
					}
					
					values = pe.getRawData(new double[] { 0, 0, 20 });
					values2 = pe.getRawData(new double[] { 20, 0, 0 });
					values3 = pe.getRawData(new double[] { 0, 20, 0 });
					values4 = pe.getRawData(new double[] { direction[0], direction[1], 0 });
					
					view.refresh();
			
					break;
					
				case Sensor.TYPE_MAGNETIC_FIELD:
					pe.inputRawData(DataType.TYPE_MAG, raw, event.timestamp);
					dpc.inputRawData(DataType.TYPE_MAG, raw, event.timestamp);
					break;
					
				case Sensor.TYPE_GYROSCOPE:
					pe.inputRawData(DataType.TYPE_GYR, raw, event.timestamp);
					dpc.inputRawData(DataType.TYPE_GYR, raw, event.timestamp);
					break;
		
				default:
					break;
				}
			}

		};

		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);

		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_FASTEST);
		
	}
	
	private static double[] floatArrToDoubleArr(float[] arr) {
		double[] result = new double[arr.length];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		return result;
	}
	
}
