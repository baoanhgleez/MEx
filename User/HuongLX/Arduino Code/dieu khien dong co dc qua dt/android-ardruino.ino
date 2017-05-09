int pin,value;
void setup()
{
  Serial.begin(9600);
  pinMode(13,OUTPUT);
  pinMode(3,OUTPUT);
  pin=value=0;
}
 
void loop() 
{
  if (Serial.available()){
    String s = Serial.readString(); 
    Serial.setTimeout(10);
    pin = s.substring(0,1).toInt();
    value = s.substring(2).toInt();
//    Serial.print("pin: ");
//    Serial.print(pin);
//    Serial.print(",value: ");
//    Serial.print(value);
//    Serial.println();
  }
  if(pin>0){
    analogWrite(pin,value);
    digitalWrite(13,1);
  }
}
