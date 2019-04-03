public void handlePutSensorPir(RoutingContext routingConext){
		JsonObject body=routingConext.getBodyAsJson();
		mySQLClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("INSERT INTO sensorespir nombrePIR,tempPIR "
						+ "VALUES (\"" + body.getString("nombrePIR") + "\"," + body.getInteger("tempPIR") + ");", result -> {
					if (result.succeeded()) {
						routingConext.response()
						.putHeader("content-type", "application/json")
						.end(body.encode());
					}else {
						System.out.println(result.cause().getMessage());
						routingConext.response().setStatusCode(400).end();
					}
					connection.result().close();
				});
			}else {
				routingConext.response().end();
				connection.result().close();
				System.out.println(connection.cause().getMessage());
				routingConext.response().setStatusCode(400).end();
			}
		});	
	}
