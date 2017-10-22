package andy;


import java.util.Arrays;
import java.util.List;

public class LogRegression {
	
	public void train(ReadData rd, float step, int type) {
		List<List<Float>> datas = rd.dataList;
		List<Float> labels = rd.labelList;
		int size = datas.size();
		int dim = datas.get(0).size();
		float[] weight = new float[dim]; // Init
		float changes = Float.MAX_VALUE;
		int caculate = 0;

		switch (type) {
		case 1: // BGD
			while (changes > 0.0001) {
				float[] wClone = weight.clone();
				float[] out = new float[size];
				for (int s = 0; s < size; s++) {
					float lire = innerProduct(weight, datas.get(s));
					out[s] = sigmoid(lire);
				}
				for (int d = 0; d < dim; d++) {
					float sum = 0;
					for (int s = 0; s < size; s++) {
						sum += (labels.get(s) - out[s]) * datas.get(s).get(d);
					}
					float q = weight[d];
					weight[d] = (float) (q + step * sum);

					// w[d] = (float) (q + step * sum-0.01*Math.pow(q,2)); L2����
					// w[d] = (float) (q + step * sum-0.01*Math.abs(q)); L1����
				}
				changes = changsWeight(wClone, weight);
				caculate++;
				System.out.println("Iteration: " + caculate + "  Weight:" + Arrays.toString(weight));
			}

			break;
		case 2:// SGD
			while (changes > 0.0001) {
				float[] wClone = weight.clone();
				for (int s = 0; s < size; s++) {
					float lire = innerProduct(weight, datas.get(s));
					float out = sigmoid(lire);
					float error = labels.get(s) - out;
					for (int d = 0; d < dim; d++) {
						weight[d] += step * error * datas.get(s).get(d);
					}
				}
				changes = changsWeight(wClone, weight);
				caculate++;

				System.out.println("Iteration:" + caculate + "  Weight:" + Arrays.toString(weight));
			}

			break;

		default:
			break;
		}

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