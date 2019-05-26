import io.vertx.core.Vertx
import service.Discovery

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(Discovery()) }
}
