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
            get(GET_ALL_EVENT_DETAILS).handler { VtService.handlerGetAllEventsDetails(it) }
            get(GET_ALL_MISSION_DETAILS).handler { VtService.handlerGetAllMissions(it)  }
            get(GET_OC_CALL).handler { VtService.handlerGetOcCall(it) }
            post(POST_OC_CALL).handler { VtService.handlerPostOcCall(it) }
            get(GET_CREW_DEPARTURE).handler { VtService.handlerGetCrewDeparture(it) }
            post(POST_CREW_DEPARTURE).handler { VtService.handlerPostCrewDeparture(it) }
            get(GET_LANDING_ONSITE).handler { VtService.handlerGetLandingOnsite(it) }
            post(POST_LANDING_ONSITE).handler { VtService.handlerPostLandingOnsite(it) }
            get(GET_TAKEOFF_ONSITE).handler { VtService.handlerGetTakeoffOnsite(it) }
            post(POST_TAKEOFF_ONSITE).handler { VtService.handlerPostTakeoffOnsite(it) }
            get(GET_LANDING_HELIPAD).handler { VtService.handlerGetLandingHelipad(it) }
            post(POST_LANDING_HELIPAD).handler { VtService.handlerPostLandingHelipad(it) }
            get(GET_ARRIVAL_ER).handler { VtService.handlerGetArrivalEr(it) }
            post(POST_ARRIVAL_ER).handler { VtService.handlerPostArrivalEr(it) }
        }
    }

    companion object {
        private const val PORT = 5151
        private const val HOST = "localhost"
        private const val GET_ALL_EVENT_DETAILS = "/events-tracking/:eventId"
        private const val GET_ALL_MISSION_DETAILS = "/events-tracking/:eventId/missions/:missionId"
        private const val GET_OC_CALL = "/events-tracking/:eventId/oc-call/missions/:missionId"
        private const val POST_OC_CALL = "/events-tracking/:eventId/oc-call/missions/:missionId"
        private const val GET_CREW_DEPARTURE = "/events-tracking/:eventId/missions/:missionId/crew-departure"
        private const val POST_CREW_DEPARTURE = "/events-tracking/:eventId/missions/:missionId/crew-departure"
        private const val GET_LANDING_ONSITE = "/events-tracking/:eventId/missions/:missionId/landing-onsite"
        private const val POST_LANDING_ONSITE = "/events-tracking/:eventId/missions/:missionId/landing-onsite"
        private const val GET_TAKEOFF_ONSITE = "/events-tracking/:eventId/missions/:missionId/takeoff-onsite"
        private const val POST_TAKEOFF_ONSITE = "/events-tracking/:eventId/missions/:missionId/takeoff-onsite"
        private const val GET_LANDING_HELIPAD = "/events-tracking/:eventId/missions/:missionId/landing-helipad"
        private const val POST_LANDING_HELIPAD = "/events-tracking/:eventId/missions/:missionId/landing-helipad"
        private const val GET_ARRIVAL_ER = "/events-tracking/:eventId/missions/:missionId/arrival-er"
        private const val POST_ARRIVAL_ER  = "/events-tracking/:eventId/missions/:missionId/arrival-er"
    }
}