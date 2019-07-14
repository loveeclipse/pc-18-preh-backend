import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

import verticle.RouterVerticle
import utils.DiscoveryData.DISCOVERY_HOST
import utils.DiscoveryData.DISCOVERY_PORT
import utils.DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import utils.DiscoveryData.SERVICE_NAME
import utils.DiscoveryData.SERVICE_HOST
import utils.DiscoveryData.SERVICE_PORT
import utils.ServiceData.HOST

object Main {

    private const val NAME = "patients-service"

    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger(this.javaClass.simpleName)
        val isExternal = System.getenv("PATIENTS_EXTERNAL_HOST") != null

        val vertx = Vertx.vertx()
        vertx.deployVerticle((RouterVerticle())) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx, WebClientOptions().setSsl(isExternal))
                            .post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(SERVICE_NAME, NAME)
                            .addQueryParam(SERVICE_HOST, System.getenv("PATIENTS_EXTERNAL_HOST")?.toString() ?: HOST)
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