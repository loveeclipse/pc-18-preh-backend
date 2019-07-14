package services

import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import utils.DiscoveryData.DISCOVERY_GET_SERVICE
import utils.DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import utils.DiscoveryData.DISCOVERY_UNPUBLISH_SERVICE

import utils.DiscoveryData.HOST
import utils.DiscoveryData.PORT

class DiscoveryVerticle : AbstractVerticle() {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private var discovery: ServiceDiscovery? = null

    override fun start() {
        initializeDiscovery()
        vertx
                .createHttpServer()
                .requestHandler(initializeRouter())
                .listen(PORT, HOST)
        log.info("Starting Discovery service on host ${System.getenv("DISCOVERY_EXTERNAL_HOST")?.toString() ?: "$HOST:$PORT"}")
    }

    private fun initializeDiscovery() {
        discovery = ServiceDiscovery.create(vertx,
                ServiceDiscoveryOptions()
                        .setAnnounceAddress("service-announce")
                        .setName("DiscoveryService-service"))
        log.debug("Create Discovery $discovery.")
    }

    private fun initializeRouter(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            post(DISCOVERY_PUBLISH_SERVICE).handler { DiscoveryService.publishService(it, discovery) }
            delete(DISCOVERY_UNPUBLISH_SERVICE).handler { DiscoveryService.unpublishService(it, discovery) }
            get(DISCOVERY_GET_SERVICE).handler { DiscoveryService.retrieveService(it, discovery) }
        }
    }
}