import RPi.GPIO as GPIO
import socket
import time

GPIO.setmode(GPIO.BCM)
GPIO.setwarning(False)

#setup control motors
GPIO.setup(21, GPIO.OUT)
GPIO.setup(20, GPIO.OUT)
GPIO.setup(16, GPIO.OUT)
GPIO.setup(12, GPIO.OUT)
#setup control servos
GPIO.setup(18, GPIO.OUT)
GPIO.setup(15, GPIO.OUT)
GPIO.setup(14, GPIO.OUT)

#define dc motors
IA_A= GPIO.PWM(21, 50)
IA_B= GPIO.PWM(20, 50)
IB_A= GPIO.PWM(16, 50)
IB_B= GPIO.PWM(12, 50)

#define servo
Servo90U= GPIO.PWM(18, 50)
Servo90L= GPIO.PWM(15, 50)
Servo995= GPIO.PWM(14, 50)

#open socket server
s = socket.socket()
address= '192.168.43.35'
port = 5005
print 'Address: ', address + 'Port: ' + str(port)
s.bind((address, port))
s.listen(5)

def init():
    IA_A.start(0)
    IA_B.start(0)
    IB_A.start(0)
    IB_B.start(0)
    Servo90U.start(0)
    Servo90L.start(0)
    Servo995.start(0)
