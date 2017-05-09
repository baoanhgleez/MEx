import RPi.GPIO as GPIO
import time
from random import randint
global dem
dem=0
GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)
GPIO_TRIGGER = 22
GPIO_ECHO = 21
LINE1=3
LINE2=5
LINE3=7
LINE4=13
GPIO.setup(12, GPIO.OUT)
GPIO.setup(11, GPIO.OUT)
GPIO.setup(15, GPIO.OUT)
GPIO.setup(16, GPIO.OUT)
GPIO.setup(18, GPIO.OUT)
GPIO.setup(LINE1, GPIO.IN)
GPIO.setup(LINE2, GPIO.IN)
GPIO.setup(LINE3, GPIO.IN)
GPIO.setup(LINE4, GPIO.IN)
A0 = GPIO.PWM(11, 50)
A1 = GPIO.PWM(12, 50)
B0 = GPIO.PWM(15, 50)
B1 = GPIO.PWM(16, 50)
GPIO.setup(GPIO_TRIGGER,GPIO.OUT)
GPIO.setup(GPIO_ECHO,GPIO.IN)  
SERVO = GPIO.PWM(18, 50)
A0.start(0)
A1.start(0)
B0.start(0)
B1.start(0)
SS=0.4
SERVO.start(0)
def retrai1(tocdo):
	A0.ChangeDutyCycle(0)
	A1.ChangeDutyCycle(0)
	B0.ChangeDutyCycle(0)
	B1.ChangeDutyCycle(tocdo)
def rephai1(tocdo):
	A0.ChangeDutyCycle(0)
	A1.ChangeDutyCycle(tocdo)
	B0.ChangeDutyCycle(0)
	B1.ChangeDutyCycle(0)
def chaythang(tocdo):
	A0.ChangeDutyCycle(0)
	A1.ChangeDutyCycle(tocdo)
	B0.ChangeDutyCycle(0)
	B1.ChangeDutyCycle(tocdo)
def chaylui(tocdo):
	A0.ChangeDutyCycle(tocdo)
	A1.ChangeDutyCycle(0)
	B0.ChangeDutyCycle(tocdo)
	B1.ChangeDutyCycle(0)
def retrai(tocdo):
	A0.ChangeDutyCycle(tocdo)
	A1.ChangeDutyCycle(0)
	B0.ChangeDutyCycle(0)
	B1.ChangeDutyCycle(tocdo)
def rephai(tocdo):
	A0.ChangeDutyCycle(0)
	A1.ChangeDutyCycle(tocdo)
	B0.ChangeDutyCycle(tocdo)
	B1.ChangeDutyCycle(0)
def dung():
	A0.ChangeDutyCycle(0)
	A1.ChangeDutyCycle(0)
	B0.ChangeDutyCycle(0)
	B1.ChangeDutyCycle(0)
def servo(goc):
        x=(goc*4.5)/90
        if x==9:
                x=10
        SERVO.ChangeDutyCycle(2.5+x)
        time.sleep(0.4)
	SERVO.ChangeDutyCycle(0)
def cambien():
	GPIO.output(GPIO_TRIGGER, False)
	time.sleep(0.1)
	GPIO.output(GPIO_TRIGGER, True)
	time.sleep(0.00001)
	GPIO.output(GPIO_TRIGGER, False)
	start = time.time()
	while GPIO.input(GPIO_ECHO)==0:
                start = time.time()
        while GPIO.input(GPIO_ECHO)==1:
                stop = time.time()
        giatri = ((stop-start) * 34000)/2
        return giatri
servo(90)
while True:
        if GPIO.input(LINE2)==1 and GPIO.input(LINE3)==0:
                dung()
                while GPIO.input(LINE2)==1 and GPIO.input(LINE3)==0:
                        retrai1(15)
        elif GPIO.input(LINE3)==1 and GPIO.input(LINE2)==0:
                dung()
                while GPIO.input(LINE3)==1 and GPIO.input(LINE2)==0:
                        rephai1(15)
        elif GPIO.input(LINE2)==0 and GPIO.input(LINE3)==0:
                dem=dem+1
                if GPIO.input(LINE1)==1 and GPIO.input(LINE4)==0:
                        dung()
                        while GPIO.input(LINE1)==1 and GPIO.input(LINE4)==0:
                                retrai1(15)
                elif GPIO.input(LINE4)==1 and GPIO.input(LINE1)==0:
                        dung()
                        while GPIO.input(LINE4)==1 and GPIO.input(LINE1)==0:
                                rephai1(15)
                elif GPIO.input(LINE1)==0 and GPIO.input(LINE4)==0:
                        while True:
                                chaylui(15)
                                if GPIO.input(LINE1)==1 or GPIO.input(LINE2)==1 or GPIO.input(LINE3)==1 or GPIO.input(LINE4)==1:
                                        break
        else:
                if GPIO.input(LINE1)==1 and GPIO.input(LINE4)==0:
                        dung()
                        dem=0
                        xuly=randint(1,3)
                        if xuly == 1:
                                retrai(15)
                                time.sleep(0.1)
                        if xuly == 2:
                                chaythang(15)
                                time.sleep(0.1)
                        if xuly == 3:
                                while GPIO.input(LINE1)==1 and GPIO.input(LINE4)==0:
                                        retrai1(15)
                elif GPIO.input(LINE4)==1 and GPIO.input(LINE1)==0:
                        dung()
                        dem=0
                        xuly=randint(1,3)
                        if xuly == 1:
                                rephai(15)
                                time.sleep(0.1)
                        if xuly == 2:
                                chaythang(15)
                                time.sleep(0.1)
                        if xuly == 3:
                                while GPIO.input(LINE4)==1 and GPIO.input(LINE1)==0:
                                        rephai1(15)
                elif GPIO.input(LINE1)==0 and GPIO.input(LINE4)==0:
                        chaythang(15)
                        if dem>5:
                                while True:
                                        rephai(15)
                                        if GPIO.input(LINE1)==1 or GPIO.input(LINE4)==1:
                                                dem=0
                                                break
                                                
                elif GPIO.input(LINE1)==1 and GPIO.input(LINE4)==1:
                        xuly=randint(1,3)
                        if xuly == 1:
                                retrai(15)
                                time.sleep(0.1)
                        if xuly == 2:
                                rephai(15)
                                time.sleep(0.1)
                        if xuly == 3:
                                chaythang(15)
                                time.sleep(0.1)
