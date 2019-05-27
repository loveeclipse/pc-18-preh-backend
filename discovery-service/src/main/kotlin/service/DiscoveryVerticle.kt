package service

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions

class DiscoveryVerticle : AbstractVerticle() {

    private val log = LoggerFactory.getLogger("DiscoveryVerticle")

    private var discovery: ServiceDiscovery? = null

    override fun start() {
        vertx
                .createHttpServer()
                .requestHandler(initializeRouter())
                .listen(PORT, HOST)
        initializeDiscovery()
        log.info("Starting Discovery.")
        initializeRouter()
    }

    private fun initializeDiscovery() {
        discovery = ServiceDiscovery.create(vertx,
                ServiceDiscoveryOptions()
                        .setAnnounceAddress("service-announce")
                        .setName("DiscoveryService-service"))
    }

    /*
    override fun stop() {
        discovery.close()
    }
    */

    private fun initializeRouter(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            post(DISCOVERY_PUBLISH_SERVICE).handler { DiscoveryService.publishService(it, discovery) }
            delete(DISCOVERY_UNPUBLISH_SERVICE).handler { DiscoveryService.unpublishService(it, discovery) }
            get(DISCOVERY_GET_SERVICE).handler { DiscoveryService.getService(it, discovery) }
        }
    }

    companion object {
        private const val PORT = 1000
        private const val HOST = "localhost"

        private const val DISCOVERY_BASE_PATH = "/v1/discovery"
        private const val DISCOVERY_PUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/publish"
        private const val DISCOVERY_UNPUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/unpublish"
        private const val DISCOVERY_GET_SERVICE = "$DISCOVERY_BASE_PATH/discover"
    }
}