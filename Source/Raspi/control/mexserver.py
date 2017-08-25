import socket, json, threading
from time import sleep as tdelay
import RPi.GPIO as GPIO
from mexutils import *
from mexdev import MExCar, Indicators, Servo, Buzzer

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

class MExManager():
    __MAX_ALLOW = 1
    __PACKAGE_SIZE = 1024
    __CONTROL_SET = {'angle', 'speed', 'mode', 'buzzer', 'led'}
    __FRAME_SET = {'viewX'}
    __ARDUINO_SET = {'angle', 'speed', 'mode', 'buzzer', 'led'}

    __AUTHENTICATE_TIME = 5     # device has time to send authenString
    __TIMEOUT_DEFAULT= 20       # Auto disconnect after several seconds
    
    __car_info = {'speed':0, 'mode':0, 'angle':0, 'led':0, 'buzz':0}
    
    _indicator_flag = False

    def __del__(self):
        GPIO.cleanup()

    def __init__(self, sock_info, devices):
        # attach devices
        self._car = devices[0]
        self._frame = devices[1]
        self._indicator = devices[2]
        self._buzzer = devices[3]
        
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
        self._stack = Stack()

    
    def listen(self):
        self._sock.listen(5)
        logf('Waiting for a connection..')
        self._indicator.lightOn()
        while True:
            try:
                client, address = self._sock.accept()
                logf('Request connection from '+str(address))
                threading.Thread(target = self.authenDevice,args = (client,address)).start()
            except Exception as e:
                logf('Exception occur: '+str(e))
            
    def authenDevice(self, client, address):
        try:
            client.settimeout(self.__AUTHENTICATE_TIME)
            authenString = client.recv(10).decode('ascii')
            if authenString =='':
                logf('Device disconnected to server')
                return
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
                    self._indicator.blink(LedColor.GREEN, 0.1, 5)
                  
                    # set timeout cho thiet bi duoc ket noi
                    client.settimeout(self.__TIMEOUT_DEFAULT)
                    self._token[authenString] +=1
                    client.sendall(b'OK\n')
                    logf('Connected to a/an '+authenString+str(address))
                    if authenString==ANDROID_TAG:
                        threading.Thread(target = self.listenToAndroid,args = (client,address)).start()
                        threading.Thread(target = self.sendCarInfo,args = (client,address)).start()
                    elif authenString==ARDUINO_TAG:
                        threading.Thread(target = self.listenToArduino,args = (client,address)).start()
        except socket.timeout:
            logf('Time out to authenticate device')
            logf('Force close connection')
            client.close()
        except Exception as e:
            logf('Exception occur ' + str(e))
            logf('Force close connection')
            client.close()

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
                logf('Broken pipe to ANDROID'+str(address))
                break
            except Exception as e:
                logf('Exception occur :'+ str(e))
                break
                

    def controlCar(self, angle_, speed_, mode_):
        mode  = mode_
        angle = mapValue(angle_, 90, 270, 180, 0)
        speed = speed_
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
        self.__car_info['angle']=angle
        self.__car_info['mode']=mode
        self._car.move(angle, speed, mode)

    def reverseOrder(self):
        counter=0
        while counter<1000:
            logf('Reverse order: '+order. 
            order = self._stack.pop()
            if set('angle', 'speed', 'mode') <= set(order.keys()):
                if order['mode']!=0:
                    # reverse mode
                    mode = 3-order['mode']
                    # keep speed
                    speed = order['speed']
                    # reverse angle
                    delta = abs(90-order['angle'])
                    angle = 90 + delta if order['angle']<90 else 90-delta
                    # car control
                    self.controlCar(angle, speed, mode)
                tdelay(0.1)

    def controlLedBuzz(self, led_, buzzer_):
        if led_==1:
            logf('Indicator LEFT')
        elif led_ == 2:
            logf('Indicator RIGH')
        self.__car_info['led'] = led_
        self._indicator.setIndicate(led_)

        if buzzer_!=0:
            logf('Buzz ON')
            self._buzzer.buzz(1000)
        self.__car_info['buzzer'] = buzzer_

    
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
                self.disconnect(client, address, ANDROID_TAG, 'Device disconnected to server')
                self._require = False
                break
            
            logf('RECV: '+str(data), ANDROID_TAG)
            try:
                data = getJson(data.decode("ascii"))
                if data == '':
                    logf('Can not detect JSON string')
                    continue
                  
                logf('Detected JSON String: '+data)
                
                order = json.loads(data)

                if 'vr' not in set(order.keys()):
                    raise ValueError
                elif order['vr']==1:
                    self._require = False
                    self._frame.rotate(order['angle']) #  view angle, not steering angle
                elif order['vr']==0:
                    self._require = True
                    self.controlCar(order['angle'], order['speed'], order['mode'])
                    self._stack.push(order)
                    self.controlLedBuzz(order['led'], order['buzzer'])
                    
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
                self.disconnect(client, address, ARDUINO_TAG, 'Device disconnected to server')
                break
            
            logf('RECV: '+str(data), ARDUINO_TAG)
            try:
                data = getJson(data.decode("ascii"))
                if data == '':
                    logf('Can not detect JSON string')
                    continue
                    
                logf('Detected JSON String: '+data)
                order = json.loads(data)
                self.controlCar(order['angle'], order['speed'], order['mode'])
                self._stack.push(order)
                self.controlLedBuzz(order['led'], order['buzzer'])
            except ValueError:
                logf('ERR: Invalid JSON string!',ARDUINO_TAG)
            except Exception as e:
                logf('Exception occur: '+str(e))

        
    def disconnect(self, client, address, devName, reason):
        self._indicator_flag = True
        self._indicator.blink(LedColor.RED, 0.1, 5)
        self._indicator_flag = False

        logf(reason, devName)
        self._token[devName] -=1
        
        logf('Close connection to '+devName+str(address)) 
        client.close()
        
                
if __name__ == "__main__":
    piCar = MExCar(14, (20, 21), (12, 16))
    frame = Servo(15)
    indicator = Indicators((13, 6, 5), (22, 27, 17))
    buzzer = Buzzer(26)

    sock=('192.168.0.1',8011)
    MExManager(sock, (piCar, frame, indicator, buzzer)).listen()
