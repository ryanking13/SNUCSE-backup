# split data with each sensor
# one resulting txt data will have only one sensor
# make folders like /data/stepX/v00x
# total about 25step * 40sensors = 1000 folders
# each folder will have data txt of each wafer

import os

# make folders if not exists
for i in range(1,26):
	if not os.path.exists("./data/step" + str(i)):
   		os.makedirs("./data/step" + str(i))

# for each wafer and step, split files into each sensors
def makeSplit(waferName, waferState, stepNum):
	f = open("./data/" + waferName + "_step" + str(stepNum) + ".txt" ,"r")
	data = []
	lines = f.readlines()
	headerSize = 0
	for line in lines:
		split = line.split()
		headerSize = len(split)
		curlist = []
		for i in range(0,headerSize):
			curlist.append(split[i])
		data.append(curlist)

	dataSize = len(data)
	# for each sensors, make txt file of each wafer
	for i in range(1,headerSize):
		
		curSensor = data[0][i]
		if not os.path.exists("./data/step" + str(stepNum) + "/" + curSensor):
   			os.makedirs("./data/step" +  str(stepNum) + "/" + curSensor)

		outfile = open("./data/step" + str(stepNum) +  "/" + curSensor + "/" + waferState + "_" + waferName + ".txt" ,"w")
		for j in range(0,dataSize):
			outfile.write("%s %s\n"%(data[j][0],data[j][i]))
		outfile.close()
		
	f.close()

indexfile = open("./data/waferList.txt",'r')
lines = indexfile.readlines()

for line in lines:
	split = line.split()
	print("Processing " + split[0] + " " + split[1])
	# for each wafer and step, split files into each sensors
	for i in range(1,26):
		makeSplit(split[0], split[1],i)

indexfile.close()