#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <Servo.h>
#include <ArduinoJson.h>

const char* ssid = "fakehub";       
const char* password = "fakepass";  
const int port = 8002;  // port của socket server
const int servoPin = 2; // khai bao GPIO2 la PWM cua servo

WiFiServer server(port);
Servo myServo;

void setup() {
    // cai dat servo
    myServo.attach(servoPin);
    myServo.write(90);

    Serial.begin(115200);
    
    // phat wifi
    WiFi.softAP(ssid, password);

    // nhan dia chi socket server
    IPAddress myIP = WiFi.softAPIP();
    
    Serial.println();
    Serial.print("Activated WiFI Hostpot: "); Serial.println(ssid);
    Serial.print("Password: "); Serial.println(password);
    Serial.print("Server address: ");   Serial.println(myIP);
    Serial.print("Server port "); Serial.println(port);

    // bat socket server
    server.begin();
    
}

WiFiClient client;
int ind=0;
char data[1000];
String json;
int angle, value;
DynamicJsonBuffer jsonBuffer;

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
      
      Serial.print("data: ");
      Serial.println(data);
      
      json="";
      
      for(int i=0; i<ind; i++){
        json += data[i];
      }
      Serial.print("json: ");
      Serial.println(json);
        
      JsonObject& root = jsonBuffer.parseObject(json);

      // speed
      int speedk = root[String("speed")];
      Serial.print("speed: "); Serial.println(speedk);
      
      // angle
      int angle = root[String("angle")];
      Serial.print("angle: "); Serial.println(angle);
      
      // mode
      int modek = root[String("mode")];
      Serial.print("angle: "); Serial.println(modek);
      
      // xoa gia tri bien tam
      ind = 0;

      // gui lai chuoi co client
      // client.print("OK!");
    }
  }
}
