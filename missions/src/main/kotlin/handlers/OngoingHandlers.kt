package handlers

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import handlers.Shared.MISSIONS_COLLECTION
import handlers.Shared.MONGODB_CONFIGURATION

object OngoingHandlers {

    fun updateOngoing(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val ongoing: String = context.bodyAsString

        if (ongoing != "true" && ongoing != "false") {
            response.putHeader("Content-Type", "plain/text")
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end("Invalid request body. Accepted values: true or false")
        } else {

            val query = json { obj("_id" to missionId) }
            val update = json { obj(
                    "\$set" to obj("ongoing" to ongoing.toBoolean())
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                        when {
                            updateOperation.failed() ->
                                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docMatched == 0L ->
                                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end()
                            else ->
                                response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                        }
                    }
        }
    }

    fun retrieveOngoing(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .findOne(MISSIONS_COLLECTION, query, null) { findOneOperation ->
                    if (findOneOperation.failed()) {
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val result: JsonObject? = findOneOperation.result()
                        if (result == null) {
                            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end()
                        } else {
                            val ongoing: Boolean? = result["ongoing"]
                            if (ongoing == null) {
                                response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                            } else {
                                response.putHeader("Content-Type", "text/plain")
                                        .setStatusCode(HttpResponseStatus.OK.code())
                                        .end(ongoing.toString())
                            }
                        }
                    }
                }
    }
}
