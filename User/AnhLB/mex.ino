#include <SoftwareSerial.h>    
const byte RX = 3;                    // Chân 3 được dùng làm chân RX
const byte TX = 2;                    // Chân 2 được dùng làm chân TX
SoftwareSerial mySerial = SoftwareSerial(RX, TX); 
int r3,l3, phanhtay,pedantrai,pedanphai,volang;
int mode_status = 0;
bool handbreak_status = false; // TO-DO xem lại dùng kiểu bool được ko?
int m_pedantrai,m_pedanphai,m_volang;
int led =0;
const int buzzer = 9; 

const int rightLEDBtn = 4;    
const int modeBtn = 5;
const int buzzerBtn = 6;     
const int leftLEDBtn = 7; 
                
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
    pinMode(A5,INPUT);
    pinMode(buzzerBtn, INPUT);
    pinMode(modeBtn, INPUT);
    pinMode(buzzerPin, OUTPUT);
    pinMode(modePin, OUTPUT);
    digitalWrite(modePin, LOW);
    digitalWrite(buzzerPin, LOW);
    digitalWrite(rightLEDPin, LOW);
    digitalWrite(leftLEDPin, LOW);
        
}
void loop() { 
        volang = analogRead(A5); 
        pedanphai =analogRead(A0);
        pedantrai= analogRead(A1);    
        phanhtay = analogRead(A3); 
        l3 =analogRead(A4); 
        r3    = analogRead(A2);
        
        leftBtnState = digitalRead(leftLEDBtn);
        rightBtnState = digitalRead(rightLEDBtn);
        buzzerBtnState = digitalRead(buzzerBtn);
        modeBtnState = digitalRead(modeBtn); 
        modeAndBuzzerAnalyze();
        modeAndHandBreak(r3,l3,phanhtay);
        m_pedantrai = map(pedantrai,515,1015,90,0);        
        m_pedanphai = map(pedanphai,565,1015,100,0);
        speedAnalyze();
        volangAnalyze(volang);
        ledAnalyze();                
        String data = "1234567890 {\"speed\":"+String(m_pedanphai)+",\"angle\":"+String(m_volang)+",\"mode\":"+String(mode_status)+
                                        ",\"led\":"+String(led)+",\"buzzer\":"+String(buzzerState)+"}@@@";
        Serial.println(data);
        mySerial.print(data);
        buzzerState = 0;
        delay(100);            
}
    
void modeAndHandBreak(int i_r3 , int i_l3, int i_phanhtay ){
	if( i_r3 < 100 && i_l3 > 100){
             mode_status =2; 
             buzz(200);            
        }     
    if( i_l3 < 100 && i_r3 >100){
        mode_status =1;             
        buzz(200);
    }     
    if( (i_l3 >100 || i_r3 >100 )&& i_phanhtay <3){
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
    if(m_pedanphai <0 ){
        m_pedanphai = 0;
    }
}
void ledAnalyze(){
    int fnState = 0;
     if (leftBtnState != lastLeftBtnState) {
        if (leftBtnState == HIGH) {
            leftBtnPushCounter++;
            rightBtnPushCounter=1;         
        }
        buzz(100);
    }
    if (rightBtnState != lastRightBtnState) {
        if (rightBtnState == HIGH) {
            rightBtnPushCounter++;         
            leftBtnPushCounter=1;
        }
        buzz(100);
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
             buzz(500);
             buzzerState =1;    
        }
        delay(10);
    }
    if (modeBtnState != lastmodeBtnState) {
        if (modeBtnState == HIGH) {
            modeBtnPushCounter++;    
            mode_status=0;    
            digitalWrite(modePin, HIGH); 
            buzz(200);
            digitalWrite(modePin, LOW);             
        }
        delay(10);
    }
    lastmodeBtnState = modeBtnState;
    lastbuzzerBtnState = buzzerBtnState;
}
void buzz(int timer){
            digitalWrite(buzzerPin, HIGH);    
            tone(buzzer, 1000); 
            delay(timer);            
            noTone(buzzer);
            digitalWrite(buzzerPin, LOW);        
}


//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------


class Car{
    // input for car control
    int wheel;          //  volang = analogRead(A5); 
    int pedalSpeed;     //  pedanphai =analogRead(A0);
    int pedalBrake;     //  pedantrai= analogRead(A1);
    int handBrake;      //  phanhtay = analogRead(A3); 
    int gearL3;         //  analogRead(A4); 
    int gearR3;         //  analogRead(A2);
    int btnParking;     //const int    modeBtn = 5;

    // led will turn on when in parking mode
    int ledParking

    // Car info to JSON parsing
    int angle, speed, mode;
    Indicator indicator;
    Buzzer buzzer;


    const int MIN_WHEEL = 0;
    const int MAX_WHEEL = 1023;
    const int MIN_ANGLE = 90;
    const int MAX_ANGLE = 270;

    const int MAXPUSH_PEDAL_SPEED = 565;
    const int RELEASE_PEDAL_SPEED = 1015;
    const int MAXPUSH_PEDAL_BRAKE = 515;
    const int RELEASE_PEDAL_BRAKE = 1015;

public:
    Car(int wheel, int pedalSpeed, int pedalBrake, int handBrake,  int gearL3, int gearR3, 
            int btnParking, int ledParking, Indicator indicator, Buzzer buzzer){
        pinMode(wheel, INPUT);
        this.wheel = wheel;

        pinMode(pedalSpeed, INPUT);
        this.pedalSpeed = pedalSpeed;

        pinMode(pedalBrake, INPUT);
        this.pedalBrake = pedalBrake;
        
        pinMode(handbrake, INPUT);
        this.handBrake = handBrake;
        
        pinMode(gearL3, INPUT);
        this.gearL3 = gearL3;
        
        pinMode(gearR3, INPUT);
        this.gearR3 = gearR3;

        pinMode(btnParking, INPUT);
        this.btnParking = btnParking;

        pinMode(ledParking, OUTPUT);
        this.ledParking = ledParking;

        this.indicator = indicator;
        this.buzzer = buzzer;


        this.init();
    }

