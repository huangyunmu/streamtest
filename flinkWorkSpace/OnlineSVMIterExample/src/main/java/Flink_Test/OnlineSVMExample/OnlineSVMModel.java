package Flink_Test.OnlineSVMExample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.DenseVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

public class OnlineSVMModel extends OnlineLearningModel {
	public OnlineSVMModel(String[] args) {
		super(args);
	}

	@Override
	protected CoFlatMapFunction<LabeledVector, DenseVector, DenseVector> train() {
		return new CoFlatMapFunction<LabeledVector, DenseVector, DenseVector>() {
			private DenseVector oldParams = null;
			private DenseVector latestParams = null;
			private DenseVector accuGradient = null;
			private int updateCnt = 0;
			private boolean inited = false;
			private Queue<Integer> itemQueue = null;
			private int correctCount = 0;
			private int countInterval = 2000;
			private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

			private void init() {
				if (!inited) {
					inited = true;
					oldParams = newParams();
					latestParams = newParams();
					accuGradient = newParams();
					updateCnt = 0;
					itemQueue = new LinkedList<Integer>();
					correctCount = 0;
					String timeStamp = df.format(new Date());
					System.out.println("Start:"+timeStamp);

				}
			}

			public void flatMap1(LabeledVector v, Collector<DenseVector> coll) throws Exception { // use
																									// each
																									// sample
																									// to
																									// train
																									// local
																									// model
				init();
				// http://www.mit.edu/~rakhlin/6.883/lectures/lecture04.pdf,
				// Algorithm 2, Pegasos
				double fac = v.label() * latestParams.dot(v.vector());
				double dec = 1 - learningRate * regularization;
				for (int i = 0; i < paramSize; i++) {
					latestParams.data()[i] *= dec;
				}
				if (itemQueue.size() == countInterval) {
					Integer temp = itemQueue.poll();
					if (temp == 1) {
						correctCount--;
					}
				}
				if (fac < 1) {
					SparseVector features = (SparseVector) v.vector();
					double[] values = features.data();
					int[] indices = features.indices();
					for (int i = 0; i < indices.length; i++) {
						latestParams.data()[indices[i]] += learningRate * v.label() * values[i];
					}
					itemQueue.add(0);
				} else {
					itemQueue.add(1);
					correctCount++;
				}
				updateCnt += 1;
				if (updateFreq == updateCnt) { // Can use count based here, not
												// timer based, because we need
												// to ensure coll is legal.
					updateCnt = 0;
					DenseVector grad = newParams();
					for (int i = 0; i < paramSize; i++) {
						grad.data()[i] = latestParams.data()[i] - oldParams.data()[i];
					}
					coll.collect(grad);
					for (int i = 0; i < paramSize; i++) {
						oldParams.data()[i] += accuGradient.data()[i];
						accuGradient.data()[i] = 0;
					}
					latestParams = oldParams.copy();

					// Print the current accuracy
					String timeStamp = df.format(new Date());
					String message = "Current time:" + timeStamp + "\n";
					message = message + "Correct count:" + correctCount + "\n";
					message = message + "Currect accuracy:" + ((float) correctCount / itemQueue.size());
					System.out.println(message);
				}

			}

			public void flatMap2(DenseVector grad, Collector<DenseVector> coll) throws Exception { // update
																									// local
																									// params
																									// with
																									// broadcasted
																									// gradient
				init();
				for (int i = 0; i < paramSize; i++) {
					accuGradient.update(i, accuGradient.data()[i] + grad.data()[i]);
				}
			}
		};
	}
}
