package cn.edu.tsinghua.thss.tns.motiontrace.motion;

import cn.edu.tsinghua.thss.tns.motiontrace.RawDataInput;

public class DevicePositionClassifier implements RawDataInput {

	public static final int STATIC = 0;
	public static final int CARRYING = 1;
	public static final int SWING = 2;
	public static final int OTHER = 3;

	public static final double TIME_WINDOW = 1;

	private static final String[] STATE_STRING = {"STATIC", "CARRYING", "SWING", "OTHER"};

	private int state = STATIC;
	
	private double[][] savedE = new double[4][3];
	private double[][] savedV = new double[4][3];
	private double[] dataRadius = new double[]{9.81, 40, 0, 0};
	private double[] dataVRadius = new double[4];
	private long[] lastTime = new long[4];

	@Override
	public void inputRawData(int type, double[] data, long timestamp) {
		if(lastTime[type] == 0) {
			lastTime[type] = timestamp;
			return;
		}
		
		double timeDiff = (timestamp - lastTime[type]) / 1e9;
		
		if(timeDiff > TIME_WINDOW) {
			savedE[type] = data.clone();
			savedV[type] = new double[3];
		} else {
			for(int i=0; i<3; i++) {
				savedE[type][i] = (data[i]*timeDiff + savedE[type][i]*(TIME_WINDOW-timeDiff)) / TIME_WINDOW;
				savedV[type][i] = ((data[i]-savedE[type][i])*(data[i]-savedE[type][i])*timeDiff + savedV[type][i]*(TIME_WINDOW-timeDiff)) / TIME_WINDOW;
			}
		}
		
		dataRadius[type] = Math.sqrt(savedE[type][0]*savedE[type][0] +
				savedE[type][1]*savedE[type][1] +
				savedE[type][2]*savedE[type][2]);

		dataVRadius[type] = Math.sqrt(savedV[type][0]*savedV[type][0] +
				savedV[type][1]*savedV[type][1] +
				savedV[type][2]*savedV[type][2]);
		
		if(Math.abs(dataRadius[0] - 9.81) < 0.3 && dataVRadius[0] < 1.5) {
			state = STATIC;
		} else {
			if(dataRadius[2] > 1 || dataVRadius[2] > 5) {
				state = SWING;
			} else {
				state = CARRYING;
			}
		}

		lastTime[type] = timestamp;
	}

	public int getState() {
		return state;
	}
	
	public String getStateString() {
		return STATE_STRING[state];
	}

}
