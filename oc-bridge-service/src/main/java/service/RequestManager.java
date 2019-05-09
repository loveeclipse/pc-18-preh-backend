package service;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;

class RequestManager {
    private static Vertx vertx;
    private static final JsonObject CONFIG = new JsonObject()
            .put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr");

    static void setVertx(Vertx vertx) {
        RequestManager.vertx = vertx;
    }

    static void handleCreateEvent(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String uuid = UUID.randomUUID().toString();
        JsonObject document = new JsonObject().put("_id", uuid);

        MongoClient.createNonShared(vertx, CONFIG).insert("events", document, result -> {
            if (result.succeeded()) {
                response
                        .putHeader("eventId", uuid)
                        .setStatusCode(200)
                        .end();
            } else if (isDuplicateKey(result.cause().getMessage())) {
                handleCreateEvent(routingContext);
            }
            else {
                response.setStatusCode(500).end();
            }
        });
    }

    private static boolean isDuplicateKey(String errorMessage) {
        return errorMessage.startsWith("E11000");
    }

    static void handleGetEventById(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        String eventId = routingContext.request().getParam("eventId");
        JsonObject queryResult = new JsonObject().put("_id", eventId);

        MongoClient.createNonShared(vertx, CONFIG).find("events", queryResult, result -> {
            if(result.succeeded()){
                try {
                    JsonObject resultJson = result.result().get(0);
                    resultJson.put("eventId", resultJson.remove("_id"));
                    response
                            .putHeader("eventInformation", resultJson.toString())
                            .setStatusCode(200)
                            .end();
                } catch (IndexOutOfBoundsException ex) {    // nessuna risorsa trovata o parametro errato?
                    response.setStatusCode(400).end();
                }
            }else {
                response.setStatusCode(500).end();
            }
        });
    }

    static void handleUpdateEvent(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        MultiMap params = routingContext.request().params();
        JsonObject query = new JsonObject().put("_id", params.get("eventId"));
        System.out.println(query);
        JsonObject document = new JsonObject();
        try {
            params.forEach(entry -> {
                if (entry.getKey().equals("secondary")) {
                    document.put(entry.getKey(), parseBoolean(entry.getValue()));
                } else
                    document.put(entry.getKey(), entry.getValue());
            });
            JsonObject update = new JsonObject().put("$set", document);
            MongoClient.createNonShared(vertx, CONFIG).updateCollection("events", query, update, res -> {
                if (res.succeeded()) {
                    response.setStatusCode(200).end();
                } else {
                    response.setStatusCode(500).end();
                }
            });
        } catch (IllegalArgumentException ex) {
            response.setStatusCode(400).end();
        }
    }

    private static boolean parseBoolean(String string) throws IllegalArgumentException {
        if (string.equalsIgnoreCase("true"))
            return true;
        else if (string.equalsIgnoreCase("false"))
            return false;
        else
            throw new IllegalArgumentException();
    }
}
