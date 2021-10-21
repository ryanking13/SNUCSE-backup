# Plot graph from data
# data are in ./data/ directory with .txt format
# output plot goes into ./data/plot/ directory with .png format
# time step is 0.01 second

from mpl_toolkits.axes_grid.axislines import SubplotZero
import matplotlib.pyplot as plt
import numpy as np
import os 
import time 

if not os.path.exists("./plot"):
   		os.makedirs("./plot")

for i in range(1,26):
	if not os.path.exists("./plot/step" + str(i)):
   		os.makedirs("./plot/step" + str(i))

sensorList = []
def createGraph( waferName, waferState, stepNum , sensor):
	#print("Drawing " + waferName + " " + waferState + " Step" + str(stepNum) + " " + sensor)
	fig = plt.figure(1)
	ax = SubplotZero(fig, 111)
	fig.add_subplot(ax)

	for direction in ["xzero", "yzero"]:
	    ax.axis[direction].set_axisline_style("-|>")
	    ax.axis[direction].set_visible(True)

	for direction in ["left", "right", "bottom", "top"]:
	    ax.axis[direction].set_visible(False)

	x = []
	y = [[]]

	openname = "./data/step" + str(stepNum) +  "/" + sensor + "/" + waferState + "_" + waferName + ".txt"
	f = open(openname,'r')
	lines = f.readlines()

	cnt = 0
	starttime = 0
	sensorNum = 0
	for line in lines:
		cnt = cnt+1
		split = line.split()
		
		if cnt == 1:
			sensorNum = len(split)-1
			for i in range(0,sensorNum):
				y.append([])
			continue
		if cnt == 2:
			starttime = int(split[0])

		curtime = int(split[0])

		x.append(1.0*(curtime-starttime))
		for i in range(1,sensorNum+1):
				y[i].append(float(split[i]))


	# x = np.linspace(-1., 1., 10	)
	for i in range(1,sensorNum+1):
		ax.plot(x, y[i])

	outname = "./plot/step" + str(stepNum) +  "/" + sensor + "/" + waferState + "_" + waferName + ".png"
	plt.savefig(outname)
	#plt.show()
	f.close()
	plt.close()

sensorfile = open("./sensorList.txt","r")
lines = sensorfile.readlines()

for line in lines:
	split = line.split()
	for i in range(1,len(split)):
		sensorList.append(split[i])

sensorfile.close()

# make sensor directories
for i in range(1,26):
	for sensor in sensorList:
		if not os.path.exists("./plot/step" + str(i) + "/" + sensor):
   			os.makedirs("./plot/step" + str(i) + "/" + sensor)


indexfile = open("./data/waferList.txt","r")
lines = indexfile.readlines()

# Draw bad wafers 
for line in lines:
	split = line.split()
	if split[1] == "1":
		continue
	print("Processing " + split[0] + " " + split[1])
	# for each wafer and step, plot it
	for i in range(1,26):
		print( time.strftime('%H:%M:%S') + " Drawing " + split[0] + " " + split[1] + " Step" + str(i))
		for sensor in sensorList:
			createGraph(split[0], split[1],i, sensor)

# Draw good wafers
for line in lines:
	split = line.split()
	if split[1] == "0":
		continue
	print("Processing " + split[0] + " " + split[1])
	# for each wafer and step, plot it
	for i in range(1,26):
		print( time.strftime('%H:%M:%S') + " Drawing " + split[0] + " " + split[1] + " Step" + str(i))
		for sensor in sensorList:
			createGraph(split[0], split[1],i, sensor)


indexfile.close()

