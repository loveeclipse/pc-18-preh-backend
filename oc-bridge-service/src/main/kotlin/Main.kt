import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import service.DiscoveryWrapper
import service.OcBridge

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("Oc-bridgeService")
        val discoveryHost = "localhost"
        val discoveryPort = 5150
        val name = "DiscoveryService-service"
        val serviceHost = "localhost"
        val servicePort = 10000
        Vertx.vertx().deployVerticle((OcBridge())) { result ->
            when { result.succeeded() ->
                DiscoveryWrapper(discoveryHost, discoveryPort).publish(name, serviceHost, servicePort)
            else -> log.debug("Error starting verticle.")
            }
        }
    }
}
