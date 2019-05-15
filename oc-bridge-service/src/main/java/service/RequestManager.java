package service;

import io.netty.handler.codec.http.HttpResponseStatus;
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
    private static final String DB_CONNECTION_STRING = "connection_string";
    private static final String DOCUMENT_ID = "_id";
    private static final String EVENT_ID = "eventId";
    private static final String COLLECTION_NAME = "events";
    private static final String DUPLICATED_KEY_CODE = "E11000";


    static void initializeRequestManager(Vertx vertx) {
        RequestManager.vertx = vertx;

        Yaml yaml = new Yaml();
        InputStream inputStream = OcBridge.class
                .getClassLoader()
                .getResourceAsStream("mongoConfig.yaml");
        Map<String, Object> obj = yaml.load(inputStream);
        config.put(DB_CONNECTION_STRING, obj.get(DB_CONNECTION_STRING));
    }

    static void handleCreateEvent(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String uuid = UUID.randomUUID().toString();
        JsonObject document = new JsonObject().put(DOCUMENT_ID, uuid);

        MongoClient.createNonShared(vertx, config).insert(COLLECTION_NAME, document, result -> {
            if (result.succeeded()) {
                response
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .end(Json.encodePrettily(document));
            } else if (isDuplicateKey(result.cause().getMessage())) {
                handleCreateEvent(routingContext);
            }
            else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
    }

    private static boolean isDuplicateKey(String errorMessage) {
        return errorMessage.startsWith(DUPLICATED_KEY_CODE);
    }

    static void handleGetEventById(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        String eventId = routingContext.request().getParam(EVENT_ID);
        try{
            UUID.fromString(eventId);
            JsonObject query = new JsonObject().put(DOCUMENT_ID, eventId);
            MongoClient.createNonShared(vertx, config).find(COLLECTION_NAME, query, result -> {
                if(result.succeeded()){
                    try {
                        JsonObject resultJson = result.result().get(0);
                        resultJson.remove(DOCUMENT_ID);
                        if (resultJson.size() > 0)
                            response
                                    .putHeader("Content-Type", "application/json")
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(resultJson));
                        else
                            response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                    } catch (IndexOutOfBoundsException ex) {
                        response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
                    }
                }else {
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            });
        } catch (IllegalArgumentException exception){
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
        }
    }

    static void handleUpdateEvent(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        JsonObject body = routingContext.getBodyAsJson();
        JsonObject query = new JsonObject().put(DOCUMENT_ID, routingContext.request().getParam(EVENT_ID));
        JsonObject update = new JsonObject().put("$set", body);
        MongoClient.createNonShared(vertx, config).updateCollection(COLLECTION_NAME, query, update, res -> {
            if (res.succeeded()) {
                if (res.result().getDocModified() == 0)
                    response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
                else
                    response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
    }
}
