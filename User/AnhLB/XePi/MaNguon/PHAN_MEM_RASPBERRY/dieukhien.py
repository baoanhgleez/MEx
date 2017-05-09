import socket
import RPi.GPIO as GPIO
import time
global dem,settd
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
s = socket.socket()
#address = socket.gethostname()
address= '192.168.43.35'
#addresuuuuis= '192.168.1.77'

port = 5005
print 'Address: ', address + 'Port: ' + str(port)
s.bind((address, port))
s.listen(5)
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
        time.sleep(0.1)
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
dem=90
settd=20
while 1:
    conn, addr = s.accept()
    print 'Connection address: ', addr
    while 1:
        data= conn.recv(1024);  print 'received data: ', data
        if data.startswith('close'):
            dung()
            break
        elif data.startswith('TOI'):
            chaythang(settd)
        elif data.startswith('LUI'):
            chaylui(settd)
        elif data.startswith('TRAI'):
            retrai(settd)
        elif data.startswith('PHAI'):
            rephai(settd)
        elif data.startswith('DUNG'):
            dung()
        elif data.startswith('TOCDO1'):
            settd=20
        elif data.startswith('TOCDO2'):
            settd=40
        elif data.startswith('TOCDO3'):
            settd=60
        elif data.startswith('TOCDO4'):
            settd=80
        elif data.startswith('TOCDO5'):
            settd=100
        elif data.startswith('SVTRAI'):
            dem=dem+30
            if dem==210:
                    dem=180
            servo(dem)
            conn.send(str(cambien()))
            print str(cambien())
        elif data.startswith('SVPHAI'):
            dem=dem-30
            if dem==-30:
                    dem=0
            servo(dem)
            conn.send(str(cambien()))
            print str(cambien())
    conn.close()
    break
