import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient

import DiscoveryData.DISCOVERY_PORT
import DiscoveryData.DISCOVERY_HOST
import DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import DiscoveryData.SERVICE_NAME
import DiscoveryData.SERVICE_HOST
import DiscoveryData.SERVICE_PORT
import DiscoveryData.NAME
import DiscoveryData.HOST
import DiscoveryData.PORT
import DiscoveryData.SERVICE_URI
import RouterVerticle.Companion.MISSIONS_PATH

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger(this.javaClass.simpleName)

        val vertx = Vertx.vertx()
        vertx.deployVerticle((RouterVerticle())) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx).post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(SERVICE_NAME, NAME)
                            .addQueryParam(SERVICE_HOST, HOST)
                            .addQueryParam(SERVICE_PORT, PORT.toString())
                            .addQueryParam(SERVICE_URI, MISSIONS_PATH)
                            .send { publishResult ->
                                if (publishResult.succeeded()) {
                                    log.info("Received response with status code ${publishResult.result().statusCode()}")
                                } else {
                                    log.info("Something went wrong ${publishResult.cause()}")
                                }
                            }
                else -> log.debug("Error starting verticle ${startService.cause()}.")
            }
        }
    }
}