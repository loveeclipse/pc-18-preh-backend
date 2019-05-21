package service

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class PatientRegistry : AbstractVerticle() {

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
            post().handler {RequestManager.doSomething(it)}
            get().handler {RequestManager.getSomething(it)}
            patch().handler {RequestManager.updateSomething(it)}
        }
    }

    companion object {
        private const val PORT = 10005
        private const val HOST = "localhost"
    }
}