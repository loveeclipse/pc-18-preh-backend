import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import services.EventsService

import utils.DiscoveryData.DISCOVERY_HOST
import utils.DiscoveryData.DISCOVERY_PORT
import utils.DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import utils.DiscoveryData.SERVICE_NAME
import utils.DiscoveryData.SERVICE_HOST

object Main {

    private const val NAME = "events-service"

    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger(this.javaClass.simpleName)

        val vertx = Vertx.vertx()
        vertx.deployVerticle((EventsService())) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx, WebClientOptions().setSsl(true))
                            .post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(SERVICE_NAME, NAME)
                            .addQueryParam(SERVICE_HOST, System.getenv("HEROKU_HOST_NAME")?.toString())
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
