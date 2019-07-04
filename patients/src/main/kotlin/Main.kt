import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import verticle.RouterVerticle
import utils.DiscoveryData.DISCOVERY_PORT
import utils.DiscoveryData.DISCOVERY_HOST
import utils.DiscoveryData.DISCOVERY_PUBLISH_SERVICE
import utils.DiscoveryData.SERVICE_NAME
import utils.PatientsData.NAME
import utils.DiscoveryData.SERVICE_HOST
import utils.PatientsData.HOST
import utils.DiscoveryData.SERVICE_PORT
import utils.PatientsData.PORT

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("patients")

        val vertx = Vertx.vertx()
        vertx.deployVerticle((RouterVerticle())) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx).post(DISCOVERY_PORT, DISCOVERY_HOST, DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(SERVICE_NAME, NAME)
                            .addQueryParam(SERVICE_HOST, HOST)
                            .addQueryParam(SERVICE_PORT, PORT.toString())
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