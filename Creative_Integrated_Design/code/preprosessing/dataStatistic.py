# calculate min, max, avg, std statistic of each wafer & sensor
# input files are ./data/WAFERNAME_STEPX.txt
# output file goes to ./data/stat/WAFERNAME_STEPX.txt


import os
from math import sqrt
if not os.path.exists("./data/stat"):
   		os.makedirs("./data/stat")

def createStat( waferName, waferState, stepNum):
	mn = []
	mx = []
	avg = []
	std = []
	data = []
	sensorNum = 0
	cnt = 0

	openname = "./data/" + waferName + "_step" + str(stepNum) + ".txt"
	outname = "./data/stat/" + waferName + "_step" + str(stepNum) + ".txt"
	f = open(openname,'r')
	of = open(outname,'w')
	lines = f.readlines()
	for line in lines:
		split = line.split()
		if split[0] == "date_time":
			sensorNum = len(split)
			of.write("stat ")
			for i in range(1,sensorNum):
				of.write("%s "%(split[i]))
			of.write("\n")
			for i in range(0,sensorNum):
				mn.append(999999999999999999.9)
				mx.append(-99999999999999999.9)
				avg.append(0)
				std.append(0)		
			continue

		cnt = cnt + 1
		for i in range(0,sensorNum):
			mn[i] = min(mn[i],float(split[i]))
			mx[i] = max(mx[i],float(split[i]))
			avg[i] = avg[i] + float(split[i])
	f.close()

	# write min
	of.write("min ")
	for i in range(1,sensorNum):
		of.write("%.2f "%(mn[i]))
	of.write("\n")

	# write max
	of.write("max ")
	for i in range(1,sensorNum):
		of.write("%.2f "%(mx[i]))
	of.write("\n")

	# write avg
	of.write("avg ")
	for i in range(1,sensorNum):
		avg[i] = avg[i] / (1.0*cnt)
		of.write("%.2f "%(avg[i]))
	of.write("\n")

	# calc std again..
	f = open(openname,'r')
	lines = f.readlines()
	for line in lines:
		split = line.split()
		if split[0] == "date_time":
			continue
		for i in range(0,sensorNum):
			std[i] = std[i] + (float(split[i]) - avg[i])*(float(split[i]) - avg[i])

	f.close()

	# write std
	of.write("std ")
	for i in range(1,sensorNum):
		std[i] = std[i] / (1.0*(cnt-1))
		std[i] = sqrt(std[i])
		of.write("%.2f "%(std[i]))
	of.write("\n")
	of.close()


indexfile = open("./data/waferList.txt","r")
lines = indexfile.readlines()

for line in lines:
	split = line.split()
	print("Processing " + split[0] + " " + split[1])
	# for each wafer and step, make statistic data file into ./data/stat/WAFERNAME_STEPX.txt
	for i in range(1,26):
		createStat(split[0],split[1],i)