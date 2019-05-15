package service

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class OcBridge : AbstractVerticle() {

    override fun start() {
        vertx
                .createHttpServer()
                .requestHandler(createRouter())
                .listen(PORT, HOST)
        println("Service ready.")
    }

    private fun createRouter(): Router {
        RequestManager.initializeRequestManager(vertx)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            post("/v1/events").handler { RequestManager.handleCreateEvent(it) }
            get("/v1/events/:eventId").handler { RequestManager.handleGetEventById(it) }
            patch("/v1/events/:eventId").handler { RequestManager.handleUpdateEvent(it) }
        }
    }

    companion object {
        private const val PORT = 10000
        private val HOST = "localhost"
    }
}
