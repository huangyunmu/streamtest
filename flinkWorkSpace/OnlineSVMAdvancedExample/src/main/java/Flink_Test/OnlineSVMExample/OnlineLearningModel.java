package Flink_Test.OnlineSVMExample;

import java.io.Serializable;
import java.util.Properties;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.operators.DataSink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.DenseVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uncertainty: 1. Whether it's the correct way to train online svm. The way it
 * broadcast and apply gradients here. 2. How to efficiently send gradient from
 * the sink to the source. >> A: Use kafka. Need to delete the topic each time
 * after running the program.
 */
public abstract class OnlineLearningModel implements Serializable {
	private Logger LOG = LoggerFactory.getLogger(OnlineSVMModel.class);

	protected ParameterTool parameterTool;
	protected String dataTopic;
	protected int paramSize;
	protected double learningRate;
	protected int updateFreq;
	protected double regularization;
	protected String gradTopic;
	protected String bootStrapServers;
	protected String zookeeperConnect;
	protected int trainFreq;// Each sample will be process in train frequency
							// times.
	protected String tempTopic;// Used to store the data with count

	private static double formalize(double label) {
		if (label == 1) {
			return 1;
		} else {
			return -1;
		}
	}

	private LabeledVector parseExample(String s) {
		// format of s: timestamp sample
		// format of sample: label idx1:val1 idx2:val2 idx3:val3 ...
		String[] splits = s.split("\\s");
		double label = formalize(Integer.valueOf(splits[0]));

		int[] indices = new int[splits.length - 1];
		double[] values = new double[splits.length - 1];
		for (int i = 1; i < splits.length; i++) {
			String[] iv = splits[i].split(":");
			indices[i - 1] = Integer.valueOf(iv[0]);
			values[i - 1] = Double.valueOf(iv[1]);
		}
		return new LabeledVector(label, new SparseVector(paramSize, indices, values));
	}

	protected abstract CoFlatMapFunction<LabeledVector, DenseVector, DenseVector> train();

	public OnlineLearningModel(String[] args) {
		parameterTool = ParameterTool.fromArgs(args);
		dataTopic = parameterTool.get("data.topic");
		paramSize = parameterTool.getInt("feature.num");
		learningRate = parameterTool.getDouble("learning.rate", 0.01);
		updateFreq = parameterTool.getInt("update.frequency", 100);
		regularization = parameterTool.getDouble("regularization", 1);
		gradTopic = parameterTool.get("grad.topic", "online-svm-grad");
		// brokerList = parameterTool.get("broker.list", "online-svm-grad");
		bootStrapServers = parameterTool.get("bootstrap.servers",
				"proj10:9092,proj9:9092,proj8:9092,proj7:9092,proj6:9092,proj5:9092");
		zookeeperConnect = parameterTool.get("zookeeper.connect", "localhost:2181");
		trainFreq = parameterTool.getInt("train.frequency", 1);
		tempTopic = parameterTool.get("temp.topic");
	}

