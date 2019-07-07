package verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import services.AnagraphicService
import services.ComplicationsService
import services.DrugsService
import services.InjectionTreatmentsService
import services.IppvTreatmentsService
import services.SimpleManeuversService
import services.PatientsService
import services.SimpleTreatmentsService
import services.StatusService
import services.VitalParametersService
import utils.PatientsData.HOST
import utils.PatientsData.PORT
import utils.PathItem.maneuversItems
import utils.PathItem.complicationsItems

class RouterVerticle : AbstractVerticle() {

    val log = LoggerFactory.getLogger(this.javaClass.simpleName)

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
            post(PATIENTS_PATH).handler { PatientsService.createPatient(it) }
            put(ANAGRAPHIC_PATH).handler { AnagraphicService.updateAnagraphic(it) }
            get(ANAGRAPHIC_PATH).handler { AnagraphicService.retrieveAnagraphic(it) }
            put(STATUS_PATH).handler { StatusService.updateStatus(it) }
            post(VITAL_PARAMETERS_PATH).handler { VitalParametersService.createVitalParameters(it) }
            post(DRUGS_PATH).handler { DrugsService.createDrug(it) }
            maneuversItems.forEach { name ->
                val path = "$MANEUVERS_SIMPLE_PATH/$name"
                post(path).handler { SimpleManeuversService.createSimpleManeuver(it, name) }
                delete(path).handler { SimpleManeuversService.deleteSimpleManeuver(it, name) }
            }
            post(TREATMENTS_SIMPLE_PATH).handler { SimpleTreatmentsService.createSimpleTreatment(it) }
            post(TREATMENTS_INJECTION_PATH).handler { InjectionTreatmentsService.createInjectionTreatment(it) }
            post(TREATMENTS_IPPV_PATH).handler { IppvTreatmentsService.createIppvTreatment(it) }
            complicationsItems.forEach { name ->
                val path = "$COMPLICATIONS_PATH/$name"
                post(path).handler { ComplicationsService.createComplication(it, name) }
                delete(path).handler { ComplicationsService.deleteComplication(it, name) }
            }
        }
    }

    private fun initializationService() {
        PatientsService.vertx = vertx
        AnagraphicService.vertx = vertx
        ComplicationsService.vertx = vertx
        DrugsService.vertx = vertx
        SimpleManeuversService.vertx = vertx
        SimpleTreatmentsService.vertx = vertx
        InjectionTreatmentsService.vertx = vertx
        IppvTreatmentsService.vertx = vertx
        StatusService.vertx = vertx
        VitalParametersService.vertx = vertx
    }
    companion object {
        const val PATIENTS_PATH = "/patients"
        private const val PATIENT_PATH = "$PATIENTS_PATH/:patientId"
        private const val ANAGRAPHIC_PATH = "$PATIENT_PATH/anagraphic"
        private const val STATUS_PATH = "$PATIENT_PATH/status"
        private const val VITAL_PARAMETERS_PATH = "$PATIENT_PATH/vital-parameters"
        private const val DRUGS_PATH = "$PATIENT_PATH/drugs"
        private const val MANEUVERS_PATH = "$PATIENT_PATH/maneuvers"
        private const val MANEUVERS_SIMPLE_PATH = "$MANEUVERS_PATH/simple"
        private const val TREATMENTS_PATH = "$PATIENT_PATH/treatments"
        private const val TREATMENTS_SIMPLE_PATH = "$TREATMENTS_PATH/simple"
        private const val TREATMENTS_INJECTION_PATH = "$TREATMENTS_PATH/injection"
        private const val TREATMENTS_IPPV_PATH = "$TREATMENTS_PATH/ippv"
        private const val COMPLICATIONS_PATH = "$PATIENT_PATH/complications"
    }
}