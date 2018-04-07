/data/opt/course/1155086998/flink/bin/flink run \
-C file:///data/opt/course/1155086998/lib/flink-connector-kafka-0.10_2.11-1.3.2.jar \
-C file:///data/opt/course/1155086998/lib/flink-connector-kafka-0.9_2.11-1.3.2.jar \
-C file:///data/opt/course/1155086998/lib/flink-connector-kafka-0.8_2.11-1.3.2.jar \
-C file:///data/opt/course/1155086998/lib/flink-connector-kafka-base_2.11-1.3.2.jar \
-C file:///data/opt/course/1155086998/lib/kafka_2.11-0.10.2.1.jar \
-C file:///data/opt/course/1155086998/lib/kafka-clients-0.10.2.1.jar \
-C file:///data/opt/course/1155086998/lib/flink-ml_2.11-1.3.2.jar \
-c Flink_Test.OnlineSVMExample.OnlineSVMExample \
./target/OnlineSVMExample-0.0.1.jar \
--train.frequency 10 \
--data.topic kdd_test_2_part_data_topic \
--feature.num 20216831  \
--learning.rate 0.01 \
--update.frequency 1000 \
--grad.topic kdd_online_svm_1_grad \
--bootstrap.servers proj10:9092,proj9:9092,proj8:9092,proj7:9092,proj6:9092,proj5:9092 \
--zookeeper.connect localhost:2181 \
2>&1 | tee -a log.txt