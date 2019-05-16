import io.vertx.core.Vertx
import service.OcBridge

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(OcBridge()) }
}
