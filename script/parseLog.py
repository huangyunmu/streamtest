import os
def convertFile(inputFileName, fileType=".out"):
#     inputFileName = "test"
    l = len(inputFileName)
    fileName = inputFileName[0:l - len(fileType)]
#     fileType = ".log"
    print(fileName)
    print(fileType)
    result = list()
    file_object = open(fileName + fileType, 'r')
    try:
        for line in file_object:
            splitStr = line.split(":")
            if(splitStr[0] == "Currect accuracy"):
                result.append(str(splitStr[1]))
            if(splitStr[0] == "Start"):
                result = list()
                result.append(str(splitStr[1]))
    finally:
        file_object.close()
    outputFileName = "converted_" + fileName + fileType
    file_object = open(outputFileName, 'w')
    file_object.writelines(result)
    file_object.close()
    return outputFileName
logPath="/data/opt/course/1155086998/flink/log/"
tempPath="~/stream/log/"
targetPath="~/stream/streamtest/convertedLog/"
for i in range(5,10):
    logFileName="flink-1155086998-taskmanager-0-proj"+str(i)+".cse.cuhk.edu.hk.out"
    cmd =""
    cmd =cmd+"cp "+logPath+logFileName+" "
    cmd =cmd+tempPath+logFileName
#    print(cmd)
    os.system(cmd)
    outputFileName=convertFile(logFileName,".out")
    #print(outputFileName)
    cmd=""
    cmd =cmd+"cp "+tempPath+outputFileName+" "
    cmd =cmd+targetPath+outputFileName
    os.system(cmd)
    print(str(i)+":complete!")
# print cmd
