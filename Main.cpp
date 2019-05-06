#include <Arduino.h>
#include <Servo.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

Servo servoMotor;
int servoValue;
bool manual;

int valPir=LOW;
int valFC=LOW;
int estadoPir=LOW;
int estadoFC=LOW;

int pinBuzzer=D2;
int pinFC=D5;
int pinServo=D4;
int pinPir=D7;

const char* ssid ="WI-NET 02"; //"MiFibra-0B3E";//"WI-NET 02"; //"Simon";
const char* password ="68674052"; //"rmoxrP9m";//"68674052";//"tusmuertos";
const char* channel_name = "topic_2";
const char* mqtt_server ="192.168.1.110"; //"192.168.1.59";
const char* http_server ="192.168.1.110"; //"192.168.1.59";
const char* http_server_port = "8090";
String clientId;

WiFiUDP ntpUDP;
WiFiClient espClient;
PubSubClient client(espClient);
/*long lastMsg = 0;
long lastMsgRest = 0;
char msg[50];
int value = 0;
*/
int idUsuario;
int idServo;
int idPir;
String nameServo;
int pass[10];
int dentro[10];
int sumaDentro=10;

NTPClient timeClient(ntpUDP,"3.es.pool.ntp.org",60*60,60*1000);

void setup_wifi() {

  delay(10);

  // Fijamos la semilla para la generación de número aleatorios. Nos hará falta
  // más adelante para generar ids de clientes aleatorios
  randomSeed(micros());

  Serial.println();
  Serial.print("Conectando a la red WiFi ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  // Mientras que no estemos conectados a la red, seguimos leyendo el estado
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  // En este punto el ESP se encontrará registro en la red WiFi indicada, por
  // lo que es posible obtener su dirección IP
  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.println("Dirección IP registrada: ");
  Serial.println(WiFi.localIP());
}

void makePutRequestServo(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/servos/";
    url += "/price";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);
    root["idUsuario"] = idUsuario;
    root["nombreServo"]=nameServo;
    root["tempServo"]=timeClient.getEpochTime()-3600;
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    http.end();
}

void makeGetRequestServo(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/servosId/";

    String message = "Enviando petición GET al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);
    int httpCode = http.GET();

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload: " + payload);

     const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
     DynamicJsonDocument root(bufferSize);
     deserializeJson(root, payload); //propiedades del Json por separado
     idServo = root["results"][0][0];
    }

    Serial.printf("\nRespuesta servidor REST %d\n", httpCode);
    http.end();
}

void makeGetRequestPIR(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/sensorespirId/";

    String message = "Enviando petición GET al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);
    int httpCode = http.GET();

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload: " + payload);

     const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
     DynamicJsonDocument root(bufferSize);
     deserializeJson(root, payload); //propiedades del Json por separado
     idPir = root["results"][0][0];
    }

    Serial.printf("\nRespuesta servidor REST %d\n", httpCode);
    http.end();
}

void makePutRequestFC(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/finalescarrera/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    root["idServo"] = idServo;
    if(servoValue==0){
      root["cerrado"]=1;
    }else if(servoValue==95){
      root["cerrado"]=0;
    }
    root["tempFC"]=timeClient.getEpochTime()-3600;
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    http.end();
}

void makePutRequestPir(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/sensorespir/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    root["nombrePIR"]="puerta principal";
    root["tempPir"]=timeClient.getEpochTime()-3600;
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    http.end();
}

void makePutRequestBuzzer(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/buzzers/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    root["idPIR"]=idPir;
    root["tempBuzz"]=timeClient.getEpochTime()-3600;
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    http.end();
}

void makeGetRequestUsersPass(){
  HTTPClient http;
  String url = "http://";
  url += http_server;
  url += ":";
  url += http_server_port;
  url += "/usuarios/";
  String message = "Enviando petición GET al servidor REST. ";
  message += url;
  Serial.println(message);
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode > 0)
  {
   String payload = http.getString();
   Serial.println("payload: " + payload);

   const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
   DynamicJsonDocument root(bufferSize);
   deserializeJson(root, payload); //propiedades del Json por separado

   for(int i=0;i<root["numRows"];i++){
     pass[i] = root["results"][i][(root["numColumns"].as<int>())-2];
   }
}
}

void makeGetRequestUsersDentro(){
  HTTPClient http;
  String url = "http://";
  url += http_server;
  url += ":";
  url += http_server_port;
  url += "/usuarios/";
  String message = "Enviando petición GET al servidor REST. ";
  message += url;
  Serial.println(message);
  http.begin(url);
  int httpCode = http.GET();

  if (httpCode > 0)
  {
   String payload = http.getString();
   Serial.println("payload: " + payload);

   const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
   DynamicJsonDocument root(bufferSize);
   deserializeJson(root, payload); //propiedades del Json por separado

   sumaDentro=root["numRows"];

   for(int i=0;i<root["numRows"];i++){
     dentro[i] = root["results"][i][(root["numColumns"].as<int>())-1];
     if(dentro[i]==0)
      sumaDentro--;
   }
   }
}

