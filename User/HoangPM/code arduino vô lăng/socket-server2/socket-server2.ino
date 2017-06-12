#include <ArduinoJson.h>
#include <AFMotor.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <SerialCommand.h>  

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
//  sCmd.addCommand("angle",   angleController); //Khi có lệnh LED thì sẽ thực thi hàm led  
//  sCmd.addCommand("speed",   speedController); //Khi có lệnh LED thì sẽ thực thi hàm led  
  Serial.println("Da san sang nhan lenh");
}
 
void loop() {
  sCmd.readSerial();  
  int value = analogRead(A5);
  Serial.println(value);        //xuất ra giá trị vừa đọc
  
  int voltage;
  voltage = map(value,0,1023,0,255);   //chuyển thang đo của value 
                                        //từ 0-1023 sang 0-5000 (mV)
  Serial.println(voltage);              //xuất ra điện áp (đơn vị là mV)
  
  Serial.println();     //xuống hàng
  mySerial.print("{'speed':'"+String(voltage)+"'}");
  delay(200);           //đợi 0.2 giây
}
  
