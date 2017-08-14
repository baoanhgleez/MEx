import RPi.GPIO as GPIO
import threading
from time import sleep as tdelay
from mexutils import dutyCycle, mapValue, GearMode
from mexutils import LedColor


class LedRGB:
    
    def __init__(self, pinR, pinG, pinB, frequency=50):
        GPIO.setup(pinR, GPIO.OUT)
        GPIO.setup(pinG, GPIO.OUT)
        GPIO.setup(pinB, GPIO.OUT)
        
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
        
    def on(self, color=LedColor.RED, light_time=None):
        self._red.ChangeDutyCycle(color[0])
        self._green.ChangeDutyCycle(color[1])
        self._blue.ChangeDutyCycle(color[2])
        
        if light_time != None:
            tdelay(light_time)
            self.off()

    def blink(self, color=LedColor.RED, delay_time = 0.5, blink_time=None):
        if blink_time==None:
            while True:
                self.on(color, delay_time)
                tdelay(delay_time)
        else:
            for i in range(0, blink_time):
                self.on(color, delay_time)
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
        self._motor.ChangeDutyCycle(dutyCycle(d))

    def rotate(self, angle):
        d=7
        if (angle < 0):
            d = 0
        elif (angle > 180):
            d = 180
        else:
            d = mapValue(angle, 0, 180, 2, 12)
        self._changePulse(d)
        
    def reset(self):
        self.rotate(90)
        
        

class SteeringServo(Servo):
    '''
    Used for servo MG996R
    '''
    __ANGLE_45_DUTY = 8.5
    __ANGLE_135_DUTY = 5.5
    def __init__(self, pinPWM, frequency=50):
        super(SteeringServo,self).__init__(pinPWM, frequency)

    def rotate(self, angle):
        d=7
        if (angle < 45):
            d = self.__ANGLE_45_DUTY 
        elif (angle > 135):
            d = self.__ANGLE_135_DUTY
        else:
            d = mapValue(angle, 0, 180, self.__ANGLE_45_DUTY, self.__ANGLE_135_DUTY)
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

    def stop(self):
        self._pinA.ChangeDutyCycle(0)
        self._pinB.ChangeDutyCycle(0)
        
    def forward(self, speed):
        self._pinB.ChangeDutyCycle(0)
        self._pinA.ChangeDutyCycle(dutyCycle(speed))

    def backward(self, speed):
        self._pinA.ChangeDutyCycle(0)
        self._pinB.ChangeDutyCycle(dutyCycle(speed))


class Buzzer():
    def __init__(self, buzzer_pin):
        self._pin= buzzer_pin
        GPIO.setup(self._pin, GPIO.OUT)

    def buzz(self, pitch, duration=1):
        if (pitch!=0):                
            period = 1.0/pitch
            delay = period / 2
            cycles= int(duration * pitch)
            for i in range(cycles):
                GPIO.output(self._pin, True)
                tdelay(delay)
                GPIO.output(self._pin, False)

class Indicators:
    '''
    Use to control led indicators in car
    '''
    
    def __init__(self, indicatorLeft, indicatorRigh):
        self._ledLeft = LedRGB(indicatorLeft[0], indicatorLeft[1], indicatorLeft[2])
        self._ledRigh = LedRGB(indicatorRigh[0], indicatorRigh[1], indicatorRigh[2])
        self.__indicatorState = 0     # trang thai cua xi nhan, 0:tat, 1:left, 2:righ
    
    def lightOn(self, color=LedColor.WHITE):
        self._ledLeft.on(color)
        self._ledRigh.on(color)
    
    def lightOff(self):
        self._ledLeft.off()
        self._ledRigh.off()

    def blink(self, color=LedColor.WHITE, delay=0.1, time=1 ):
        for i in range(time):
            self.lightOn(color)
            tdelay(delay)
            self.lightOff()

    def setIndicate(self, state):
        self.__indicatorState = state
        if (state==1):
            self._ledLeft.blink(LedColor.YELLOW, 0.2, 1)
            self._ledRigh.off()
        elif (state==2):
            self._ledRigh.blink(LedColor.YELLOW, 0.2, 1)
            self._ledLeft.off()
        else:
            self.lightOff()
        

class MExCar:
    
    def __init__(self, steeringPin, motorLeft, motorRigh):
        self._steeringServo = SteeringServo(steeringPin)
        self._motorLeft = DCMotor(motorLeft[0], motorLeft[1])
        self._motorRigh = DCMotor(motorRigh[0], motorRigh[1])

        # create start signal        
        self._steeringServo.rotate(135)
        tdelay(0.5)
        self._steeringServo.rotate(45)
        tdelay(0.5)
        self._steeringServo.rotate(90)

    def __del__(self):
        GPIO.cleanup()
        

    def move(self, angle, speed, gear):
        self._steeringServo.rotate(angle)

        # TO-DO config speed of two wheels for drift
        speedL=speedR=speed
        if angle<45:
            speedL=mapValue(angle, 45, 0, speed, 1.5*speed)
            speedR=mapValue(angle, 45, 0, speed, 0.5*speed)
        elif angle>135:
            speedR=mapValue(angle, 135, 180, speed, 1.5*speed)
            speedL=mapValue(angle, 135, 180, speed, 0.5*speed)
        
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
            self._motorLeft.stop()
            self._motorRigh.stop()

    def reset(self):
        self.move( 90, 0, GearMode.PARKING)