void makePutRequestUpdateUsers(){
    HTTPClient http;
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/updateUsuarios/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    root["idUsuario"]=idUsuario;
    root["dentro"]=dentro[idUsuario-1];
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    http.end();
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Mensaje recibido [canal: ");
  Serial.print(topic);
  Serial.print("] ");

  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();


  DynamicJsonDocument doc(length);
  deserializeJson(doc, payload, length);
  const char* action = doc["action"];
  idUsuario = doc["idUsuario"];
  nameServo = doc["name"].as<String>();
  Serial.printf("Acción %s\n", action);

  if (strcmp(action, "servo_on") == 0) {
    if(servoValue==0){
    servoValue=95;
    servoMotor.write(servoValue);
    delay(1000);
    makePutRequestServo();
    delay(100);
    valFC=digitalRead(pinFC);
    //if(valFC==LOW){
      //if(estadoFC==LOW){
      makeGetRequestServo();
      delay(100);
      makePutRequestFC();
      delay(100);
      estadoFC=HIGH;
      //}
    //}
    Serial.println("Detectada acción de apertura de puerta");
  }else{
    Serial.println("La puerta ya estaba abierta");
  }
  } else if (strcmp(action, "servo_off") == 0) {
    if(servoValue==95){
    servoValue=0;
    servoMotor.write(servoValue);
    delay(1000);
    makePutRequestServo();
    delay(100);
    valFC=digitalRead(pinFC);
    //if(valFC==HIGH){
    //if(estadoFC==HIGH){
      makeGetRequestServo();
      delay(100);
      makePutRequestFC();
      delay(100);
      estadoFC=LOW;
      //}
    //}
    Serial.println("Detectada acción de cierre de puerta");
  }else{
    Serial.println("La puerta ya estaba cerrada");
  }
  }else if(strcmp(action, "pir_manual_on") == 0){
    manual=true;
    Serial.println("Detecada acción de activación manual del sensor PIR");
  }else if(strcmp(action, "pir_manual_off") == 0){
    manual=false;
    Serial.println("Detecada acción de desactivación manual del sensor PIR");
  }else{
    Serial.println("Acción no reconocida");
  }
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Conectando al servidor MQTT...");

    clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);

    if (client.connect(clientId.c_str())) {
      String printLine = "   Cliente " + clientId + " conectado al servidor " + mqtt_server;
      Serial.println(printLine);

      String body = "Dispositivo con ID = ";
      body += clientId;
      body += " conectado al canal ";
      body += channel_name;
      client.publish(channel_name, "");

      client.subscribe(channel_name);
    } else {
      Serial.print("Error al conectar al canal, rc=");
      Serial.print(client.state());
      Serial.println(". Intentando de nuevo en 5 segundos.");
      delay(5000);
    }
  }
}

void setup() {
  pinMode(pinPir,INPUT);
  pinMode(pinFC,INPUT);
  pinMode(pinBuzzer,OUTPUT);
  servoMotor.attach(pinServo);

  servoMotor.write(0);

  Serial.begin(115200);

  setup_wifi();
  timeClient.begin();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void loop() {
  timeClient.update();
  delay(100);

  if (!client.connected()) {
    reconnect();
  }

  client.loop();

  /*reconocimiento de usuarios
    primera tecla marca idUsuario
    guardamos en variable idUsuario
    tecleamos contraseña
    si acierta contraseña -> servoMotor.write(95),putTN(acierto),getUsers(variable "dentro"),updateUsers(!dentro)
    si falla contraseña -> putTN(falla,contador+1)
  */

  valPir=digitalRead(pinPir);
  if((valPir==HIGH && manual==true) || (valPir==HIGH && sumaDentro==0)){
    digitalWrite(pinBuzzer,HIGH);
    if(estadoPir==LOW){
      makePutRequestPir();
      delay(100);
      makeGetRequestPIR();
      delay(100);
      makePutRequestBuzzer();
      estadoPir=HIGH;
    }
  }else{
    digitalWrite(pinBuzzer,LOW);
    if(estadoPir==HIGH)
      estadoPir=LOW;
  }

  // Cada 2 segundos publicaremos un mensaje en el canal procedente del cliente
  // actual. Esto se hace sin bloquear el loop ya que de lo contrario afectarí­a
  // a la recepción de los mensajes MQTT
  /*long now = millis();
  if (now - lastMsg > 2000) {
    lastMsg = now;
    ++value;
    // Construimos un objeto JSON con el contenido del mensaje a publicar
    // en el canal.
    StaticJsonDocument<200> doc;
    doc["clientId"] = clientId;
    doc["message"] = "periodic message";
    doc["number"] = value;
    String output;
    serializeJson(doc, output);
    Serial.print("Mensaje publicado: ");
    Serial.println(output);
    client.publish(channel_name, output.c_str());
  }
  */
}
