__author__ = 'michael'


import numpy as np
import os
import cPickle as pkl
import gzip
import itertools
from collections import Counter
from nltk import FreqDist
import numpy as np
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Activation, Embedding, TimeDistributed, Bidirectional, Flatten,Convolution1D, MaxPooling1D, GlobalMaxPooling1D
from keras.layers import LSTM
from keras.utils import np_utils

#TODO: inspect output/sanity checking via validation module
#TODO: hyperparamter tuning (so far started with 300,200 LSTM units)
#TODO: CRoss-Validation over Folds
#TODO: compute F1

embeddingsPath = '/Users/michael/git/ucsm_git/stance_in_youtube/src/main/resources/list/prunedEmbeddings.84B.300d.txt'

leave_out_Files=['data/QkW-0ewjiJw','data/TgQRgT15f9U','/Users/michael/git/ucsm_git/stance_lstm/data/UtaVKVIoWyk','/Users/michael/git/ucsm_git/stance_lstm/data/_5aodBfdFTA','/Users/michael/git/ucsm_git/stance_lstm/data/gV6OoypZMco','/Users/michael/git/ucsm_git/stance_lstm/data/ka1B59ir1mI']
wholeTestLength=0

for file in leave_out_Files:
    leave_out_File = file
    print 'Using File: ',leave_out_File
    train_folder=leave_out_File+'/train'
    test_folder=leave_out_File+'/test'

    files = [train_folder+'/against.txt',train_folder+'/favor.txt',train_folder+'/none.txt', test_folder+'/against.txt',test_folder+'/favor.txt',test_folder+'/none.txt']

    # Mapping of the labels to integers
    labelsMapping = {'none': 0, 'favor': 1, 'against': 2}

    words = {}
    maxSentenceLen = [0,0,0,0,0,0]
    labelsDistribution = FreqDist()

    distanceMapping = {'PADDING': 0, 'LowerMin': 1, 'GreaterMax': 2}
    minDistance = -30
    maxDistance = 30
    for dis in xrange(minDistance,maxDistance+1):
        distanceMapping[dis] = len(distanceMapping)

    #print distanceMapping

    for fileIdx in xrange(len(files)):
        file = files[fileIdx]
        for line in open(file):
            #print line
            #splits = line.strip().split('\t')

            #label = splits[0]

            #sentence = splits[3]
            tokens = line.split(" ")
            maxSentenceLen[fileIdx] = max(maxSentenceLen[fileIdx], len(tokens))
            for token in tokens:
                words[token.lower()] = True


    print "Max Sentence Lengths: ",maxSentenceLen

    # :: Read in word embeddings ::

    word2Idx = {}
    embeddings = []

    for line in open(embeddingsPath):
        split = line.strip().split(" ")
        word = split[0]

        if len(word2Idx) == 0: #Add padding+unknown
            word2Idx["PADDING"] = len(word2Idx)
            vector = np.zeros(len(split)-1) #Zero vector vor 'PADDING' word
            embeddings.append(vector)

            word2Idx["UNKNOWN"] = len(word2Idx)
            vector = np.random.uniform(-0.25, 0.25, len(split)-1)
            embeddings.append(vector)

        if split[0].lower() in words:
            vector = np.array([float(num) for num in split[1:]])
            embeddings.append(vector)
            word2Idx[split[0]] = len(word2Idx)

    embeddings = np.array(embeddings)

    print "Embeddings shape: ", embeddings.shape
    print "Len words: ", len(words)



    def getWordIdx(token, word2Idx):
        """Returns from the word2Idex table the word index for a given token"""
        if token in word2Idx:
            return word2Idx[token]
        elif token.lower() in word2Idx:
            return word2Idx[token.lower()]

        return word2Idx["UNKNOWN"]


    def createMatrices(files, word2Idx, maxSentenceLen=100):
        """Creates matrices for the events and sentence for the given file"""
        labels = []
        positionMatrix1 = []
        positionMatrix2 = []
        tokenMatrix = []
        noOfFiles =0
        for file in files:
            noOfFiles+=1
            noOfLines=0
            for line in open(file):
                base=os.path.basename(file)
                label= os.path.splitext(base)[0]
                labelsDistribution[label] += 1
                noOfLines+=1

                tokens = line.split(" ")

                tokenIds = np.zeros(maxSentenceLen)

                for idx in xrange(0, min(maxSentenceLen, len(tokens))):
                    tokenIds[idx] = getWordIdx(tokens[idx], word2Idx)

                tokenMatrix.append(tokenIds)

                labels.append(labelsMapping[label])
            #print 'lines', noOfLines,file
        #print 'files', noOfFiles

        #cast to categorial
        labels = np_utils.to_categorical(labels,3)
        return labels, np.array(tokenMatrix, dtype='int32')

    # :: Create token matrix ::
    train_set = createMatrices(files[0:3], word2Idx, max(maxSentenceLen))
    test_set = createMatrices(files[3:6], word2Idx, max(maxSentenceLen))

    print 'number of training instances ',len(train_set[1])
    print 'number of test instances ',len(test_set[0])
    wholeTestLength+=len(test_set[0])
    print '     sum ',len(train_set[1])+len(test_set[0])

    for label, freq in labelsDistribution.most_common(100):
        print "%s : %f%%" % (label, 100*freq / float(labelsDistribution.N()))
        print freq

    ########## NETWORK######

    longest_sequence = max(len(s) for s in (train_set+test_set))


    print 'max length', max(maxSentenceLen)

print 'whole test instances', wholeTestLength