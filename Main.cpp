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
int estadoPir=LOW;
//int auto;

int pinBuzzer=D2;
int pinServo=D4;
int pinPir=D7;
// Update these with values suitable for your network.

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
long lastMsg = 0;
long lastMsgRest = 0;
char msg[50];
int value = 0;

int idUsuario;
int idServo;

NTPClient timeClient(ntpUDP,"3.es.pool.ntp.org",60*60,60*1000);

// Conexión a la red WiFi
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
    // Abrimos la conexión con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/servos/";
  /*  url += "41231";
    url += "/price";
    */
    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    // Realizamos la petición y obtenemos el código de estado de la respuesta
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);
    root["idUsuario"] = idUsuario;
    root["nombreServo"]="puerta principal";
    root["tempServo"]=timeClient.getEpochTime()-3600;
    String json_string;
    serializeJson(root, json_string);

    int httpCode = http.PUT(json_string);

    if (httpCode > 0)
    {
     // Si el código devuelto es > 0, significa que tenemos respuesta, aunque
     // no necesariamente va a ser positivo (podrí­a ser un código 400).
     // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    // Cerramos la conexión con el servidor REST
    http.end();
}

void makeGetRequestServo(){
    HTTPClient http;
    // Abrimos la conexión con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/servosId/";
    String message = "Enviando petición GET al servidor REST. ";
    message += url;
    Serial.println(message);
    http.begin(url);
    // Realizamos la petición y obtenemos el código de estado de la respuesta
    int httpCode = http.GET();

    if (httpCode > 0)
    {
     // Si el código devuelto es > 0, significa que tenemos respuesta, aunque
     // no necesariamente va a ser positivo (podría ser un código 400).
     // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie
     String payload = http.getString();
     Serial.println("payload: " + payload);

     const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
     DynamicJsonDocument root(bufferSize);
     deserializeJson(root, payload); //propiedades del Json por separado
     idServo = root["results"][0][0];
    }

    Serial.printf("\nRespuesta servidor REST %d\n", httpCode);
    // Cerramos la conexión con el servidor REST
    http.end();
}

void makePutRequestFC(){
    HTTPClient http;
    // Abrimos la conexión con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/finalescarrera/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    // Realizamos la petición y obtenemos el código de estado de la respuesta
    http.begin(url);

    const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
    DynamicJsonDocument root(bufferSize);

    root["idServo"] = idServo;
    root["nombreFC"]="puerta principal";
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
     // Si el código devuelto es > 0, significa que tenemos respuesta, aunque
     // no necesariamente va a ser positivo (podrí­a ser un código 400).
     // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    // Cerramos la conexión con el servidor REST
    http.end();
}

void makePutRequestPir(){
    HTTPClient http;
    // Abrimos la conexión con el servidor REST y definimos la URL del recurso
    String url = "http://";
    url += http_server;
    url += ":";
    url += http_server_port;
    url += "/sensorespir/";

    String message = "Enviando petición PUT al servidor REST. ";
    message += url;
    Serial.println(message);
    // Realizamos la petición y obtenemos el código de estado de la respuesta
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
     // Si el código devuelto es > 0, significa que tenemos respuesta, aunque
     // no necesariamente va a ser positivo (podrí­a ser un código 400).
     // Obtenemos el cuerpo de la respuesta y lo imprimimos por el puerto serie
     String payload = http.getString();
     Serial.println("payload put: " + payload);
    }

    Serial.printf("\nRespuesta servidor REST PUT %d\n", httpCode);
    // Cerramos la conexión con el servidor REST
    http.end();
}

