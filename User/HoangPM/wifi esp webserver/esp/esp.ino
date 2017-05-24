#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

MDNSResponder mdns;
const char* ssid = "hoangpm";          //Tên mạng Wifi mà Socket server của bạn đang kết nối
const char* password = "johncena";  //Pass mạng wifi ahihi, anh em rãnh thì share pass cho mình với.
ESP8266WebServer server(80);
String webPage = "";
void setup()
{
  webPage += "<h1>ESP8266 Web Server</h1><p>Socket #1 <a href=\"socket1On\"><button>ON</button></a>&nbsp;<a href=\"socket1Off\"><button>OFF</button></a></p>";
  webPage += "<p>Socket #2 <a href=\"socket2On\"><button>ON</button></a>&nbsp;<a href=\"socket2Off\"><button>OFF</button></a></p>";
  
    Serial.begin(115200);
    delay(10);
    WiFi.begin(ssid, password);
    //Chờ đến khi đã được kết nối
    while (WiFi.status() != WL_CONNECTED) { //Thoát ra khỏi vòng 
        delay(500);       
    }
    server.on("/", [](){
      server.send(200, "text/html", webPage);
    });
    server.on("/socket1On", [](){
      server.send(200, "text/html", webPage);
      Serial.println("180");
      delay(1000);
    });
    server.on("/socket1Off", [](){
      server.send(200, "text/html", webPage);
      Serial.println("130");
      delay(1000); 
    });
    server.on("/socket2On", [](){
      server.send(200, "text/html", webPage);
     Serial.println("80");
      delay(1000);
    });
    server.on("/socket2Off", [](){
      server.send(200, "text/html", webPage);
      Serial.println("0");
      delay(1000); 
    });
    server.begin();
}

void loop()
{
  server.handleClient();
//    Serial.println("180");              // tell servo to go to position in variable 'pos' 
//    delay(1000);                       // waits 15ms for the servo to reach the position
//    Serial.println("0");    
//    delay(1000);
}
