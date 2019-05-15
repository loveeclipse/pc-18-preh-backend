import io.vertx.core.Vertx
import service.OcBridge

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        val service = OcBridge()
        vertx.deployVerticle(service)
    }
}
