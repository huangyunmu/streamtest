package benchmarks.wordcount;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class WordCount {

    private static final Logger LOG = LoggerFactory.getLogger(WordCount.class);

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        String topic = parameterTool.getRequired("topic");
        DataStream<String> text = env.addSource(new FlinkKafkaConsumer010<String>(topic, new SimpleStringSchema(), parameterTool.getProperties())).setParallelism(30);

        DataStream<Tuple2<String, Integer>> counts =
            // normalize and split each line
            text.map(line -> line.toLowerCase().split("\\W+"))
                // convert splitted line in pairs (2-tuples) containing: (word,1)
                .flatMap((String[] tokens, Collector<Tuple2<String, Integer>> out) -> {
                    // emit the pairs with non-zero-length words
                    Arrays.stream(tokens)
                        .filter(t -> t.length() > 0)
                        .forEach(t -> out.collect(new Tuple2<>(t, 1)));
                })
                // group by the tuple field "0" and sum up tuple field "1"
                .keyBy(0)
                .sum(1);

        // emit result
        counts.print();
        env.execute("Apache Flink WordCount");
    }
}

