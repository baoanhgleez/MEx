#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

MDNSResponder mdns;
ESP8266WebServer server(80);
String webPage = "";
IPAddress local_IP(192,168,4,22);
IPAddress gateway(192,168,4,9);
IPAddress subnet(255,255,255,0);
void setup()
{
  webPage += "<h1>ESP8266 Web Server</h1><p>Socket #1 <a href=\"socket1On\"><button>ON</button></a>&nbsp;<a href=\"socket1Off\"><button>OFF</button></a></p>";
  webPage += "<p>Socket #2 <a href=\"socket2On\"><button>ON</button></a>&nbsp;<a href=\"socket2Off\"><button>OFF</button></a></p>";  
  
  Serial.begin(115200);
  Serial.println();

  Serial.print("Setting soft-AP ... ");
  Serial.println(WiFi.softAP("hoangpm", "johncena") ? "Ready" : "Failed!");
  Serial.println(WiFi.softAPConfig(local_IP, gateway, subnet) ? "Ready" : "Failed!");
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
  Serial.printf("Stations connected = %d\n", WiFi.softAPgetStationNum());
  delay(3000);
}