// Método llamado por el cliente MQTT cuando se recibe un mensaje en un canal
// al que se encuentra suscrito. Los parámetros indican el canal (topic),
// el contenido del mensaje (payload) y su tamaño en bytes (length)
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Mensaje recibido [canal: ");
  Serial.print(topic);
  Serial.print("] ");
  // Leemos la información del cuerpo del mensaje. Para ello no solo necesitamos
  // el puntero al mensaje, si no su tamaño.
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();


  DynamicJsonDocument doc(length);
  deserializeJson(doc, payload, length);
  const char* action = doc["action"];
  idUsuario = doc["idUsuario"];
  Serial.printf("Acción %s\n", action);
  // Encendemos un posible switch digital (un diodo led por ejemplo) si el
  // contenido del cuerpo es 'on'
  if (strcmp(action, "servo_on") == 0) {
    servoValue=95;
    servoMotor.write(servoValue);
    delay(1000);
    makePutRequestServo();
    delay(100);
    makeGetRequestServo();
    delay(100);
    makePutRequestFC();
    delay(100);
    Serial.println("Detectada acción de apertura de puerta");
  } else if (strcmp(action, "servo_off") == 0) {
    servoValue=0;
    servoMotor.write(servoValue);
    delay(1000);
    makePutRequestServo();
    delay(100);
    makeGetRequestServo();
    delay(100);
    makePutRequestFC();
    delay(100);
    Serial.println("Detectada acción de cierre de puerta");
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

// Función para la reconexión con el servidor MQTT y la suscripción al canal
// necesario. También se fija el identificador del cliente
void reconnect() {
  // Esperamos a que el cliente se conecte al servidor
  while (!client.connected()) {
    Serial.print("Conectando al servidor MQTT...");
    // Creamos un identificador de cliente aleatorio. Cuidado, esto debe
    // estar previamente definido en un entorno real, ya que debemos
    // identificar al cliente de manera uní­voca en la mayorí­a de las ocasiones
    clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Intentamos la conexión del cliente
    if (client.connect(clientId.c_str())) {
      String printLine = "   Cliente " + clientId + " conectado al servidor " + mqtt_server;
      Serial.println(printLine);
      // Publicamos un mensaje en el canal indicando que el cliente se ha
      // conectado. Esto avisará al resto de clientes que hay un nuevo
      // dispositivo conectado al canal. Puede ser interesante en algunos casos.
      String body = "Dispositivo con ID = ";
      body += clientId;
      body += " conectado al canal ";
      body += channel_name;
      client.publish(channel_name, "");
      // Y, por último, suscribimos el cliente al canal para que pueda
      // recibir los mensajes publicados por otros dispositivos suscritos.
      client.subscribe(channel_name);
    } else {
      Serial.print("Error al conectar al canal, rc=");
      Serial.print(client.state());
      Serial.println(". Intentando de nuevo en 5 segundos.");
      delay(5000);
    }
  }
}

// Método de inicialización de la lógica
void setup() {
  // Ajustamos el pinmode del pin de salida para poder controlar un
  // switch digial (dido led por ejemplo)
  //pinMode(BUILTIN_LED, OUTPUT);
  pinMode(pinPir,INPUT);
  pinMode(pinBuzzer,OUTPUT);
  servoMotor.attach(pinServo);
  servoMotor.write(0);
  // Fijamos el baudrate del puerto de comunicación serie
  Serial.begin(115200);
  // Nos conectamos a la red WiFi
  setup_wifi();
  timeClient.begin();
  // Indicamos la dirección y el puerto del servidor donde se encuentra el
  // servidor MQTT
  client.setServer(mqtt_server, 1883);
  // Fijamos la función de callback que se ejecutará cada vez que se publique
  // un mensaje por parte de otro dispositivo en un canal al que el cliente
  // actual se encuentre suscrito
  client.setCallback(callback);
}

void loop() {
  timeClient.update();
  delay(100);
  // Nos conectamos al servidor MQTT en caso de no estar conectado previamente
  if (!client.connected()) {
    reconnect();
  }
  // Esperamos (de manera figurada) a que algún cliente suscrito al canal
  // publique un mensaje que será recibido por el dispositivo actual
  client.loop();

  int valPir=digitalRead(pinPir);

  //makeGetRequestUpdateTN();
  if((valPir==HIGH && manual==true)/* || (valPir==HIGH && )*/){
    digitalWrite(pinBuzzer,HIGH);
    if(estadoPir==LOW){
      makePutRequestPir();
      //delay(100);
      //makeGetRequestBuzzer();
      //delay(100);
      //makePutRequestBuzzer();
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
  long now = millis();
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
}
