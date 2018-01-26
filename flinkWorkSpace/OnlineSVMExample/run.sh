/data/opt/course/1155086998/flink/bin/flink run \
-c Flink_Test.OnlineSVMExample.OnlineSVMExample \
./target/OnlineSVMExample-0.0.1.jar \
--data.topic a9_data_topic \
--feature.num 123 \
--learning.rate 0.01 \
--update.frequency 1 \
--grad.topic a9_online_svm_grad \
2>&1 | tee -a log.txt
