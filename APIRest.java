import java.util.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle{
	
private AsyncSQLClient mySQLClient;
	
	public void start(Future<Void> startFuture) {
		JsonObject config = new JsonObject()
				.put("host", "localhost")
				.put("username", "root")
				.put("password", "root")
				.put("database", "dad")
				.put("port", 3306);
		mySQLClient = 
				MySQLClient.createShared(vertx, config);
		
		Router router = Router.router(vertx);
		vertx.createHttpServer().requestHandler(router).
			listen(8090, result -> {
				if (result.succeeded()) {
					System.out.println("Servidor database desplegado");
				}else {
					System.out.println("Error de despliegue");
				}
			});
		
		router.route().handler(BodyHandler.create());
		router.get("/sensorespir").handler(this::handleAllPIR);
		router.get("/finalescarrera").handler(this::handleAllFC);
		router.get("/usuarios").handler(this::handleAllUsers);
		router.get("/servos").handler(this::handleAllServos);
		router.get("/buzzers").handler(this::handleAllBuzzers);
		router.get("/tecladosnumericos").handler(this::handleAllTNv2);
		router.put("/sensorespir").handler(this::handlePutSensorPir);
		router.put("/finalescarrera").handler(this::handlePutFC);
		router.put("/buzzers").handler(this::handlePutBuzzer);
		router.put("/servos").handler(this::handlePutServos);
		router.put("/tecladosnumericos").handler(this::handlePutTN);
	}
	
	private void handleAllPIR(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idPIR,nombrePIR,tempPir FROM sensorespir " , result -> {
					if (result.succeeded()) {
						for(int i=0; i<result.result().getNumRows(); i++){
						Long fecha=result.result().getRows().get(i).getLong("tempPir");
						result.result().getRows().get(i).put("tempPir",getFecha(fecha).toString());
						}
						String jsonResult = result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleAllFC(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idFC,nombreFC,cerrado,tempFC FROM finalescarrera" , result -> {
					if (result.succeeded()) {
						String jsonResult=result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleAllUsers(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idUsuario,nombre,passUsuario,dentro FROM usuarios" , result -> {
					if (result.succeeded()) {
						String jsonResult = result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleAllBuzzers(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idPIR,tempBuzz FROM buzzers" , result -> {
					if (result.succeeded()) {
						String jsonResult=result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleAllTNv2(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if(connection.succeeded()) {
				connection.result().query("SELECT idUsuario, cont, acierto,tempTN FROM dad.tecladosnumericos "
							, result -> {
					if(result.succeeded()) {
						String jsonResult=result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					} else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			} else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handleAllServos(RoutingContext routingContext){
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idFC,idUsuario,nombreServo,tempServo FROM servos" , result -> {
					if (result.succeeded()) {
						String jsonResult=result.result().toJson().encodePrettily();
						routingContext.response().end(jsonResult);
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});
	}
	
	private void handlePutSensorPir(RoutingContext routingContext){
		JsonObject body=routingContext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO sensorespir (nombrePIR,tempPIR) "
						+ "VALUES (\"" + body.getString("nombrePIR") + "\"," + body.getLong("tempPir") + ");", result -> {
					if (result.succeeded()) {
						routingContext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});	
	}

	private void handlePutFC(RoutingContext routingContext){
		JsonObject body=routingContext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO finalescarrera (nombreFC,cerrado,tempFC) "
						+ "VALUES (\"" + body.getString("nombreFC") + "\"," + body.getInteger("cerrado") + "," +
									body.getLong("tempFC") + ");", result -> {
					if (result.succeeded()) {
						routingContext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});	
	}
	
	private void handlePutBuzzer(RoutingContext routingContext){
		JsonObject body=routingContext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO buzzers (idPIR,tempBuzz) "
						+ "VALUES (" + body.getInteger("idPIR") + "," + 
									body.getLong("tempBuzz") + ");", result -> {
					if (result.succeeded()) {
						routingContext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});	
	}

	private void handlePutServos(RoutingContext routingContext){
		JsonObject body=routingContext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO servos (idFC,idUsuario,nombreServo,tempServo) "
						+ "VALUES (" + body.getInteger("idFC") + "," + body.getInteger("idUsuario") +
						",\"" + body.getString("nombreServo") + "\"," 
						+ body.getLong("tempServo") +");", result -> {
					if (result.succeeded()) {
						routingContext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});	
	}
	
	private void handlePutTN(RoutingContext routingContext){
		JsonObject body=routingContext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO tecladosnumericos (passTN,tempTN,idUsuario,cont,acierto) "
						+ "VALUES (\"" + body.getString("passTN") + "\"," + body.getLong("tempTN") + "," + 
									body.getInteger("idUsuario") + "," + body.getInteger("cont") + 
									"," + body.getInteger("acierto") + ");", result -> {
					if (result.succeeded()) {
						routingContext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingContext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingContext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingContext.response().setStatusCode(400).end();
			}
		});	
	}
	
	public Date getFecha(Long fecha){
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(fecha*1000);
		return calendar.getTime();
		
	}

}
