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

		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

		mqttClient.connect(1883, "localhost", s -> {

			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");

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
           
           //A partir de aquí están todos los distintos mensajes que se pueden intercambiar en el canal
					System.out.println("Introduce tu ID de usuario: ");
					Integer user=scan.nextInt();
					System.out.println("Introduce el código correspondiente para realizar la acción que desees: ");
					Integer code=scan.nextInt();
					
					if (mqttClient.isConnected()) {
						if(code==0){
						mqttClient.publish("topic_2",
								Buffer.buffer(new JsonObject().put("action", "servo_on").put("idUsuario", user)
										.put("timestamp", Calendar.getInstance().getTimeInMillis())
										.put("clientId", mqttClient.clientId()).encode()),
								MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==1){
							mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "servo_off").put("idUsuario", user)
											.put("timestamp", Calendar.getInstance().getTimeInMillis())
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==2){
							mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "pir_manual_on")
											.put("timestamp", Calendar.getInstance().getTimeInMillis())
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}else if(code==3){
							mqttClient.publish("topic_2",
									Buffer.buffer(new JsonObject().put("action", "pir_manual_off")
											.put("timestamp", Calendar.getInstance().getTimeInMillis())
											.put("clientId", mqttClient.clientId()).encode()),
									MqttQoS.AT_LEAST_ONCE, false, false);
						}
					}
				}
			}, 1000, 3000);
		});
	}
  
  //Aquí van los demás métodos que vienen en el proyecto de Luismi los cuales, en principio, no hace falta modificar.
