import java.util.*;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class MqttServidor extends AbstractVerticle{

	private static Multimap<String, MqttEndpoint> clientTopics;

	public void start(Future<Void> startFuture) {
		clientTopics = HashMultimap.create();
		// Configuramos el servidor MQTT
		MqttServer mqttServer = MqttServer.create(vertx);
		init(mqttServer);

		// Creamos un cliente de prueba para MQTT que publica mensajes cada 3 segundos
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

		/*
		 * Nos conectamos al servidor que está desplegado por el puerto 1883 en la
		 * propia máquina. Recordad que localhost debe ser sustituido por la IP de
		 * vuestro servidor. Esta IP puede cambiar cuando os desconectáis de la red, por
		 * lo que aseguraros siempre antes de lanzar el cliente que la IP es correcta.
		 */
		mqttClient.connect(1883, "localhost", s -> {

			/*
			 * Nos suscribimos al topic_2. Aquí debera indicar el nombre del topic al que os
			 * queréis suscribir. Además, podéis indicar el QoS, en este caso AT_LEAST_ONCE
			 * para asegurarnos de que el mensaje llega a su destinatario.
			 */
			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					/*
					 * En este punto el cliente ya está suscrito al servidor, puesto que se ha
					 * ejecutado la función de handler
					 */
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");

					/*
					 * Además de suscribirnos al servidor, registraremos un manejador para
					 * interceptar los mensajes que lleguen a nuestro cliente. De manera que el
					 * proceso sería el siguiente: El cliente externo envía un mensaje al servidor
					 * -> el servidor lo recibe y busca los clientes suscritos al topic -> el
					 * servidor reenvía el mensaje a esos clientes -> los clientes (en este caso el
					 * cliente actual) recibe el mensaje y lo procesa si fuera necesario.
					 */
					mqttClient.publishHandler(new Handler<MqttPublishMessage>() {
						@Override
						public void handle(MqttPublishMessage arg0) {
							/*
							 * Si se ejecuta este código es que el cliente 2 ha recibido un mensaje
							 * publicado en algún topic al que estaba suscrito (en este caso, al topic_2).
							 */
							JsonObject message = new JsonObject(arg0.payload());
							System.out.println("-----" + message.getString("clientId"));
							System.out.println("-----" + mqttClient.clientId());
							if (!message.getString("clientId").equals(mqttClient.clientId()))
								System.out.println("Mensaje recibido por el cliente: " + arg0.payload().toString());
						}
					});
				}
			});

			/*
			 * Este timer COMPRUEBA cada 3 segundos si queremos enviar algún mensaje. En tal caso, lo envía.
			 */
			Scanner scan = new Scanner(System.in);
			
			new Timer().scheduleAtFixedRate(new TimerTask() {

				public void run() {
					
					 //Publicamos un mensaje en el topic "topic_2"
					Integer user;
					String nombre;
					System.out.println("Introduce el código correspondiente para realizar la acción que desees: ");
					Integer code=scan.nextInt();
					if (mqttClient.isConnected()) {
						if(code==0){
						System.out.println("Introduce tu ID de usuario: ");
						user=scan.nextInt();
						scan.nextLine();
						System.out.println("Introduce el lugar donde quieres actuar: ");
						nombre=scan.nextLine();
						mqttClient.publish("topic_2",
								Buffer.buffer(new JsonObject().put("action", "servo_on")
										.put("idUsuario", user)
										.put("name", nombre)
										.put("clientId", mqttClient.clientId()).encode()),
								MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==1){
						System.out.println("Introduce tu ID de usuario: ");
						user=scan.nextInt();
						scan.nextLine();
						System.out.println("Introduce el lugar donde quieres actuar: ");
						nombre=scan.nextLine();
						mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "servo_off")
											.put("idUsuario", user)
											.put("name", nombre)
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==2){
							mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "pir_manual_on")
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==3){
							mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "pir_manual_off")
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}
					}
				}
			}, 1000, 3000);
		});
	}
  
  //Aquí van los demás métodos que vienen en el proyecto de Luismi los cuales, en principio, no hace falta modificar.
