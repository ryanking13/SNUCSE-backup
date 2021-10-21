#Import Library of Gaussian Naive Bayes model
from sklearn.naive_bayes import GaussianNB
import time
import numpy as np

sets = int(input("Enter number of train/test sets : "))

fileA = open("NB_accuracy.txt", "w")
fileF = open("NB_fmeasure.txt", "w")
fileTrain = open("NB_traintime.txt", "w")
fileTest = open("NB_testtime.txt", "w")

for step in range(1, 26):
    for s in range(1, sets+1):
        startTrain = int(round(time.time() * 1000))

        nTrain = 0  # number of wafers for training
        nTest = 0   # number of wafers for testing
        sensors = 42

        trainLabelsList = []    # Holds labels(status) of train wafers
        testLabelsList = []     # Holds labels(status) of test wafers
        trainFeaturesList = []  # Holds features(min, max, std, avg for each sensor in each step) of train wafers
        testFeaturesList = []   # Holds features(min, max, std, avg for each sensor in each step) of test wafers
        with open('./data/trainList' + str(s) + '.txt') as f:
            for line in f:
                nTrain += 1 # number of wafers in trainList
                waferName = line.split(" ")[0]
                waferStatus = int(line.split(" ")[1])
                trainLabelsList.append(waferStatus)
                featureList = []
                file = open('./data/stat/' + waferName + '_step' + str(step) + '.txt', "r")
                for line in file:
                    if line.split(" ")[0] == 'stat':    # Skipping row with sensor names
                        continue
                    for j in range(0, sensors):
                        featureList.append(float(line.split(" ")[j+1]))
                trainFeaturesList.append(featureList)

        trainFeatures = np.reshape(trainFeaturesList, (nTrain, 4*sensors)) # np array of train features

        trainLabels = np.array(trainLabelsList, dtype=int)                 # np array of train labels

        # Creating and fitting a Naive Bayes model with given evidence.
        model = GaussianNB()
        model.fit(trainFeatures, trainLabels)

        endTrain = int(round(time.time() * 1000))
        totalTrain = endTrain - startTrain
        fileTrain.write(str(totalTrain) + ' ')

        # Now perform tests on the fit model.
        startTest = int(round(time.time() * 1000))
        with open('./data/testList' + str(s) + '.txt') as f:
            for line in f:
                nTest += 1 # number of wafers in testList
                waferName = line.split(" ")[0]
                waferStatus = int(line.split(" ")[1])
                testLabelsList.append(waferStatus)
                featureList = []

                file = open('./data/stat/' + waferName + '_step' + str(step) + '.txt', "r")
                for line in file:
                    if line.split(" ")[0] == 'stat':    # Skipping row with sensor names
                        continue
                    for j in range(0, sensors):
                        featureList.append(float(line.split(" ")[j+1]))
                testFeaturesList.append(featureList)

        testFeatures = np.reshape(testFeaturesList, (nTest, 4*sensors)) # np array of test features
        testLabels = np.array(testLabelsList, dtype=int)                # np array of test labels

        # holds the predicted labels for each wafer
        predicted = model.predict(testFeatures)

        # parameters for calculating accuracy and f-measure
        zero_match = 0      # both zero
        one_match = 0       # both one
        one_zero = 0        # real: one, predicted: zero
        zero_one = 0        # real: zero, predicted: one

        for test in range(0, nTest):
            if testLabels[test] == predicted[test]:
                if testLabels[test] == 0 :
                    zero_match += 1
                else :
                    one_match += 1
            else :
                if testLabels[test] == 0 :
                    zero_one += 1
                else :
                    one_zero += 1
        precision = 0
        if zero_match+one_zero != 0:
            precision = zero_match / (zero_match+one_zero);
        recall = 0
        if zero_match+zero_one != 0:
            recall = zero_match / (zero_match+zero_one);
        accuracy =  (one_match + zero_match) / (one_match + zero_match + one_zero + zero_one);

        F1 = 0
        if precision+recall != 0:
            F1 = 2 * (precision * recall) / (precision + recall);

        print ('Results for step ' + str(step) + ', set ' + str(s) + ' : ')
        print ('Real      :', testLabels)
        print ('Predicted :', predicted)
        print ('Accuracy  :', accuracy)
        print ('F1 score  :', F1)
        fileA.write(str(accuracy) + ' ')
        fileF.write(str(F1) + ' ')

        endTest = int(round(time.time() * 1000))
        totalTest = endTest - startTest
        fileTest.write(str(totalTest) + ' ')
        #score = model.score(testFeatures, testLabels)
        #print (score)
    fileA.write('\n')
    fileF.write('\n')
    fileTrain.write('\n')
    fileTest.write('\n')