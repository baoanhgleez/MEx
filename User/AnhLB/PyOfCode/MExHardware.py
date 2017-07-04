import RPi.GPIO as GPIO
import sys
import json
GPIO.setwarnings(False)

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
        
        return d

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

class Mode:
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
        
        if (gear == Mode.FORWARD):
            # Drive
            self._motorLeft.forward(speed)
            self._motorRigh.forward(speed)
            
        elif (gear == Mode.BACKWARD):
            # Reverse 
            self._motorLeft.backward(speed)
            self._motorRigh.backward(speed)
            
        elif (gear == Mode.PARKING):
            # Motor will not run when in Parking mode
            self._motorLeft.backward(0)
            self._motorRigh.backward(0)

            
    def reset(self):
        self.move( 90, 0, Mode.PARKING)

    def __del__():
        GPIO.cleanup()

if __name__=="__main__":
    GPIO.setmode(GPIO.BCM)
    import time
    
    # define servos
    mg996 = SteeringServo(14)
    s9gu = Servo(18)
    s9gl = Servo(15)

    # define dc motor
    motorL = DCMotor(21,20)
    motorR = DCMotor(16,12)

    # define the car
    piCar = MExCar(mg996, s9gu, s9gl, motorL, motorR)

    # test json
    keys = {'angle', 'speed', 'gear'}
    while True:
        order = input("--> ")
        try:
            print(order)
            dat = json.loads(order)
            
            if not (keys == set(dat.keys())):
                raise Exception("Key is not mapped")

            piCar.move(dat['angle']. dat['speed'], dat['gear'])
        except ValueError:
            print("Invalid JSON string")
        except Exception as error:
            print("Invalid field of JSON: " + repr(error))
        except KeyboardInterrupt:
            GPIO.cleanup()
            print("Error occur")
            
