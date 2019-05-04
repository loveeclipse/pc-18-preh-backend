import core.RouterVerticle;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        RouterVerticle verticle = new RouterVerticle();
        vertx.deployVerticle(verticle);
    }
}
