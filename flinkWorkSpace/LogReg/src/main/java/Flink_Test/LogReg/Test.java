package Flink_Test.LogReg;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class Test {

	// public static final String PATH =
	// "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";
	static int INPUT_DATA_SIZE = 3;
	static Float STEP = 0.001f;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// Checking input parameters
		final ParameterTool inputParams = ParameterTool.fromArgs(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(inputParams);
		DataStream<String> text = null;
		if (inputParams.has("input")) {
			// read the text file from given input path
			text = env.readTextFile(inputParams.get("input"));
		} else {
			System.out.println("Executing WordCount example with default input data set.");
			System.out.println("Use --input to specify file input.");
			// get default test text data
		}

		// Get the data from txt file
		DataStream<String[]> strData;
		strData = text.map(new MapFunction<String, String[]>() {
			public String[] map(String value) throws Exception {
				return value.split("\t");
			}
		});

		// final Float[] weight = new Float[MODEL_SISE];

		// Transform the data from string to float
		DataStream<Float[]> floatData;
		floatData = strData.map(new MapFunction<String[], Float[]>() {
			public Float[] map(String[] value) throws Exception {
				Float[] temp = new Float[value.length];
				int dim = value.length;
				for (int i = 0; i < dim; i++) {
					temp[i] = Float.parseFloat(value[i]);
				}
				// Local version. Didn't work.
				// ArrayList<Float> tempList=new ArrayList<Float>();
				// Float tempLabel;
				// for(int i=0;i<dim-1;i++){
				// tempList.add(temp[i]);
				// }
				// tempLabel=temp[dim-1];
				// DataRecord tempDr=new DataRecord(tempList,tempLabel);
				// Float lire = LogRegression.innerProduct(weight,
				// tempDr.getDataList());
				// Float out = LogRegression.sigmoid(lire);
				// Float error = tempDr.getLabel() - out;
				// for (int d = 0; d < dim; d++) {
				// weight[d] += STEP * error * tempDr.getDataList().get(d);
				// }
				// System.out.println("Current Weight:" +
				// Arrays.toString(weight));
				return temp;
			}
		});

		// Generate data
		List<Params> paramsList = new LinkedList<Params>();
		for (int i = 0; i < INPUT_DATA_SIZE; i++) {
			paramsList.add(new Params(0.1f, 0.2f));
		}
		DataStream<Params> parameters = env.fromCollection(paramsList);
		

		if (inputParams.has("output")) {
			 parameters.writeAsText(inputParams.get("output"));
			// floatData.writeAsCsv(params.get("output"));
			// System.out.println("Final Weight:" + Arrays.toString(weight));
		} else {
			System.out.println("Printing result to stdout. Use --output to specify output path.");
			// output.print();
		}
		// LogRegression lr = new LogRegression();
		// ReadData rd = new ReadData(PATH);
		//
		// float[] weight=lr.train(rd, 0.001f, 2, 1);
		// System.out.print("Final Weight:" + Arrays.toString(weight));
		env.execute("My Log Reg Test");
	}

	/**
	 * A set of parameters -- theta0, theta1.
	 */
	public static class Params implements Serializable {

		private Float theta0, theta1;

		public Params() {
		}

		public Params(Float x0, Float x1) {
			this.theta0 = x0;
			this.theta1 = x1;
		}

		@Override
		public String toString() {
			return theta0 + " " + theta1;
		}

		public double getTheta0() {
			return theta0;
		}

		public double getTheta1() {
			return theta1;
		}

		public void setTheta0(Float theta0) {
			this.theta0 = theta0;
		}

		public void setTheta1(Float theta1) {
			this.theta1 = theta1;
		}

		public Params div(Integer a) {
			this.theta0 = theta0 / a;
			this.theta1 = theta1 / a;
			return this;
		}

	}

}
