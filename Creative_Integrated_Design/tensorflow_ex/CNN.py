# -*- coding: utf-8 -*-
# code adapted from : https://github.com/golbin/TensorFlow-Tutorials/

import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data

mnist = input_data.read_data_sets("../data/mnist", one_hot=True)

# Neural Network Model
X = tf.placeholder(tf.float32, [None, 28, 28, 1])
Y = tf.placeholder(tf.float32, [None, 10])

# W1    [3, 3, 1, 32] -> [3, 3]: size of kernel, 1: num of features of input, 32: num of filters
# L1    Conv shape  = (?, 28, 28, 32)
#       Pool        = (?, 14, 14, 32)
W1 = tf.Variable(tf.random_normal([3, 3, 1, 32], stddev=0.01))
L1 = tf.nn.relu(tf.nn.conv2d(X, W1, strides=[1, 1, 1, 1], padding='SAME'))
L1 = tf.nn.max_pool(L1, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
L1 = tf.nn.dropout(L1, 0.8)

# L2    Conv shape  = (?, 14, 14, 64)
#       Pool        = (?, 7, 7, 64)
#       Reshape     = (?, 7*7*64)
W2 = tf.Variable(tf.random_normal([3, 3, 32, 64], stddev=0.01))
L2 = tf.nn.relu(tf.nn.conv2d(L1, W2, strides=[1, 1, 1, 1], padding='SAME'))
L2 = tf.nn.max_pool(L2, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
L2 = tf.reshape(L2, [-1, 7 * 7 * 64])
L2 = tf.nn.dropout(L2, 0.8)

# Fully Connected layer
W3 = tf.Variable(tf.random_normal([7 * 7 * 64, 256], stddev=0.01))
L3 = tf.nn.relu(tf.matmul(L2, W3))
L3 = tf.nn.dropout(L3, 0.5)

W4 = tf.Variable(tf.random_normal([256, 10], stddev=0.01))
model = tf.matmul(L3, W4)

cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=model, labels=Y))
optimizer = tf.train.AdamOptimizer(0.001).minimize(cost)


# NN model train
init = tf.global_variables_initializer()
sess = tf.Session()
sess.run(init)

batch_size = 100
total_batch = int(mnist.train.num_examples/batch_size)

for epoch in range(15):
    total_cost = 0

    for i in range(total_batch):
        batch_xs, batch_ys = mnist.train.next_batch(batch_size)
        batch_xs = batch_xs.reshape(-1, 28, 28, 1)
        _, cost_val = sess.run([optimizer, cost], feed_dict={X: batch_xs, Y: batch_ys})
        total_cost += cost_val

    print('Epoch', '%04d' % (epoch+1),
          'Avg.cost =', '{:.4f}'.format(total_cost/total_batch))

print('[*] Done optimization')

# result

check_prediction = tf.equal(tf.argmax(model, 1), tf.argmax(Y, 1))
accuracy = tf.reduce_mean(tf.cast(check_prediction, tf.float32))

print('Accuracy: ', sess.run(accuracy, feed_dict= {X: mnist.test.images.reshape(-1,28,28,1),
                                                   Y: mnist.test.labels}))