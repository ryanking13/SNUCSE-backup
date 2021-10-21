# RNN train/test script

import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
import sys
from Database import Database
from LSTMnetwork import LSTMNetwork
from accuracy_measure import f1_score
import random, time

# slice data into same raw(time step) size
def cut(data_set):
    min_len = 987654321
    for i in range(len(data_set)):
        min_len = min( len(data_set[i]), min_len)

    for i in range(len(data_set)):
        data_set[i] = data_set[i][:min_len]

    return data_set

# train
def start_train(dm, network, batch_size=10, epoch_size=10):

    # print('[*] train start')
    start_time = time.time()

    for epoch in range(epoch_size):
        x_batch, y_batch = dm.get_train_data(batch_size)
        
        # cut train data into same time length
        x_batch = cut(x_batch)

        network.train(x_batch, y_batch)

    end_time = time.time()
    # print('[*] train end')

    # returns train time
    return end_time - start_time


# test
def start_test(dm, network):
    x, y = dm.get_test_data()
 
    # print('[*] test start')
    start_time = time.time()

    # cut test data into same time length
    x = cut(x)

    real, predict, accuracy = network.test(x, y)
    f1 = f1_score(real, predict)

    end_time = time.time()
    # print('[*] test end')

    return real, predict, accuracy, f1, end_time-start_time


def main():

    # try_num : 사용할 train, train data 번호
    # step_num : 사용할 step 번호
    try_num = sys.argv[1]
    step_num = '_step' + sys.argv[2] + '_reduced'

    # path : path that train, test data is in
    # train_answer, test_answer : file that contains [wafer name, label]
    path = '../../data/'
    train_answer = 'trainList' + try_num + '.txt'
    test_answer = 'testList' + try_num + '.txt'

    batch_size = 20
    epoch_size = 20

    # print('[*] Loading data manager')
    dm = Database(train_path=path, train_answer_file=train_answer,
                  test_path=path, test_answer_file=test_answer,
                  sufix=step_num)
    # print('[*] Done loading data manager')

    # print('[*] Constructing network')
    network = LSTMNetwork()
    # print('[*] Done constructing network')

    # train
    train_time = start_train(batch_size=batch_size, epoch_size=epoch_size,
                            dm=dm, network=network)
    
    # test
    real, predict, accuracy, f1, test_time = start_test(dm, network)

    # print all stats
    if True:
        print('   real: ', real)
        print('predict: ', predict)
        print('accuracy: %.6f' % accuracy)
        print('f1 score: %.6f' % f1)
        # print('train_time: ', train_time)
        # print('test_time: ', test_time)
    
    # just print f1 score    
    else:
        print('%.6f' % f1)

    # for exporting output
    '''
    f_accuracy = open("RNN_accuracy.txt", "a")
    f_f1 = open("RNN_fmeasure.txt", "a")
    f_train_time = open("RNN_traintime.txt", "a")
    f_test_time = open("RNN_testtime.txt", "a")
        
    postfix = ''
    try_size = '20'
    if try_num.endswith(try_size):
        postfix = '\n'
        print("done one set")

    f_accuracy.write("%.6f %s" % (accuracy, postfix))
    f_f1.write("%.6f %s" % (f1, postfix))
    f_train_time.write("%.6f %s" % (train_time, postfix))
    f_test_time.write("%.6f %s" % (test_time, postfix))
    '''


if __name__ == '__main__':
    main()
