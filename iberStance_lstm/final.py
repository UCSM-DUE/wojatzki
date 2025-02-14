
__author__ = 'michael'

from nltk import FreqDist
from keras.models import Sequential
from keras.layers import Dense, Embedding, TimeDistributed, Bidirectional, Flatten, Dropout
from keras import metrics
from keras.optimizers import RMSprop
from keras.layers import LSTM
from keras.utils import np_utils
import numpy as np
import os

np.random.seed(1337)  # reproducibility

language='es'

embeddingsPath = '/Users/michael/git/ucsm_git/interactiveAM/stanceCatalanIndependence/src/main/resources/prunedEmbeddings_test_wiki.'+language+'.vec'
#for each leave out file (video) run a separated experiment (this implements the LOO-CV)


#Hyperparams (other paramter are configured according to input length etc)
n_hidden = 100
n_out = 3
lstm_units=64
numberEpochs=5
activation='tanh'
optimizer='nadam'
dropout=0.2
lossFunction='sparse_categorical_crossentropy'
learningRate=0.005

#prepare data structure
leave_out_File = file
train_folder='/Users/michael/git/ucsm_git/iberStance_lstm/data/train_all/'+language+'/'+'/train/'
test_folder='/Users/michael/git/ucsm_git/iberStance_lstm/data/train_all/'+language+'/'+'/test'

files = [train_folder+'/against.txt',train_folder+'/favor.txt',train_folder+'/neutral.txt', test_folder+'/favor.txt']

# Mapping of labels to ints
labelsMapping = {'neutral': 0, 'favor': 1, 'against': 2}

words = {}
maxSentenceLen = [0,0,0,0,0,0]
labelsDistribution = FreqDist()
distanceMapping = {'PADDING': 0, 'LowerMin': 1, 'GreaterMax': 2}
minDistance = -30
maxDistance = 30
for dis in xrange(minDistance,maxDistance+1):
    distanceMapping[dis] = len(distanceMapping)

for fileIdx in xrange(len(files)):
    file = files[fileIdx]
    for line in open(file):
        #print line

        #print line
        #print line.split("\t")

        tokens=line.split("\t")[1].split(" ")
       # tokens = line.split(" ")
        maxSentenceLen[fileIdx] = max(maxSentenceLen[fileIdx], len(tokens))
        for token in tokens:
            words[token.lower()] = True


print "Max Sentence Lengths: ",maxSentenceLen

# Read  wordembeddings
word2Idx = {}
embeddings = []

for line in open(embeddingsPath):
    split = line.strip().split(" ")
    word = split[0]

    if len(word2Idx) == 0:
        word2Idx["PADDING"] = len(word2Idx)
        vector = np.zeros(len(split)-1)
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
    if token in word2Idx:
        return word2Idx[token]
    elif token.lower() in word2Idx:
        return word2Idx[token.lower()]

    return word2Idx["UNKNOWN"]


def createMatrices(files, word2Idx, maxSentenceLen=100):
    labels = []
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

            tokens=line.split("\t")[1].split(" ")
            # tokens = line.split(" ")

            tokenIds = np.zeros(maxSentenceLen)

            for idx in xrange(0, min(maxSentenceLen, len(tokens))):
                tokenIds[idx] = getWordIdx(tokens[idx], word2Idx)

            tokenMatrix.append(tokenIds)

            labels.append(labelsMapping[label])
    #labels = np_utils.to_categorical(labels,3)
    return labels, np.array(tokenMatrix, dtype='int32')


train_set = createMatrices(files[0:3], word2Idx, max(maxSentenceLen))
test_set = createMatrices(files[3:4], word2Idx, max(maxSentenceLen))


for label, freq in labelsDistribution.most_common(100):
    print "%s : %f%%" % (label, 100*freq / float(labelsDistribution.N()))
    print freq

longest_sequence = max(len(s) for s in (train_set+test_set))


print 'max length', max(maxSentenceLen)

n_in = max(maxSentenceLen)

words = Sequential()
words.add(Embedding(output_dim=embeddings.shape[1], input_dim=embeddings.shape[0], input_length=n_in,  weights=[embeddings], trainable=False))
words.add(Dropout(dropout))
words.add(Bidirectional(LSTM(lstm_units, return_sequences=True,dropout_W=dropout)))
words.add(TimeDistributed(Dense(n_out, activation=activation)))
words.add(Flatten())
words.add(Dense(n_out, activation='softmax'))

words.compile(loss=lossFunction,optimizer=optimizer,metrics=['sparse_categorical_accuracy'])
# print network
words.summary()

for epoch in xrange(numberEpochs):
    print "\n------------- Epoch %d ------------" % (epoch+1)
    words.fit(train_set[1], train_set[0], nb_epoch=1, batch_size=64, verbose=True, shuffle=True)
    #score, acc = words.evaluate(test_set[1], test_set[0])
    #print('Accuracy calculated by Keras:', acc*100)


correct = 0.0
incorrect = 0.0

predictions = words.predict_classes(test_set[1])
# probabilities = words.predict_proba(test_set[1])

correct = 0.0
incorrect = 0.0

def labelFromOneHotVec(vector):
    for i in range(0,len(vector)):
        if vector[i]==1: return i


def getTCVector(index):
    if index==0 :
        return "0,0,1"
    elif index==1 :
        return "0,1,0"
    else:
        return "1,0,0"

def getId(folder,j):
   # print folder
    z=0
    for file in folder:
        for line in open(file):
            id =line.split("\t")[0]
            if(j==z): return id
            z+=1

for i in range(0, len(predictions)):
    predicted = predictions[i]
    gold=test_set[0][i]
    id=getId(files[3:6],i)
    #print predicted, test_set[0][i],labelFromOneHotVec(test_set[0][i])

    with open("result/trainTest/"+language+"_activation_"+activation+"dropOut_"+str(dropout)+"_sparse"+str(numberEpochs)+"_units_"+str(lstm_units)+"_.txt", "a+") as file:
                             file.write(str(gold)+"\t"+str(predicted)+"\n")
    # #
    with open("result/trainTest/"+language+"_activation_"+activation+"_dropOut_"+str(dropout)+"_sparse"+str(numberEpochs)+"_units_"+str(lstm_units)+"_id2Outcome.txt", "a+") as file2:
                              file2.write(id+"="+str(getTCVector(predicted))+";"+str(getTCVector(gold))+"\n")

   # with open("result/cv_lr/"+"dropOut_"+str(dropout)+language+"dropOut_"+str(dropout)+"_sparse"+str(numberEpochs)+"_id2Prob.txt", "a+") as file2:
   #                          file2.write(id+"="+str(probabilities[i])+";"+str(getTCVector(gold))+"\n")

    if predicted==gold: correct+=1
    else: incorrect += 1


print("Man acc: ", correct / (correct + incorrect) * 100)