#include <SoftwareSerial.h>  
const byte RX = 3;          // Chân 3 được dùng làm chân RX
const byte TX = 2;          // Chân 2 được dùng làm chân TX
SoftwareSerial mySerial = SoftwareSerial(RX, TX); 
int r3,l3, phanhtay,pedantrai,pedanphai,volang;
boolean mode_status= true;
boolean handbreak_status = false;
int m_pedantrai,m_pedanphai,m_volang;

void setup() {
  Serial.begin(115200);
  mySerial.begin(115200); 
  //pinMode(11, INPUT);
    
}
void loop() { 
    volang = analogRead(A5); 
    pedanphai =analogRead(A4);
    pedantrai= analogRead(A3);  
    phanhtay = analogRead(A2);
    l3 =analogRead(A1);
    r3  = analogRead(A0);
    //xu ly tin hieu mode
    if( r3 < 300 && l3 ==1023){
       mode_status =false;       
    }   
     if( l3 == 0 && r3 ==1023){
       mode_status =true;       
    }   
   //handbreak
     if( (l3 == 1023 || r3 ==1023 )&& phanhtay == 0){
       handbreak_status =true;       
    }else{
      handbreak_status =false;  
    }
    m_pedantrai = map(pedantrai,515,1015,0,100);
    // speed 0-10
    m_pedanphai = map(pedanphai,565,1015,0,10);
    if(volang<1015){
       volang =1015;
    }
    // angle 90 - 270
    if(volang == 1019 || volang == 1018){
      m_volang =90;
    }else{
      m_volang = map(volang,1015,1023,90,270);  
    }             
   //Serial.println(",");
   String data = "{'speed':"+String(m_pedanphai)+",'angle':"+String(m_volang)+",'break':"+String(m_pedantrai)+",'mode':"+String(mode_status)+",'hbreak':"+String(handbreak_status)+"} \n";
   Serial.println(data);
   mySerial.print(data);
   //mySerial.print("{'speed':111,'angle':222,'break':333,'mode1':444,'mode2':555,'handbreak':666}\n");
   delay(10);           //đợi 0.2 giây
}
  
