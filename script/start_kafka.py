import os
kafkaPath="/data/opt/course/1155086998/kafka/"
for i in range(5,11):
    cmd ="ssh 1155086998@proj" + str(i) + " "
    cmd =cmd+kafkaPath+"bin/kafka-server-start.sh "+ kafkaPath+"config/server_proj"+str(i)+".properties &"
    #print(cmd)
    os.system(cmd)
    cmd =""
# print cmd
