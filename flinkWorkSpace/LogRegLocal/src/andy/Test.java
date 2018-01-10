package andy;

import java.util.List;

public class Test {
	public static final String PATH = "D:\\VM\\Sharefolder\\Data\\Stream\\logistic_old.txt";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogRegression lr = new LogRegression();
		ReadData rd = new ReadData(PATH);
		lr.train(rd, 0.001f, 3); 
	}

}
