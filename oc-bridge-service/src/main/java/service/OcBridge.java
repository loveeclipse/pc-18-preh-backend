package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class OcBridge extends AbstractVerticle {
    private static final int PORT = 10000;

    @Override
    public void start() {
        startServer(createRouter());
    }

    private Router createRouter() {
        HandleRequestManager.setVertx(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/api/event").handler(HandleRequestManager::handleCreateEvent);
        router.get("/api/event/:eventId").handler(HandleRequestManager::handleGetEventById);
        router.patch("/api/event/:eventId").handler(HandleRequestManager::handleUpdateEvent);
        return router;
    }

    private void startServer(Router router) {
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT);
        System.out.println("Service ready.");
    }
}
