import io.vertx.core.Vertx
import service.Test

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(Test()) }
}
