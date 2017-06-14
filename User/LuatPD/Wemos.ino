#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <Servo.h>
#include <ArduinoJson.h>

const char* ssid = "fakehub";       
const char* password = "fakepass";  
const int port = 8002;  // port của socket server
const int servoPin = 2; // khai bao GPIO2 la PWM cua servo
const int dc1x = 16;
const int dc1y = 5;
const int dc2x = 4;
const int dc2y = 14;
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
    pinMode(dc1x,OUTPUT);
    pinMode(dc1y,OUTPUT);
    pinMode(dc2x,OUTPUT);
    pinMode(dc2y,OUTPUT);
    
    
}

WiFiClient client;
int ind=0;
char data[1000];
String json;
int angle, value;
DynamicJsonBuffer jsonBuffer;
int speedk = 0;
int anglek = 0;
int modek = 0;

void runForward(){
  analogWrite(dc1x,0);
  analogWrite(dc1y,speedk);
  analogWrite(dc2x,0);
  analogWrite(dc2y,speedk);
}
void runBackward(){
  analogWrite(dc1x,speedk);
  analogWrite(dc1y,0);
  analogWrite(dc2x,speedk);
  analogWrite(dc2y,0);
}
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
      speedk = root[String("speed")];
            speedk = map(speedk,0,10,0,1023);
      Serial.print("speed: "); Serial.println(speedk);
      
      // angle
      anglek = root[String("angle")];
      Serial.print("angle: "); Serial.println(angle);
      
      // mode
      modek = root[String("mode")];
      Serial.print("mode: "); Serial.println(modek);

      // START: Truyen du lieu cho Serial Command
      Serial.print("MOVE ");

      Serial.print(speedk); Serial.print(" "); 
      Serial.print(anglek); Serial.print(" "); 
      Serial.print(modek); Serial.println(); 
      
      if(modek == 1) {
        runBackward();
          Serial.println("Back Ward");
      } else {
        runForward();
Serial.println("For Ward");
      }
      myServo.write(map(anglek,270,90,45,135));
      // END
      
      // xoa gia tri bien tam
      ind = 0;

      // gui lai chuoi co client
      // client.print("OK!");
    }
  }
}
