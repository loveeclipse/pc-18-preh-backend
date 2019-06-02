import io.vertx.core.Vertx
import service.RouterVerticle

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(RouterVerticle()) }
}