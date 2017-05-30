#include <AFMotor.h>
#include<Servo.h>

Servo servo1;
AF_DCMotor motor1(1,MOTOR12_64KHZ);
AF_DCMotor motor2(2,MOTOR12_64KHZ);
long time;
long resetTime = 1000;
bool switchChannel = false;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  time = millis();
  motor1.setSpeed(100);
  motor2.setSpeed(100);
  servo1.attach(9);
}
int rad = 0;
void loop() {
//
// // put your main code here, to run repeatedly:
  if(millis()-time >resetTime) reset();
   if(switchChannel){
    motor2.run(FORWARD);
    motor1.run(BACKWARD);
    servo1.write(55);
    }
    else{
      motor2.run(BACKWARD);
      motor1.run(FORWARD);
      servo1.write(135);
    }
}
void reset(){
  time = millis();
  switchChannel=!switchChannel;
// servo1.write(90);
}

