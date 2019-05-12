package service;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class VtVerticle {

    private static final Logger log = LoggerFactory.getLogger(VtVerticle.class);

    private static final String DB_DOCUMENT_NAME = "events";
    // TODO: guadare confgurazione file e import
    private static final JsonObject MONGODB_CONFIGURATION = new JsonObject()
            .put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr");


    private static Vertx vertx;

    static void setVertx(Vertx vertx){
        VtVerticle.vertx = vertx;
    }

    static void handlerGetAllEvent(RoutingContext routingContext) {
        log.info("Get all tracking details by event-id");
        HttpServerResponse response = getResponse(routingContext);
        String eventId = getRequest(routingContext).getParam("eventId");
       try {
           // TODO: crea un udd partendo da una stringa... ma perchè qui?
           UUID.fromString(eventId);
           if (eventId.isEmpty()) {
               // TODO: chiedere con altri, se concordi da aggiungere in swagger.
               sendResponse(HttpResponseStatus.UNPROCESSABLE_ENTITY, response);
           } else {
               JsonObject entries = new JsonObject().put("_id", eventId);
               MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                       .find(DB_DOCUMENT_NAME, entries, result -> {
                           log.info("Mongodb connected!");
                           if(result.succeeded()){
                               try{
                                   JsonObject query = result.result().get(0);
                                   query.remove("_id");
                                   if(!query.isEmpty()){
                                       response.putHeader("trackingDetails", query.toString());
                                       sendResponse(HttpResponseStatus.OK, response);
                                   }
                                   else sendResponse(HttpResponseStatus.NOT_FOUND, response);
                               } catch (Exception e) {
                                   sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, response);
                               }
                           }
                       });
           }
       } catch (Exception e) {
           log.info("BAD REQUEST");
           sendResponse(HttpResponseStatus.BAD_REQUEST, response);
       }
    }

    static void handlerPostOcCall(RoutingContext routingContext) {
        log.info("Get oc-call tracking details by event-id and (eventually) mission-id");
        HttpServerResponse response = getResponse(routingContext);
        String eventId = getRequest(routingContext).getParam("eventId");
        String missionId = getRequest(routingContext).getParam("missionId");
    }


    private static void sendResponse(HttpResponseStatus statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode.code()).end();
    }

    private static HttpServerResponse getResponse(RoutingContext routingContext) {
        return routingContext.response();
    }

    private static HttpServerRequest getRequest(RoutingContext routingContext) {
        return routingContext.request();
    }

    private JsonObject getRequestAndBody(RoutingContext routingContext) {
        return routingContext.getBodyAsJson();
    }
}
