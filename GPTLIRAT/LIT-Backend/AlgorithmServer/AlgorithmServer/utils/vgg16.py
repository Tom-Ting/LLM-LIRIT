from keras.applications.vgg16 import VGG16
from keras.applications.vgg16 import preprocess_input
import numpy as np
import os
import cv2
import json
from tensorflow.python.keras.models import load_model
from keras.preprocessing.image import img_to_array, load_img

# 载入VGG16结构（去除全连接层）
model_vgg = VGG16(weights='imagenet', include_top=False)
model = load_model('AlgorithmServer/models/VGG16/my_model.h5')
# 原代码
# threshold=0.5
threshold=0.7

def pred(img):
    widget_dict = {0: 'add', 1: 'arrow_down', 2: 'arrow_left', 3: 'check_mark', 4: 'close', 5: 'delete', 6: 'menu', 7: 'other', 8: 'settings', 9: 'share'}
    img = cv2.resize(img, (224, 224), interpolation=cv2.INTER_CUBIC)
    img = img_to_array(img)
    x = np.expand_dims(img, axis=0)
    x = preprocess_input(x)
    x_vgg = model_vgg.predict(x)
    x_vgg = x_vgg.reshape(1, 25088)
    result = (model.predict(x_vgg) > threshold).astype("int32")  # 这就是预测结果了
    return '{}'.format(widget_dict[result.argmax(axis=1)[0]])


def pred_all(img):
    widget_dict = {0: 'add', 1: 'arrow_down', 2: 'arrow_left', 3: 'check_mark', 4: 'close', 5: 'delete', 6: 'menu', 7: 'other', 8: 'settings', 9: 'share'}
    img = cv2.resize(img, (224, 224), interpolation=cv2.INTER_CUBIC)
    img = img_to_array(img)
    x = np.expand_dims(img, axis=0)
    x = preprocess_input(x)
    x_vgg = model_vgg.predict(x)
    x_vgg = x_vgg.reshape(1, 25088)
    # model.predict(x_vgg): 1*N维数据
    # probability_list：N维数组，为对应位置分类的可能性
    probability_list = model.predict(x_vgg)[0]
    # print("probability_list: ", probability_list)
    # print("probability: ", max(probability_list))
    # print("widget type: ", widget_dict[np.argmax(probability_list)])
    return '{}'.format(widget_dict[np.argmax(probability_list)]), max(probability_list).tolist()
