#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <SoftwareSerial.h> 
const char* ssid = "RazzPi";         
const char* pass = "123@123a";  
char host[] = "192.168.0.1";  
int port = 8011; 

const byte RX1 = 4;
const byte TX1 = 5;  
SoftwareSerial unoSerial(RX1, TX1); 
WiFiClient client;      
void setup() {
  Serial.begin(115200); 
  unoSerial.begin(9600); 
  delay(1000);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("My IP: ");
  Serial.println(WiFi.localIP());
  client.connect(host, port);
  client.print("ARDUINO");
}
String data="";
void loop() {  
    while(unoSerial.available()) {
        data= unoSerial.readStringUntil('@@@');
    }       
    Serial.println(data);
    client.print(data);
    //data = "";
    delay(100);
    //client.println("{'speed':111,'angle':222,'break':333,'mode1':444,'mode2':555,'handbreak':666}");
} 
