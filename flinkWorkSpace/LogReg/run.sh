/data/opt/course/1155086998/flink/bin/flink run \
-c WordCount \
./target/LogReg-0.01.jar \
--input file:///data/opt/course/1155086998/data/logistic.txt \
2>&1 | tee -a log.txt
