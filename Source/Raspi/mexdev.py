from time import sleep as tdelay
from mexutils import dutyCycle, mapValue

class LedRGB:
    RED = (100, 0, 0)
    GREEN = (0, 100, 0)
    BLUE = (0, 0, 100)
    WHITE = (100, 100, 100)
    YELLOW = (100, 100, 0)
    CYAN = (0, 100, 100)
    PURPLE = (100, 0, 100)
    
    def __init__(self, pinR, pinG, pinB, frequency=50):
        GPIO.setup(pin, GPIO.OUT)
        self._red = GPIO.PWM(pinR, frequency)
        self._green = GPIO.PWM(pinG, frequency)
        self._blue = GPIO.PWM(pinB, frequency)
        self._red.start(0)
        self._green.start(0)
        self._blue.start(0)

    def off(self):
        self._red.ChangeDutyCycle(0)
        self._green.ChangeDutyCycle(0)
        self._blue.ChangeDutyCycle(0)
        
    def on(self, color=LedRGB.RED, light_time=None):
        self._red.ChangeDutyCycle(color[0])
        self._green.ChangeDutyCycle(color[1])
        self._blue.ChangeDutyCycle(color[2])
        if not (light_time == None):
            tdelay(light_time)
            self.off()

    def blink(self, color, delay_time = 0.5, blink_time=1):
        for i in range(0, blink_time):
            self.on(color, light_time=delay_time)
            tdelay(delay_time)
        
class Servo:
    '''
    Used for servo SG90
    '''
    def __init__(self, pinPWM, frequency=50):
        GPIO.setup(pinPWM, GPIO.OUT)
        self._motor = GPIO.PWM(pinPWM, frequency)
        self._motor.start(0)

    def _changePulse(self, d):
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
        

class Rotator:
    '''
    Khung servo de dieu chinh camera
    '''
    def __init__(self, pin_servo_upper, pin_servo_lower):
        self._servoU = Servo(pin_servo_upper)
        self._servoL = Servo(pin_servo_lower)
        self.reset()

    def rotate(self, angleX, angleY):
        self._servoU.rotate(angleY)
        self._servoL.rotate(angleX)

    def reset(self):
        self.rotate(90, 90)
        

class SteeringServo(Servo):
    '''
    Used for servo MG996R
    '''
    def __init__(self, pinPWM, frequency=50):
        super(SteeringServo,self).__init__(pinPWM, frequency)

    def rotate(self, angle):
        d=7
        # TO-DO: remap value of steering servo
        if (angle < 45):
            d = 5.5 #TO-DO
        elif (angle > 135):
            d = 8.5 #TO-DO
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
        self._pinA.ChangeDutyCycle(dutyCycle(speed))

    def backward(self, speed):
        self._pinA.ChangeDutyCycle(0)
        self._pinB.ChangeDutyCycle(dutyCycle(speed))

class GearMode:
    PARKING  = 0
    FORWARD  = 1
    BACKWARD = 2

class MExCar:
    def __init__(self, steeringPin, motorLeft, motorRigh):
        self._steeringServo = SteeringServo(steeringPin)
        self._motorLeft = DCMotor(motorLeft[0], motorRigh[1])
        self._motorRigh = DCMotor(motorRigh[0], motorRigh[1])

        # create start signal
        self._steeringServo.rotate(135)
        tdelay(1)
        self._steeringServo.rotate(45)
        tdelay(1)
        self._steeringServo.rotate(90)
        

    def move(self, angle, speed, gear):
        self._steeringServo.rotate(angle)

        # TO-DO config speed of two wheels for drift
        if angle<45:
            speedL=mapValue(angle, 45, 0, speed, 1.5*speed)
            speedR=mapValue(angle, 45, 0, speed, 0.5*speed)
        elif angle>135:
            speedR=mapValue(angle, 135, 180, speed, 1.5*speed)
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


def __main():
    import RPi.GPIO as GPIO
    GPIO.setmode(BCM)
    GPIO.setwarnings(False)

if __name__ == '__main__':
    __main()
