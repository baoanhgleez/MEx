import RPi.GPIO as GPIO
import socket
import json
import os
from time import gmtime, strftime

## CONSTANT
LOG_DIR = 'MEx/logs/'
STIME = strftime("%Y-%m-%d_%H:%M:%S",gmtime())

# setup GPIO
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# setup socket server
serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('192.168.0.1', 8002))
serversocket.listen(3)

def mapValue( value, fromMin, fromMax, toMin, toMax):
    '''
    Re-maps a number from one range to another.
    That is, a value of fromLow would get mapped to toLow, a value of fromHigh
    to toHigh, values in-between to values in-between, etc.
    '''
    alpha = (toMax-toMin)/(fromMax-fromMin)
    return (value-fromMin)*alpha + toMin


class Servo:
    '''
    Used for servo 9G
    '''
    def __init__(self, pinPWM, frequency=50):
        GPIO.setup(pinPWM, GPIO.OUT)
        self._motor = GPIO.PWM(pinPWM, frequency)
        self._motor.start(0)

    def _changePulse(self, dutyCycle):
        self._motor.ChangeDutyCycle(d)

    def rotate(self, angle):
        d=7
        if (angle < 0):
            d = 0
        elif (angle > 180):
            d = 180
        else:
            d = mapValue(angle, 0, 180, 2, 12)
        self._changePulse(d)

class SteeringServo(Servo):
    '''
    Used for servo MG996R
    '''
    def __init__(self, pinPWM, frequency=50):
        super(SteeringServo,self).__init__(pinPWM, frequency)

    def rotate(self, angle):
        d=7
        if (angle < 45):
            d = 5.5
        elif (angle > 135):
            d = 8.5
        else:
            d = mapValue(angle, 45, 135, 5.5, 8.5)
        self._motor.ChangeDutyCycle(d)
        return d

class DCMotor:
    def __init__(self, inputA, inputB, frequency=50):
        GPIO.setup( inputA, GPIO.OUT)
        GPIO.setup( inputB, GPIO.OUT)
        self._pinA = GPIO.PWM( inputA, frequency)
        self._pinB = GPIO.PWM( inputB, frequency)
        self._pinA.start(0)
        self._pinB.start(0)

    def forward(self, speed):
        self._pinB.ChangeDutyCycle(0)
        self._pinA.ChangeDutyCycle(speed)

    def backward(self, speed):
        self._pinA.ChangeDutyCycle(0)
        self._pinB.ChangeDutyCycle(speed)

class GearMode:
    PARKING  = 0
    FORWARD  = 1
    BACKWARD = 2

class MExCar:
    def __init__(self, steeringServo, servoUpper, servoLower,
                 motorLeft, motorRigh):
        self._steeringServo = steeringServo
        self._steeringServo.rotate(84)
        self._servoUpper = servoUpper
        self._servoLower = servoLower
        self._motorLeft = motorLeft
        self._motorRigh = motorRigh

    def move(self, angle, speed, gear):
        self._steeringServo.rotate(angle)

        # TO-DO config speed of two wheels for drift
        if angle<45:
            speedL=mapValue(angle, 45, 0, speed, min(1.5*speed,100))
            speedR=mapValue(angle, 45, 0, speed, 0.5*speed)
        elif angle>135:
            speedR=mapValue(angle, 135, 180, speed, min(1.5*speed,100))
            speedL=mapValue(angle, 135, 180, speed, 0.5*speed)
        else:
            speedL=speedR=speed
        
        if (gear == GearMode.FORWARD):
            # Drive
            self._motorLeft.forward(speedL)
            self._motorRigh.forward(speedR)

        elif (gear == GearMode.BACKWARD):
            # Reverse 
            self._motorLeft.backward(speedL)
            self._motorRigh.backward(speedR)

        elif (gear == GearMode.PARKING):
            # Motor will not run when in Parking mode
            self._motorLeft.backward(0)
            self._motorRigh.backward(0)

    def reset(self):
        self.move( 90, 0, GearMode.PARKING)

# create log directory in the first time
if not os.path.exists(LOG_DIR):
    os.makedirs(LOG_DIR)

with open(LOG_DIR+STIME, 'w') as logFile:
    pass
logFile.close()

def logf(content):
    print(content)
    with open(LOG_DIR+STIME, 'a') as f:
        f.write(content)
    f.close()
    

## MAIN PROCESS
try:
    mg996r = SteeringServo(14)
    s9gu = Servo(18)
    s9gl = Servo(15)
    motorLeft = DCMotor(21,20)
    motorRigh = DCMotor(16,12)

    piCar = MExCar(mg996r, s9gu, s9gl, motorLeft, motorRigh)
    piCar.reset()

    keySet = {'angle', 'speed', 'mode'} 

    while True:
        # accept connections from outside
        logf('Waiting for a connection..\n')
        conn, client_adr = serversocket.accept()
        logf('Connection Address: '+str(client_adr)+'\n')
        while True:
            data = conn.recv(1024)
            logf('RECV: '+str(data)+'\n')
            if data == b'':
                logf('Disconnected from '+str(client_adr)+'\n')
                conn.close()
                break

            try:
                order = json.loads(data.decode("ascii"))
            except ValueError:
                logf('ERR: Invalid JSON string!\n')
            if (keySet == set(order.keys()) ):
                angle = mapValue(order['angle'], 90, 270, 180, 0)
                speed = mapValue(order['speed'], 0, 10, 0, 100)
                if angle<0 or angle>180:
                    logf('ERR: Invalide rotate angle')
                else:
                # TO-DO
                    mode  = 3-order['mode'] #remap gear order
                    if mode == GearMode.FORWARD:
                        logf('FORWARD -r'+str(angle)+' -s'+str(speed)+'\n')
                    elif mode == GearMode.BACKWARD:
                        logf('BACKWARD -r'+str(angle)+' -s'+str(speed)+'\n')
                    elif mode == GearMode.PARKING:
                        logf('PARKING -r'+str(angle)+' -s0'+'\n')
                    piCar.move(angle, speed, mode)

except KeyboardInterrupt:
    logf('ERR: Interrupt ^C\n')
except Exception as error:
    logf('ERR: '+repr(error)+'\n')
except:
    logf('ERR: An error occur!\n')
finally:
    serversocket.close()
    GPIO.cleanup()
