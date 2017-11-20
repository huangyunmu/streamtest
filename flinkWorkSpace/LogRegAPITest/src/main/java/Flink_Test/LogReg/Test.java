package Flink_Test.LogReg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;

public class Test {

	// public static final String PATH =
	// "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";
	static int INPUT_DATA_SIZE = 3;
	static Float LEARNIN_RATE = 0.01f;
	static int PARALLELISM = 1;

	public static String dataToString(Tuple2<Integer, Float[]> value) {
		String temp = "Feature:";
		for (int i = 0; i < value.f1.length; i++) {
			temp = temp + value.f1[i] + " ### ";
		}
		temp = temp + " Label:" + value.f0;
		System.out.println(temp);
		return temp;
	}

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
		DataStream<String> inputData = null;
		if (inputParams.has("input")) {
			// read the text file from given input path
			inputData = env.readTextFile(inputParams.get("input"));
		} else {
			System.out.println("Use --input to specify file input.");
			// get default test text data
		}

		// Convert string to tuple

		DataStream<Tuple2<Integer, Float[]>> dataStream;
		dataStream = inputData.map(new MapFunction<String, Tuple2<Integer, Float[]>>() {

			private static final long serialVersionUID = 1L;

			public Tuple2<Integer, Float[]> map(String value) throws Exception {
				// TODO Auto-generated method stub
				Tuple2<Integer, Float[]> tempTuple = new Tuple2<Integer, Float[]>();
				String[] split = value.split(" ");
				int dim = split.length - 1;
				// Label
				tempTuple.f0 = Integer.parseInt(split[dim]);
				// Feature
				Float[] tempFeature = new Float[dim];
				Float tempSum = 0F;
				for (int i = 0; i < dim; i++) {
					tempFeature[i] = Float.parseFloat(split[i]);
					tempSum = tempSum + 0F;
				}
				tempTuple.f1 = tempFeature;
				return tempTuple;
			}

		});
		// List<Params> paramsList = new LinkedList<Params>();
		List<Params> tempList = new LinkedList<Params>();
		tempList.add(new Params(0F, 0F));
		DataStream<Params> paraStream = env.fromCollection(tempList);

		ConnectedStreams<Tuple2<Integer, Float[]>, Params> connectStream = dataStream.connect(paraStream);
		DataStream<String> testStream = connectStream
				.map(new CoMapFunction<Tuple2<Integer, Float[]>, Params, String>() {
					public String map1(Tuple2<Integer, Float[]> value) {
						return dataToString(value);
					}

					public String map2(Params value) {
						return value.toString();
					}
				});
		DataStream<Params> flatStream = connectStream
				.flatMap(new CoFlatMapFunction<Tuple2<Integer, Float[]>, Params, Params>() {

					public void flatMap1(Tuple2<Integer, Float[]> value, Collector<Params> out) {

						out.collect(new Params());
					}

					public void flatMap2(Params value, Collector<Params> out) {
						// out.collect(new Params());
					}
				});
		// DataStream<Tuple2<Integer, Float>> sumStream;
		// sumStream = dataStream.map(new MapFunction<Tuple2<Integer, Float[]>,
		// Tuple2<Integer, Float>>() {
		//
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

		// KeyedStream ks = sumStream.keyBy(0);
		// DataStream<Float> reduceks = ks.reduce(new ReduceFunction<Float>() {
		// public Float reduce(Float value1, Float value2) throws Exception {
		// return value1 + value2;
		// }
		// });

		// DataStream<String> output;
		// output = strData.map(new MapFunction<String[], String>() {
		// /**
		// *
		// */
		// private static final long serialVersionUID = 1L;
		//
		// public String map(String[] value) throws Exception {
		// String temp = "";
		// for (int i = 0; i < value.length; i++) {
		// temp = temp + value[i] + "|";
		// }
		// temp=temp+"Andy Test";
		// return temp;
		// }
		// });

		DataStream<String> output;
		output = dataStream.map(new MapFunction<Tuple2<Integer, Float[]>, String>() {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			public String map(Tuple2<Integer, Float[]> value) throws Exception {
				String temp = "Feature:";
				for (int i = 0; i < value.f1.length; i++) {
					temp = temp + value.f1[i] + " ### ";
				}
				temp = temp + " Label:" + value.f0;
				System.out.println(temp);
				return temp;
			}
		});
		// output = testStream;

		output = flatStream.map(new MapFunction<Params, String>() {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			public String map(Params value) throws Exception {
				String temp;
				temp = value.toString();
				return temp;
			}
		});
		// output = testStream.map(new MapFunction<Boolean, String>() {
		// /**
		// *
		// */
		// private static final long serialVersionUID = 1L;
		//
		// public String map(Boolean value) throws Exception {
		// String temp = "";
		// if (value == true) {
		// temp = "True";
		// } else {
		// temp = "False";
		// }
		// System.out.println(temp);
		// return temp;
		// }
		// });

		// output = sumStream.map(new MapFunction<Tuple2<Integer, Float>,
		// String>() {
		// /**
		// *
		// */
		// private static final long serialVersionUID = 1L;
		//
		// public String map(Tuple2<Integer, Float> value) throws Exception {
		// String temp = "";
		// temp = temp + value;
		// return temp;
		// }
		// });

		if (inputParams.has("output")) {
			output.writeAsText(inputParams.get("output"), WriteMode.OVERWRITE);
			// System.out.println("Final Weight:" + Arrays.toString(weight));
		} else {
			System.out.println("Printing result to stdout. Use --output to specify output path.");
			inputData.print();
		}
		env.execute("My Log Reg Test");
		bw.write("Program End at :" + df.format(new Date()));
		bw.newLine();
		bw.close();
	}

}
