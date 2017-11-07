package Flink_Test.LogReg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
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
			text = env.readTextFile(inputParams.get("input")).setParallelism(1);
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
		DataStream<DataRecord> dataStream;
		dataStream = strData.map(new MapFunction<String[], DataRecord>() {
			public DataRecord map(String[] value) throws Exception {
				Float[] temp = new Float[value.length];
				int dim = value.length;
				for (int i = 0; i < dim; i++) {
					temp[i] = Float.parseFloat(value[i]);
				}

				ArrayList<Float> tempList = new ArrayList<Float>();
				Float tempLabel;
				for (int i = 0; i < dim - 1; i++) {
					tempList.add(temp[i]);
				}
				tempLabel = temp[dim - 1];
				DataRecord tempDr = new DataRecord(tempList, tempLabel);
				// Didn't work
				// Float lire = LogRegression.innerProduct(weight,
				// tempDr.getDataList());
				// Float out = LogRegression.sigmoid(lire);
				// Float error = tempDr.getLabel() - out;
				// for (int d = 0; d < dim; d++) {
				// weight[d] += STEP * error * tempDr.getDataList().get(d);
				// }
				// System.out.println("Current Weight:" +
				// Arrays.toString(weight));
				return tempDr;
			}
		});

		// Generate parameter
		List<Params> paramsList = new LinkedList<Params>();
		paramsList.add(new Params(0.1f, 0.2f));

		DataStream<Params> parameters = env.fromCollection(paramsList);
//		DataStream<Params> newParameters = dataStream
//				// compute a single step using every sample
//				.map(new SubUpdate()).withBroadcastSet(parameters)
//				// sum up all the steps
//				.reduce(new UpdateAccumulator())
//				// average the steps and update all parameters
//				.map(new Update());
//		
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

	// *************************************************************************
	// USER FUNCTIONS
	// *************************************************************************
	/**
	 * A set of parameters -- theta0, theta1.
	 */
	public static class Params implements Serializable {

		private Float theta0, theta1;

		public Params() {
		}

		public Float[] toFloat(){
			Float[] temp={theta0,theta1};
			return temp; 
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

	/**
	 * Compute a single BGD type update for every parameters.
	 */
	public static class SubUpdate extends RichMapFunction<DataRecord, Tuple2<Params, Integer>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Collection<Params> parameters;

		private Params parameter;

		private int count = 1;

		/** Reads the parameters from a broadcast variable into a collection. */
		@Override
		public void open(Configuration parameters) throws Exception {
			this.parameters = getRuntimeContext().getBroadcastVariable("parameters");
		}

		@Override
		public Tuple2<Params, Integer> map(DataRecord dr) throws Exception {

			for (Params p : parameters) {
				this.parameter = p;
			}
			Float[] weight=this.parameter.toFloat();
			Float lire=LogRegression.innerProduct(weight, dr.getDataList());
			
			Float theta0=0F;
//			= parameter.theta0 - 0.01 * ((parameter.theta0 + (parameter.theta1 * dr.x)) - dr.y);
			Float theta1=0F;
//			= parameter.theta1 - 0.01 * (((parameter.theta0 + (parameter.theta1 * dr.x)) - dr.y) * dr.x);

			return new Tuple2<Params, Integer>(new Params(theta0, theta1), count);
		}
	}
	/**
	 * Accumulator all the update.
	 * */
	public static class UpdateAccumulator implements ReduceFunction<Tuple2<Params, Integer>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Tuple2<Params, Integer> reduce(Tuple2<Params, Integer> val1, Tuple2<Params, Integer> val2) {

			Float newTheta0 = val1.f0.theta0 + val2.f0.theta0;
			Float newTheta1 = val1.f0.theta1 + val2.f0.theta1;
			Params result = new Params(newTheta0, newTheta1);
			return new Tuple2<Params, Integer>(result, val1.f1 + val2.f1);

		}
	}

}
