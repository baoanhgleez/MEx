#include <SoftwareSerial.h>  
const byte RX = 3;          // Chân 3 được dùng làm chân RX
const byte TX = 2;          // Chân 2 được dùng làm chân TX
SoftwareSerial mySerial = SoftwareSerial(RX, TX); 
void setup() {
  Serial.begin(115200);
  mySerial.begin(115200); 
}
void loop() { 
   int volang = analogRead(A5); 
   int pedantrai =analogRead(A4);
   int pedanphai = analogRead(A3);  
   int phanhtay = analogRead(A2);
   int l3 = analogRead(A1);
   int r3 = analogRead(A0);
   Serial.println(",");
   String data = "{'speed':"+String(pedanphai)+",'angle':"+String(volang)+",'break':"+String(pedantrai)+",'mode1':"+String(l3)+",'mode2':"+String(r3)+",'handbreak':"+String(phanhtay)+"} \n";
   mySerial.print(data);
   //mySerial.print("{'speed':111,'angle':222,'break':333,'mode1':444,'mode2':555,'handbreak':666}\n");
   delay(100);           //đợi 0.2 giây
}
  
