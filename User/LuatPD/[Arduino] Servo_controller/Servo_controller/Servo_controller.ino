#include<Servo.h>

Servo servo;
int accelero;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(5);
  servo.attach(9);
}

void loop() {
  if(Serial.available()){
    accelero = Serial.parseInt();
    if(accelero > -95 && accelero < 95){
      accelero = map(accelero, -95, 95, 0, 180);
      servo.write(accelero);
    }
  }

}
