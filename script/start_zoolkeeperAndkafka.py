import os
kafkaPath="/data/opt/course/1155086998/kafka/"
cmd ="."+kafkaPath+"bin/zookeeper-server-start.sh"+ kafkaPath+" config/zookeeper.properties"
print(cmd)
#os.system(cmd)
for i in range(5,11):
    pass
    #cmd ="."+kafkaPath+"bin/zookeeper-server-start.sh"+ #kafkaPath+"config/zookeeper.properties"
    #os.system(cmd)
# print cmd
