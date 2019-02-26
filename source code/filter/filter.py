# -*- coding: utf-8 -*-
"""
Created on Sun Dec 31 15:09:56 2017
cross project
@author: Lin
"""

from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers.core import Dense, Masking
from keras.layers.recurrent import LSTM
from keras.layers import Merge, BatchNormalization
import numpy as np
import gensim
import csv
import os
import sys
sys.setrecursionlimit(1000000)


# parameters
vec_dim = 100
padding_len = 23
batch_size = 128
epochs = 20
dropout = 0.4
hidden_dim = 10
hidden_activation = 'elu'
output_activation = 'sigmoid'
optimizer = 'nadam'
loss = 'binary_crossentropy'
metric = 'binary_accuracy'

path = './features/'
word2vec_file = 'word2vec.model'


def getData(file):
    print 'Getting data from ' + file
    sentences = np.array([row[4:] for row in csv.reader(open(file))])
    features = np.array([row[:4] for row in csv.reader(open(file))])
    labels = features[:, :1].astype(int)
    rules = features[:, 1].astype(int)
    similarities = features[:, 2].astype(float)
    candidates = features[:, 3].astype(int)
    features = np.array([[rules[j], similarities[j], candidates[j]] for j in range(rules.shape[0])])
    print sentences.shape[0], 'samples'
    return sentences, labels, features


def word2vec_train(sentences):
    print 'Training word2vec', sentences.shape
    model = gensim.models.Word2Vec(size = vec_dim, min_count = 1, window = 10, iter = 10)
    model.build_vocab(sentences)
    model.train(sentences, total_examples = sentences.shape[0], epochs = 10)
    model.save(path + word2vec_file)


def getVectors(sentences, word2vec):
    vectors = np.array([[word2vec[word] for word in sentences[i]] for i in range(sentences.shape[0])])
    vectors = sequence.pad_sequences(vectors, maxlen = padding_len, dtype = 'float32')
    return vectors


def filter_train(x_train, y_train, features):
    model1 = Sequential()
    model1.add(Masking(mask_value = 0., input_shape = (padding_len, vec_dim)))
    model1.add(LSTM(vec_dim, activation = hidden_activation, recurrent_dropout = dropout, dropout = dropout))

    model2 = Sequential()
    model2.add(Dense(vec_dim, input_dim = 3))
    model2.add(BatchNormalization())
    
    model = Sequential()
    model.add(Merge([model1, model2], mode = 'concat'))
    model.add(Dense(hidden_dim, activation = hidden_activation))
    model.add(Dense(hidden_dim, activation = hidden_activation))
    model.add(Dense(hidden_dim, activation = hidden_activation))
    model.add(Dense(1, activation = output_activation))

    model.compile(loss = loss, optimizer = optimizer, metrics = [metric])
    model.fit([x_train, features], y_train, batch_size = batch_size, epochs = epochs, verbose = 2)
    return model


def filter_evaluate(x_test, y_test, features_test, model):
    y_predict = model.predict_classes([x_test, features_test], verbose = 0)
    # print classification_report(y_test, y_predict)
    y_predict = y_predict[:, 0]
    y_test = y_test[:, 0]

    total = y_test.shape[0]
    predict1 = sum(y_predict)
    both1 = sum([1 if y_predict[i] == 1 and y_test[i] == 1 else 0 for i in range(y_predict.shape[0])])
    p = both1 * 1.0 / predict1
    r = both1 * 1.0 / total
    f1 = p * r * 2 / (p + r)
    return p, r, f1, predict1, both1


if __name__ == "__main__":
    sentences_train = []
    labels_train = []
    features_train = []
    for file in os.listdir(path + 'train/'):
        sentences, labels, features = getData(path + 'train/' + file)
        if len(sentences_train) > 0:
            sentences_train = np.concatenate((sentences_train, sentences))
            labels_train = np.concatenate((labels_train, labels))
            features_train = np.concatenate((features_train, features))
        else:
            sentences_train = sentences
            labels_train = labels
            features_train = features
    print 'training set size:', sentences_train.shape[0]

    sentences_test = []
    labels_test = []
    features_test = []
    for file in os.listdir(path + 'test/'):
        sentences, labels, features = getData(path + 'test/' + file)
        if len(sentences_test) > 0:
            sentences_test = np.concatenate((sentences_test, sentences))
            labels_test = np.concatenate((labels_test, labels))
            features_test = np.concatenate((features_test, features))
        else:
            sentences_test = sentences
            labels_test = labels
            features_test = features
    print 'test set size:', sentences_test.shape[0]

    word2vec = gensim.models.Word2Vec.load(path + word2vec_file)
    x_train = getVectors(sentences_train, word2vec)
    x_test = getVectors(sentences_test, word2vec)
    y_train = labels_train
    y_test = labels_test

    model = filter_train(x_train, y_train, features_train)

    p, r, f1, predict1, both1 = filter_evaluate(x_test, y_test, features_test, model)
    print 'p = ', p
    print 'r = ', r
    print 'f1 = ', f1