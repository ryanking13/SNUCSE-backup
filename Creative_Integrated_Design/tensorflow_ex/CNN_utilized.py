# -*- coding: utf-8 -*-
# code adapted from : https://github.com/golbin/TensorFlow-Tutorials/

import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data
from tensorflow.contrib import layers

mnist = input_data.read_data_sets("../data/mnist", one_hot=True)

# Neural Network Model
X = tf.placeholder(tf.float32, [None, 28, 28, 1])
Y = tf.placeholder(tf.float32, [None, 10])

L1 = layers.conv2d(X, 32, [3, 3])
L2 = layers.max_pool2d(L1, [2, 2])
L3 = layers.conv2d(L2, 64, [3, 3],
                   normalizer_fn=tf.nn.dropout,
                   normalizer_params={'keep_prob': 0.8})
L4 = layers.max_pool2d(L3, [2, 2])
L5 = layers.flatten(L4)
L5 = layers.fully_connected(L5, 256,
                            normalizer_fn=layers.batch_norm)
model = layers.fully_connected(L5, 10)

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
