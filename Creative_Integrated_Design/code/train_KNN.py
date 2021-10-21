# KNN train/test script

from KNN import KNN
from Database import Database
import sys
from accuracy_measure import f1_score
import time

# find minimum time length
def find_min_len(mat1, mat2):
    min_len = 987654321
    for i in range(len(mat1)):
        min_len = min(len(mat1[i]), min_len)
    for i in range(len(mat2)):
        min_len = min(len(mat2[i]), min_len)

    return min_len

# cut data to same time length
def cut(data_set, length):

    for i in range(len(data_set)):
        data_set[i] = data_set[i][:length]

    return data_set

# train
def start_train(knn, dm, x, y, length=0):
    # print('[*] train start')

    # if time step need to be equlized, uncomment this
    # if length > 0:
    #    x = cut(x, length)
    
    start_time = time.time()
    for i in range(len(x)):
        knn.train(x[i], y[i][0])
    end_time = time.time()

    return end_time - start_time
    # print('[*] train end')

# test
def start_test(knn, dm, x, y, length=0):
    # print('[*] test start')

    # post-setup for Eros and PCA related method
    if knn.distance_method == 'Eros' or knn.distance_method.startswith('PCA'):
        knn.eigenvalues_post_setup()

    # if time step need to be equlized, uncomment this
    # if length > 0:
    #    x = cut(x, length)

    # dimension reduce
    if knn.distance_method in ['ED', 'DTW', 'FastDTW']:
        knn.reduce_dimensions(n_dimensions=2)

    start_time = time.time()
    real, predicted, accuracy = knn.test(x, y)
    f1 = f1_score(real, predicted)
    end_time = time.time()

    # print('[*] test end')

    return real, predicted, accuracy, f1, end_time-start_time

def main():
   
    # try_num : 사용할 train, test data 번호
    # step_num : 사용할 step 번호
    try_num = sys.argv[1]
    step_num = '_step' + sys.argv[2] + '_reduced' # used for sufix

    # path : path that train, test data is located
    # train_answer, test_answer : file that contains [wafer name, label]
    path = '../../data/'
    train_answer = 'trainList' + try_num + '.txt'
    test_answer = 'testList' + try_num + '.txt'
    
    # print('[*] Loading data manager')
    dm = Database(train_path=path, train_answer_file=train_answer,
                test_path=path, test_answer_file=test_answer,
                sufix=step_num)
    # print('[*] Done loading data manager')
    
    # print('[*] Constructing KNN model')
    d_method = sys.argv[3]
    n_method = sys.argv[4]
    knn = KNN(distance_method=d_method, neighbor_method=n_method)
    # print('[*] Done Const

    train_data_set, train_label_set = dm.get_train_data(get_all=True)
    test_data_set, test_label_set = dm.get_test_data()

    min_len = find_min_len(train_data_set, test_data_set)
    
    # train
    train_time = start_train(knn, dm, train_data_set, train_label_set, length=min_len)
    
    # test
    real, predict, accuracy, f1, test_time = start_test(knn, dm, test_data_set, test_label_set, length=min_len)

    # print all stats
    if True:
        print('   real: ', real)
        print('predict: ', predict)
        print('accuracy: %.6f' % accuracy)
        print('f1 score: %.6f' % f1)
        print('time: %.6f %.6f' % (train_time, test_time))
    
    # just print f1 score
    else:
        print('%.6f' % f1)

    # for exporting stat
    if False:
        f_accuracy = open("%s_%s_accuracy.txt" % (d_method, n_method), "a")
        f_f1 = open("%s_%s_fmeasure.txt" % (d_method, n_method), "a")
        f_train_time = open("%s_%s_traintime.txt" % (d_method, n_method), "a")
        f_test_time = open("%s_%s_testtime.txt" % (d_method, n_method), "a")
        
        # postfix = ''
        # try_size = '20'
        # if try_num.endswith(try_size):
        #    postfix = '\n'
        #    print("done one set")

        f_accuracy.write("%.6f %s" % (accuracy, postfix))
        f_f1.write("%.6f %s" % (f1, postfix))
        f_train_time.write("%.6f %s" % (train_time, postfix))
        f_test_time.write("%.6f %s" % (test_time, postfix))

if __name__ == '__main__':
    main()
