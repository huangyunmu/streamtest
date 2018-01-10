/data/opt/course/1155086998/flink/bin/flink run \
-c Flink_Test.OnlineSVMExample.Test \
./target/OnlineSVMExample-0.0.1.jar \
--input file:///data/opt/course/1155086998/data/logistic.txt \
--output file:///data/opt/course/1155086998/data/output-api.txt \
2>&1 | tee -a log.txt
