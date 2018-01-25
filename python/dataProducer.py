import os
kafkaPath="/data/opt/course/1155086998/kafka/"
fileName="a9"
topic="test"
while True:
    with open(fileName,"r") as f:
        data=f.read()
		cmd=kafkaPath+"bin/kafka-console-producer.sh --broker-list localhost:9092 --topic "+topic+data
		os.system(cmd)
		#print()
