package Flink_Test.LogReg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;



public class Test {

	// public static final String PATH =
	// "D:\\VM\\Sharefolder\\Data\\Stream\\logistic.txt";
	static int INPUT_DATA_SIZE = 3;
	static Float LEARNIN_RATE = 0.01f;
	static int PARALLELISM = 1;
	public static final Object[][] PARAMS = new Object[][] { new Object[] { 0.0, 0.0 } };
	public static String sampleToString(Tuple2<Integer, Float[]> value) {
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
		
		// set up the execution environment
		// final StreamExecutionEnvironment env =
		// StreamExecutionEnvironment.getExecutionEnvironment();
	
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment()
				.setParallelism(PARALLELISM);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String timeStamp = df.format(new Date());
		String logFileName = "log/api-test-log-";
		logFileName="d://api-test-log-";
		File file = new File(logFileName + timeStamp + ".txt");
		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("Program Start at :" + df.format(new Date()));
		bw.newLine();
		
		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(inputParams);
		// Get the data from txt file
		DataStream<String> text = null;
		if (inputParams.has("input")) {
			// read the text file from given input path
			text = env.readTextFile(inputParams.get("input"));
		} else {
			System.out.println("Use --input to specify file input.");
			// get default test text data
		}

		// Convert string to tuple

		DataStream<Tuple2<Integer, Float[]>> dataStream;
		dataStream = text.map(new MapFunction<String, Tuple2<Integer, Float[]>>() {

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
					tempSum = tempSum + tempFeature[i];
				}
			
				tempTuple.f1 = tempFeature;
				return tempTuple;
			}

		});
		
		//Parameter stream
		List<Params> tempList = new LinkedList<Params>();
		tempList.add(new Params(0F, 0F));
		DataStream<Params> paraStream = env.fromCollection(tempList);
		paraStream.broadcast();
		
        //Set the loop to 1
		IterativeStream<Params> loop = paraStream.iterate(1);	

		DataSet<Params> newParams=dataStream.map(new SubUpdate()).reduce(new UpdateAccumulator()).map(new Update());;

		DataStream<String> output;
		output = dataStream.map(new MapFunction<Tuple2<Integer, Float[]>, String>() {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			public String map(Tuple2<Integer, Float[]> value) throws Exception {
				String temp;
				temp=sampleToString(value);
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
		bw.newLine();
		bw.write("Program End at :" + df.format(new Date()));
		bw.newLine();
		bw.close();
	}
	public static class SubUpdate extends RichMapFunction<Tuple2<Integer,Float[]>, Tuple2<Params, Integer>> {

		private Collection<Params> parameters;

		private Params parameter;

		private int count = 1;

		/** Reads the parameters from a broadcast variable into a collection. */

		public void open(Configuration parameters) throws Exception {
			this.parameters = getRuntimeContext().getBroadcastVariable("parameters");
		}

		@Override
		public Tuple2<Params, Integer> map(Tuple2<Integer,Float[]> data) throws Exception {

			for (Params p : parameters){
				this.parameter = p;
			}   

			Float theta0 = (float) (parameter.theta0 - 0.01 * ((parameter.theta0 + (parameter.theta1 * data.f1[0])) - data.f1[1]));
			Float theta1 = (float) (parameter.theta1 - 0.01 * (((parameter.theta0 + (parameter.theta1 * data.f1[0])) - data.f1[1]) * data.f1[0]));

			return new Tuple2<Params, Integer>(new Params(theta0, theta1), count);
		}

		
	}
	
	/**
	 * Accumulator all the update.
	 * */
	public static class UpdateAccumulator implements ReduceFunction<Tuple2<Params, Integer>> {

		public Tuple2<Params, Integer> reduce(Tuple2<Params, Integer> val1, Tuple2<Params, Integer> val2) {

			Float newTheta0 = val1.f0.theta0 + val2.f0.theta0;
			Float newTheta1 = val1.f0.theta1 + val2.f0.theta1;
			Params result = new Params(newTheta0, newTheta1);
			return new Tuple2<Params, Integer>(result, val1.f1 + val2.f1);

		}
	}

	/**
	 * Compute the final update by average them.
	 */
	public static class Update implements MapFunction<Tuple2<Params, Integer>, Params> {

		public Params map(Tuple2<Params, Integer> arg0) throws Exception {

			return arg0.f0.div(arg0.f1);

		}

	}
}
