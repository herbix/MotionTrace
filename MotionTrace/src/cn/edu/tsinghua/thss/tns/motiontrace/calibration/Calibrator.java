package cn.edu.tsinghua.thss.tns.motiontrace.calibration;

import Jama.Matrix;
import cn.edu.tsinghua.thss.tns.motiontrace.RawDataInput;

public class Calibrator implements RawDataInput {
	
	private boolean isCalibrating = false;
	
	private double[] zero = new double[3];
	private double[] scale = new double[] {1, 1, 1};
	
	private Matrix P;
	private Matrix k;
	
	private final int type;
	
	public Calibrator(int type) {
		this.type = type;
	}

	@Override
	public void inputRawData(int type, double[] data, long timestamp) {
		if (!isCalibrating || type != this.type) {
			return;
		}
		
		double x = data[0];
		double y = data[1];
		double z = data[2];
		
		double h = -x*x;
		
		Matrix a = new Matrix(new double[]{-2*x, y*y, -2*y, z*z, -2*z, 1}, 1);
		Matrix K = P.times(a.transpose()).times(1.0 / (1 + a.times(P).times(a.transpose()).get(0, 0)));
		k.plusEquals(K.timesEquals(h - a.times(k).get(0, 0)));
		P = Matrix.identity(6, 6).minusEquals(K.times(a)).times(P);
		
		zero[0] = k.get(0,0);
		zero[1] = k.get(2,0) / k.get(1,0);
		zero[2] = k.get(4,0) / k.get(3,0);
		scale[0] = Math.sqrt(zero[0]*zero[0] + zero[1]*k.get(2,0) + zero[2]*k.get(4,0) - k.get(5,0));
		scale[1] = scale[0] / Math.sqrt(k.get(1,0));
		scale[2] = scale[0] / Math.sqrt(k.get(3,0));
	}
	
	public void startCalibration() {
		P = Matrix.identity(6, 6).times(10000);
		k = new Matrix(new double[]{0, 1, 0, 1, 0, -1}, 6);
		zero = new double[3];
		scale = new double[] {1, 1, 1};
		isCalibrating = true;
	}
	
	public void finishCalibration() {
		isCalibrating = false;
	}

	public double[] getCalibratedData(double[] data) {
		double[] result = new double[3];
		for(int i=0; i<3; i++) {
			result[i] = (data[i] - zero[i]) / scale[i];
		}
		return result;
	}

	public double[] getZero() {
		return zero;
	}

	public void setZero(double[] zero) {
		this.zero = zero;
	}

	public double[] getScale() {
		return scale;
	}

	public void setScale(double[] scale) {
		this.scale = scale;
	}

}
