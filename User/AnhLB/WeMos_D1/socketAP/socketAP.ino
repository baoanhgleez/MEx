#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <Servo.h>

const char* ssid = "fakehub";       
const char* password = "fakepass";  
const int port = 8002;  // port của socket server
const int servoPin = 2; // khai bao GPIO2 la PWM cua servo

WiFiServer server(port);
Servo myServo;

void setup() {
    // cai dat servo
    myServo.attach(servoPin);

    Serial.begin(115200);
    
    // phat wifi
    WiFi.softAP(ssid, password);

    // nhan dia chi socket server
    IPAddress myIP = WiFi.softAPIP();
    Serial.print("Local address: ");   Serial.println(myIP);
    Serial.print("Port "); Serial.println(port);

    // bat socket server
    server.begin();
    
}

WiFiClient client;
int ind=0;
char data[1000];
int angle, value;

void loop() {    
  if(!client.connected()) {
    //try to connect to a new client
    client = server.available();
  }
  else
  {
    if(client.available() > 0)
    {
      while(client.available())
      {
        data[ind] = client.read();
        ind++;
      }   
      // xoa bo nho dem tu client
      client.flush();
      
      Serial.println(data);
      for(int i=0; i<ind; i++){
        value = value*10 + (data[i]-'0');
      }
      Serial.println(value);

      // dieu khien servo
      // luu y servo chi quay khi 0<=value<=180
      myServo.write(value);

      // xoa gia tri bien tam
      ind = 0;       value=0;

      // gui lai chuoi co client
      client.print("OK!");
    }
  }
}
