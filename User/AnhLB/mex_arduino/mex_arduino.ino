#include <SoftwareSerial.h>

const byte RX = 3; 
const byte TX = 2; 
const byte BTN_BUZZER_PIN = 6;
const byte OUT_BUZZER_PIN = 9;
const byte LED_BUZZER_PIN = 11;
const byte BTN_LEFT_PIN = 7;
const byte BTN_RIGH_PIN = 4;
const byte BTN_LEDLEFT_PIN = 13;
const byte BTN_LEDRIGH_PIN = 10;
const byte IN_WHEELING_PIN = A5;
const byte IN_PEDALSPEED_PIN = A0;
const byte IN_PEDALBRAKE_PIN = A1;
const byte IN_HANDBRAKE_PIN = A3;
const byte IN_GEARL3_PIN = A4;
const byte IN_GEARR3_PIN = A2;
const byte BTN_PARKING_PIN = 5;
const byte LED_PARKING_PIN = 12;

const int MIN_WHEEL = 0;
const int MAX_WHEEL = 1023;
const int MIN_ANGLE = 90;
const int MAX_ANGLE = 270;
const int MAXPUSH_PEDAL_SPEED = 565;
const int RELEASE_PEDAL_SPEED = 1015;
const int MAXPUSH_PEDAL_BRAKE = 515;
const int RELEASE_PEDAL_BRAKE = 1015;


class Indicator
{
	// input button trigger
	byte btnLeft, btnRigh;
	// output led pin
	byte ledLeft, ledRigh;
	// to control state of indicator
	byte state;
	public:
	
	Indicator()
	{
		state = 0;
		digitalWrite(ledLeft, LOW);
		digitalWrite(ledLeft, HIGH);
	}
	Indicator(byte btnLeft, byte btnRigh, byte ledLeft, byte ledRigh)
	{
		pinMode(btnLeft, INPUT);
		this->btnLeft = btnLeft;
		pinMode(btnRigh, INPUT);
		this->btnRigh = btnRigh;
		pinMode(btnLeft, OUTPUT);
		this->ledLeft = ledLeft;
		pinMode(btnRigh, OUTPUT);
		this->ledRigh = ledRigh;
		state = 0;
		digitalWrite(ledLeft, LOW);
		digitalWrite(ledLeft, HIGH);
	}
	byte getState()
	{
		if (digitalRead(btnLeft) == HIGH)
		{
		if (state == 1)
		{
			state = 0;
			digitalWrite(ledLeft, LOW);
		}
		else
		{
			state = 1;
			digitalWrite(ledLeft, HIGH);
		}
		}
		else
		if (digitalRead(btnRigh) == HIGH)
		{
			if (state == 2)
			{
			state = 0;
			digitalWrite(ledRigh, LOW);
			}
			else
			{
			state = 2;
			digitalWrite(ledRigh, HIGH);
			}
		}
	}
	String toString()
	{
		return String(state);
	}
};

class Buzzer
{
	// input button trigger
	byte btnBuzzer;
	// output buzzer pin
	byte pinBuzzer;
	// led indicate when buzz
	byte ledBuzzer;
	// save state of buzz
	byte state;
	const int DEFAULT_FREQ = 1000;
	public:
	Buzzer()
	{
		state = 0;
		digitalWrite(ledBuzzer, LOW);
	}
	
	Buzzer(byte btnBuzzer, byte pinBuzzer, byte ledBuzzer)
	{
		pinMode(btnBuzzer, INPUT);
		this->btnBuzzer = btnBuzzer;
		pinMode(pinBuzzer, OUTPUT);
		this->pinBuzzer = pinBuzzer;
		pinMode(ledBuzzer, OUTPUT);
		this->ledBuzzer = ledBuzzer;
		state = 0;
		digitalWrite(ledBuzzer, LOW);
	}
	byte getBtnState()
	{
		if (digitalRead(btnBuzzer) == HIGH)
		{
		state = 1;
		buzz(100);
		}
		else
		{
		state = 0;
		}
	}
	void buzz(int time)
	{
		digitalWrite(ledBuzzer, HIGH);
		tone(pinBuzzer, DEFAULT_FREQ);
		delay(time);
		noTone(pinBuzzer);
		digitalWrite(ledBuzzer, LOW);
	}
	String toString()
	{
		return String(state);
	}
};


class Controller
{
	// input for car control
	byte wheel;
	byte pedalSpeed;
	byte pedalBrake;
	byte handBrake;
	byte gearL3;
	byte gearR3;
	byte btnParking;
	// led will turn on when in parking mode
	byte ledParking;
	// Car info to JSON parsing
	int angle, speed, mode;
	Indicator *indicator;
	Buzzer *buzzer;
	
