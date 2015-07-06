package cn.edu.tsinghua.thss.tns.motiontrace;

public interface RawDataInput extends DataType {

	public void inputRawData(int type, double[] data, long timestamp);

}
