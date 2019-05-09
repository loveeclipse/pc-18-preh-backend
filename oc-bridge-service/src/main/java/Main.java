import io.vertx.core.Vertx;
import service.OcBridge;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        OcBridge service = new OcBridge();
        vertx.deployVerticle(service);
    }
}
