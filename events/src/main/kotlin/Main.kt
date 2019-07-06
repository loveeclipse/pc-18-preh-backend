import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import services.EventsService
import services.Data.DISCOVERY_PORT
import services.Data.DISCOVERY_HOST
import services.Data.DISCOVERY_PUBLISH_SERVICE
import services.Data.SERVICE_NAME
import services.Data.NAME
import services.Data.SERVICE_HOST
import services.Data.HOST
import services.Data.SERVICE_PORT
import services.Data.PORT

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
