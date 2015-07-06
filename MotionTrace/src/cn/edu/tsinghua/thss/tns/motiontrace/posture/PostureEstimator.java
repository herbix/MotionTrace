package cn.edu.tsinghua.thss.tns.motiontrace.posture;

import Jama.Matrix;
import cn.edu.tsinghua.thss.tns.motiontrace.MatrixUtils;
import cn.edu.tsinghua.thss.tns.motiontrace.RawDataInput;

public class PostureEstimator implements RawDataInput {

	private Matrix gravity = new Matrix(new double[] {0, 0, 9.81}, 1);
	private Matrix magnetic = new Matrix(new double[] {1, 0, 0}, 1);

	private int gravityRotateCount = 0;
	private int magneticRotateCount = 0;
	
	private Matrix localToWorld = Matrix.identity(3, 3);
	private Matrix worldToLocal = Matrix.identity(3, 3);

	private long lastAccTime = -1;
	private long lastMagTime = -1;
	private long lastGyrTime = -1;
	
	private boolean shouldUpdateLocalToWorldMatrix = false;

	@Override
	public void inputRawData(int type, double[] data, long timestamp) {
		switch (type) {
		case TYPE_MAG:
			inputMag(data, timestamp);
			break;
		case TYPE_ACC:
			inputAcc(data, timestamp);
			break;
		case TYPE_GYR:
			inputGyr(data, timestamp);
			break;
		}
	}

	private void inputAcc(double[] data, long timestamp) {
		if (lastAccTime < 0) {
			lastAccTime = timestamp;
			return;
		}

		double K = 1.0 / (1 + gravityRotateCount * 0.001);
		
		gravity.timesEquals(K).plusEquals(new Matrix(data, 1).timesEquals(1 - K));

		gravityRotateCount = 0;
		shouldUpdateLocalToWorldMatrix = true;
		lastAccTime = timestamp;
	}

	private void inputMag(double[] data, long timestamp) {
		if (lastMagTime < 0) {
			lastMagTime = timestamp;
			return;
		}
		
		double K = 1.0 / (1 + magneticRotateCount * 0.01);

		magnetic.timesEquals(K).plusEquals(new Matrix(data, 1).timesEquals(1 - K));

		magneticRotateCount = 0;
		shouldUpdateLocalToWorldMatrix = true;
		lastMagTime = timestamp;
	}
	
	private void inputGyr(double[] data, long timestamp) {
		if (lastGyrTime < 0) {
			lastGyrTime = timestamp;
			return;
		}

		Matrix axis = new Matrix(data, 1);
		double rad = axis.normF() * (timestamp - lastGyrTime) / 1e9;
		axis.timesEquals(1 / axis.normF());
		Matrix rotate = MatrixUtils.createRotateMatrix(axis, -rad).transpose();

		gravity = gravity.times(rotate);
		magnetic = magnetic.times(rotate);

		gravityRotateCount++;
		magneticRotateCount++;
		lastGyrTime = timestamp;
	}

	public Matrix getLocalToWorldMatrix() {
		if (shouldUpdateLocalToWorldMatrix) {
			Matrix gra2 = gravity.times(-1 / gravity.normF());
			Matrix mag2 = MatrixUtils.getQuadratureComponent(magnetic, gra2);
			mag2.timesEquals(1 / mag2.normF());
			Matrix y = MatrixUtils.cross(gra2, mag2);
			localToWorld.setMatrix(0, 0, 0, 2, mag2);
			localToWorld.setMatrix(1, 1, 0, 2, y);
			localToWorld.setMatrix(2, 2, 0, 2, gra2);
			worldToLocal = localToWorld.inverse();
		}

		return localToWorld;
	}
	
	public double[] getPosturedData(double[] data) {
		return getLocalToWorldMatrix().times(new Matrix(data, 3)).getRowPackedCopy();
	}

	public double[] getRawData(double[] data) {
		getLocalToWorldMatrix();
		return worldToLocal.times(new Matrix(data, 3)).getRowPackedCopy();
	}
	
	public double getGravityValue() {
		return gravity.normF();
	}
	
	public double getMagneticValue() {
		return magnetic.normF();
	}

}