	public:
	Controller(byte wheel, byte pedalSpeed, byte pedalBrake, byte handBrake, byte gearL3, byte gearR3, byte btnParking, byte ledParking, 
	byte btnLeft, byte btnRigh, byte ledLeft, byte ledRigh, byte btnBuzzer, byte pinBuzzer, byte ledBuzzer)
	{
		pinMode(wheel, INPUT);
		this->wheel = wheel;
		pinMode(pedalSpeed, INPUT);
		this->pedalSpeed = pedalSpeed;
		pinMode(pedalBrake, INPUT);
		this->pedalBrake = pedalBrake;
		pinMode(handBrake, INPUT);
		this->handBrake = handBrake;
		pinMode(gearL3, INPUT);
		this->gearL3 = gearL3;
		pinMode(gearR3, INPUT);
		this->gearR3 = gearR3;
		pinMode(btnParking, INPUT);
		this->btnParking = btnParking;
		pinMode(ledParking, OUTPUT);
		this->ledParking = ledParking;
		this->indicator = new Indicator(btnLeft, btnRigh, ledLeft, ledRigh);
		this->buzzer = new Buzzer(btnBuzzer, pinBuzzer, ledBuzzer);
		
		init();
	}
	void init()
	{
		angle = 180;
		speed = 0;
		mode = 0;
	}
	int getAngle()
	{
		angle = map(analogRead(wheel), MAX_WHEEL, MIN_WHEEL, MIN_ANGLE, MAX_ANGLE);
		return angle;
	}
	int getMode()
	{
		if (digitalRead(btnParking) == HIGH)
		{
		mode = 0;
		}
		else
		{
		// Expected: R3 & L3 tra ve gia tri 0, 1
		// if (digitalRead(gearL3)==0){
		// return 1;
		// }else if (digitalRead(gearR3)==0){
		// return 2;
		// }
		// Actual: R3 va L3 ko tra ve gia tri 0, 1
		int L3 = analogRead(gearL3);
		int R3 = analogRead(gearR3);
		if (L3 < 100 && R3 >= 100)
		{
			mode = 1;
			buzzer->buzz(100);
		}
		else
			if (R3 < 100 && L3 >= 100)
			{
			mode = 2;
			buzzer->buzz(100);
			}
		}
		return mode;
	}
	int getSpeed()
	{
		if ((mode == 0) || (analogRead(handBrake) < 3))
		{
		// Expect: digitalWrite(handbrake)==0
		speed = 0;
		}
		else
		{
		int pedalInput = analogRead(pedalSpeed);
		if (pedalInput < RELEASE_PEDAL_SPEED)
		{
			// co nhan ga
			speed = map(pedalInput, RELEASE_PEDAL_SPEED, MAXPUSH_PEDAL_SPEED, 10, 100);
		}
		pedalInput = analogRead(pedalBrake);
		if (pedalInput < RELEASE_PEDAL_BRAKE)
		{
			// co nhan phanh
			speed = map(pedalInput, MAXPUSH_PEDAL_BRAKE, RELEASE_PEDAL_BRAKE, 0, speed);
		}
		}
		return speed;
	}
	int getIndicator()
	{
		byte state = indicator->getState();
		if (state != 0)
		{
		buzzer->buzz(100);
		}
		return state;
	}
	int getBuzzer()
	{
		return buzzer->getBtnState();
	}
	void refreshData()
	{
		getMode();
		getAngle();
		getSpeed();
		getIndicator();
		getBuzzer();
	}
	String toJSON()
	{
		return "{\"speed\":" + String(speed) + ",\"angle\":" + String(angle) + ",\"mode\":" + String(mode) +
		",\"led\":" + indicator->toString() + ",\"buzzer\":" + buzzer->toString() + "}";
	}
};





SoftwareSerial weMos = SoftwareSerial(RX, TX);

Controller *controller;
void setup()
{
	Serial.begin(115200);
	weMos.begin(9600);
	
	
	// Controller(byte wheel, byte pedalSpeed, byte pedalBrake, byte handBrake, 
	controller = new Controller(IN_WHEELING_PIN, IN_PEDALSPEED_PIN, IN_PEDALBRAKE_PIN, IN_HANDBRAKE_PIN,
		// byte gearL3, byte gearR3, byte btnParking, byte ledParking, 
		IN_GEARL3_PIN, IN_GEARR3_PIN, BTN_PARKING_PIN, LED_PARKING_PIN, 
		// byte btnLeft, byte btnRigh, byte ledLeft, byte ledRigh, 
		BTN_LEFT_PIN, BTN_RIGH_PIN, BTN_LEDLEFT_PIN, BTN_LEDRIGH_PIN,
		// byte btnBuzzer, byte pinBuzzer, byte ledBuzzer)
		BTN_BUZZER_PIN, OUT_BUZZER_PIN, LED_BUZZER_PIN);
}

void loop()
{
	controller->refreshData();
	String data = "01234567890123456789 " + controller->toJSON() + "@@@";
	Serial.println(data);
	weMos.print(data);
	delay(100);
}

