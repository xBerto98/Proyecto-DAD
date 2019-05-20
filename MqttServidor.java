package hola;
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
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class MqttServidor extends AbstractVerticle{

	private static Multimap<String, MqttEndpoint> clientTopics;

	AsyncSQLClient mySQLClient;
	
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
							getVertx().eventBus().consumer("mensaje", message2 -> {
								String s = (String) message2.body();
								mqttClient.publish("topic_2", Buffer.buffer(s), MqttQoS.AT_LEAST_ONCE, false, false);
							});
							
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

	/**
	 * MÃ©todo encargado de inicializar el servidor y ajustar todos los manejadores
	 * 
	 * @param mqttServer
	 */
	private static void init(MqttServer mqttServer) {
		mqttServer.endpointHandler(endpoint -> {
			/*
			 * Si se ejecuta este cÃ³digo es que un cliente se ha suscrito al servidor MQTT
			 * para algÃºn topic.
			 */
			System.out.println("Nuevo cliente MQTT [" + endpoint.clientIdentifier()
					+ "] solicitando suscribirse [Id de sesiÃ³n: " + endpoint.isCleanSession() + "]");
			/*
			 * Indicamos al cliente que se ha contectado al servidor MQTT y que no tenÃ­a
			 * sesiÃ³n previamente creada (parÃ¡metro false)
			 */
			endpoint.accept(false);

			/*
			 * Handler para gestionar las suscripciones a un determinado topic. AquÃ­
			 * registraremos el cliente para poder reenviar todos los mensajes que se
			 * publicen en el topic al que se ha suscrito.
			 */
			handleSubscription(endpoint);

			/*
			 * Handler para gestionar las desuscripciones de un determinado topic. Haremos
			 * lo contrario que el punto anterior para eliminar al cliente de la lista de
			 * clientes registrados en el topic. De este modo, no seguirÃ¡ recibiendo
			 * mensajes en este topic.
			 */
			handleUnsubscription(endpoint);

			/*
			 * Este handler serÃ¡ llamado cuando se publique un mensaje por parte del cliente
			 * en algÃºn topic creado en el servidor MQTT. En esta funciÃ³n obtendremos todos
			 * los clientes suscritos a este topic y reenviaremos el mensaje a cada uno de
			 * ellos. Esta es la tarea principal del broken MQTT. En este caso hemos
			 * implementado un broker muy muy sencillo. Para gestionar QoS, asegurar la
			 * entrega, guardar los mensajes en una BBDD para despuÃ©s entregarlos, guardar
			 * los clientes en caso de caÃ­da del servidor, etc. debemos recurrir a un cÃ³digo
			 * mÃ¡s elaborado o usar una soluciÃ³n existente como por ejemplo Mosquitto.
			 */
			publishHandler(endpoint);

			/*
			 * Handler encargado de gestionar las desconexiones de los clientes al servidor.
			 * En este caso eliminaremos al cliente de todos los topics a los que estuviera
			 * suscrito.
			 */
			handleClientDisconnect(endpoint);
		}).listen(ar -> {
			if (ar.succeeded()) {
				System.out.println("MQTT server estÃ¡ a la escucha por el puerto " + ar.result().actualPort());
			} else {
				System.out.println("Error desplegando el MQTT server");
				ar.cause().printStackTrace();
			}
		});
	}

	/**
	 * MÃ©todo encargado de gestionar las suscripciones de los clientes a los
	 * diferentes topics. En este mÃ©todo se registrarÃ¡ el cliente asociado al topic
	 * al que se suscribe
	 * 
	 * @param endpoint
	 */
	private static void handleSubscription(MqttEndpoint endpoint) {
		endpoint.subscribeHandler(subscribe -> {
			// Los niveles de QoS permiten saber el tipo de entrega que se realizarÃ¡:
			// - AT_LEAST_ONCE: Se asegura que los mensajes llegan a los clientes, pero no
			// que se haga una Ãºnica vez (pueden llegar duplicados)
			// - EXACTLY_ONCE: Se asegura que los mensajes llegan a los clientes un Ãºnica
			// vez (mecanismo mÃ¡s costoso)
			// - AT_MOST_ONCE: No se asegura que el mensaje llegue al cliente, por lo que no
			// es necesario ACK por parte de Ã©ste
			List<MqttQoS> grantedQosLevels = new ArrayList<>();
			for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
				System.out.println("SuscripciÃ³n al topic " + s.topicName() + " con QoS " + s.qualityOfService());
				grantedQosLevels.add(s.qualityOfService());

				// AÃ±adimos al cliente en la lista de clientes suscritos al topic
				clientTopics.put(s.topicName(), endpoint);
			}

			/*
			 * Enviamos el ACK al cliente de que se ha suscrito al topic con los niveles de
			 * QoS indicados
			 */
			endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
		});
	}

	/**
	 * MÃ©todo encargado de eliminar la suscripciÃ³n de un cliente a un topic. En este
	 * mÃ©todo se eliminarÃ¡ al cliente de la lista de clientes suscritos a ese topic.
	 * 
	 * @param endpoint
	 */
	private static void handleUnsubscription(MqttEndpoint endpoint) {
		endpoint.unsubscribeHandler(unsubscribe -> {
			for (String t : unsubscribe.topics()) {
				// Eliminos al cliente de la lista de clientes suscritos al topic
				clientTopics.remove(t, endpoint);
				System.out.println("Eliminada la suscripciÃ³n del topic " + t);
			}
			// Informamos al cliente que la desuscripciÃ³n se ha realizado
			endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
		});
	}

	/**
	 * Manejador encargado de notificar y procesar la desconexiÃ³n de los clientes.
	 * 
	 * @param endpoint
	 */
	private static void handleClientDisconnect(MqttEndpoint endpoint) {
		endpoint.disconnectHandler(h -> {
			// Eliminamos al cliente de todos los topics a los que estaba suscritos
			Stream.of(clientTopics.keySet()).filter(e -> clientTopics.containsEntry(e, endpoint))
					.forEach(s -> clientTopics.remove(s, endpoint));
			System.out.println("El cliente remoto se ha desconectado [" + endpoint.clientIdentifier() + "]");
		});
	}

	/**
	 * Manejador encargado de interceptar los envÃ­os de mensajes de los diferentes
	 * clientes. Este mÃ©todo deberÃ¡ procesar el mensaje, identificar los clientes
	 * suscritos al topic donde se publica dicho mensaje y enviar el mensaje a cada
	 * uno de esos clientes.
	 * 
	 * @param endpoint
	 */
	private static void publishHandler(MqttEndpoint endpoint) {
		endpoint.publishHandler(message -> {
			/*
			 * Suscribimos un handler cuando se solicite una publicaciÃ³n de un mensaje en un
			 * topic
			 */
//			handleMessage(message, endpoint);
			handleMessageServo(message, endpoint);
		}).publishReleaseHandler(messageId -> {
			/*
			 * Suscribimos un handler cuando haya finalizado la publicaciÃ³n del mensaje en
			 * el topic
			 */
			endpoint.publishComplete(messageId);
		});
	}

	/**
	 * MÃ©todo de utilidad para la gestiÃ³n de los mensajes salientes.
	 * 
	 * @param message
	 * @param endpoint
	 */
	private static void handleMessage(MqttPublishMessage message, MqttEndpoint endpoint) {
		System.out.println("Mensaje publicado por el cliente " + endpoint.clientIdentifier() + " en el topic "
				+ message.topicName());
		System.out.println("    Contenido del mensaje: " + message.payload().toString());

		/*
		 * Obtenemos todos los clientes suscritos a ese topic (exceptuando el cliente
		 * que envÃ­a el mensaje) para asÃ­ poder reenviar el mensaje a cada uno de ellos.
		 * Es aquÃ­ donde nuestro cÃ³digo realiza las funciones de un broken MQTT
		 */
		System.out.println("Origen: " + endpoint.clientIdentifier());
		for (MqttEndpoint client : clientTopics.get(message.topicName())) {
			System.out.println("Destino: " + client.clientIdentifier());
			if (!client.clientIdentifier().equals(endpoint.clientIdentifier()))
				try {
					client.publish(message.topicName(), message.payload(), message.qosLevel(), message.isDup(),
							message.isRetain()).publishReleaseHandler(idHandler -> {
								client.publishComplete(idHandler);
							});
				} catch (Exception e) {
					System.out.println("Error, no se pudo enviar mensaje.");
				}
		}

		if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			String topicName = message.topicName();
			switch (topicName) {
			/*
			 * Se podrÃ­a hacer algo con el mensaje como, por ejemplo, almacenar un registro
			 * en la base de datos
			 */
			}
			// EnvÃ­a el ACK al cliente de que el mensaje ha sido publicado
			endpoint.publishAcknowledge(message.messageId());
		} else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
			/*
			 * EnvÃ­a el ACK al cliente de que el mensaje ha sido publicado y cierra el canal
			 * para este mensaje. AsÃ­ se evita que los mensajes se publiquen por duplicado
			 * (QoS)
			 */
			endpoint.publishRelease(message.messageId());
		}
	}
	
	private static void handleMessageServo(MqttPublishMessage message, MqttEndpoint endpoint) {
		System.out.println("Mensaje publicado por el cliente " + endpoint.clientIdentifier() + " en el topic "
				+ message.topicName());
		System.out.println("	Contenido del mensaje: " + message.payload().toString());

		System.out.println("Origen: " + endpoint.clientIdentifier());
		for (MqttEndpoint client : clientTopics.get(message.topicName())) {
			System.out.println("Destino: " + client.clientIdentifier());
			if (!client.clientIdentifier().equals(endpoint.clientIdentifier()))
				try {
					client.publish(message.topicName(), message.payload(), message.qosLevel(), message.isDup(),
							message.isRetain()).publishReleaseHandler(idHandler -> {
								client.publishComplete(idHandler);
							});
				} catch (Exception e) {
					System.out.println("Error, no se pudo enviar mensaje.");
				}
		}

		if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			String topicName = message.topicName();
			switch (topicName) {
			}
			endpoint.publishAcknowledge(message.messageId());
		} else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
			endpoint.publishRelease(message.messageId());
		}
	}

}
