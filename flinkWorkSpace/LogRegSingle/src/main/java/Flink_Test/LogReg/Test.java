package Flink_Test.LogReg;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Test {

	// public static final String PATH =
	// "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";
	static int INPUT_DATA_SIZE = 3;
	static Float LEARNIN_RATE = 0.01f;
	static int PARALLELISM = 1;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// Checking input parameters
		final ParameterTool inputParams = ParameterTool.fromArgs(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment()
				.setParallelism(PARALLELISM);

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

		final Float[] weight = { 0.0f, 0.0f };

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
					System.out.println(temp[i]);
					tempList.add(temp[i]);
				}
				tempLabel = temp[dim - 1];
				DataRecord tempDr = new DataRecord(tempList, tempLabel);
				// Didn't work
				Float lire = LogRegression.innerProduct(weight, tempDr.getDataList());
				Float out = LogRegression.sigmoid(lire);
				Float error = tempDr.getLabel() - out;
				for (int d = 0; d < dim; d++) {
					weight[d] += LEARNIN_RATE * error * tempDr.getDataList().get(d);
				}
				System.out.println("Current Weight:" + Arrays.toString(weight));
				return tempDr;
			}
		});

		if (inputParams.has("output")) {

			System.out.println("Final Weight:" + Arrays.toString(weight));
		} else {
			System.out.println("Printing result to stdout. Use --output to specify output path.");

		}

		env.execute("My Log Reg Test");
	}

}
