package service

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

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
            get(MISSION_TRACKING_PATH).handler { VtService.retrieveMissionTracking(it)  }

            get(OC_CALL_PATH).handler { VtService.retrieveSingleTrackingItem(it, OC_CALL_REP) }
            post(OC_CALL_PATH).handler { VtService.createSingleTrackingItem(it, OC_CALL_REP) }

            get(CREW_DEPARTURE_PATH).handler { VtService.retrieveSingleTrackingItem(it, CREW_DEPARTURE_REP) }
            post(CREW_DEPARTURE_PATH).handler { VtService.createSingleTrackingItem(it, CREW_DEPARTURE_REP) }

            get(ARRIVAL_ONSITE_PATH).handler { VtService.retrieveSingleTrackingItem(it, ARRIVAL_ONSITE_REP) }
            post(ARRIVAL_ONSITE_PATH).handler { VtService.createSingleTrackingItem(it, ARRIVAL_ONSITE_REP) }

            get(DEPARTURE_ONSITE_PATH).handler { VtService.retrieveSingleTrackingItem(it, DEPARTURE_ONSITE_REP) }
            post(DEPARTURE_ONSITE_PATH).handler { VtService.createSingleTrackingItem(it, DEPARTURE_ONSITE_REP) }

            get(LANDING_HELIPAD_PATH).handler { VtService.retrieveSingleTrackingItem(it, LANDING_HELIPAD_REP) }
            post(LANDING_HELIPAD_PATH).handler { VtService.createSingleTrackingItem(it, LANDING_HELIPAD_REP) }

            get(ARRIVAL_ER_PATH).handler { VtService.retrieveSingleTrackingItem(it, ARRIVAL_ER_REP) }
            post(ARRIVAL_ER_PATH).handler { VtService.createSingleTrackingItem(it, ARRIVAL_ER_REP) }
        }
    }

    companion object {
        private const val PORT = 10000
        private const val HOST = "localhost"

        private const val EVENT_TRACKING_PATH = "/v1/events-tracking/:eventId"
        private const val MISSION_TRACKING_PATH = "$EVENT_TRACKING_PATH/missions/:missionId"

        private const val OC_CALL_REP = "oc-call"
        private const val OC_CALL_PATH = "$MISSION_TRACKING_PATH/$OC_CALL_REP"

        private const val CREW_DEPARTURE_REP = "crew-departure"
        private const val CREW_DEPARTURE_PATH = "$MISSION_TRACKING_PATH/$CREW_DEPARTURE_REP"

        private const val ARRIVAL_ONSITE_REP = "arrival-onsite"
        private const val ARRIVAL_ONSITE_PATH = "$MISSION_TRACKING_PATH/$ARRIVAL_ONSITE_REP"

        private const val DEPARTURE_ONSITE_REP = "departure-onsite"
        private const val DEPARTURE_ONSITE_PATH = "$MISSION_TRACKING_PATH/$DEPARTURE_ONSITE_REP"

        private const val LANDING_HELIPAD_REP = "landing-helipad"
        private const val LANDING_HELIPAD_PATH = "$MISSION_TRACKING_PATH/$LANDING_HELIPAD_REP"

        private const val ARRIVAL_ER_REP = "arrival-er"
        private const val ARRIVAL_ER_PATH = "$MISSION_TRACKING_PATH/$ARRIVAL_ER_REP"
    }
}