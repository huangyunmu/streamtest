import os
kafkaPath="/data/opt/course/1155086998/kafka/"
fileName="a9"
topic="test"
cmd=kafkaPath+"bin/kafka-console-producer.sh --broker-list localhost:9092 --topic "+topic
os.system(cmd)
while True:
    max=0
    for line in open(fileName,"r"):
        if(len(line)==0):
            break
        num=line.split(" ")
        for i in range(1,len(num)-1):
            #print(num[i])
            index,data=num[i].split(":")
            if(int(index)>int(max)):
                max=index
  
        #cmd=kafkaPath+"bin/kafka-console-producer.sh --broker-list localhost:9092 --topic "+topic+" "+data
        #os.system(cmd)
    #print(cmd)
    print(max)
    break
