package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class OcBridge extends AbstractVerticle {
    private static final int PORT = 10000;
    private static final String HOST = "localhost";

    @Override
    public void start() {
        startServer(createRouter());
    }

    private Router createRouter() {
        RequestManager.initializeRequestManager(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/v1/event").handler(RequestManager::handleCreateEvent);
        router.get("/v1/event/:eventId").handler(RequestManager::handleGetEventById);
        router.patch("/v1/event/:eventId").handler(RequestManager::handleUpdateEvent);
        return router;
    }

    private void startServer(Router router) {
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT, HOST);
        System.out.println("Service ready.");
    }
}
