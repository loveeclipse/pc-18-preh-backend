package handlers

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import handlers.Shared.MISSIONS_COLLECTION
import handlers.Shared.MONGODB_CONFIGURATION

object TrackingHandlers {
    private val trackingStepsConversions: Map<String, String> = mapOf(
            "oc-call" to "ocCall",
            "crew-departure" to "crewDeparture",
            "arrival-onsite" to "arrivalOnsite",
            "departure-onsite" to "departureOnsite",
            "landing-helipad" to "landingHelipad",
            "arrival-er" to "arrivalEr"
    )

    fun retrieveTracking(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .find(MISSIONS_COLLECTION, query) { findOperation ->
                    if (findOperation.failed()) {
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val results: List<JsonObject> = findOperation.result()
                        if (results.isEmpty()) {
                            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end()
                        } else {
                            val firstResult: JsonObject = results.first()
                            val tracking: JsonObject? = firstResult["tracking"]
                            if (tracking == null) {
                                response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                            } else {
                                response.putHeader("Content-Type", "application/json")
                                        .setStatusCode(HttpResponseStatus.OK.code())
                                        .end(Json.encodePrettily(tracking))
                            }
                        }
                    }
                }
    }

    fun updateTrackingStep(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val step = context.request().getParam("step")

        if (!trackingStepsConversions.containsKey(step)) {
            response.putHeader("Content-Type", "text/plain")
                    .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end("Unknown tracking step: $step")
        } else {

            val query = json { obj("_id" to missionId) }
            val requestBody: JsonObject? = context.bodyAsJson
            val update = json { obj(
                    "\$set" to obj(
                            "tracking.${trackingStepsConversions.getValue(step)}" to requestBody
                    )
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                        when {
                            updateOperation.failed() -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docMatched == 0L -> response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end()
                            else -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                        }
                    }
        }
    }

    fun retrieveTrackingStep(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val step = context.request().getParam("step")

        if (!trackingStepsConversions.containsKey(step)) {
            response.putHeader("Content-Type", "text/plain")
                    .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end("Unknown tracking step: $step")
        } else {

            val query = json { obj("_id" to missionId) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .findOne(MISSIONS_COLLECTION, query, null) { findOneOperation ->
                        when {
                            findOneOperation.failed() -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                            findOneOperation.result() == null -> {
                                response.putHeader("Content-Type", "text/plain")
                                        .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                                        .end("Mission not found")
                            }
                            else -> {

                                val tracking: JsonObject? = findOneOperation.result()["tracking"]
                                if (tracking == null) {
                                    response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                                } else {
                                    val stepTracking: JsonObject? = tracking[trackingStepsConversions.getValue(step)]
                                    if (stepTracking == null) {
                                        response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                                    } else {
                                        response.putHeader("Content-Type", "application/json")
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(stepTracking))
                                    }
                                }
                            }
                        }
                    }
        }
    }

    fun deleteTrackingStep(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val step = context.request().getParam("step")

        if (!trackingStepsConversions.containsKey(step)) {
            response.putHeader("Content-Type", "text/plain")
                    .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .end("Unknown tracking step: $step")
        } else {

            val query = json { obj("_id" to missionId) }
            val update = json { obj(
                    "\$unset" to obj("tracking.${trackingStepsConversions[step]}" to 1)
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                        when {
                            updateOperation.failed() -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docModified < 1L -> response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end()
                            else -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
                        }
                    }
        }
    }
}
