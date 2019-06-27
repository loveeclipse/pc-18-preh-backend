import io.vertx.core.Vertx
import service.DiscoveryVerticle

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(DiscoveryVerticle()) }
}
