import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient

import DiscoveryData.DISCOVERY_HOST
import DiscoveryData.DISCOVERY_PORT
import DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import DiscoveryData.SERVICE_NAME
import DiscoveryData.SERVICE_HOST
import DiscoveryData.SERVICE_PORT
import ServiceData.HOST
import io.vertx.ext.web.client.WebClientOptions

object Main {

    val vertx = Vertx.vertx()
    private const val NAME = "missions-service"

    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger(this.javaClass.simpleName)
        val isExternal = System.getenv("MISSIONS_EXTERNAL_HOST") != null

        vertx.deployVerticle(RouterVerticle()) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx, WebClientOptions().setSsl(isExternal))
                            .post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(SERVICE_NAME, NAME)
                            .addQueryParam(SERVICE_HOST, System.getenv("MISSIONS_EXTERNAL_HOST")?.toString() ?: HOST)
                            .addQueryParam(SERVICE_PORT, if (isExternal) "443" else "10001")
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
