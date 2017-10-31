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
    static int MODEL_SISE=3;
    static Float STEP=0.01f;
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
		final Float[] weight=new Float[MODEL_SISE];
		DataStream<Float[]> floatData;
		floatData = strData.map(new MapFunction<String[], Float[]>() {
			public Float[] map(String[] value) throws Exception {
				Float[] temp = new Float[value.length];
				int dim=value.length;
				ArrayList<Float> tempList=new ArrayList<Float>();
				Float tempLabel;
				for (int i = 0; i < dim; i++) {
					temp[i] = Float.parseFloat(value[i]);
				}
				for(int i=0;i<dim-1;i++){
					tempList.add(temp[i]);
				}
				tempLabel=temp[dim-1];
				DataRecord tempDr=new DataRecord(tempList,tempLabel);
				Float[] wClone = weight.clone();
				Float lire = LogRegression.innerProduct(weight, tempDr.getDataList());
				Float out = LogRegression.sigmoid(lire);
				Float error = tempDr.getLabel() - out;
				for (int d = 0; d < dim; d++) {
					weight[d] += STEP * error * tempDr.getDataList().get(d);
				}
				System.out.println("Current  Weight:" + Arrays.toString(weight));
				return temp;
			}
		});
		
//		IterativeStream<Float[]> worker = floatData.iterate();
      
        
//		DataStream<String> output = floatData.map(new MapFunction<Float[], String>() {
//			public String map(Float[] value) throws Exception {
//				String temp = "";
//				for (int i = 0; i < value.length; i++) {
//					temp = temp + value[i].toString() + " ";
//				}
//				return temp;
//			}
//		});
		
		
		if (params.has("output")) {
//			output.writeAsText(params.get("output"));
			// floatData.writeAsCsv(params.get("output"));
			System.out.println("Final  Weight:" + Arrays.toString(weight));
		} else {
			System.out.println("Printing result to stdout. Use --output to specify output path.");
//			output.print();
		}
		// LogRegression lr = new LogRegression();
		// ReadData rd = new ReadData(PATH);
		//
		// float[] weight=lr.train(rd, 0.001f, 2, 1);
		// System.out.print("Final Weight:" + Arrays.toString(weight));
		env.execute("My Log Reg Test");
	}

}
