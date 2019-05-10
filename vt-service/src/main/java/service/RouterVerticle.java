package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RouterVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(RouterVerticle.class);

    private static final int PORT = 5151;
    private static final String HOST = "localhost";
    private static final String URL = "/api/event/:eventId";

    @Override
    // if use future Future<Void> future
    public void start() {
        VtVerticle.setVertx(vertx);
        Router router = Router.router(vertx);
        initializeRouter(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT, HOST
                       /* config().getInteger("http.port", PORT),
                        result -> {
                            if (result.succeeded()) {
                                future.complete();
                                log.debug("Service listen on port: " + PORT);
                            } else {
                                future.fail(result.cause());
                                log.debug("Service fail starting.");
                            }
                        }*/
                );
        log.info("Service listen on port: " + PORT);
    }

    // Initialize all roting service
    private void initializeRouter(Router router){
        router.route().handler(BodyHandler.create());
        router.get(URL).handler(VtVerticle::handlerGetAllEvent);
        //router.post(URL).handler(this::handlerCreate);
    }
}
