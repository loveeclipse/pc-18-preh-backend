package database;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoDb {
    private static final JsonObject CONFIG = new JsonObject()
            .put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr");

    public static MongoClient getClient(Vertx vertx) {
        return MongoClient.createNonShared(vertx, CONFIG);
    }
}
