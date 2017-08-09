#include <SoftwareSerial.h>  
const byte RX = 3;          // Chân 3 được dùng làm chân RX
const byte TX = 2;          // Chân 2 được dùng làm chân TX
SoftwareSerial mySerial = SoftwareSerial(RX, TX); 
int r3,l3, phanhtay,pedantrai,pedanphai,volang;
int mode_status = 0;
boolean handbreak_status = false;
int m_pedantrai,m_pedanphai,m_volang;
int led =0;
const int buzzer = 9; 

const int  rightLEDBtn = 4;  
const int  modeBtn = 5;
const int  buzzerBtn = 6;   
const int  leftLEDBtn = 7; 
        
const int rightLEDPin = 10;
const int buzzerPin = 11;        
const int modePin = 12; 
const int leftLEDPin = 13;         
                          
int buzzerState = 0;
int leftBtnPushCounter = 1;
int rightBtnPushCounter = 1;
int leftBtnState = 0;         
int rightBtnState = 0; 
int lastLeftBtnState = 0;     
int lastRightBtnState = 0;

int buzzerBtnPushCounter = 1;
int modeBtnPushCounter = 1;
int buzzerBtnState = 0;         
int modeBtnState = 0; 
int lastbuzzerBtnState = 0;     
int lastmodeBtnState = 0;     

void setup() {
  Serial.begin(115200);
  mySerial.begin(9600);
  pinMode(buzzer, OUTPUT); 
  pinMode(leftLEDBtn, INPUT);
  pinMode(rightLEDBtn, INPUT);
  pinMode(leftLEDPin, OUTPUT);
  pinMode(rightLEDPin, OUTPUT); 
  digitalWrite(rightLEDPin, LOW);
  digitalWrite(leftLEDPin, LOW);
  pinMode(A5,INPUT);
  pinMode(buzzerBtn, INPUT);
  pinMode(modeBtn, INPUT);
  pinMode(buzzerPin, OUTPUT);
  pinMode(modePin, OUTPUT);
  digitalWrite(modePin, LOW);
  digitalWrite(buzzerPin, LOW);
    
}
void loop() { 
    volang = analogRead(A5); 
    pedanphai =analogRead(A4);
    pedantrai= analogRead(A3);  
    phanhtay = analogRead(A1);
    l3 =analogRead(A0);
    r3  = analogRead(A2);
    
    leftBtnState = digitalRead(leftLEDBtn);
    rightBtnState = digitalRead(rightLEDBtn);
    buzzerBtnState = digitalRead(buzzerBtn);
    modeBtnState = digitalRead(modeBtn); 
    modeAndBuzzerAnalyze();
    modeAndHandBreak(r3,l3,phanhtay);

    m_pedantrai = map(pedantrai,515,1015,7,0);    
    // speed 0-1
    m_pedanphai = map(pedanphai,565,1015,10,0);

    speedAnalyze();
    volangAnalyze(volang);
    ledAnalyze();        

   String data = "1234567890asdfghjkl {\"speed\":"+String(m_pedanphai)+",\"angle\":"+String(m_volang)+",\"mode\":"+String(mode_status)+",\"led\":"+String(led)+",\"buzzer\":"+String(buzzerState)+"}@@@";
   Serial.println(data);
   mySerial.print(data);
   buzzerState = 0;   
   delay(100);      
}
  
void modeAndHandBreak(int i_r3 , int i_l3, int i_phanhtay ){
	if( i_r3 < 100 && i_l3 > 100){
       mode_status =2;       
    }   
    if( i_l3 == 0 && i_r3 >100){
       mode_status =1;       
    }   
   //handbreak
    if( (i_l3 >100 || i_r3 >100 )&& i_phanhtay == 0){
       handbreak_status =true;       
    }else{
      handbreak_status =false;  
    }
}

void volangAnalyze( int i_volang){
      m_volang = map(volang,1023,0,90,270);         
}

void speedAnalyze(){
	if(m_pedantrai>0 ){
      m_pedanphai = m_pedanphai - m_pedantrai;      
    }
    if(handbreak_status == true){
      m_pedanphai = 0;
    }
}
void ledAnalyze(){
  int fnState =0;
   if (leftBtnState != lastLeftBtnState) {
    if (leftBtnState == HIGH) {
      leftBtnPushCounter++;
      rightBtnPushCounter=1;     
    }
    delay(10);
  }
  if (rightBtnState != lastRightBtnState) {
    if (rightBtnState == HIGH) {
      rightBtnPushCounter++;     
      leftBtnPushCounter=1;
    }
    delay(10);
  }
  lastRightBtnState = rightBtnState;
  lastLeftBtnState = leftBtnState;

  if (leftBtnPushCounter % 2 == 0) {
    fnState =1;
    digitalWrite(leftLEDPin, HIGH);   
  } else {
    digitalWrite(leftLEDPin, LOW);
    led =0;
  }
  
  if (rightBtnPushCounter % 2 == 0) {
    digitalWrite(rightLEDPin, HIGH);
    fnState =2;
  } else {
    digitalWrite(rightLEDPin, LOW);
    led =0;
  }
  if(fnState != 0){
    led = fnState;
  }
}
void modeAndBuzzerAnalyze(){
   if (buzzerBtnState != lastbuzzerBtnState) {
    if (buzzerBtnState == HIGH) {
      buzzerBtnPushCounter++;
      digitalWrite(buzzerPin, HIGH);  
      tone(buzzer, 1000); 
      delay(500);      
      noTone(buzzer);
      digitalWrite(buzzerPin, LOWn
      );    
      buzzerState =1;  
    }
    delay(10);
  }
  if (modeBtnState != lastmodeBtnState) {
    if (modeBtnState == HIGH) {
      modeBtnPushCounter++;  
      mode_status=0;  
      digitalWrite(modePin, HIGH); 
      delay(500);
      digitalWrite(modePin, LOW);       
    }
    delay(10);
  }
  lastmodeBtnState = modeBtnState;
  lastbuzzerBtnState = buzzerBtnState;
}

