package database;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;


public class MongoDBConnection {

    private static final JsonObject CONFIG = new JsonObject()
            .put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr");

    private Vertx vertx = Vertx.vertx();
    private MongoClient client = null;

    public void databaseConnection() {
        client = MongoClient.createNonShared(vertx, CONFIG);
        System.out.println("Database connect!");
    }

    public void dbOperation(JsonObject object){
        client.save("events", createDocument(object), result -> {
            if (result.succeeded()) {
                String id = result.result();
                System.out.println("Saved event with id " + id);
            } else {
                result.cause().printStackTrace();
            }
        });
    }

    private JsonObject createDocument(JsonObject object){
        return new JsonObject().put("_id", object.getString("event_id"));
    }
}
