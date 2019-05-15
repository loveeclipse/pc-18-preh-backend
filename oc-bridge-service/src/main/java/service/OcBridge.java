package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class OcBridge extends AbstractVerticle {
    private static final int PORT = 10000;
    private static final String HOST = "localhost";

    @Override
    public void start() {
        vertx
                .createHttpServer()
                .requestHandler(createRouter())
                .listen(PORT, HOST);
        System.out.println("Service ready.");
    }

    private Router createRouter() {
        RequestManager.initializeRequestManager(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/v1/events").handler(RequestManager::handleCreateEvent);
        router.get("/v1/events/:eventId").handler(RequestManager::handleGetEventById);
        router.patch("/v1/events/:eventId").handler(RequestManager::handleUpdateEvent);
        return router;
    }
}
