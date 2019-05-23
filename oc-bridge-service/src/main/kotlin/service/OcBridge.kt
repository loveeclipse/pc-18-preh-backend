package service

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class OcBridge : AbstractVerticle() {

    override fun start() {
        vertx
                .createHttpServer()
                .requestHandler(createRouter())
                .listen(PORT, HOST)
        println("Service ready on port $PORT and host $HOST")
    }

    private fun createRouter(): Router {
        RequestManager.initializeRequestManager(vertx)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            post(EVENTS_PATH).handler { RequestManager.createEvent(it) }
            get(SINGLE_EVENT_PATH).handler { RequestManager.retrieveEventById(it) }
            patch(SINGLE_EVENT_PATH).handler { RequestManager.updateEvent(it) }
        }
    }

    companion object {
        private const val PORT = 10000
        private const val HOST = "localhost"
        private const val EVENTS_PATH = "/v1/events"
        private const val SINGLE_EVENT_PATH = "$EVENTS_PATH/:eventId"
    }
}
