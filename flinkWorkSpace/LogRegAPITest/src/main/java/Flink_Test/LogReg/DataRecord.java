package Flink_Test.LogReg;

import java.util.ArrayList;

public class DataRecord {
	private ArrayList<Float> dataList;
	private Float label;

	DataRecord() {

	}

	DataRecord(ArrayList<Float> dataList, Float label) {
		this.dataList = dataList;
		this.label = label;
	}

	public void setDataList(ArrayList<Float> dataList) {
		this.dataList = dataList;
	}

	public void setLabel(Float label) {
		this.label = label;
	}

	public ArrayList<Float> getDataList() {
		return this.dataList;
	}

	public Float getLabel() {
		return this.label;
	}
}
