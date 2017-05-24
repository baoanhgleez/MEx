#include <Servo.h>
Servo servo1;                 //Servo horizontal
Servo servo2;                 //Servo vertical
int pos1 = 0;                 //Servo horizontal position
int pos2 = 0;                 //Servo vertical position
char data = 0;                //Variable for storing received data
int change = 0;
//khởi tạo các job
String readString;

void setup()
{
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(13, OUTPUT);        //Sets digital pin 13 as output pin of the led
  servo1.attach(9);           //Servo horizontal output pin 9
  servo2.attach(8);           //Servo vertical output pin 9
  servo1.write(90);
  servo2.write(90);   
}
void loop()
{
//  if (Serial.available() > 0) // Send data only when you receive data:
//  {
//    data = Serial.read();      //Read the incoming data and store it into variable data
//    Serial.print(data);        //Print Value inside data in Serial monitor
//    //Serial.print("\n");        //New line
//   
//  }
  while (Serial.available()) {
    delay(10);
    char c = Serial.read();  //gets one byte from serial buffer
    if (c == ',') {
      break;
    }  //breaks out of capture loop to print readstring
    readString += c; 
  } //makes the string readString  

  if (readString.length() >0) {
    Serial.println(readString); 
    readBluetooth(readString);
    readString=""; 
  }
}

void readBluetooth(String input){
  char servo = input.charAt(0);
  Serial.println(servo); 
  String temp = input.substring(2,input.length());
  Serial.println(temp.toInt()); 
  servoChange(servo,temp.toInt());  
}

void servoChange(char servo , int value){  
  if (servo =='1' ){
      servo1.write(value);                
  }else if(servo =='2' ){
      servo2.write(value);                
  }    
}




//  for (pos1 = 0; pos1 <= 180; pos1 += 1) { // goes from 0 degrees to 180 degrees
//    // in steps of 1 degree
//    servo1.write(pos1);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
//  for (pos1 = 180; pos1 >= 0; pos1 -= 1) { // goes from 180 degrees to 0 degrees
//    servo1.write(pos1);              // tell servo to go to position in variable 'pos'
//    delay(15);                       // waits 15ms for the servo to reach the position
//  }
