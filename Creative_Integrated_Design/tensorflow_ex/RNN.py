# -*- coding: utf-8 -*-
# code adapted from : https://github.com/golbin/TensorFlow-Tutorials/

import tensorflow as tf
import numpy as np

num_arr = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0']
num_dic = {n: i for i, n in enumerate(num_arr)}
dic_len = len(num_dic)

seq_data = ['1234', '2345', '3456', '4567', '5678', '6789', '7890']
seq_data2 = ['123', '234', '345', '456', '567', '678', '789', '890']

def one_hot_seq(seq_data):
    x_batch = []
    y_batch = []
    for seq in seq_data:
        x_data = [num_dic[n] for n in seq[:-1]]
        y_data = num_dic[seq[-1]]

        # do one-hot encoding
        x_batch.append(np.eye(dic_len)[x_data])
        # sparse_softmax_cross_entropy_with_logits not use ont-hot encoding
        y_batch.append([y_data])

    return x_batch, y_batch


# setting options
n_input = 10    # num of input types
n_classes = 10  # num of ouput types
n_hidden = 128  # num of features of hidden layer
n_layers = 3

# NN model compose
# [batch_size, time_steps, input_size]
X = tf.placeholder(tf.float32, [None, None, n_input])
# [batch_size, time_steps]
Y = tf.placeholder(tf.int32, [None, 1])

W = tf.Variable(tf.random_normal([n_hidden, n_classes]))
b = tf.Variable(tf.random_normal([n_classes]))

# [batch_size, n_steps, n_input]
# ->[n_steps, batch_size, n_input]
X_t = tf.transpose(X, [1, 0, 2])

# make RNN cell
cell = tf.contrib.rnn.BasicLSTMCell(n_hidden)
cell = tf.contrib.rnn.DropoutWrapper(cell, output_keep_prob=0.5)
cell = tf.contrib.rnn.MultiRNNCell([cell] * n_layers)
outputs, states = tf.nn.dynamic_rnn(cell, X_t, dtype=tf.float32, time_major=True)

logits = tf.matmul(outputs[-1], W) + b
labels = tf.reshape(Y, [-1])

cost = tf.reduce_mean(tf.nn.sparse_softmax_cross_entropy_with_logits(logits=logits, labels=labels))
train_op = tf.train.RMSPropOptimizer(learning_rate=0.01).minimize(cost)

# train NN
sess = tf.Session()
sess.run(tf.global_variables_initializer())

x_batch, y_batch = one_hot_seq(seq_data)
x_batch2, y_batch2 = one_hot_seq(seq_data2)

for epoch in range(10):
    _, loss4 = sess.run([train_op, cost], feed_dict={X: x_batch, Y: y_batch})
    _, loss3 = sess.run([train_op, cost], feed_dict={X: x_batch, Y: y_batch})

    print('Epoch:', '%04d' % (epoch+1),
          'cost[4] =', '{:.6f}'.format(loss4),
          'cost[3] =', '{:.6f}'.format(loss3))

print('[*] done optimization')

# check result

def prediction(seq_data):
    prediction = tf.cast(tf.argmax(logits, 1), tf.int32)
    prediction_check = tf.equal(prediction, labels)
    accuracy = tf.reduce_mean(tf.cast(prediction_check, tf.float32))

    x_batch, y_batch = one_hot_seq(seq_data)
    real, predict, accuracy_val = sess.run([labels, prediction, accuracy], feed_dict={X: x_batch, Y: y_batch})

    print("result")
    print("Sequential Data: ", seq_data)
    print("Real value: ", [num_arr[i] for i in real])
    print("Predicted value: ", [num_arr[i] for i in predict])
    print("Accuracy: ", accuracy_val)


def main():
    seq_data_test = ['123', '345', '789']
    prediction(seq_data_test)

    seq_data_test = ['1234','2345','7890']
    prediction(seq_data_test)

    seq_data_test = ['23', '78', '90']
    prediction(seq_data_test)

    seq_data_test = ['12345', '34567', '67890']
    prediction(seq_data_test)