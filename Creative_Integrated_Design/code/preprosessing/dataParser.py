# parse csv data into txt format
# input files are snu_info1.csv ... (total 6 files)
# output file is WaferName_stepX.txt

import csv
import os
#parse date of string format into integer. (0.01ms timescale) 
# don't care change of date
# unit time is 0.01 sec
def dateToInt( string ):
	string = string.replace('-',':')
	string = string.replace(' ',':')
	string = string.replace('.',':')
	#print(string)
	
	split = string.split(':')
	year = int(split[0])
	month = int(split[1])
	day = int(split[2])
	hour = int(split[3])
	minute = int(split[4])
	second = int(split[5])
	csecond = int(split[6])

	p = 1
	totaltime = csecond
	p = p * 100
	totaltime = totaltime + (second * p)
	p = p * 60
	totaltime = totaltime + (minute * p)
	p = p * 60
	totaltime = totaltime + (hour * p)
	p = p * 24
	totaltime = totaltime + (day * p)
	p = p * 31
	totaltime = totaltime + (month * p)
	p = p * 365
	totaltime = totaltime + (year * p)
	return totaltime

# make data folder is not exists
if not os.path.exists("data"):
    os.makedirs("data")

waferList = []
waferDic = {}
startTime = []
endTime = []
stepNum = []
waferName = []
waferStatus = []
cnt = 0
header = []
headerSize = 0
dieSet = set()
# append waferList
with open('./raw_data/snu_label1.csv') as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )
	
	for row in spamreader:		
			if row[0] == "material_id" :
				continue
			print("Adding " + row[0] + " " + row[1] + " to wafer List")
			curWafer = []
			curWafer.append(row[0])
			curWafer.append(row[1])
			waferList.append(curWafer)
			waferDic[row[0]] = row[1]

cnt = 0

# get wafer info
# start, end time of each step
# also with waferName, waferIndex, ... (additional information) of each step
with open('./raw_data/snu_info1.csv') as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )
	print("Processing wafer Info")
	for row in spamreader:		
			if row[0] == "date_time" :
				continue
			if row[4] == "-1" :
				continue

			curTime = dateToInt(row[0])
			curStepNum = row[4]
			curWaferName = row[2]
			curStatus = row[1]
			cnt = cnt + 1
			if curStatus == "START":
				startTime.append(curTime)
				stepNum.append(curStepNum)
				waferName.append(curWaferName)
				waferStatus.append(waferDic[curWaferName])
			if curStatus == "END":
				endTime.append(curTime)


# get redundant sensor list
# these sensors will not included in result file
f = open("./aDielist.txt","r")
lines = f.readlines()
for line in lines:
	split = line.split()
	for x in split:
		dieSet.add(x)
f.close()

header = []
idx = -1
data = []

# make result file
# read snu_data1.csv and make output txt
with open("./raw_data/snu_data1.csv") as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )
	print("Processing wafer Data")
	
	for row in spamreader:	
		if row[0] == "date_time":
			for x in row:
				header.append(x)

			headerSize = len(header)
			continue
		

		curTime = dateToInt(row[0])

		if idx == -1 or curTime > endTime[idx]:
			fileName = waferName[idx] + "_step" + stepNum[idx]  + ".txt"
			f = open("./data/"+fileName,"w")
			# print header
			for i in range(0,headerSize):
				if header[i] not in dieSet:
					f.write("%s "%header[i])
			f.write("\n")

			# print data
			for y in data:
				for i in range(0,headerSize):
					if header[i] not in dieSet:
						f.write("%s "%y[i])
				f.write("\n")
			data = []
			f.close();
			idx = idx + 1

		curlist = []
		cnt = 0
		for x in row:
			cnt = cnt + 1
			if cnt == 1:
				curlist.append(str(dateToInt(x)))
			else:
				curlist.append(x)

		data.append(curlist)

	# print last data..
	fileName = waferName[idx] + "_step" + stepNum[idx]  + ".txt"
	f = open("./data/"+fileName,"w")
	# print header
	for i in range(0,headerSize):
		if header[i] not in dieSet:
			f.write("%s "%header[i])
	f.write("\n")

	# print data
	for y in data:
		for i in range(0,headerSize):
			if header[i] not in dieSet:
				f.write("%s "%y[i])
		f.write("\n")
	data = []
	f.close();
	idx = idx + 1

# make WaferList.txt
f = open("./data/waferList.txt","w")
for x in waferList:
	f.write("%s %s\n"%(x[0],x[1]))
f.close()

############################################################################################
################################	Phase 2 	############################################
############################################################################################
# Phase 2 is same as before, but with another files
# also, minor  modification are made
# Re-initialize variables
waferList = []
waferDic = {}
startTime = []
endTime = []
stepNum = []
waferName = []
waferStatus = []
cnt = 0
header = []
headerSize = 0


# append waferList
with open('./raw_data/snu_label2.csv') as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )

	for row in spamreader:		
			if row[0] == "material_id" :
				continue
			print("Adding " + row[0] + " " + row[1] + " to wafer List")
			curWafer = []
			curWafer.append(row[0])
			curWafer.append(row[1])
			waferList.append(curWafer)
			waferDic[row[0]] = row[1]

cnt = 0
with open('./raw_data/snu_info2.csv') as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )
	print("Processing wafer Info")
	for row in spamreader:		
			if row[0] == "date_time" :
				continue
			if row[8] == "-1" :
				continue

			curTime = dateToInt(row[0])
			curStepNum = row[8]
			curWaferName = row[4]
			curStatus = row[3]
			cnt = cnt + 1
			if curStatus == "START":
				startTime.append(curTime)
				stepNum.append(curStepNum)
				waferName.append(curWaferName)
				waferStatus.append(waferDic[curWaferName])
			if curStatus == "END":
				endTime.append(curTime)


header = []
idx = -1
data = []
with open("./raw_data/snu_data2.csv") as csvfile:
	spamreader = csv.reader(csvfile, delimiter = ',' , quotechar = '|' )
	print("Processing wafer Data")
	
	for row in spamreader:	
		if row[0] == "date_time":
			for x in row:
				header.append(x)

			headerSize = len(header)
			continue
		

		curTime = dateToInt(row[0])
		
		if idx == -1 or curTime > endTime[idx]:
			fileName = waferName[idx] + "_step" + stepNum[idx]  + ".txt"
			f = open("./data/"+fileName,"w")
			# print header
			for i in range(0,headerSize):
				if header[i] not in dieSet:
					f.write("%s "%header[i])
			f.write("\n")

			# print data
			for y in data:
				for i in range(0,headerSize):
					if header[i] not in dieSet:
						f.write("%s "%y[i])
				f.write("\n")
			data = []
			f.close();
			idx = idx + 1

		curlist = []
		cnt = 0
		for x in row:
			cnt = cnt + 1
			if cnt == 1:
				curlist.append(str(dateToInt(x)))
			else:
				curlist.append(x)

		data.append(curlist)

	# print last data..
	fileName = waferName[idx] + "_step" + stepNum[idx]  + ".txt"
	f = open("./data/"+fileName,"w")
	# print header
	for i in range(0,headerSize):
		if header[i] not in dieSet:
			f.write("%s "%header[i])
	f.write("\n")

	# print data
	for y in data:
		for i in range(0,headerSize):
			if header[i] not in dieSet:
				f.write("%s "%y[i])
		f.write("\n")
	data = []
	f.close();
	idx = idx + 1

# make WaferList.txt
f = open("./data/waferList.txt","a")
for x in waferList:
	f.write("%s %s\n"%(x[0],x[1]))
f.close()