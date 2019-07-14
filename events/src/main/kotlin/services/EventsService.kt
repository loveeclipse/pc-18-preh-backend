package services

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import utils.ServiceData.HOST
import utils.ServiceData.PORT

class EventsService : AbstractVerticle() {

    private val log = LoggerFactory.getLogger(this.javaClass.canonicalName)

    override fun start() {
        vertx
                .createHttpServer()
                .requestHandler(createRouter())
                .listen(PORT, HOST)
        log.info("Service ready on host ${System.getenv("EVENTS_EXTERNAL_HOST")?.toString() ?: "$HOST:$PORT"}")
    }

    private fun createRouter(): Router {
        RequestManager.vertx = vertx
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            post(EVENTS_PATH).handler { RequestManager.createEvent(it) }
            get(SINGLE_EVENT_PATH).handler { RequestManager.retrieveEventById(it) }
            patch(SINGLE_EVENT_PATH).handler { RequestManager.updateEvent(it) }
        }
    }

    companion object {
        private const val EVENTS_PATH = "/events"
        private const val SINGLE_EVENT_PATH = "$EVENTS_PATH/:eventId"
    }
}
