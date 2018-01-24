import os
kafkaPath="/data/opt/course/1155086998/kafka/"
cmd =kafkaPath+"bin/zookeeper-server-stop.sh "+ kafkaPath+"config/zookeeper.properties"
os.system(cmd)
# print cmd