	public void modeling(StreamExecutionEnvironment env) {
		final int metricInterval = parameterTool.getInt("metric.interval", 1);
		DataStream<DenseVector> gradients = env.addSource(new FlinkKafkaConsumer010<DenseVector>(gradTopic,
				new DenseVectorSchema(), parameterTool.getProperties())).broadcast();

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		// Set up data consumer
		FlinkKafkaConsumer010<String> newDataConsumer = new FlinkKafkaConsumer010<String>(dataTopic,
				new SimpleStringSchema(), parameterTool.getProperties());
		// For new data (generated from external system), always read from
		// beginning
		newDataConsumer.setStartFromEarliest();

		FlinkKafkaConsumer010<String> oldDataConsumer = new FlinkKafkaConsumer010<String>(dataTopic,
				new SimpleStringSchema(), parameterTool.getProperties());
		// For old data (copy of new data for multiple training) read the data
		// in this training
		oldDataConsumer.setStartFromLatest();

		// Set up data producer
		Properties producerPropersteis = new Properties();
		producerPropersteis.setProperty("bootstrap.servers", bootStrapServers);
		producerPropersteis.setProperty("zookeeper.connect", zookeeperConnect);
		producerPropersteis.setProperty("name", "test1");

		// Modified by Andy
		FlinkKafkaProducer010<DenseVector> gradTopicProducer = new FlinkKafkaProducer010<DenseVector>(gradTopic,
				new DenseVectorSchema(), producerPropersteis);
		

		producerPropersteis.setProperty("name", "test2");
		FlinkKafkaProducer010<CountLabelExample> tempDataProducer = new FlinkKafkaProducer010<CountLabelExample>(
				tempTopic, new CountLabelExampleSchema(paramSize), producerPropersteis);

		// New data is pure lib svm format
		DataStream<String> newRawData = env.addSource(newDataConsumer);
		DataStream<CountLabelExample> newConvertedData = newRawData.map(new MapFunction<String, CountLabelExample>() {
			private static final long serialVersionUID = 1L;

			@Override
			public CountLabelExample map(String s) {
				LabeledVector vector = parseExample(s);
				int count = trainFreq;
				CountLabelExample countLabelExample = new CountLabelExample(vector, count);
				return countLabelExample;
			}
		});
		// Old data is in a format like as below:
		// count|lib svm
		DataStream<String> oldRawData = env.addSource(oldDataConsumer);
		DataStream<CountLabelExample> oldConvertedData = oldRawData.map(new MapFunction<String, CountLabelExample>() {
			private static final long serialVersionUID = 1L;

			@Override
			public CountLabelExample map(String s) {
				String[] tempSplits = s.split("|");
				int count = Integer.parseInt(tempSplits[0]);
				LabeledVector vector = parseExample(tempSplits[1]);
				CountLabelExample countLabelExample = new CountLabelExample(vector, count);
				return countLabelExample;
			}
		});

		// Merge the data stream
		DataStream<CountLabelExample> mergedData = newConvertedData;

		mergedData = mergedData.union(oldConvertedData);

		DataStream<LabeledVector> trainData = mergedData.map(new MapFunction<CountLabelExample, LabeledVector>() {
			private static final long serialVersionUID = 1L;

			@Override
			public LabeledVector map(CountLabelExample example) {
				return example.getVector();
			}
		});

		DataStream<CountLabelExample> nextRoundData = mergedData
				.map(new MapFunction<CountLabelExample, CountLabelExample>() {
					private static final long serialVersionUID = 1L;

					@Override
					public CountLabelExample map(CountLabelExample example) {
						example.decreaseCount();
						return example;
					}
				});
		DataStream<CountLabelExample> nextRoundFilteredData = nextRoundData
				.filter(new FilterFunction<CountLabelExample>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public boolean filter(CountLabelExample value) throws Exception {
						return value.getCount() > 0;
					}
				});
//		DataSink<CountLabelExample> sinkFunction=new DataSink<CountLabelExample>(tempDataProducer);
		nextRoundFilteredData.addSink(tempDataProducer);
		DataStream<DenseVector> middle = trainData.connect(gradients).flatMap(train());

		middle.addSink(gradTopicProducer);
		middle.process(new ProcessFunction<DenseVector, Long>() {
			@Override
			public void processElement(DenseVector value, Context ctx, Collector<Long> out) throws Exception {
				out.collect(System.currentTimeMillis() - ctx.timestamp());
			}
		}).windowAll(TumblingProcessingTimeWindows.of(Time.seconds(metricInterval)))
				.aggregate(new AggregateFunction<Long, LatencyThroughputAccumulator, Tuple2<Double, Double>>() {
					@Override
					public LatencyThroughputAccumulator createAccumulator() {
						return new LatencyThroughputAccumulator(metricInterval);
					}

					@Override
					public void add(Long value, LatencyThroughputAccumulator accumulator) {
						accumulator.add(value);
					}

					@Override
					public Tuple2<Double, Double> getResult(LatencyThroughputAccumulator accumulator) {
						return accumulator.getResult();
					}

					@Override
					public LatencyThroughputAccumulator merge(LatencyThroughputAccumulator a,
							LatencyThroughputAccumulator b) {
						return LatencyThroughputAccumulator.merge(a, b);
					}
				}).map(new MapFunction<Tuple2<Double, Double>, String>() {
					@Override
					public String map(Tuple2<Double, Double> value) {
						String result = String.format("Average latency: %s ms, throughput: %s rec/sec", value.f0,
								value.f1);
						System.out.println(result);
						System.out.println("2018-02-10");
						return result;
					}
				}).print();
	}

	protected DenseVector newParams() {
		return new DenseVector(new double[paramSize]);
	}
}

class LatencyThroughputAccumulator {
	private long count = 0;
	private long sum = 0;
	private double interval;

	public LatencyThroughputAccumulator(double interval) {
		this.interval = interval;
	}

	public void add(Long latency) {
		sum += latency;
		count += 1;
	}

	public Tuple2<Double, Double> getResult() {
		return new Tuple2<Double, Double>((sum + 0.) / count, count / interval);
	}

	static public LatencyThroughputAccumulator merge(LatencyThroughputAccumulator a, LatencyThroughputAccumulator b) {
		LatencyThroughputAccumulator acc = new LatencyThroughputAccumulator(a.interval);
		acc.count = a.count + b.count;
		acc.sum = a.sum + b.sum;
		return acc;
	}
}
