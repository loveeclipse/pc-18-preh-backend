import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import handlers.GenericMissionsHandlers
import handlers.OngoingHandlers
import handlers.ReturnInformationHandlers
import handlers.TrackingHandlers

class RouterVerticle : AbstractVerticle() {

    private val log = LoggerFactory.getLogger(this.javaClass.canonicalName)

    override fun start() {
        vertx.createHttpServer()
                .requestHandler(createRoute())
                .listen(PORT, HOST)
        log.info("Service ready on port $PORT and host $HOST")
    }

    private fun createRoute(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())

            post(MISSIONS_PATH).handler { GenericMissionsHandlers.createMission(it) }
            get(MISSIONS_PATH).handler { GenericMissionsHandlers.retrieveMissions(it) }
            get(MISSION_PATH).handler { GenericMissionsHandlers.retrieveMission(it) }
            delete(MISSION_PATH).handler { GenericMissionsHandlers.deleteMission(it) }

            put(ONGOING_PATH).handler { OngoingHandlers.updateOngoing(it) }
            get(ONGOING_PATH).handler { OngoingHandlers.retrieveOngoing(it) }

            put(MEDIC_PATH).handler { OngoingHandlers.updateOngoing(it) }
            get(MEDIC_PATH).handler { OngoingHandlers.retrieveOngoing(it) }

            put(RETURN_INFORMATION_PATH).handler { ReturnInformationHandlers.updateReturnInformation(it) }
            get(RETURN_INFORMATION_PATH).handler { ReturnInformationHandlers.retrieveReturnInformation(it) }
            delete(RETURN_INFORMATION_PATH).handler { ReturnInformationHandlers.deleteReturnInformation(it) }

            get(TRACKING_PATH).handler { TrackingHandlers.retrieveTracking(it) }
            put(TRACKING_STEP_PATH).handler { TrackingHandlers.updateTrackingStep(it) }
            get(TRACKING_STEP_PATH).handler { TrackingHandlers.retrieveTrackingStep(it) }
            delete(TRACKING_STEP_PATH).handler { TrackingHandlers.deleteTrackingStep(it) }
        }
    }

    companion object {
        const val PORT = 10000
        const val HOST = "localhost"

        private const val MISSIONS_PATH = "/missions"
        private const val MISSION_PATH = "$MISSIONS_PATH/:missionId"
        private const val ONGOING_PATH = "$MISSION_PATH/ongoing"
        private const val MEDIC_PATH = "$MISSION_PATH/medic"
        private const val RETURN_INFORMATION_PATH = "$MISSION_PATH/return-information"
        private const val TRACKING_PATH = "$MISSION_PATH/tracking"
        private const val TRACKING_STEP_PATH = "$TRACKING_PATH/:step"
    }
}
