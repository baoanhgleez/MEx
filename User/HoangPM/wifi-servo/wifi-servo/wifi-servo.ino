#include <Servo.h>
#include <SoftwareSerial.h>
SoftwareSerial fakeSerial = SoftwareSerial(2, 3); 
Servo myservo;
String readString;
void setup() {
  Serial.begin(9600);
  fakeSerial.begin(115200);
  myservo.attach(9);  // attaches the servo on pin 9 to the servo object
}
void loop() {
  while (fakeSerial.available()) {
    delay(10);
    char c = fakeSerial.read();  //gets one byte from serial buffer
    Serial.println(c);
    if (c == ',') {
      break;
    }  //breaks out of capture loop to print readstring
    readString += c; 
    
  }
  if (readString.length() >0) {
    Serial.println(readString);
    myservo.write(readString.toInt()); 
    readString=""; 
  }  
}

