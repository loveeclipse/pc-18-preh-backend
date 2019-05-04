package core;

import database.MongoDBConnection;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RouterVerticle extends AbstractVerticle {

    private static final int PORT = 5151;
    private static final String URL = "/api/event";
    private MongoDBConnection mongoDBConnection;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post(URL).handler(this::handlerCreate);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT);
        System.out.println("Server successfully started on port: " + PORT);
    }

    private void handlerCreate(RoutingContext routingContext) {
        HttpServerResponse response =routingContext.response();
        JsonObject result = routingContext.getBodyAsJson();
        if(result == null) {
            System.out.println("Bad request");
            sendResponse(HttpResponseStatus.BAD_REQUEST, response);
        }
        else {
            System.out.println("OK");
            sendResponse(HttpResponseStatus.CREATED, response);
            mongoDBConnection.databaseConnection();
            mongoDBConnection.dbOperation(result);
            sendResponse(HttpResponseStatus.CREATED, response);
        }
    }

    private void sendResponse(HttpResponseStatus statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode.code()).end();
    }

    // initialize all roting service
    private void initializeRouter(Router router){
        router.route().handler(BodyHandler.create());
    }
}
