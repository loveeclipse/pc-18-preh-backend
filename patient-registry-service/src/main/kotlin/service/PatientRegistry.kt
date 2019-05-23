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
            post("/v1/patients").handler { RequestManager.handleNewPatient(it) }
            get("/v1/patients/:patientId").handler { RequestManager.handleGetPatientData(it) }
            patch("/v1/patients/:patientId").handler { RequestManager.handleUpdatePatientData(it) }
        }
    }

    companion object {
        private const val PORT = 10005
        private const val HOST = "localhost"
    }
}