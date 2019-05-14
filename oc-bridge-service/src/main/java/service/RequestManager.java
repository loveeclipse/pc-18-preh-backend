package service;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

class RequestManager {
    private static Vertx vertx;
    private static JsonObject config = new JsonObject();

    static void initializeRequestManager(Vertx vertx) {
        RequestManager.vertx = vertx;

        Yaml yaml = new Yaml();
        InputStream inputStream = OcBridge.class
                .getClassLoader()
                .getResourceAsStream("mongoConfig.yaml");
        Map<String, Object> obj = yaml.load(inputStream);
        config.put("connection_string", obj.get("connection_string"));
    }

    static void handleCreateEvent(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String uuid = UUID.randomUUID().toString();
        JsonObject document = new JsonObject().put("_id", uuid);

        MongoClient.createNonShared(vertx, config).insert("events", document, result -> {
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
        try{
            UUID.fromString(eventId);
        JsonObject queryResult = new JsonObject().put("_id", eventId);
        MongoClient.createNonShared(vertx, config).find("events", queryResult, result -> {
            if(result.succeeded()){
                try {
                    JsonObject resultJson = result.result().get(0);
                    resultJson.remove("_id");
                    if (resultJson.size() > 0)
                        response
                                .putHeader("eventInformation", resultJson.toString())
                                .setStatusCode(200)
                                .end();
                    else
                        response.setStatusCode(204).end();
                } catch (IndexOutOfBoundsException ex) {
                    response.setStatusCode(404).end();
                }
            }else {
                response.setStatusCode(500).end();
            }
        });
    } catch (IllegalArgumentException exception){
            response.setStatusCode(400).end();
    }
    }

    static void handleUpdateEvent(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        MultiMap params = routingContext.request().params();
        JsonObject query = new JsonObject().put("_id", params.get("eventId"));
        params.remove("eventId");
        JsonObject document = new JsonObject();
        try {
            params.forEach(entry -> {
                if (entry.getKey().equals("secondary")) {
                    document.put(entry.getKey(), parseBoolean(entry.getValue()));
                } else
                    document.put(entry.getKey(), entry.getValue());
            });
            JsonObject update = new JsonObject().put("$set", document);
            MongoClient.createNonShared(vertx, config).updateCollection("events", query, update, res -> {
                if (res.succeeded()) {
                    if (res.result().getDocModified() == 0)
                        response.setStatusCode(400).end();
                    else
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
