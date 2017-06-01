#include <ArduinoJson.h>
#include <AFMotor.h>
#include<Servo.h>
#include <SoftwareSerial.h>
#include <SerialCommand.h>  

Servo servo1;
AF_DCMotor motor1(1,MOTOR12_64KHZ);
AF_DCMotor motor2(2,MOTOR12_64KHZ);

const byte RX = 3;          // Chân 3 được dùng làm chân RX
const byte TX = 2;          // Chân 2 được dùng làm chân TX
SoftwareSerial mySerial = SoftwareSerial(RX, TX); 
SerialCommand sCmd(mySerial); // Khai báo biến sử dụng thư viện Serial Command

void setup() {
  //Khởi tạo Serial ở baudrate 57600 để debug ở serial monitor
  Serial.begin(57600);
  //Khởi tạo Serial ở baudrate 57600 cho cổng Serial thứ hai, dùng cho việc kết nối với ESP8266
  mySerial.begin(57600);
  //pinMode 2 đèn LED là OUTPUT
  motor1.setSpeed(100);
  motor2.setSpeed(100);
  servo1.attach(9);    
  // Một số hàm trong thư viện Serial Command
  sCmd.addCommand("angle",   angleController); //Khi có lệnh LED thì sẽ thực thi hàm led  
  sCmd.addCommand("speed",   speedController); //Khi có lệnh LED thì sẽ thực thi hàm led  
  Serial.println("Da san sang nhan lenh");
}
 
void loop() {
  sCmd.readSerial();  
}
 
void angleController() {
  Serial.println("ANGLE");
  char *json = sCmd.next(); //Chỉ cần một dòng này để đọc tham số nhận đươc
  Serial.println(json);
  StaticJsonBuffer<200> jsonBuffer; //tạo Buffer json có khả năng chứa tối đa 200 ký tự
  JsonObject& root = jsonBuffer.parseObject(json);//đặt một biến root mang kiểu json
 
  String param1 = root["angle"][0];//json -> tham số root --> phần tử thứ 0. Đừng lo lắng nếu bạn không có phần tử này, không có bị lỗi đâu!
  String param2 = root["angle"][1];//json -> tham số root --> phần tử thứ 0. Đừng lo lắng nếu bạn không có phần tử này, không có bị lỗi đâu!
  map(param1.toInt(),0,180,55,135);
  servo1.write(param1.toInt());
}
 
void speedController() {
  Serial.println("SPEED");
  char *json = sCmd.next(); //Chỉ cần một dòng này để đọc tham số nhận đươc
  Serial.println(json);
  StaticJsonBuffer<200> jsonBuffer; //tạo Buffer json có khả năng chứa tối đa 200 ký tự
  JsonObject& root = jsonBuffer.parseObject(json);//đặt một biến root mang kiểu json
 
  String param1 = root["speed"][0];//json -> tham số root --> phần tử thứ 0. Đừng lo lắng nếu bạn không có phần tử này, không có bị lỗi đâu!
  String param2 = root["speed"][1];//json -> tham số root --> phần tử thứ 0. Đừng lo lắng nếu bạn không có phần tử này, không có bị lỗi đâu!

  if(param1 == "FORWARD"){
    motor2.run(FORWARD);
    motor1.run(FORWARD);
  }else if (param1 == "BACKWARD"){
    motor2.run(BACKWARD);
    motor1.run(BACKWARD);
  }else if(param1 == "BRAKE"){
    motor2.run(BRAKE);
    motor1.run(BRAKE);
  }
}
