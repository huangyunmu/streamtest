package andy;

import java.util.Arrays;

public class Test {

	public static final String PATH = "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogRegression lr = new LogRegression();
		ReadData rd = new ReadData(PATH);
		float[] weight=lr.train(rd, 0.001f,2,1); 
		System.out.println(Arrays.toString(weight));
	}

}
