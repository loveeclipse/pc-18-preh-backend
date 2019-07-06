package services

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import service.TrackingStep

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
        VtService.initializeRequestManager(vertx)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())

            post(MISSIONS_PATH).handler { VtService.createMission(it) }
            get(MISSIONS_PATH).handler { VtService.retrieveMissions(it) }

            get(MISSION_PATH).handler { VtService.retrieveMission(it) }
            delete(MISSION_PATH).handler { VtService.deleteMission(it) }

            put(RETURN_INFORMATION_PATH).handler { VtService.updateReturnInformation(it) }

//            for (item in trackingSteps) {
//                val path = MISSION_TRACKING_PATH + "/timeline/" + item.pathName
//                get(path).handler { VtService.retrieveTimelineItem(it, item) }
//                put(path).handler { VtService.updateTimelineItem(it, item) }
//            }
//
//            get(CHOSEN_HOSPITAL_PATH).handler { VtService.retrieveChosenHospital(it) }
//            put(CHOSEN_HOSPITAL_PATH).handler { VtService.updateChosenHospital(it) }
        }
    }

    companion object {
        private const val PORT = 10000
        private const val HOST = "localhost"

        const val MISSIONS_PATH = "/missions"
        private const val MISSION_PATH = "/missions/:missionId"
        private const val RETURN_INFORMATION_PATH = "$MISSION_PATH/return-information"
        private const val TRACKING_PATH = "$MISSION_PATH/tracking"
        private const val ONGOING_PATH = "$MISSION_PATH/ongoing"

        private val trackingSteps = listOf(
                TrackingStep(pathName = "oc-call", fieldName = "ocCall"),
                TrackingStep(pathName = "crew-departure", fieldName = "crewDeparture"),
                TrackingStep(pathName = "arrival-onsite", fieldName = "arrivalOnsite"),
                TrackingStep(pathName = "departure-onsite", fieldName = "departureOnsite"),
                TrackingStep(pathName = "landing-helipad", fieldName = "landingHelipad"),
                TrackingStep(pathName = "arrival-er", fieldName = "arrivalEr")
        )
    }
}
