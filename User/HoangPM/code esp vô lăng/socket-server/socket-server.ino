#include <SoftwareSerial.h> 
#include <SerialCommand.h>
#include <ESP8266wifi.h>

 
const byte RX = 4;
const byte TX = 5;
 
SoftwareSerial mySerial(RX, TX); 
SerialCommand sCmd(mySerial); // Khai báo biến sử dụng thư viện Serial Command
ESP8266wifi wifi(mySerial, mySerial, 10, Serial);
const char* ssid = "Nonsense";          //Tên mạng Wifi mà Socket server của bạn đang kết nối
const char* password = "dungcomalu";  //Pass mạng wifi ahihi, anh em rãnh thì share pass cho mình với.
char host[] = "192.168.125.109";  //Địa chỉ IP dịch vụ, hãy thay đổi nó theo địa chỉ IP Socket server của bạn.
int port = 9876;                  //Cổng dịch vụ socket server do chúng ta tạo!

void setup()
{
    Serial.begin(57600);
    mySerial.begin(57600); //Bật software serial để giao tiếp với Arduino, nhớ để baudrate trùng với software serial trên mạch arduino
    delay(10);
    Serial.print("Ket noi vao mang ");
    Serial.println(ssid);
    //Kết nối vào mạng Wifi
    wifi.begin();
    wifi.connectToAP(ssid, password);
    wifi.connectToServer(host, port);    
    Serial.println(F("Da ket noi WiFi"));
    Serial.println(F("Di chi IP cua ESP8266 (Socket Client ESP8266): "));
    //Serial.println(wifi.localIP());
 
    if (!wifi.connectToServer(host, port)) {
        Serial.println(F("Ket noi den socket server that bai!"));
        return;
    } 
    sCmd.addDefaultHandler(defaultCommand); //Lệnh nào đi qua nó cũng bắt hết, rồi chuyển xuống hàm defaultCommand!
    Serial.println("Da san sang nhan lenh");   
}
 
void loop()
{

    if (!wifi.connectToServer(host, port)) {
      wifi.connectToServer(host, port);
    }
 
    sCmd.readSerial();
}
void defaultCommand(String command) {
  char *json = sCmd.next();
  wifi.send(SERVER, json, true);
  //In ra serial monitor để debug
  Serial.print(command);
  Serial.print(' ');
  Serial.println(json);
}
