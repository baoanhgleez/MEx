from time import strftime,  gmtime

## CONSTANT
ACT_DIR = '/home/pi/MEx/'
LOG_DIR = 'logs/'
STIME = strftime("%Y-%m-%d_%H:%M:%S",gmtime())

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
