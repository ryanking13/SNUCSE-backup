f = open("./data/N9R1U.13_step25.txt","r")
outfile = open("./sensorList.txt","w")
lines = f.readlines()
cnt = 0
for line in lines:
	cnt = cnt + 1
	split = line.split()
	for i in range(0,len(split)):
		outfile.write("%s "%(split[i]))
	if cnt == 1 :
		break

outfile.close()
f.close()