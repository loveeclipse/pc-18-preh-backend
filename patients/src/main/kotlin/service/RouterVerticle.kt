package service

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RouterVerticle : AbstractVerticle() {

    val log = LoggerFactory.getLogger("RouterVerticle")

    override fun start() {
        vertx
                .createHttpServer()
                .requestHandler(createRoute())
                .listen(PORT, HOST)
        log.info("Service ready on port $PORT and host $HOST")
    }

    private fun createRoute(): Router {
        initializationService()
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
        }
    }

    private fun initializationService() {
        AnagraphicService.vertx = vertx
        ComplicationsService.vertx = vertx
        DrugsService.vertx = vertx
        ManouversService.vertx = vertx
        StatusService.vertx = vertx
        VitalParametersService.vertx
    }
    companion object {
        private const val PORT = 10000
        private const val HOST = "localhost"

        private const val PATIENT_PATH = "/patients/:patientId"
        private const val ANAGRAPHIC_PATH = "$PATIENT_PATH/anagraphic"
        private const val STATUS_PATH = "$PATIENT_PATH/status"
        private const val VITAL_PARAMETERS_PATH = "$PATIENT_PATH/vital-parameters"
        private const val DRUGS_PATH = "$PATIENT_PATH/drugs"
        private const val MANEUVERS_PATH = "$PATIENT_PATH/maneuvers/simple/:simpleManeuver"
        private const val TREATMENTS_PATH = "$PATIENT_PATH/treatments"
        private const val TREATMENTS_SIMPLE_PATH = "$TREATMENTS_PATH/simple"
        private const val TREATMENTS_INJECTION_PATH = "$TREATMENTS_PATH/injection"
        private const val TREATMENTS_IPPV_PATH = "$TREATMENTS_PATH/ippv"
        private const val COMPLICATIONS_PATH = "$PATIENT_PATH/complications/:complication"
    }
}