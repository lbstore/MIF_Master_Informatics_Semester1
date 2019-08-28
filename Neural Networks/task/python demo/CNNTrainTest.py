from keras.models import Sequential
from keras.layers import Convolution2D, MaxPooling2D
from keras.layers import Activation, Dropout, Flatten, Dense, BatchNormalization
from keras.preprocessing.image import ImageDataGenerator

from keras import callbacks
from keras import optimizers

import os

import keras.models

import numpy as np
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score, roc_curve

import tensorflow as tf
import cv2
import sys

from keras import backend as K

from keras.utils import np_utils
import multi_gpu


def cnn_TrainTest(no_of_epochs, no_of_gpus, train_b_size, valid_b_size, data_type,  experiment_folder, input_data_dir, data_sub_type='', setSize = 1, width=128, height=128, chanels = 3, test_b_size=32):

    sess = tf.Session(config=tf.ConfigProto(log_device_placement=True))
    K.set_session(sess)

    epochs = no_of_epochs #50-100-150
    gpu_no = no_of_gpus #1-2
    train_batch_size = train_b_size #32
    valid_batch_size = valid_b_size #32
    test_batch_size = test_b_size #32

    setDimensions = setSize #1
    img_height = width #128
    img_width = height #128
    channels = chanels #3

    class_names = []

    datatype = data_type #'Cepstr'
    exp_folder = experiment_folder #'no_of_classes_111'

    if data_sub_type =='':
        train_data_dir = input_data_dir + datatype + '/' + exp_folder + '/train/'
        validation_data_dir = input_data_dir + datatype + '/' + exp_folder + '/validate/'
        test_data_dir = input_data_dir + datatype + '/' + exp_folder + '/test/'
        path = '/home/gpu/Documents/PycharmProjects/' + datatype + '/Results'
    else:
        train_data_dir = input_data_dir + datatype + '/' + data_sub_type +'/'+ exp_folder + '/train/'
        validation_data_dir = input_data_dir + datatype + '/' + data_sub_type +'/'+ exp_folder + '/validate/'
        test_data_dir = input_data_dir + datatype + '/' + data_sub_type +'/'+ exp_folder + '/test/'
        path = '/home/gpu/Documents/PycharmProjects/' + datatype +'/Results/' + data_sub_type


    run_name = '_' + datatype + '_' + exp_folder + '_' + str(train_batch_size) + '_' + str(valid_batch_size)

    graphPath = path + '/' + exp_folder + '/Graph/' + run_name +'/'
    csvPath = path + '/' + exp_folder + '/'
    checkpointerPath = path + '/' + exp_folder + '/Model/'
    predictionsPath = path + '/' + exp_folder + '/'

    if not os.path.exists(graphPath):
        os.makedirs(graphPath)
    if not os.path.exists(csvPath):
        os.makedirs(csvPath)
    if not os.path.exists(checkpointerPath):
        os.makedirs(checkpointerPath)
    if not os.path.exists(predictionsPath):
        os.makedirs(predictionsPath)

    csvPath = csvPath + run_name + '_loss.csv'
    checkpointerPath = checkpointerPath + run_name + '.h5'
    summaryPath = predictionsPath + run_name + '_summary.csv'
    predictionsPath = predictionsPath + run_name + '_predictions.csv'

    class_names = [d for d in os.listdir(train_data_dir)]
    no_of_classes = len(class_names)

    train_file_no = 0
    aa = 1
    for x in class_names:
        list_dir = os.path.join(train_data_dir, x)
        for name in os.listdir(list_dir):
            isfile = os.path.isfile(list_dir + '/' + name)
            if isfile:
                train_file_no = train_file_no + 1;  # count files
            if aa == 1 and isfile:  # for one time set the tensor shape
                img = cv2.imread(os.path.join(list_dir + '/', name))
                if setDimensions == 0:  # if do not set dimmensions e3xplicitly do it from the first file
                    img_height, img_width, channels = img.shape
                # set the tensor shape according to image size
                if K.image_data_format() == 'channels_first':
                    input_shape = (channels, img_width, img_height)
                else:
                    input_shape = (img_width, img_height, channels)  # tensorflow
                aa = 2

    validation_file_no = 0
    for x in class_names:
        list_dir = os.path.join(validation_data_dir, x)
        for name in os.listdir(list_dir):
            isfile = os.path.isfile(list_dir + '/' + name)
            if isfile:
                validation_file_no = validation_file_no + 1;  # count files

    test_file_no = 0
    for x in class_names:
        list_dir = os.path.join(test_data_dir, x)
        for name in os.listdir(list_dir):
            isfile = os.path.isfile(list_dir + '/' + name)
            if isfile:
                test_file_no = test_file_no + 1;  # count files

    train_batches = ImageDataGenerator(rescale=1. / 255).flow_from_directory(
        train_data_dir,
        target_size=(img_width, img_height),
        classes=class_names,
        shuffle=True,
        class_mode='categorical',
        batch_size=train_batch_size)

    valid_batches = ImageDataGenerator(rescale=1. / 255).flow_from_directory(
        validation_data_dir,
        target_size=(img_width, img_height),
        classes=class_names,
        shuffle=True,
        batch_size=valid_batch_size,
        class_mode='categorical')

    test_batches = ImageDataGenerator(rescale=1. / 255).flow_from_directory(
        test_data_dir,
        target_size=(img_width, img_height),
        classes=class_names,
        shuffle=False,
        batch_size=test_batch_size,
        class_mode='categorical')

    # TEST show images
    # imgs,labels = next(train_batches)
    # showImages.plots(imgs, titles=labels)



    # network topology
    model = Sequential()

    model.add(Convolution2D(32, (3, 3), input_shape=input_shape))
    model.add(Activation('relu'))
    model.add(BatchNormalization())
    model.add(MaxPooling2D(pool_size=(3, 3)))

    model.add(Convolution2D(64, (2, 2)))
    model.add(Activation('relu'))
    model.add(BatchNormalization())
    model.add(MaxPooling2D(pool_size=(3, 3)))

    model.add(Convolution2D(64, (2, 2)))
    model.add(Activation('relu'))
    model.add(BatchNormalization())
    model.add(MaxPooling2D(pool_size=(2, 2)))

    model.add(Flatten())

    model.add(Dense(64))
    model.add(Activation('relu'))
    model.add(BatchNormalization())

    model.add(Dense(no_of_classes))
    model.add(Activation('softmax'))

    # opt = SGD(lr=2e-3, momentum=0.9)
    opt = optimizers.Adam(lr=0.0001, beta_1=0.95, beta_2=0.999, epsilon=1e-08, decay=0.0005)

    print(model.summary())

    if gpu_no > 1:
        model = multi_gpu.make_parallel(model, gpu_no)

    model.compile(loss='categorical_crossentropy', optimizer=opt, metrics=['accuracy'])

    csv_log = callbacks.CSVLogger(csvPath, separator=',', append=False)

    checkpointer = callbacks.ModelCheckpoint(filepath=checkpointerPath, verbose=0, save_best_only=True, mode='min')

    # early_stopping=callbacks.EarlyStopping(monitor='val_loss', min_delta= 0, patience= 0, verbose= 0, mode= 'min')

    tbCallBack = keras.callbacks.TensorBoard(log_dir=graphPath, histogram_freq=0, write_graph=True, write_images=True)

    # print(len(test_img_data))
    history = model.fit_generator(
        train_batches,
        steps_per_epoch=(train_file_no // train_batch_size + 1) * gpu_no,
        epochs=epochs,
        verbose=1,
        validation_data=valid_batches,
        validation_steps=(validation_file_no // valid_batch_size + 1) * gpu_no,
        callbacks=[checkpointer, tbCallBack, csv_log]
    )

    print('Testing model')

    # model = load_model(path + 'Models/02ClNoNoise/' + name + '.h5')

    predictionResult = model.predict_generator(test_batches, steps=test_file_no // test_batch_size + 1, verbose=0)
    f_names = test_batches.filenames
    # test_imgs, test_labels = next(test_batches)

    tmp = test_batches.class_indices
    tmp_batch_files = test_batches.filenames
    Y_true = []
    for t in tmp_batch_files:
        for item in tmp:
            if t.split("/")[0] == item:
                Y_true.append(tmp[item])

    Y_true = np_utils.to_categorical(Y_true, no_of_classes)

    # original = sys.stdout
    file_w = open(predictionsPath, 'w')
    file_s = open(summaryPath, 'w')

    predictions = np.argmax(predictionResult, axis=1)

    print>> file_w, run_name
    print>> file_s, run_name


    #print("\n")
    #print("acc: " + str(history.history["acc"][epochs - 1]) + " loss: " + str(
    #    history.history["loss"][epochs - 1]) +
    #      " val_acc: " + str(history.history["val_acc"][epochs - 1]) + " val_loss: " + str(
    #    history.history["val_loss"][epochs - 1]))

    aaa = "acc: " + str(history.history["acc"][epochs - 1]) + " loss: " + str(
        history.history["loss"][epochs - 1]) + " val_acc: " + str(
        history.history["val_acc"][epochs - 1]) + " val_loss: " + str(history.history["val_loss"][epochs - 1])

    bbb = "train batch size - " + str(train_batch_size) + " validation batch size - " + str(valid_batch_size)

    print>> file_s, "\n"
    print>> file_s, "Stats"
    print>> file_s, aaa

    print>> file_s, "\n"
    print>> file_s, "Batches"
    print>> file_s, bbb

    print>> file_s, "\n"
    print>> file_s, "Classification report"
    cr = classification_report(y_true=np.argmax(Y_true, axis=1), y_pred=predictions,
                               target_names=class_names)
    print>> file_s, cr
    # print(classification_report(y_true=np.argmax(Y_true, axis=1), y_pred=predictions,
    #                            target_names=class_names))

    print>> file_s, "\n"
    print>> file_s, "Confusion Matrix"
    cm = confusion_matrix(np.argmax(Y_true, axis=1), predictions)
    qq = cm.tolist()
    for item in qq:
        print>> file_s, item
    #print>> file_s, qq
    # print(confusion_matrix(np.argmax(Y_true, axis=1), predictions))
    file_s.close()

    print>> file_w, "\n"
    print>> file_w, "Prediction Result"
    qq = predictionResult.tolist()
    for item in qq:
        print>> file_w, item

    print>> file_w, "\n"
    print>> file_w, "Classification Result"
    qq = predictionResult.tolist()
    for item in qq:
        maxVal = max(item)
        maxValIndx = item.index(max(item))
        newRow = [0] * len(item)
        newRow[maxValIndx] = 1
        print>> file_w, newRow

    # print("\n")
    # print("Predictions")
    # print(predictionResult)
    print>> file_w, "\n"
    print>> file_w, "True classes"
    qq = Y_true.tolist()
    for item in qq:
        print>> file_w, item
    # print>>file_w, Y_true

    # print("\n")
    # print("True classes")
    # print(Y_true)
    print>> file_w, "\n"
    print>> file_w, "Filenames"
    for item in f_names:
        print>> file_w, item
    # print>>file_w, f_names

    # print("\n")
    # print("Filenames")
    # print(f_names)

    file_w.close()

    # sys.stdout = original
    sess.close()

    print('done')


