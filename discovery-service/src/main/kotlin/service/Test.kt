package service

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions

class Test : AbstractVerticle() {

    private var discovery = ServiceDiscovery.create(vertx)

    override fun start() {
        discovery = ServiceDiscovery.create(vertx,
                ServiceDiscoveryOptions()
                        .setAnnounceAddress("service-announce")
                        .setName("Discovery-service"))
    }

    override fun stop() {
        discovery.close()
    }

    private fun initializeRouter(ruter: Router) {
        // request for publish/unpublish service
    }

    private fun publishService(routingContext: RoutingContext) {
    }

    private fun unpublishServide(routingContext: RoutingContext) {
    }
}