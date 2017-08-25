from time import strftime,  gmtime

## CONSTANT
ACT_DIR = '/home/pi/MEx/'
LOG_DIR = 'logs/'
ANDROID_TAG = 'ANDROID'
ARDUINO_TAG = 'ARDUINO'
STIME = strftime("%Y-%m-%d_%H:%M:%S",gmtime())

def getJson(string):
    while (len(string)>2) and (string[0]!='{'):
        string = string[1:]
    if string[0]!='{':
        return ''

    closePos = -1
    for i in range(1, len(string)):
        if string[i]=='}':
            closePos = i
            break
        
    if closePos == -1 :
        return ''
    else:
        return string[:closePos+1]

def dutyCycle(value):
    if value<0:
        return 0
    elif value>100:
        return 100
    else:
        return value

def mapValue( value, fromMin, fromMax, toMin, toMax):
    '''
    Re-maps a number from one range to another.
    That is, a value of fromLow would get mapped to toLow, a value of fromHigh
    to toHigh, values in-between to values in-between, etc.
    '''
    alpha = (toMax-toMin)/(fromMax-fromMin)
    return (value-fromMin)*alpha + toMin

class GearMode:
    PARKING  = 0
    FORWARD  = 1
    BACKWARD = 2

class LedColor:
    RED = (100, 0, 0)
    GREEN = (0, 100, 0)
    BLUE = (0, 0, 100)
    WHITE = (100, 100, 100)
    YELLOW = (100, 100, 0)
    CYAN = (0, 100, 100)
    PURPLE = (100, 0, 100)

class Stack:
     def __init__(self):
         self.items = []

     def isEmpty(self):
         return self.items == []

     def push(self, item):
         self.items.append(item)

     def pop(self):
         return self.items.pop()

     def peek(self):
         return self.items[len(self.items)-1]

     def size(self):
         return len(self.items)

import os
if os.getcwd()!=ACT_DIR:
    os.chdir(ACT_DIR)

# create log directory in the first time
if not os.path.exists(LOG_DIR):
    os.makedirs(LOG_DIR)

with open(LOG_DIR+STIME, 'w') as logFile:
    pass
logFile.close()

def logf(message, tag='RASPI'):
    content = '['+tag+'] '+message
    print(content)
    with open(LOG_DIR+STIME, 'a') as f:
        f.write(content+'\n')
    f.close()
