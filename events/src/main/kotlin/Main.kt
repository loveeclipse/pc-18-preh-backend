import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import services.EventsService

import services.EventsService.Companion.EVENTS_PATH
import utils.DiscoveryData.DISCOVERY_PORT
import utils.DiscoveryData.DISCOVERY_HOST
import utils.DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import utils.DiscoveryData.SERVICE_NAME
import utils.DiscoveryData.SERVICE_HOST
import utils.DiscoveryData.SERVICE_PORT
import utils.DiscoveryData.SERVICE_URI
import utils.EventsData.HOST
import utils.EventsData.NAME
import utils.EventsData.PORT

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("Oc-bridgeService")

        val vertx = Vertx.vertx()
        vertx.deployVerticle((EventsService())) { startService ->
            when { startService.succeeded() ->
                WebClient.create(vertx).post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                        .addQueryParam(SERVICE_NAME, NAME)
                        .addQueryParam(SERVICE_HOST, HOST)
                        .addQueryParam(SERVICE_PORT, PORT.toString())
                        .addQueryParam(SERVICE_URI, EVENTS_PATH)
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
