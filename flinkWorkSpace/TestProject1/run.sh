/data/opt/course/1155086998/flink/bin/flink run \
-c WordCount \
./target/AnyName-AnyVersion.jar \
--input file:///data/opt/course/1155086998/flink/conf/flink-conf.yaml \
2>&1 | tee -a log.txt
