package andy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CheckResult {
	private static float sigmoid(float src) {
		return (float) (1.0 / (1 + Math.exp(-src)));
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

//		ArrayList<Float> param = new ArrayList<Float>();
//		int N = 2;
//		// Online 20
//		Float th1 = 0.4837126F;
//		Float th2 = -0.6400407F;
//		Float th3 = 3.5971315F;
//		// Online 10
//		// Float th1 = 0.3563895F;
//		// Float th2 = -0.50079465F;
//		// Float th3 = 2.4334385F;
//		// Online 5
//		// Float th1 =0.22853912F;
//		// Float th2 =-0.39139557F;
//		// Float th3 =1.5168636F;
//		// Online 20
//		// Float th1 = 0.10212765F;
//		// Float th2 = -0.30418792F;
//		// Float th3 = 0.71384054F;
//
//		// BGD
//		// Float th1 = 1.14127F;
//		// Float th2 = -1.784762F;
//		// Float th3 = 13.115594F;
//		// MBGD
//		// Float th1 = 0.872F;
//		// Float th2 = -1.255F;
//		// Float th3 = 8.968F;
//		// SGD
//		// Float th1 = 0.30F;
//		// Float th2 = -0.38F;
//		// Float th3 = 2.14F;
//		param.add(th1);
//		param.add(th2);
//		param.add(th3);
//		String filename = "D:\\VM\\Sharefolder\\Data\\Stream\\logistic_old.txt";
//		BufferedReader buff = new BufferedReader(new InputStreamReader((new FileInputStream(new File(filename)))));
//		String str = buff.readLine();
//		int countAll, countY;
//		countAll = 0;
//		countY = 0;
//		while (str != null) {
//			String[] arr = str.split("\t");
//			Float sum = 0F;
//			for (int i = 0; i < 2; i++) {
//				// System.out.print(arr[i]+" ");
//				sum = sum + param.get(i) * Float.parseFloat(arr[i]);
//			}
//			sum = sum + param.get(2);
//			System.out.println();
//			Float result = sigmoid(sum);
//
//			Boolean testTag;
//			Boolean realTag = arr[N].equals("1") ? true : false;
//			// 0.15 for stream
//			// 0.2 for batch
//			if (result > 0.5) {
//				testTag = true;
//			} else {
//				testTag = false;
//			}
//			if (testTag == realTag) {
//				countY++;
//			}
//			countAll++;
//			System.out.println(testTag + "|" + result + "|" + realTag);
//			str = buff.readLine();
//		}
//		System.out.println();
//		System.out.println(((float) countY) / ((float) countAll));
		int num1=10;
		int num2=25;
		System.out.println(num1/(float)num2);
	}

}
