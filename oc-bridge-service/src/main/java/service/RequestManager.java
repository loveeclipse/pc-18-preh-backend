package service;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
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
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(201)
                        .end(Json.encodePrettily(document));
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
            JsonObject query = new JsonObject().put("_id", eventId);
            MongoClient.createNonShared(vertx, config).find("events", query, result -> {
                if(result.succeeded()){
                    try {
                        JsonObject resultJson = result.result().get(0);
                        resultJson.remove("_id");
                        if (resultJson.size() > 0)
                            response
                                    .putHeader("Content-Type", "application/json")
                                    .setStatusCode(200)
                                    .end(Json.encodePrettily(resultJson));
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
        JsonObject body = routingContext.getBodyAsJson();
        JsonObject query = new JsonObject().put("_id", routingContext.request().getParam("eventId"));
        JsonObject update = new JsonObject().put("$set", body);
        MongoClient.createNonShared(vertx, config).updateCollection("events", query, update, res -> {
            if (res.succeeded()) {
                if (res.result().getDocModified() == 0)
                    response.setStatusCode(400).end();
                else
                    response.setStatusCode(204).end();
            } else {
                response.setStatusCode(500).end();
            }
        });
    }
}
