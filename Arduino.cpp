#include <Arduino.h>
#include <LiquidCrystal.h>
#include <Keypad.h>

const int USERS = 2;
String user;
int id;
int noRecon = 0;
int acierto = 0;

const int rs = 7, en = 6, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

char customKey;

const byte ROWS = 4;
const byte COLS = 4;

const int contras[USERS] = { 1234, 4321};

char hexaKeys[ROWS][COLS] = {
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}
};

byte rowPins[ROWS] = {A0, A1, A2, A3};
byte colPins[COLS] = {11, 10, 9, 8};

Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

void introducePass();

void setup() {
  Serial.begin(115200);
  lcd.begin(16, 2);
  lcd.print("User: ");
  lcd.setCursor(0,1);
  lcd.print("Pass: ");
  Serial.print("Introduzca usuario: ");
}

void loop() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("User: ");
  lcd.setCursor(0,1);
  lcd.print("Pass: ");
  lcd.setCursor(6, 0);

  customKey = customKeypad.getKey(); // Identificador del usuario.
  id = int(customKey) - 48;
   if (customKey){
       if(customKey == '1' ){
       user = "Alberto";
       if(Serial.available()){
         Serial.write(id);
       }
       acierto = 1;
       noRecon = 0;
    //     Serial.print("Bienvenido " + user);
    //     Serial.print(id);
       } else if(customKey == '2'){
         user = "Simon";
         noRecon = 0;
         if(Serial.available()){
           Serial.write(id);
         }
         acierto = 1;
       } else{
         lcd.print("No recon.");
         delay(1000);
         noRecon=1;
       }
  Serial.print(customKey);
  lcd.print(user);

  if(!noRecon){
    introducePass();
    lcd.clear();
    lcd.setCursor(0, 0);
    if(acierto==1){
      lcd.print("CORRECTO :D");
    } else {
      lcd.print("INCORRECTO >:(");
    }
    delay(1500);
      }
//     Serial.println(customKey);
  }
   delay(100);
}

void introducePass(){
//  int i = 0; // posicion del digito en el array pass
  int k = 6; // posicion del lcd donde se imprime el digito
  int pass = 0;
  Serial.print("\nIntroduzca contraseña: ");
  while(pass<999){
    char ck = customKeypad.getKey();
    if(ck){
      int j = int(ck) - 48;
      Serial.print(j);
      pass = pass * 10 + j;
      lcd.setCursor(k, 1);
      k++;
      lcd.print("*");
      delay(200);
    }
  }
  if(Serial.available()){
    Serial.write(pass);
    //Serial.print("\nIntroduzca usuario: ");
  }


//  Serial.println(pass);
//  Serial.println(id);
}
