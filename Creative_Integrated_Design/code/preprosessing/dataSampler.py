# get user input of train dataset size & test dataset size
# randomly generate train dataset & test dataset
# output file goes to './data/trainList.txt' and './data/testList.txt'

from random import shuffle 

goodList = []
badList = []

# Get every wafer information
indexfile = open("./data/waferList.txt","r")
lines = indexfile.readlines()

for line in lines:
	split = line.split()
	if split[1] == "0":
		badList.append(split[0] + " " + split[1])
	if split[1] == "1":
		goodList.append(split[0] + " " + split[1])

indexfile.close()

# Get User input
print("Total number of good Wafers\t: %d"%(len(goodList)))
print("Total number of bad Wafers\t: %d"%(len(badList)))


print("Please enter number of Good/Bad wafers in TRAIN set (ex. 10 3)")
train = [int(x) for x in input().split()]
trainGood = train[0]
trainBad = train[1]

print("Please enter number of Good/Bad wafers in TEST set (ex. 5 1)")
test = [int(x) for x in input().split()]
testGood = test[0]
testBad = test[1]

print("Please enter total number of TEST/TRAIN set (ex. 10) ")
rawcnt = [int(x) for x in input().split()]
cnt = rawcnt[0]

for c in range(1,cnt+1):

	# generate trainList file.
	# First shuffle list
	trainFile = open("./data/trainList" + str(c) + ".txt","w");
	trainFileList = []
	shuffle(goodList)
	shuffle(badList)
	for i in range(0,trainGood):
		#trainFile.write("%s\n"%(goodList[i]))
		trainFileList.append(goodList[i])
	for i in range(0,trainBad):
		#trainFile.write("%s\n"%(badList[i]))
		trainFileList.append(badList[i])

	trainFileList.sort()

	for x in trainFileList:
		trainFile.write("%s\n"%(x))

	trainFile.close()

	# generate testList file.
	testFile = open("./data/testList" + str(c) + ".txt","w");
	testFileList = []
	for i in range(trainGood,trainGood+testGood):
		#testFile.write("%s\n"%(goodList[i]))
		testFileList.append(goodList[i])
	for i in range(trainBad,trainBad+testBad):
		#testFile.write("%s\n"%(badList[i]))
		testFileList.append(badList[i])

	testFileList.sort()

	for x in testFileList:
		testFile.write("%s\n"%(x))

	testFile.close()
	print("Randomly Generated in './data/trainList" + str(c) + ".txt' and './data/testList" + str(c)+ ".txt' ")

