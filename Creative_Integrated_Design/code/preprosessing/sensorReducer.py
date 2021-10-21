# remove redundant sensor from parsed file
# need ./data/WAFERNAME_stepX.txt and ./stat/WAFERNAME_stepX.txt
# output -> ./data/WAFERNAME_stepX_reduced.txt

import os

def createReduced(waferName, waferState, stepNum):
	openname = "./data/" + waferName + "_step" + str(stepNum) + ".txt"
	statname = "./data/stat/" + "N9R1U.13" + "_step" + str(stepNum) + ".txt"
	outname  = "./data/" + waferName + "_step" + str(stepNum) + "_reduced.txt"
	datafile = open(openname,'r')
	statfile = open(statname,'r')
	outfile = open(outname,'w')

	datalines = datafile.readlines()
	statlines = statfile.readlines()
	statline = statlines[3].split()
	
	for line in datalines:
		split = line.split()
		datalen = len(split)
		
		for i in range(0,datalen):
			if statline[i] == "0.00":
				continue
			else:
				outfile.write("%s "%split[i])	
		outfile.write("\n")

	datafile.close()
	statfile.close()
	outfile.close()

indexfile = open("./data/waferList.txt","r")
lines = indexfile.readlines()

for line in lines:
	split = line.split()
	print("Processing " + split[0] + " " + split[1])
	# for each wafer and step, make statistic data file into ./data/stat/WAFERNAME_STEPX.txt
	for i in range(1,26):
		createReduced(split[0],split[1],i)