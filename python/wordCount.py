text_file = sc.textFile("file:/1155086998/stream/test/getpi.py")
print(text_file)
counts = text_file.flatMap(lambda line: line.split(" ")).map(lambda word: (word, 1)).reduceByKey(lambda a, b: a + b)
counts.saveAsTextFile("file:~\1155086998\stream\test\output")