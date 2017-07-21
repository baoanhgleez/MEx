import socket, json, threading
import mexutils
from mexutils import logf

class MExServer(object):
    def __init__(self, address='192.168.0.1', port=8011):
        self._address = address
        self._port = port
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self._sock.bind((self._address, self._port))
        self._token = {b'ANDROID':0, b'ARDUINO':0}
        self._require = False
        self.__MAX_ALLOW_FOR_EACH_TYPE = 1
        self.__PACKAGE_SIZE = 1024
        self.__ANDROID_SET = {'angle', 'speed', 'mode', 'viewX', 'viewY'}
        self.__ARDUINO_SET = {'angle', 'speed', 'mode', 'buzz', 'indicator'}

    def listen(self):
        self.sock.listen(5)
        while True:
            logf('Waiting for a connection..')
            client, address = self._sock.accept()
            logf('Request connect at address: '+str(address))
            client.settimeout(60)
            targetDev = client.recv(10)
            logf('Device: '+str(targetDev))
            if targetDev not in set(self._token.keys()):
                client.sendall(b'NO')
                client.close()
                logf('Unable to authenticate device')
                logf('Access Denied!')
            else:
                if self._token[targetDev]>=self.__MAX_ALLOW_FOR_EACH_TYPE:
                    client.sendall(b'LIMIT')
                    client.close()
                    logf('Device Limitation Reached! Disconnected to device')
                else:
                    self._token[targetDev] +=1
                    client.sendall(b'OK')
                    logf('Connected to an '+str(targetDev)+' device at address '+str(address))
                    if targetDev==b'ANDROID':
                        threading.Thread(target = self.listenToAndroid,args = (client,address)).start()
                    elif targetDev==b'ARDUINO':
                        threading.Thread(target = self.listenToArduino,args = (client,address)).start()

    def listentoAndroid(self, client, address):
        while True:
            data = client.recv(self.__PACKAGE_SIZE)
            logf('RECV: '+str(data))
            if data == b'':
                logf('Disconnected from ', address)
                self._token[b'ANDROID'] -=1
                self._require = False    
                client.close()
                break;
            self._require = True
            try:
                order = json.loads(data.decode("ascii"))
                if (self.__ANDROID_SET == set(order.keys()) ):
                    angle = mapValue(order['angle'], 90, 270, 180, 0)
                    speed = mapValue(order['speed'], 0, 10, 0, 100)
                    if angle<0 or angle>180:
                        logf('ERR: Invalid rotate angle')
                    else:
                    # TO-DO
                        mode  = 3 - order['mode'] #remap gear order
                        if mode == GearMode.FORWARD:
                            logf('FORWARD -r'+str(angle)+' -s'+str(speed))
                        elif mode == GearMode.BACKWARD:
                            logf('BACKWARD -r'+str(angle)+' -s'+str(speed))
                        elif mode == GearMode.PARKING:
                            logf('PARKING -r'+str(angle)+' -s0')

                        # TO-DO: Control Car
            except ValueError:
                logf('ERR: Invalid JSON string!')

    def listenToArduino(self, client, address):
        while True:
            if self.self._require:
               continue
            data = client.recv(self.__PACKAGE_SIZE)
            logf('RECV: '+str(data))
            if data == b'':
                logf('Disconnected from ', address)
                self._token[b'ARDUINO'] -=1
                self._require = False
                client.close()
                break;
            self._require = True
            try:
                order = json.loads(data.decode("ascii"))
                if (self.__ARDUINO_SET == set(order.keys()) ):
                    angle = mapValue(order['angle'], 90, 270, 180, 0)
                    speed = mapValue(order['speed'], 0, 10, 0, 100)
                    if angle<0 or angle>180:
                        logf('ERR: Invalid rotate angle')
                    else:
                    # TO-DO
                        mode  = 3 - order['mode'] #remap gear order
                        if mode == GearMode.FORWARD:
                            logf('FORWARD -r'+str(angle)+' -s'+str(speed))
                        elif mode == GearMode.BACKWARD:
                            logf('BACKWARD -r'+str(angle)+' -s'+str(speed))
                        elif mode == GearMode.PARKING:
                            logf('PARKING -r'+str(angle)+' -s0')

                        # TO-DO: Control Car
            except ValueError:
                logf('ERR: Invalid JSON string!')
                
if __name__ == "__main__":
    MExServer().listen()
