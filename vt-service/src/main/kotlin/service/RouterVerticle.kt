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
        VtService.initializeRequestManager(vertx)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())

            get(EVENT_TRACKING_PATH).handler { VtService.retrieveEventTracking(it) }
            get(MISSION_TRACKING_PATH).handler { VtService.retrieveMissionTracking(it) }

            for (item in trackingItems) {
                val path = MISSION_TRACKING_PATH + "/" + item.pathName
                get(path).handler { VtService.retrieveSingleTrackingItem(it, item) }
                post(path).handler { VtService.createSingleTrackingItem(it, item) }
            }
//            get(OC_CALL_PATH).handler { VtService.retrieveSingleTrackingItem(it, OC_CALL_REP) }
//            post(OC_CALL_PATH).handler { VtService.createSingleTrackingItem(it, OC_CALL_REP) }
//
//            get(CREW_DEPARTURE_PATH).handler { VtService.retrieveSingleTrackingItem(it, CREW_DEPARTURE_REP) }
//            post(CREW_DEPARTURE_PATH).handler { VtService.createSingleTrackingItem(it, CREW_DEPARTURE_REP) }
//
//            get(ARRIVAL_ONSITE_PATH).handler { VtService.retrieveSingleTrackingItem(it, ARRIVAL_ONSITE_REP) }
//            post(ARRIVAL_ONSITE_PATH).handler { VtService.createSingleTrackingItem(it, ARRIVAL_ONSITE_REP) }
//
//            get(DEPARTURE_ONSITE_PATH).handler { VtService.retrieveSingleTrackingItem(it, DEPARTURE_ONSITE_REP) }
//            post(DEPARTURE_ONSITE_PATH).handler { VtService.createSingleTrackingItem(it, DEPARTURE_ONSITE_REP) }
//
//            get(LANDING_HELIPAD_PATH).handler { VtService.retrieveSingleTrackingItem(it, LANDING_HELIPAD_REP) }
//            post(LANDING_HELIPAD_PATH).handler { VtService.createSingleTrackingItem(it, LANDING_HELIPAD_REP) }
//
//            get(ARRIVAL_ER_PATH).handler { VtService.retrieveSingleTrackingItem(it, ARRIVAL_ER_REP) }
//            post(ARRIVAL_ER_PATH).handler { VtService.createSingleTrackingItem(it, ARRIVAL_ER_REP) }
        }
    }

    companion object {
        private const val PORT = 10000
        private const val HOST = "localhost"

        private const val EVENT_TRACKING_PATH = "/v1/events-tracking/:eventId"
        private const val MISSION_TRACKING_PATH = "$EVENT_TRACKING_PATH/missions/:missionId"

        private val trackingItems = listOf(
                MissionTrackingItem(pathName = "oc-call", fieldName = "ocCall"),
                MissionTrackingItem(pathName = "crew-departure", fieldName = "crewDeparture"),
                MissionTrackingItem(pathName = "arrival-onsite", fieldName = "arrivalOnsite"),
                MissionTrackingItem(pathName = "departure-onsite", fieldName = "departureOnsite"),
                MissionTrackingItem(pathName = "landing-helipad", fieldName = "landingHelipad"),
                MissionTrackingItem(pathName = "arrival-er", fieldName = "arrivalEr")
        )
    }
}