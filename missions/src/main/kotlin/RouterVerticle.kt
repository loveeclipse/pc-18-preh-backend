import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RouterVerticle : AbstractVerticle() {

    override fun start() {
        vertx.createHttpServer()
                .requestHandler(createRoute())
                .listen(PORT, HOST)
    }

    private fun createRoute(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())

            post(MISSIONS_PATH).handler { Handlers.createMission(it) }
            get(MISSIONS_PATH).handler { Handlers.retrieveMissions(it) }

            get(MISSION_PATH).handler { Handlers.retrieveMission(it) }
            delete(MISSION_PATH).handler { Handlers.deleteMission(it) }

            post(ONGOING_PATH).handler { Handlers.updateOngoing(it) }
            get(ONGOING_PATH).handler { Handlers.retrieveOngoing(it) }

            put(RETURN_INFORMATION_PATH).handler { Handlers.updateReturnInformation(it) }
            get(RETURN_INFORMATION_PATH).handler { Handlers.retrieveReturnInformation(it) }

            get(TRACKING_PATH).handler { Handlers.retrieveTracking(it) }
            put(TRACKING_STEP_PATH).handler { Handlers.updateTrackingStep(it) }
            get(TRACKING_STEP_PATH).handler { Handlers.retrieveTrackingStep(it) }
        }
    }

    companion object {
        const val PORT = 10000
        const val HOST = "localhost"

        const val MISSIONS_PATH = "/missions"
        private const val MISSION_PATH = "$MISSIONS_PATH/:missionId"
        private const val ONGOING_PATH = "$MISSION_PATH/ongoing"
        private const val RETURN_INFORMATION_PATH = "$MISSION_PATH/return-information"
        private const val TRACKING_PATH = "$MISSION_PATH/tracking"
        private const val TRACKING_STEP_PATH = "$TRACKING_PATH/:step"
    }
}
