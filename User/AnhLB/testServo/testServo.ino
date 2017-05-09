    #include <Servo.h>      // Thư viện điều khiển servo
     
    // Khai báo đối tượng myservo dùng để điều khiển servo
    Servo downServo, upServo;          

     // Khai báo chân điều khiển servo
    int downServoPin = 9;
    int upServoPin = 10;
    int stepPos = 10;
     
    void setup ()
    {
        downServo.attach(downServoPin); 
        upServo.attach(upServoPin); 
    }
     
    void loop ()
    {
        // Dieu khien xoay vong servo ben duoi
        for (int i=0; i<=180; i+=stepPos){
          downServo.write(i);
          upServo.write(i);
          delay(100);
        }
        for (int i=180; i>=0; i-=stepPos){
          downServo.write(i);
          upServo.write(i);
          delay(100);
        }

        
    }
