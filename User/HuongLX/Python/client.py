import socket
import cv2
import pickle
import numpy as np
import time
client = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
client.connect(('localhost',8090))
while 1:
    data = client.recv(4096*1024*1024)
    ret, frame = pickle.loads(data)
    if ret:
        cv2.imshow('frame', frame)
        cv2.waitKey(1)
