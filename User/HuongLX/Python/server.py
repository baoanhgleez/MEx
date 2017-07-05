import cv2
import numpy as np
import socket
import pickle

sever = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sever.bind(('',8090))
sever.listen(1)
try:
    print('Wait client connect')
    conn , address = sever.accept()
    print('Client connected')
    cap = cv2.VideoCapture(0)
    while 1:
        data_string = pickle.dumps(cap.read())
        conn.sendall(data_string)
except Exception:
    server.close()
