import RPi.GPIO as GPIO
import socket
import json

# setup GPIO
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# create an INET, STREAMing socket
serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# bind the socket to a public host, and a well-known port
serversocket.bind(('192.168.0.1', 8002))
# become a server socket
# tells the socket library that we want it to queue up as many as 3 connect requests
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

        if (gear == GearMode.FORWARD):
            # Drive
            self._motorLeft.forward(speed)
            self._motorRigh.forward(speed)

        elif (gear == GearMode.BACKWARD):
            # Reverse 
            self._motorLeft.backward(speed)
            self._motorRigh.backward(speed)

        elif (gear == GearMode.PARKING):
            # Motor will not run when in Parking mode
            self._motorLeft.backward(0)
            self._motorRigh.backward(0)

    def reset(self):
        self.move( 90, 0, GearMode.PARKING)

    def __del__():
        GPIO.cleanup()

## MAIN PROCESS
try:
    mg996r = SteeringServo(14)
    s9gu = Servo(18)
    s9gl = Servo(15)
    motorLeft = DCMotor(21,20)
    motorRigh = DCMotor(16,12)

    piCar = MExCar(mg996r, s9gu, s9gl,, motorLeft, motorRigh)
    piCar.reset()

    while True:
        # accept connections from outside
        print('..waiting for a connection..')
        conn, client_adr = serversocket.accept()
        print('Connection Address: '+str(client_adr))
        while True:
            data = conn.recv(1024)
            if data == b'':
                print('..disconnected from '+str(client_adr)+'')
                conn.close()
                break
            order = json.loads(data.decode("ascii")
            if {'angle', 'speed', 'mode'} == set(order.keys()):
                piCar.move(order['angle'], order['speed'], order['mode'])
            
            print(order)

except KeyboardInterrupt:
    print(' Exit by interrupt ^C')
except Exception as error:
    pass
except:
    print(' An error occur!')
finally:
    GPIO.cleanup()
