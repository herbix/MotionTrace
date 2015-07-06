package cn.edu.tsinghua.thss.tns.motiontrace.motion;

import cn.edu.tsinghua.thss.tns.motiontrace.PosturedDataInput;

public class StepEstimator implements PosturedDataInput {

	private static final long MIN_STEP_INTERVAL = 400000000l;
	private static final long MAX_STEP_INTERVAL = 1300000000l;

	private static final double THRESHOLD_POS      =  0.2;
	private static final double THRESHOLD_NEG      = -0.2;
	private static final double THRESHOLD_POS_PEAK =  1.1;
	private static final double THRESHOLD_NEG_PEAK = -1.1;

	private static final double ALPHA = 0.5;

	private int stepCount = 0;
	private int state = 0;
	
	private long lastTime = -1;

	private double gravity;
	
	private long stepStartTime = -1;
	private long stepEndTime = -1;
	private long stepTime = 0;

	private long lastStepStartTime = -1;

	private long stepState2Time = 0;
	private long stepState6Time = 0;
	private long stepHalfTime = 0;

	private double vVel = 0;
	private double vDis = 0;

	private double lastVDis = 0;

	private double[] lastData;
	private double[] stepAcc = new double[2];
	private double[] lastStepAcc = null;
	private double[] lastTwoStepAcc = null;

	@Override
	public void inputPosturedData(int type, double[] data, long timestamp) {
		if (type != TYPE_ACC) {
			return;
		}
		
		if (lastTime < 0) {
			lastTime = timestamp;
			return;
		}
		
		double vAccel = data[2] + gravity;

		double vVelNew = vVel + vAccel * (timestamp - lastTime) / 1e9;
		vDis += (vVel + vVelNew) / 2 * (timestamp - lastTime) / 1e9;
		vVel = vVelNew;

		if(state == 0 || state == 8) {
			vVel = vDis = 0;
		}

		if (lastData != null) {
			vAccel = ALPHA * vAccel + (1 - ALPHA) * (lastData[2] + gravity);
		}

		int nextState = getNextState(vAccel);
		
		if (nextState == 1 && nextState != state) {
			stepStartTime = timestamp;
		}
		
		if (nextState == 2 && nextState != state) {
			stepState2Time = timestamp;
		}
		
		if (nextState == 6 && nextState != state) {
			stepState6Time = timestamp;
			stepHalfTime = stepState6Time - stepState2Time;
		}
		
		if (nextState == 8 && nextState != state) {
			stepEndTime = timestamp;
			stepTime = stepEndTime - stepStartTime;
			
			double p = (double)stepHalfTime / stepTime;
			
			if (stepTime >= MIN_STEP_INTERVAL && p > 0.4 && p < 0.6) {
				stepCount++;

				lastStepStartTime = stepStartTime;

				double timeDiff = stepTime / 1e6;

				stepAcc[0] /= timeDiff;
				stepAcc[1] /= timeDiff;

				if (lastStepAcc != null) {
					lastTwoStepAcc = new double[]{ lastStepAcc[0] + stepAcc[0], lastStepAcc[1] + stepAcc[1] };
				} else {
					lastTwoStepAcc = null;
				}
				
				lastStepAcc = stepAcc;
				stepAcc = new double[2];
				lastVDis = vDis;
			} else {
				reset();
			}
			nextState = 0;
		}

		if (timestamp - stepStartTime >= MAX_STEP_INTERVAL) {
			nextState = 0;
			reset();
		}

		if (nextState != 0) {
			double timeDiff = (timestamp - lastTime) / 1e6;
			if (nextState >= 2 && nextState <= 5) {
				stepAcc[0] -= timeDiff * data[0];
				stepAcc[1] -= timeDiff * data[1];
			} else {
				stepAcc[0] += timeDiff * data[0];
				stepAcc[1] += timeDiff * data[1];
			}
		}

		state = nextState;
		lastTime = timestamp;
		lastData = data.clone();
	}

	private void reset() {
		stepStartTime = stepEndTime = -1;
		lastStepAcc = null;
		lastTwoStepAcc = null;
		stepAcc = new double[2];
	}

	public void setGravity(double value) {
		gravity = value;
	}

	public double getStepCount() {
		return stepCount + state / 8.0;
	}

	public void clearStepCount() {
		stepCount = 0;
	}

	public double[] getStepDirection() {
		return lastStepAcc;
	}

	public double[] getDirection() {
		return lastTwoStepAcc;
	}
	
	public long getLastStepStart() {
		return lastStepStartTime;
	}
	
	public long getLastStepTime() {
		return stepTime;
	}
	
	public long getLastStepHalfTime() {
		return stepHalfTime;
	}
	
	public double getLastVDisplacement() {
		return lastVDis;
	}

	public double getEstimatedStrideLength(double height) {
		double scale = ((1e9 / stepTime) - 1.57) / 0.23 * 0.08 + 0.37;
		if(scale < 0.2) {
			scale = 0.2;
		}
		if(scale > 0.42) {
			scale = 0.42;
		}
		return scale * height;
	}

	private static final int[][] STATE_TABLE = new int[][] {
		{ 0, 0, 0, 1, 1 },
		{ 1, 1, 1, 1, 2 },
		{ 3, 3, 3, 3, 2 },
		{ 4, 4, 4, 3, 3 },
		{ 5, 5, 4, 3, 3 },
		{ 6, 5, 4, 4, 4 },
		{ 6, 7, 7, 7, 7 },
		{ 7, 7, 8, 8, 8 },
		{ 0, 0, 0, 1, 1 }
	};

	private int getNextState(double vAccel) {
		int index = 0;
		if (vAccel < THRESHOLD_NEG_PEAK) {
			index = 4;
		} else if (vAccel < THRESHOLD_NEG) {
			index = 3;
		} else if (vAccel <= THRESHOLD_POS) {
			index = 2;
		} else if (vAccel <= THRESHOLD_POS_PEAK) {
			index = 1;
		}
		return STATE_TABLE[state][index];
	}

}
