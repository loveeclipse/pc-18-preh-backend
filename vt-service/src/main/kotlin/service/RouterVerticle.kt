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
                put(path).handler { VtService.updateSingleTrackingItem(it, item) }
            }
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