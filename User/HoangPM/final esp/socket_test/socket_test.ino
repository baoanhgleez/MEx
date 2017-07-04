#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <SoftwareSerial.h> 
const char* ssid = "hoang";
const char* pass = "johncena";
const char* host = "192.168.137.67"; 
int port = 9876; 
//const char* ssid = "fakehub";         
//const char* pass = "fakepass";  
//char host[] = "192.168.4.1";  
//int port = 8002; 

const byte RX = 4;
const byte TX = 5;  
SoftwareSerial unoSerial(RX, TX);     
void setup() {
  Serial.begin(115200); 
  unoSerial.begin(115200); 
  delay(1000);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("My IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  WiFiClient client;  
  while (!client.connect(host, port)) {
    Serial.print(".");
    yield();
  }
  //client.println("hoangdeptrai");
  delay(1000);
  //String line = client.readStringUntil('\n');
  String data = unoSerial.readStringUntil('\n');
  client.println(data);
 // client.println("hoangdeptrai");
  yield();
} 
