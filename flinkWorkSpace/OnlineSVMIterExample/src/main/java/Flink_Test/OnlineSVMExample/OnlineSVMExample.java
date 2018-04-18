package Flink_Test.OnlineSVMExample;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OnlineSVMExample {
	final static int PARALLELISM=80;
    static public void main(String[] args) throws Exception {
        OnlineSVMModel svm = new OnlineSVMModel(args);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(PARALLELISM);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
        svm.modeling(env);
        env.execute("online svm alpha");
    }
}
