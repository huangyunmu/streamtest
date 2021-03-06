package andy;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadData {
	
	public ArrayList<List<Float>> dataList = new ArrayList<List<Float>>();
	public ArrayList<Float> labelList = new ArrayList<Float>();
    
	ReadData(String path) {
		try {
			init(path);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void init(String path) throws IOException {
		BufferedReader buff = new BufferedReader(new InputStreamReader(
				(new FileInputStream(new File(path)))));

		String str = buff.readLine();
		while (str != null) {
			String[] arr = str.split("\t");
			ArrayList<Float> tempList=new ArrayList<Float>();
			for(int i=0;i<arr.length-1;i++){
				tempList.add(Float.parseFloat(arr[i]));
			}
			//For bias
			tempList.add(1F);
			this.dataList.add(tempList);
			this.labelList.add(Float.parseFloat(arr[arr.length-1]));
			str = buff.readLine();
		}
		buff.close();
		
	}

}