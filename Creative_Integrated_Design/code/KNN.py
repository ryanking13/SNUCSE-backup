# -*- coding: utf-8 -*-

import numpy as np
import sys
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
import fastdtw

class KNN:

    def __init__(self, k=1, distance_method='ED', neighbor_method='SIMPLE'):
        self.k = k                  # num of neighbors
        self.train_data_set = []    # type follows distance method
        self.train_label_set = []   # [ wafer label1, ... ]
        self.n_sensors = 20

        self.distance_method = distance_method
        self.neighbor_method = neighbor_method

        # for Eros and PCA related methods
        self.w_eigenvalues = np.ones(100)

        # for sensor reduce
        self.reduced_sensors = None

    #################################################
    # TRAIN METHODS (interface)
    #################################################

    # return train data
    def get_train_data(self):
        return self.train_data_set, self.train_label_set

    # add new train data
    def train(self, train_data, train_label):

        method = self.distance_method

        # normalize data 
        train_data = self.normalize(train_data)

        # add data
        if method == "ED":
            self.train_data_set.append(train_data)

        elif method == "Eros":
            self.train_data_set.append(self.eros_setup(train_data))

        elif method == "DTW":
            self.train_data_set.append(train_data)

        elif method == "FastDTW":
            self.train_data_set.append(train_data)

        elif method == "PCA_ED":
            self.train_data_set.append(self.pca_setup(train_data))

        elif method == "PCA_DTW":
            self.train_data_set.append(self.pca_setup(train_data))

        elif method == "PCA_FastDTW":
            self.train_data_set.append(self.pca_setup(train_data))

        # add label
        self.train_label_set.append(train_label)

    #################################################
    # TRAIN METHODS (implementation)
    #################################################

    # pre-setup to use Eros method
    def eros_setup(self, data):

        U, s, V = self.get_svd(data)

        # fix array size ( executed once for first data )
        if self.w_eigenvalues.shape[0] != len(s):
            self.w_eigenvalues = np.zeros(len(s))

        # update eigenvalue
        for i in range(self.w_eigenvalues.shape[0]):
            self.w_eigenvalues[i] = max(self.w_eigenvalues[i], s[i])

        # returns eigenvector matrix
        return U

    # gets eigenvalue, eigenvector from covariance matrix of data
    def get_svd(self, mat):
        m = np.cov(mat)
        U, s, V = np.linalg.svd(m)
        
        sum_s = np.sum(s)
        s = np.divide(s, sum_s)

        return U, s, V

    # pre-setup for PCA related methods
    def pca_setup(self, data):
        mat, eigenvalues = self.get_pca(data)
       
        # fix array size ( executed once for first data )
        if self.w_eigenvalues.shape[0] != len(eigenvalues):
            self.w_eigenvalues = np.zeros(len(eigenvalues))

        # update eigenvalues
        for i in range(self.w_eigenvalues.shape[0]):
            self.w_eigenvalues[i] = max(self.w_eigenvalues[i], eigenvalues[i])

        # returns eigenvector matrix
        return mat

    # gets PCA results
    def get_pca(self, data):
        pca = PCA(n_components=6)
        # data = StandardScaler().fit_transform(data)
        mat = pca.fit_transform(data)
        ev = pca.explained_variance_
        return mat, ev

    # normalize eigenvalues
    # needed to call this after Eros, PCA related method train is finished
    def eigenvalues_post_setup(self):
        sum_w = np.sum(self.w_eigenvalues)
        self.w_eigenvalues = np.divide(self.w_eigenvalues, sum_w)

    # get distance of two train data
    def compare_train_data(self, d1, d2, method):

        # currently use ED only
        # for time efficiency
        if method == 'ED' or True:
            dists = self.get_euclidean_distance(d1, d2, per_column=True)
        elif method == 'DTW':
            dists = self.get_dtw(d1, d2, per_column=True)
        elif method == 'FastDTW':
            dists = self.get_fast_dtw(d1, d2, per_column=True)

        dists = [[d, i] for i, d in enumerate(dists)]
        dists.sort(reverse=True)

        return dists

    # reduce data dimensions
    # uses method similar to BORDA voting
    # should be called after train is done
    def reduce_dimensions(self, n_dimensions=2):

        # n_dimensions : num dimensions to be choosen

        max_pointed = n_dimensions + 3
        method = self.distance_method

        if method not in ['ED', 'DTW', 'FastDTW']:
            print("Dimension reduce not possible")
            exit(0)

        train_data, train_label = self.get_train_data()

        n_data = len(train_label)
        ones = []
        zeros = []

        # divide one, zero wafer
        for i in range(n_data):
            if train_label[i] == 1:
                ones.append(train_data[i])
            else:
                zeros.append(train_data[i])

        n_ones = len(ones)
        n_zeros = len(zeros)
        diff_weights = [[0,i] for i in range(self.n_sensors)]

        # give points to sensors
        for i in range(n_ones):
            for j in range(n_zeros):
                dists = self.compare_train_data(ones[i], zeros[j], method)
                for k in range(max_pointed):
                    diff_weights[dists[k][1]][0] += max_pointed-k
        
        diff_weights.sort(reverse=True)

        #print(diff_weights[:3])

        # pick high pointed sensors
        reduced_sensors = []
        self.w_eigenvalues = np.zeros(n_dimensions)
        cnt = 0
        for i in range(1, self.n_sensors):
                reduced_sensors.append(diff_weights[i][1])
                self.w_eigenvalues[cnt] = diff_weights[i][0] / (n_ones * n_zeros)
                cnt+=1
                if cnt >= n_dimensions:
                    break
        
        self.reduced_sensors = np.array(reduced_sensors[:])

        # update train_data
        for i in range(n_data):
            train_data[i] = train_data[i][:, self.reduced_sensors]

    # normalize matrix(data)
    def normalize(self, mat):
        m = np.array(mat)
        sum_mat = np.sum(m, axis=0)
        # print('sum_mat', sum_mat)
        for i in range(m.shape[1]):
            if sum_mat[i] != 0.0:
                m[:, i] = np.divide(m[:, i], sum_mat[i])

        return m

    #################################################
    # TEST METHODS
    #################################################

    # test trained model
    def test(self, test_data_set, test_label_set):

        predicted = self.predict(test_data_set)
        test_label_set = np.reshape(np.array(test_label_set), -1)
        accuracy = 1 - (predicted != test_label_set).sum() / float(predicted.size)
        return test_label_set, predicted, accuracy

    # predict data using trained model
    def predict(self, test_data_set):

        train_data_set, train_label_set = self.get_train_data()

        # test data normalize
        for i in range(len(test_data_set)):
            test_data_set[i] = self.normalize(test_data_set[i])
            if self.reduced_sensors is not None:
                test_data_set[i] = test_data_set[i][:, self.reduced_sensors]
        
        # test data pre-setup per method
        if self.distance_method == 'Eros':
            for i in range(len(test_data_set)):
                test_data_set[i], _, __ = self.get_svd(test_data_set[i])
        elif self.distance_method in ['PCA_ED', 'PCA_DTW', 'PCA_FastDTW']:
            for i in range(len(test_data_set)):
                test_data_set[i], _ = self.get_pca(test_data_set[i])

        # predict test data
        predicted = []
        for test_data in test_data_set:
            if self.neighbor_method == 'SIMPLE':
                neighbors = self.get_neighbors(train_data_set, train_label_set, test_data)
            elif self.neighbor_method == 'BORDA':
                neighbors = self.get_neighbors_BORDA(train_data_set, train_label_set, test_data)

            predicted.append(np.bincount(neighbors).argmax())

        return np.array(predicted)


    #################################################
    # Distance methods
    #################################################

    # get Euclidean distance
    # assume size of two matrix is same ( if not, error )
    def get_euclidean_distance(self, mat1, mat2, per_column=False):

        diff = np.absolute(mat1-mat2)

        if per_column:
            dist = np.sum(diff, axis=0)
        else:
            dist = np.sum(diff)

        return dist

    # get Eros(Extended Frobenius norm) distance
    # V1, V2 = eigenvector matrix
    def get_eros_distance(self, V1, V2):
        w = self.w_eigenvalues

        #print('w', np.sum(w))
        #print('V1', V1.shape)
        mul = np.absolute(np.sum(np.multiply(V1, V2), axis=0))
        eros = np.sum(np.multiply(w, mul))
        dist = np.sqrt(2-2*eros)

        return dist

    # get distance by Dynamic Time Warping method
    # very slow
    def get_dtw(self, mat1, mat2, per_column=False):

        global_cost_sum = 0
        cost_array = []
        num_sensors = mat1.shape[1]

        for col in range(num_sensors):
            # s1, s2 = sensor data vector
            s1 = mat1[:, col]
            s2 = mat2[:, col]

            # if M != N, still algorithm works 
            M = len(s1)
            N = len(s2)
            cost = sys.maxsize * np.ones((M, N))

            # Initalize process
            cost[0, 0] = abs(s1[0] - s2[0])
            for i in range(1, M):
                cost[i, 0] = cost[i-1, 0] + abs(s1[i] - s2[0])
            for j in range(1, N):
                cost[0, j] = cost[0, j-1] + abs(s1[0] - s2[j])

            # Filling matrix
            for i in range(1, M):
                for j in range(1, N):
                    pre_cost = cost[i-1, j-1], cost[i, j-1], cost[i-1, j]
                    cost[i, j] = min(pre_cost) + abs(s1[i] - s2[j])

            if per_column:
                cost_array.append(cost[-1,-1] / sum(cost.shape))
            else:
                global_cost_sum += cost[-1, -1] / sum(cost.shape)

        if per_column:
            return cost_array
        else:
            return global_cost_sum

    # get distance by Fase Dynamic Time Warping method
    # uses outer library
    def get_fast_dtw(self, mat1, mat2, per_column=False):

        global_cost_sum = 0
        cost_array = []
        num_sensors = mat1.shape[1]

        for col in range(num_sensors):
            # s1, s2 = sensor data vector
            s1 = mat1[:, col]
            s2 = mat2[:, col]

            dist, _ = fastdtw.fastdtw(s1, s2, radius=1)
            if per_column:
                cost_array.append(dist)
            else:
                global_cost_sum += dist

        if per_column:
            return cost_array
        else:
            return global_cost_sum

    #################################################
    # Neighbor Methods
    #################################################

    # get neighbor for test data
    # returns k data(neighbor)'s label
    # uses weighted BORDA voting
    def get_neighbors_BORDA(self, train_data_set, train_label_set, test_data):

        distances = []                  # distances for each dimension
        length = len(train_data_set)    # num total train data 
        n_dim = len(test_data[0])       # num dimensions
        max_pointed = self.k            # num neighbors to give voting poin
        weight = self.w_eigenvalues     # weight for weighted BORDA voting
        # weight = 1
        method = self.distance_method

        # initialize distance
        for i in range(n_dim):
            distances.append([])

        for i in range(length):
            if method == 'ED' or method == 'PCA_ED':
                dist = self.get_euclidean_distance(np.array(train_data_set[i]), np.array(test_data), per_column=True)
            elif method == 'Eros':
                print("IMPOSSIBLE COMBINATION")
                exit(-1)
            elif method == 'DTW' or method == 'PCA_DTW':
                dist = self.get_dtw(np.array(train_data_set[i]), np.array(test_data), per_column=True)

            elif method == 'FastDTW' or method == 'PCA_FastDTW':
                dist = self.get_fast_dtw(np.array(train_data_set[i]), np.array(test_data), per_column=True)

            # append distance per dimension
            for j in range(n_dim):
                distances[j].append((dist[j], i))

        scores = [[0, i] for i in range(length)]  # [score, train_data_num]

        # give point according to weighted BORDA voting
        for i in range(n_dim):
            distances[i].sort()
            d_p = distances[i][max_pointed][0] - distances[i][0][0]

            # if all distance is 0
            if d_p == 0:
                d_p = 1

            for j in range(max_pointed):
                d_j = distances[i][j][0] - distances[i][0][0]
                # scores[distances[i][j][1]][0] += weight * (1 + max_pointed*(1-(d_j/d_p)))
                # dimension_weight * score * distance_weight_normalized
                scores[distances[i][j][1]][0] += weight[i] * (1 + max_pointed*(1-(d_j/d_p)))

        scores.sort(reverse=True)
        neighbors = []
        for i in range(self.k):
            # print(scores[i])
            neighbors.append(train_label_set[scores[i][1]])

        return np.array(neighbors)

    # get neighbor for test data
    # returns k data(neighbor)'s label
    def get_neighbors(self, train_data_set, train_label_set, test_data):

        distances = []
        method = self.distance_method

        for i in range(len(train_data_set)):
            dist = None

            if method == 'ED' or method == 'PCA_ED':
                dist = self.get_euclidean_distance(np.array(train_data_set[i]), np.array(test_data))
            elif method == 'Eros':
                dist = self.get_eros_distance(train_data_set[i], test_data)
            elif method == 'DTW' or method == 'PCA_DTW':
                dist = self.get_dtw(np.array(train_data_set[i]), np.array(test_data))
            elif method == 'FastDTW':
                dist = self.get_fast_dtw(np.array(train_data_set[i]), np.array(test_data))

            distances.append((dist, train_label_set[i], i)) # Third index is just for debug

        distances.sort()
        neighbors = []
        for i in range(self.k):
            # print(distances[i][0], distances[i][1])
            neighbors.append(distances[i][1])

        return np.array(neighbors)
