package Flink_Test.LogReg;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Test {

	// public static final String PATH =
	// "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// Checking input parameters
		final ParameterTool params = ParameterTool.fromArgs(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(params);
		DataStream<String> text = null;
		if (params.has("input")) {
			// read the text file from given input path
			text = env.readTextFile(params.get("input"));
		} else {
			System.out.println("Executing WordCount example with default input data set.");
			System.out.println("Use --input to specify file input.");
			// get default test text data
		}
		DataStream<String[]> strData;
		strData = text.map(new MapFunction<String, String[]>() {
			public String[] map(String value) throws Exception {
				return value.split("\t");
			}
		});
		DataStream<Float[]> floatData;
		floatData = strData.map(new MapFunction<String[], Float[]>() {
			public Float[] map(String[] value) throws Exception {
				Float[] temp = new Float[value.length];
				for (int i = 0; i < temp.length; i++) {
					temp[i] = Float.parseFloat(value[i]);
				}
				return temp;
			}
		});
		floatData.print();
		// LogRegression lr = new LogRegression();
		// ReadData rd = new ReadData(PATH);
		//
		// float[] weight=lr.train(rd, 0.001f, 2, 1);
		// System.out.print("Final Weight:" + Arrays.toString(weight));
	}

}