package Flink_Test.LogReg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
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

		// Transform the data from string to float
		DataStream<Tuple2<Integer, Float[]>> dataStream;
		dataStream = strData.map(new MapFunction<String[], Tuple2<Integer, Float[]>>() {

			private static final long serialVersionUID = 1L;

			public Tuple2<Integer, Float[]> map(String[] value) throws Exception {
				// TODO Auto-generated method stub
				Tuple2<Integer, Float[]> tempTuple = new Tuple2<Integer, Float[]>();
				Float[] tempFeature = new Float[value.length];
				int dim = value.length;
				for (int i = 0; i < dim; i++) {
					tempFeature[i] = Float.parseFloat(value[i]);
				}
				// Label
				tempTuple.f0 = Integer.parseInt(value[dim]);
				// Feature
				tempTuple.f1 = tempFeature;
				return tempTuple;
			}

		});
		DataStream<Tuple2<Integer, Float>> sumStream;
		sumStream=dataStream.map(new MapFunction<Tuple2<Integer, Float[]>,Tuple2<Integer, Float>>(){

		
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Tuple2<Integer, Float> map(Tuple2<Integer, Float[]> data) throws Exception {
				// TODO Auto-generated method stub
				Float sum=0F;
				for(int i=0;i<data.f1.length;i++){
					sum=sum+data.f1[i];
				}
				Tuple2<Integer,Float> tempTuple=new Tuple2<Integer,Float>();
				tempTuple.f0=data.f0;
				tempTuple.f1=sum;
				return tempTuple;
			}
			
		});

		KeyedStream ks = sumStream.keyBy(0);
		DataStream<Float> output = ks.reduce(new ReduceFunction<Float>() {
		    public Float reduce(Float value1, Float value2)
		    throws Exception {
		        return value1 + value2;
		    }
		});

		if (inputParams.has("output")) {
			dataStream.writeAsText(inputParams.get("output"), WriteMode.OVERWRITE);
			// System.out.println("Final Weight:" + Arrays.toString(weight));
		} else {
			// System.out.println("Printing result to stdout. Use --output to
			// specify output path.");

		}

		env.execute("My Log Reg Test");
	}

}
