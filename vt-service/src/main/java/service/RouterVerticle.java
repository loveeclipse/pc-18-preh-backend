package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * This class allows to sending HTTP requests to the right handler.
 */
public class RouterVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(RouterVerticle.class);

    private static final int PORT = 5151;
    private static final String HOST = "localhost";
    private static final String GET_ALL_EVENT_URL = "/api/event/:eventId";
    private static final String POST_OC_CALL_URL = "/api/event/:eventId";

    @Override
    public void start() {
        VtVerticle.setVertx(vertx);
        Router router = Router.router(vertx);
        initializeRouter(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(PORT, HOST);
        log.info("Service listen on port: " + PORT);
    }

    // Initialize all roting service
    private void initializeRouter(Router router){
        router.route().handler(BodyHandler.create());
        router.get(GET_ALL_EVENT_URL).handler(VtVerticle::handlerGetAllEvent);
        router.post(POST_OC_CALL_URL).handler(VtVerticle::handlerPostOcCall);
    }
}
