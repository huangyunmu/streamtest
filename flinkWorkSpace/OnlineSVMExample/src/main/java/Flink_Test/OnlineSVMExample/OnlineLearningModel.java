package Flink_Test.OnlineSVMExample;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.DenseVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Uncertainty:
 * 1. Whether it's the correct way to train online svm. The way it broadcast and apply gradients here.
 * 2. How to efficiently send gradient from the sink to the source.
 * >> A: Use kafka. Need to delete the topic each time after running the program.
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

    private static double formalize(double label) {
        if (label == 1) {
            return 1;
        } else {
            return -1;
        }
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
    }

    public void modeling(StreamExecutionEnvironment env) {
        int metricInterval = parameterTool.getInt("metric.interval", 1);
        DataStream<DenseVector> gradients = env.addSource(new FlinkKafkaConsumer010<DenseVector>(
                gradTopic,
                new DenseVectorSchema(),
                parameterTool.getProperties()
        )).broadcast();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStream<String> input = env.addSource(new FlinkKafkaConsumer010<String>( // source of samples
                dataTopic,
                new SimpleStringSchema(),
                parameterTool.getProperties()
        )).filter(s -> !s.isEmpty());
       

        DataStream<DenseVector> middle = input.map(new MapFunction<String, LabeledVector>() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public LabeledVector map(String s) {
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
        }).connect(gradients).flatMap(train());
//coflatmap
        middle.addSink(new FlinkKafkaProducer010<DenseVector>(
                gradTopic,
                new DenseVectorSchema(),
                parameterTool.getProperties()
        ));
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
                    public LatencyThroughputAccumulator merge(LatencyThroughputAccumulator a, LatencyThroughputAccumulator b) {
                        return LatencyThroughputAccumulator.merge(a, b);
                    }
                }).map(new MapFunction<Tuple2<Double, Double>, String>() {
            @Override
            public String map(Tuple2<Double, Double> value) {
                return String.format("Average latency: %s ms, throughput: %s rec/sec", value.f0, value.f1);
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