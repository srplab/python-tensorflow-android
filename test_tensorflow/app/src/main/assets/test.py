from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import traceback

try:
    #import tensorflow as tf
    #matrix1 = tf.constant([[3., 3.]])
    #matrix2 = tf.constant([[2.],[2.]])
    #product = tf.matmul(matrix1, matrix2)
    #print(product)
    #sess = tf.Session()
    #result = sess.run(product)
    #print result
    #sess.close()

    #import gzip
    #import os
    #import tempfile

    #import numpy
    #from six.moves import urllib
    #from six.moves import xrange  # pylint: disable=redefined-builtin
    #import tensorflow as tf
    #from tensorflow.contrib.learn.python.learn.datasets.mnist import read_data_sets

    import tensorflow.examples.tutorials.mnist
    print(tensorflow.examples.tutorials.mnist)

except Exception :
    traceback.print_exc()