package Flink_Test.LogReg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadData {

	private ArrayList<ArrayList<Float>> dataList = new ArrayList<ArrayList<Float>>();
	private ArrayList<Float> labelList = new ArrayList<Float>();
	public int count = -1;

	ReadData(String path) {
		try {
			init(path);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void init(String path) throws IOException {
		BufferedReader buff = new BufferedReader(new InputStreamReader((new FileInputStream(new File(path)))));

		String str = buff.readLine();
		while (str != null) {
			String[] arr = str.split("\t");
			ArrayList<Float> tempList = new ArrayList<Float>();
			for (int i = 0; i < arr.length; i++) {
				tempList.add(Float.parseFloat(arr[i]));
			}
			this.dataList.add(tempList);
			this.labelList.add(Float.parseFloat(arr[arr.length - 1]));
			str = buff.readLine();
		}
		buff.close();
	}

	public DataRecord getOneData() {
		count = (count + 1) % this.dataList.size();
		return this.getOneData(count);
	}

	public DataRecord getOneData(int index) {
		DataRecord dr = new DataRecord(this.dataList.get(index), this.labelList.get(index));
		return dr;

	}

}