package andy;

import java.util.Arrays;
import java.util.List;

public class LogRegression {

	public float[] train(ReadData rd, float step, int dim, int batchSize) {
		float[] weight = new float[dim]; // Init
		float changes = Float.MAX_VALUE;
		int iterCount = 0;
		while (changes > 0.0001) {
			float[] wClone = weight.clone();

			DataRecord tempDr = rd.getOneData();
			float lire = innerProduct(weight, tempDr.getDataList());
			float out = sigmoid(lire);
			float error = tempDr.getLabel() - out;
			for (int d = 0; d < dim; d++) {
				weight[d] += step * error * tempDr.getDataList().get(d);
			}
			changes = changsWeight(wClone, weight);
			iterCount++;
			System.out.println("Iteration:" + iterCount + "  Weight:" + Arrays.toString(weight));
		}
		
//		System.out.print("Final  Weight:" + Arrays.toString(weight));
		return weight;

	}

	private float changsWeight(float[] wClone, float[] w) {
		float changs = 0;
		for (int i = 0; i < w.length; i++) {
			changs += Math.pow(w[i] - wClone[i], 2);
		}

		return (float) Math.sqrt(changs);

	}

	private float innerProduct(float[] w, List<Float> x) {
		float sum = 0;
		for (int i = 0; i < w.length; i++) {
			sum += w[i] * x.get(i);
		}

		return sum;
	}

	private float sigmoid(float src) {
		return (float) (1.0 / (1 + Math.exp(-src)));
	}

}