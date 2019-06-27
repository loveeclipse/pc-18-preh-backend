import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import service.Data
import service.RouterVerticle

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("Oc-bridgeService")

        val vertx = Vertx.vertx()
        vertx.deployVerticle((RouterVerticle())) { startService ->
            when {
                startService.succeeded() ->
                    WebClient.create(vertx).post(Data.DISCOVERY_PORT, Data.DISCOVERY_HOST, Data.DISCOVERY_PUBLISH_SERVICE)
                            .addQueryParam(Data.SERVICE_NAME, Data.NAME)
                            .addQueryParam(Data.SERVICE_HOST, Data.HOST)
                            .addQueryParam(Data.SERVICE_PORT, Data.PORT.toString())
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