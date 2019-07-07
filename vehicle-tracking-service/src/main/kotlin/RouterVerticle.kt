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
        Handlers.initializeRequestManager(vertx)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())

            post(MISSIONS_PATH).handler { Handlers.createMission(it) }
            get(MISSIONS_PATH).handler { Handlers.retrieveMissions(it) }

            get(MISSION_PATH).handler { Handlers.retrieveMission(it) }
            delete(MISSION_PATH).handler { Handlers.deleteMission(it) }

            put(RETURN_INFORMATION_PATH).handler { Handlers.updateReturnInformation(it) }
            get(RETURN_INFORMATION_PATH).handler { Handlers.retrieveReturnInformation(it) }
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
    }
}
