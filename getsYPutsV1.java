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
	}
	
	private void handleAllPIR(RoutingContext routingContext) {
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT idPIR,nombrePIR,tempPIR FROM sensorespir " , result -> {
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
				connection.result().query("SELECT idPIR,nombreBuzz,tempBuzz FROM buzzers" , result -> {
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
				LocalDateTime ldt = LocalDateTime.now();
				String fin = transfFecha(ldt);
				String ini = formatFecha(ldt);
				connection.result().query("SELECT idUsuarioTN, cont, acierto FROM dad.tecladosnumericos "
						+ "WHERE tempTN <= '" + ini + "' AND tempTN >= '" + fin + "';", result -> {
					if(result.succeeded()) {
						String jsonResult=result.result().toJson().encodePrettily();
						ResultSet rs = result.result();
						for (int i = 0; i < rs.getNumRows(); i++) {
							int user = rs.getResults().get(i).getInteger(0);
							int cont = rs.getResults().get(i).getInteger(1);
							int acierto = rs.getResults().get(i).getInteger(2);
							if(cont==3 && acierto==0) {
								System.out.println("Usted usuario " + user + " no puede entrar en el keli hasta dentro de un rato");
							}
						}
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
						+ "VALUES (\"" + body.getString("nombrePIR") + "\"," + body.getInteger("tempPIR") + ");", result -> {
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
									body.getInteger("tempFC") + ");", result -> {
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
				connection.result().query("INSERT INTO buzzers (idPIR,nombreBuzz,tempBuzz) "
						+ "VALUES (" + body.getInteger("idPIR") + ",\"" + body.getString("nombreBuzz") + "\"," + 
									body.getInteger("tempBuzz") + ");", result -> {
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
						+ body.getInteger("tempServo") +");", result -> {
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
	
	private String transfFecha(LocalDateTime ldt) {
		String res;
		ldt = ldt.minusMinutes(5);
		res=ldt.toString();
		return res;
	}
	
	private String formatFecha(LocalDateTime ldt) {
