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
	protected CoFlatMapFunction<CountLabelExample, DenseVector, DenseVector> train() {
		return new CoFlatMapFunction<CountLabelExample, DenseVector, DenseVector>() {
			private DenseVector oldParams = null;
			private DenseVector latestParams = null;
			private DenseVector accuGradient = null;
			private int updateCnt = 0;
			private int printAllDataCnt = 0;
			private int printNewDataCnt = 0;
			private int printParamsCnt=0;
			private boolean inited = false;
			private Queue<Integer> allDataQueue = null;
			private Queue<Integer> newDataQueue = null;
			private int allDataCorrectCount = 0;
			private int newDataCorrectCount = 0;
			private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

			private void init() {
				if (!inited) {
					inited = true;
					oldParams = newParams();
					latestParams = newParams();
					accuGradient = newParams();
					updateCnt = 0;
					allDataQueue = new LinkedList<Integer>();
					newDataQueue = new LinkedList<Integer>();
					allDataCorrectCount = 0;
					newDataCorrectCount = 0;
					String timeStamp = df.format(new Date());
					System.out.println("Start:" + timeStamp);

				}
			}

			public void flatMap1(CountLabelExample cv, Collector<DenseVector> coll) throws Exception { // use
																										// each
																										// sample
																										// to
																										// train
																										// local
																										// model
				init();
				// http://www.mit.edu/~rakhlin/6.883/lectures/lecture04.pdf,
				// Algorithm 2, Pegasos
				LabeledVector v = cv.getVector();
				int trainCount = cv.getCount();
				boolean isNew = false;
				if (trainCount == trainFreq) {
					isNew = true;
				} else {
					isNew = false;
				}
				double fac = v.label() * latestParams.dot(v.vector());
				double dec = 1 - learningRate * regularization;
				for (int i = 0; i < paramSize; i++) {
					latestParams.data()[i] *= dec;
				}
				// Remove the first element in the queue
				if (allDataQueue.size() == allDataCountFreq) {
					Integer temp = allDataQueue.poll();
					if (temp == 1) {
						allDataCorrectCount--;
					}
				}
				if (newDataQueue.size() == newDataCountFreq) {
					Integer temp = newDataQueue.poll();
					if (temp == 1) {
						newDataCorrectCount--;
					}
				}
				// update fac if needed
				if (fac < 1) {
					SparseVector features = (SparseVector) v.vector();
					double[] values = features.data();
					int[] indices = features.indices();
					for (int i = 0; i < indices.length; i++) {
						latestParams.data()[indices[i]] += learningRate * v.label() * values[i];
					}
					allDataQueue.add(0);
					if (isNew) {
						newDataQueue.add(0);
					}
				} else {
					allDataQueue.add(1);
					allDataCorrectCount++;
					if (isNew) {
						newDataQueue.add(1);
						newDataCorrectCount++;
					}

				}
				updateCnt += 1;
				// update parameter and print the result
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
				}
				printParamsCnt++;
				//Output the parameters
				if(printParamsCnt==paramOutputFreq){
					printParamsCnt=0;
					StringBuffer sb = new StringBuffer();
					String timeStamp = df.format(new Date());
					sb.append("1,");
					sb.append(timeStamp + ",");
					for (int i = 0; i < paramSize - 1; i++) {
						sb.append(latestParams.data()[i] + ",");
					}
					sb.append(latestParams.data()[paramSize - 1]);
					System.out.println(sb.toString());
				}
				printAllDataCnt++;
				// Print the accuracy of all data
				if (printAllDataCnt == allDataCountFreq) {

					printAllDataCnt = 0;
					String timeStamp = df.format(new Date());
					
					String message = "2,"+timeStamp + ",";
					message = message + "AllDataAccuracy:" + ((float) allDataCorrectCount / allDataQueue.size());
					System.out.println(message);
				}
				printNewDataCnt++;
				// Print the accuracy of new data
				if (printNewDataCnt == newDataCountFreq) {
					printNewDataCnt = 0;
					String timeStamp = df.format(new Date());
					String message = "3," + timeStamp + ",";
					message = message + "NewDataAccuracy:" + ((float) newDataCorrectCount / newDataQueue.size());
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
