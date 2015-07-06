package cn.edu.tsinghua.thss.tns.motiontrace;

import Jama.Matrix;

public class MatrixUtils {

	public static Matrix cross(Matrix x, Matrix y) {
		Matrix z = new Matrix(1, 3);

		if(y.getColumnDimension() == 3 && y.getRowDimension() == 1) {
			z = new Matrix(1, 3);
			z.set(0, 0, x.get(0, 1) * y.get(0, 2) - x.get(0, 2) * y.get(0, 1));
			z.set(0, 1, x.get(0, 2) * y.get(0, 0) - x.get(0, 0) * y.get(0, 2));
			z.set(0, 2, x.get(0, 0) * y.get(0, 1) - x.get(0, 1) * y.get(0, 0));
		} else {
			z = new Matrix(3, 1);
			z.set(0, 0, x.get(1, 0) * y.get(2, 0) - x.get(2, 0) * y.get(1, 0));
			z.set(1, 0, x.get(2, 0) * y.get(0, 0) - x.get(0, 0) * y.get(2, 0));
			z.set(2, 0, x.get(0, 0) * y.get(1, 0) - x.get(1, 0) * y.get(0, 0));
		}

		return z;
	}

	public static Matrix createRotateMatrix(Matrix axis, double rad) {
		Matrix rotate = new Matrix(3, 3);
		double sinr = Math.sin(rad);
		double cosr = Math.cos(rad);

		if(axis.getRowDimension() < axis.getColumnDimension()) {
			axis = axis.transpose();
		}

		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				if(i == j) {
					rotate.set(i, j, cosr+(1-cosr)*axis.get(i, 0)*axis.get(i, 0));
				} else if((i+1)%3 == j) {
					rotate.set(i, j, -sinr*axis.get((i+2)%3, 0)+(1-cosr)*axis.get(i, 0)*axis.get(j, 0));
				} else {
					rotate.set(i, j, sinr*axis.get((i+1)%3, 0)+(1-cosr)*axis.get(i, 0)*axis.get(j, 0));
				}
			}
		}

		return rotate;
	}
	
	public static Matrix getQuadratureComponent(Matrix a, Matrix q) {
		double t;
		
		if(a.getRowDimension() == 1) {
			t = a.times(q.transpose()).get(0, 0);
		} else {
			t = a.transpose().times(q).get(0, 0);
		}
		
		return a.minus(q.times(t / q.normF() / q.normF()));
	}
}
