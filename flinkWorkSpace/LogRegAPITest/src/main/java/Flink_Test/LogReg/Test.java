package Flink_Test.LogReg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem.WriteMode;
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String timeStamp = df.format(new Date());
		String logFileName = "log/api-test-log-";
		File file = new File(logFileName + timeStamp + ".txt");
		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		// set up the execution environment
		// final StreamExecutionEnvironment env =
		// StreamExecutionEnvironment.getExecutionEnvironment();
		bw.write("Program Start at :" + df.format(new Date()));
		bw.newLine();
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment()
				.setParallelism(PARALLELISM);

		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(inputParams);
		DataStream<String> text = null;
		if (inputParams.has("input")) {
			// read the text file from given input path
			text = env.readTextFile(inputParams.get("input"));
		} else {
			System.out.println("Use --input to specify file input.");
			// get default test text data
		}

		// Get the data from txt file
//		DataStream<String[]> strData;
//		strData = text.map(new MapFunction<String, String[]>() {
//			public String[] map(String value) throws Exception {
//				return value.split("\t");
//			}
//		});

		// Convert string to tuple
		DataStream<Tuple2<Integer, Float[]>> dataStream;
		dataStream = text.map(new MapFunction<String, Tuple2<Integer, Float[]>>() {

			private static final long serialVersionUID = 1L;

			public Tuple2<Integer, Float[]> map(String value) throws Exception {
				// TODO Auto-generated method stub
				Tuple2<Integer, Float[]> tempTuple = new Tuple2<Integer, Float[]>();
				tempTuple.f0=1;
				tempTuple.f1=new Float[]{2f,3f};
				// String[] split = value.split("\t");
				// int dim = split.length - 1;
				// // Label
				// tempTuple.f0 = Integer.parseInt(split[dim + 1]);
				// // Feature
				// Float[] tempFeature = new Float[dim];
				// for (int i = 0; i < dim; i++) {
				// tempFeature[i] = Float.parseFloat(split[i]);
				// }
				// tempTuple.f1 = tempFeature;
				return tempTuple;
			}

		});

		// Transform the data from string to float
		// DataStream<Tuple2<Integer, Float[]>> dataStream;
		// dataStream = strData.map(new MapFunction<String[], Tuple2<Integer,
		// Float[]>>() {
		//
		// private static final long serialVersionUID = 1L;
		//
		// public Tuple2<Integer, Float[]> map(String[] value) throws Exception
		// {
		// // TODO Auto-generated method stub
		// Tuple2<Integer, Float[]> tempTuple = new Tuple2<Integer, Float[]>();
		// int dim = value.length - 1;
		// // Label
		// tempTuple.f0 = Integer.parseInt(value[dim + 1]);
		// // Feature
		// Float[] tempFeature = new Float[dim];
		// for (int i = 0; i < dim; i++) {
		// tempFeature[i] = Float.parseFloat(value[i]);
		// }
		// tempTuple.f1 = tempFeature;
		// return tempTuple;
		// }
		//
		// });
		// DataStream<Tuple2<Integer, Float>> sumStream;
		// sumStream = dataStream.map(new MapFunction<Tuple2<Integer, Float[]>,
		// Tuple2<Integer, Float>>() {
		//
		// /**
		// *
		// */
		// private static final long serialVersionUID = 1L;
		//
		// public Tuple2<Integer, Float> map(Tuple2<Integer, Float[]> data)
		// throws Exception {
		// // TODO Auto-generated method stub
		// Float sum = 0F;
		// for (int i = 0; i < data.f1.length; i++) {
		// sum = sum + data.f1[i];
		// }
		// Tuple2<Integer, Float> tempTuple = new Tuple2<Integer, Float>();
		// tempTuple.f0 = data.f0;
		// tempTuple.f1 = sum;
		// return tempTuple;
		// }
		//
		// });
		//
		// KeyedStream ks = sumStream.keyBy(0);
		// DataStream<Float> output = ks.reduce(new ReduceFunction<Float>() {
		// public Float reduce(Float value1, Float value2) throws Exception {
		// return value1 + value2;
		// }
		// });

//		DataStream<String> output;
//		output = strData.map(new MapFunction<String[], String>() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//			public String map(String[] value) throws Exception {
//				String temp = "";
//				for (int i = 0; i < value.length; i++) {
//					temp = temp + value[i] + "|";
//				}
//                temp=temp+"Andy Test";
//				return temp;
//			}
//		});

		DataStream<String> output;
		output = dataStream.map(new MapFunction<Tuple2<Integer, Float[]>, String>() {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			public String map(Tuple2<Integer, Float[]> value) throws Exception {
				String temp = "";
				for (int i = 0; i < value.f1.length; i++) {
					temp = temp + value.f1[i] + "|";
				}
				temp = temp + value.f0;
				System.out.println(temp);
				return temp;
			}
		});

		if (inputParams.has("output")) {
			output.writeAsText(inputParams.get("output"), WriteMode.OVERWRITE);
			// System.out.println("Final Weight:" + Arrays.toString(weight));
		} else {
			System.out.println("Printing result to stdout. Use --output to specify output path.");
			text.print();
		}
		env.execute("My Log Reg Test");
		bw.write("Program End at :" + df.format(new Date()));
		bw.newLine();
		bw.close();
	}

}
