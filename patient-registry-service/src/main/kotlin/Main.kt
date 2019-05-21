import io.vertx.core.Vertx
import service.PatientRegistry

object Main {
    @JvmStatic
    fun main(args: Array<String>) { Vertx.vertx().deployVerticle(PatientRegistry()) }
}