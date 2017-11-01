/data/opt/course/1155086998/flink/bin/flink run \
-c Flink_Test.LogRegBatch.LinearRegression \
./target/LogRegBatch-0.0.1.jar \
--output file:///data/opt/course/1155086998/data/output-batch.txt \
2>&1 | tee -a log.txt
