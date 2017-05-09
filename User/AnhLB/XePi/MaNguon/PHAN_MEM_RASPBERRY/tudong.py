import RPi.GPIO as GPIO
import time
global cho
GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)
GPIO_TRIGGER = 22
GPIO_ECHO = 21
GPIO.setup(12, GPIO.OUT)
GPIO.setup(11, GPIO.OUT)
GPIO.setup(15, GPIO.OUT)
GPIO.setup(16, GPIO.OUT)
GPIO.setup(18, GPIO.OUT)
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
SERVO.start(0)
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
cho=0
while True:
	khoangcach=int(cambien())
	if khoangcach<15:
                dung()
                servo(0)
                kca=int(cambien())
                servo(180)
                kcb=int(cambien())
                servo(90)
                dung()
                if kca>kcb:
                        while khoangcach<60:
                                rephai(20)
                                time.sleep(0.1)
                                dung()
                                khoangcach=int(cambien())
                                        
                elif kca<kcb:
                        while khoangcach<60:
                                retrai(20)
                                time.sleep(0.1)
                                dung()
                                khoangcach=int(cambien())

                else:
                        chaylui(15)
                        time.sleep(1)
        elif khoangcach>15 and khoangcach<35:
                chaythang(12)
        else:
                chaythang(20)
