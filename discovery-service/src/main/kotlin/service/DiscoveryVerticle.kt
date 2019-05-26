package service

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions

class DiscoveryVerticle : AbstractVerticle() {

    private val discovery = ServiceDiscovery.create(vertx,
            ServiceDiscoveryOptions()
                    .setAnnounceAddress("service-announce")
                    .setName("DiscoveryService-service"))

    /*override fun start() {
        discovery = ServiceDiscovery.create(vertx,
                ServiceDiscoveryOptions()
                        .setAnnounceAddress("service-announce")
                        .setName("DiscoveryService-service"))
    }*/

    override fun stop() {
        discovery.close()
    }

    private fun initializeRouter(router: Router) {
        // request for publish/unpublish service
        router.post(DISCOVERY_PUBLISH_SERVICE).handler { DiscoveryService.publishService(it) }
        router.post(DISCOVERY_UNPUBLISH_SERVICE).handler { DiscoveryService.unpublishService(it) }
    }

    companion object {
        private const val PORT = 1000
        private const val HOST = "localhost"
        private const val SERVICE_NAME = "service_name"

        private const val DISCOVERY_BASE_PATH = "v1/discovery"
        private const val DISCOVERY_PUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/publish"
        private const val DISCOVERY_UNPUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/unpublish"
    }
}