    void init(){
        angle = 180;
        speed = 0;
        mode = 0;
        buzzer = 0;
        led = 0;
    }

    int getAngle(){
        angle = map( analogRead(wheel), MAX_WHEEL, MIN_WHEEL, MIN_ANGLE, MAX_ANGLE);
        return angle;
    }

    int getMode(){

        if (digitalRead(btnParking) == HIGH){
            mode = 0;
        }else{
            // Expected: R3 & L3 tra ve gia tri 0, 1
            // if (digitalRead(gearL3)==0){
            //     return 1;
            // }else if (digitalRead(gearR3)==0){
            //     return 2;
            // }

            // Actual: R3 va L3 ko tra ve gia tri 0, 1
            int L3 = analogRead(gearL3);
            int R3 = analogRead(gearR3);
    
            if( L3 < 100 && R3 >= 100){
                mode = 1;
                buzzer.buzz(100);
            }else if ( R3 < 100 && L3 >= 100){
                mode = 2;
                buzzer.buzz(100);
            }
        }

        return mode;    
    }   

    int getSpeed(){
        if ((mode==0) || (analogRead(handBrake)<3)){ // Expect: digitalWrite(handbrake)==0 
            speed = 0;
        }else{
            int pedalInput = analogRead(pedalSpeed);
            if (pedalInput < RELEASE_PEDAL_SPEED){ // co nhan ga
                speed = map (pedalInput, RELEASE_PEDAL_SPEED, MAXPUSH_PEDAL_SPEED, 10, 100);
            }

            pedalInput = analogRead(pedalBrake);
            if (pedalInput < MAX_PEDAL_BRAKE){ // co nhan phanh
                speed = map (pedalInput, MAXPUSH_PEDAL_BRAKE, RELEASE_PEDAL_BRAKE, 0, speed);
            }
        }

        return speed;
    }

    int getIndicator(){
        int state = indicator.getState();
        if (state!=0){
            buzzer.buzz(100);
        }
        return state;
    }

    int getBuzzer(){
        return buzzer.getBtnState();
    }

    void refreshData(){
        getMode();
        getAngle();
        getSpeed();
        getIndicator();
        getBuzzer();
    }

    String toJSON(){
        refreshData();
        return "{\"speed\":"+String(speed)+",\"angle\":"+String(angle)+",\"mode\":"+String(mode)+
                    ",\"led\":"+indicator.toString()+",\"buzzer\":"+buzzer.toString()+"}";
    }


}

class Indicator{
    // input button trigger
    int btnLeft, btnRigh;

    // output led pin
    int ledLeft, ledRigh;

    // to control state of indicator
    int state;

public:
    Indicator(int btnLeft, int btnRigh, int ledLeft, int ledRigh){
        pinMode(btnLeft, INPUT);
        this.btnLeft = btnLeft;

        pinMode(btnRigh, INPUT);
        this.btnRigh = btnRigh;

        pinMode(btnLeft, OUTPUT);
        this.ledLeft = ledLeft;

        pinMode(btnRigh, OUTPUT);
        this.ledRigh = ledRigh;

        state = 0;
        digitalWrite(ledLeft, LOW);
        digitalWrite(ledLeft, HIGH);
    }

    int getState(){
        if (digitalRead(btnLeft)==HIGH){
            if (state==1){
                state = 0;
                digitalWrite(ledLeft, LOW);
            }else{
                state = 1;
                digitalWrite(ledLeft, HIGH);
            }
        }else if (digitalRead(btnRigh)==HIGH){
            if (state==2){
                state = 0;
                digitalWrite(ledRigh, LOW);
            }else{
                state = 2;
                digitalWrite(ledRigh, HIGH);
            }
        }
    }

    String toString(){
        return String(state);
    }
}

class Buzzer{
    // input button trigger
    int btnBuzzer;

    // output buzzer pin
    int pinBuzzer;

    // led indicate when buzz
    int ledBuzzer;

    // save state of buzz
    int state;

    const int DEFAULT_FREQ = 1000;

public:
    Buzzer(int btnBuzzer, int pinBuzzer, int ledBuzzer){
        pinMode(btnBuzzer, INPUT);
        this.btnBuzzer = btnBuzzer;

        pinMode(pinBuzzer, OUTPUT);
        this.pinBuzzer = pinBuzzer;

        pinMode(ledBuzzer, OUTPUT);
        this.ledBuzzer = ledBuzzer;

        state = 0;
        digitalWrite(ledBuzzer, LOW);
    }

    int getBtnState(){
        if (digitalRead(btnBuzzer)==HIGH){
            state = 1;
            buzz(100);
        }else{
            state = 0;
        }
    }

    void buzz(int time){
        digitalWrite(ledBuzzer, HIGH);
        tone(pinBuzzer, DEFAULT_FREQ);
        delay(time);      
        noTone(pinBuzzer);
        digitalWrite(ledBuzzer, LOW);     
    }

    String toString(){
        return String(state);
    }

}

