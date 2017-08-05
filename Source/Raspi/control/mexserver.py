import socket, json, threading
from time import sleep as tdelay
import RPi.GPIO as GPIO
from mexutils import logf, mapValue, GearMode
from mexdev import MExCar, LedRGB, Rotator

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

class MExManager():
    __MAX_ALLOW = 1
    __PACKAGE_SIZE = 1024
    __ANDROID_SET = {'angle', 'speed', 'mode', 'viewX', 'viewY'}
    __FRAME_SET = {'viewX', 'viewY'}
    __ARDUINO_SET = {'angle', 'speed', 'mode', 'buzzer', 'led'}

    __AUTHENTICATE_TIME = 5     # device has time to send authenString
    __TIMEOUT_DEFAULT= 30     # Auto disconnect after several seconds
    
    __car_info = {'speed':0, 'led':0, 'buzz':0}
    
    _indicator_flag = False

    def __del__(self):
        GPIO.cleanup()

    def __init__(self, sock_info, devices):
        # attach devices
        self._car = devices[0]
        self._frame = devices[1]
        self._led = devices[2]
        
        # setup socket server
        self._address = sock_info[0]
        self._port = sock_info[1]
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self._sock.bind((self._address, self._port))

        ##
        self._token = {ANDROID_TAG:0, ARDUINO_TAG:0}
        self._require = False
        self._indicator_flag = False
    
    
    def listen(self):
        self._sock.listen(5)
        logf('Waiting for a connection..')
        self._led.on(LedRGB.WHITE)
        while True:
            client, address = self._sock.accept()
            logf('Request connection from '+str(address))
            threading.Thread(target = self.authenDevice,args = (client,address)).start()
            
    def authenDevice(self, client, address):
        try:
            client.settimeout(self.__AUTHENTICATE_TIME)
            authenString = client.recv(10).decode('ascii')
            logf('Device name: '+authenString)
            if authenString not in set(self._token.keys()):
                client.sendall(b'NO\n')
                client.close()
                logf('Unable to authenticate device')
                logf('Access Denied!')
            else:
                if self._token[authenString]>=self.__MAX_ALLOW:
                    client.sendall(b'LIMIT\n')
                    client.close()
                    logf('Limitation Reached!')
                    logf('Access Denied!')
                else:
                    self._led.blink(LedRGB.GREEN, 0.1, 5)
                    client.settimeout(self.__TIMEOUT_DEFAULT)
                    self._token[authenString] +=1
                    client.sendall(b'OK\n')
                    logf('Connected to an '+authenString+str(address))
                    if authenString==ANDROID_TAG:
                        threading.Thread(target = self.listenToAndroid,args = (client,address)).start()
                        threading.Thread(target = self.sendCarInfo,args = (client,address)).start()
                    elif authenString==ARDUINO_TAG:
                        threading.Thread(target = self.listenToArduino,args = (client,address)).start()
        except socket.timeout:
            logf('Time out to authenticate device')
            logf('Force close connection')

    def sendCarInfo(self, client, address):
        '''
        auto send status of car to android
        '''
        while True:
            try:
                car_info = json.dumps(self.__car_info)
                client.sendall(car_info.encode()+b'\n')
                tdelay(5)
            except BrokenPipeError:
                break
            except:
                break
                

    def controlCar(self, angle_, speed_, mode_):
        mode  = mode_
        angle = mapValue(angle_, 90, 270, 180, 0)
        speed = mapValue(speed_, 0, 10, 0, 100)
        if angle<0 or angle>180:
            logf('Invalid angle data', 'ERROR')
            return
            
        if mode == GearMode.FORWARD:
            logf('FORWARD -r'+str(angle)+' -s'+str(speed))
        elif mode == GearMode.BACKWARD:
            logf('BACKWARD -r'+str(angle)+' -s'+str(speed))
        elif mode == GearMode.PARKING:
            logf('PARKING -r'+str(angle)+' -s0')

        self.__car_info['speed']=speed
        self._car.move(angle, speed, mode)

    
    def disconnect(self, client, address, devName, reason):
        self._indicator_flag = True
        self._led.blink(LedRGB.RED, 0.1, 5)
        self._indicator_flag = False

        logf(reason, devName)
        self._token[devName] -=1
        
        logf('Close connection to '+devName+'('+str(address)+')') 
        client.close()

    
    def listenToAndroid(self, client, address):
        while True:
            data =b''
            try:
                data = client.recv(self.__PACKAGE_SIZE)
            except socket.timeout:
                self.disconnect(client, address, ANDROID_TAG, 'Time out!')
                self._require = False
                break
            except ConnectionResetError:
                self.disconnect(client, address, ANDROID_TAG, 'Connection reset by peer')
                self._require = False
                break
            if data == b'':
                self.disconnect(client, address, ANDROID_TAG, 'Disconnect request from user')
                self._require = False
                break
            
            logf('RECV: '+str(data), ANDROID_TAG)
            try:
                order = json.loads(data.decode("ascii"))
                                        
                if ('speed' not in set(order.keys())) or ('mode' not in set(order.keys())) or ('angle'not in set(order.keys())):
                    logf('Missing parameter for CAR control')
                    # unlock controller
                    self._require = False
                else:
                    # lock controller
                    self._require = True 
                    # Control Car
                    self.controlCar(order['angle'], order['speed'], order['mode'])

                if ('viewX' not in set(order.keys())) or ('viewY' not in set(order.keys())):
                    logf('Missing parameter for ajusting view frame')
                else:
                    # Adjust frame
                    viewX = order['viewX']
                    viewY = order['viewY']
                    self._frame.rotate(viewX, viewY)
                                    
            except ValueError:
                logf('Invalid JSON string!','ERROR')
                

    def listenToArduino(self, client, address):
        while True:
            if self._require:
                continue
            data =b''
            try:
                data = client.recv(self.__PACKAGE_SIZE)
            except socket.timeout:
                self.disconnect( client, address, ARDUINO_TAG, 'Time out!')
                break
            
            if data == b'':
                self.disconnect(client, address, ARDUINO_TAG, 'Disconnect request from user')
                break
            
            logf('RECV: '+str(data), ARDUINO_TAG)
            try:
                #  pre-process raw data from Arduino
                raw = data.decode("ascii")
                while raw[0]!='{':
                    if len(raw)>1:
                        raw = raw[1:]
                    else:
                        break
                    
                logf(raw,ARDUINO_TAG)
                    
                order = json.loads(raw)
                if (self.__ARDUINO_SET == set(order.keys()) ):
                    self.controlCar(order['angle'], order['speed'], order['mode'])

                    led = order['led']
                    self.__car_info['led'] = led
                    self.__car_info['buzz'] = buzz
                    if led == 1:
                        logf('Indicator LEFT',ARDUINO_TAG)
                    elif led == 2:
                        logf('Indicator RIGHT',ARDUINO_TAG)
                    if buzz>0:
                        logf('BUZZ BUZZ',ARDUINO_TAG)
                else:
                	logf('Missing parameter', ARDUINO_TAG)
            except ValueError:
                logf('ERR: Invalid JSON string!',ARDUINO_TAG)

                
                
if __name__ == "__main__":
    piCar = MExCar(14, (20, 21), (12, 16))
    frame = Rotator(18, 15)
    indicator = LedRGB(13, 6, 5)

    sock=('192.168.0.1',8011)
    MExManager(sock, (piCar, frame, indicator)).listen()
