import java.util.UUID
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.core.json.get
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.ext.web.RoutingContext
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.OK

object Handlers {

    private const val MISSIONS_COLLECTION = "missions"
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }
    private val trackingStepsConversions: Map<String, String> = mapOf(
            "oc-call" to "ocCall",
            "crew-departure" to "crewDeparture",
            "arrival-onsite" to "arrivalOnsite",
            "departure-onsite" to "departureOnsite",
            "landing-helipad" to "landingHelipad",
            "arrival-er" to "arrivalEr"
    )

    fun createMission(context: RoutingContext) {
        val response = context.response()
        val requestBody = context.bodyAsJson
        val eventId: String? = requestBody["eventId"]
        val vehicle: String? = requestBody["vehicle"]

        if (eventId.isNullOrBlank() || vehicle.isNullOrBlank()) {
            response.putHeader("Content-Type", "text/plain")
                    .setStatusCode(BAD_REQUEST.code())
                    .end("Please insert both \"eventId\" and \"vehicle\" fields in your request JSON body.")
        } else {
            val missionId = UUID.randomUUID()
            val newMission = json { obj(
                    "_id" to missionId.toString(),
                    "eventId" to eventId,
                    "vehicle" to vehicle,
                    "ongoing" to true
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .save(MISSIONS_COLLECTION, newMission) { saveOperation ->
                        if (saveOperation.failed()) {
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        } else {
                            response.putHeader("Content-Type", "text/plain")
                                    .setStatusCode(CREATED.code())
                                    .end(missionId.toString())
                        }
                    }
        }
    }

    fun retrieveMissions(context: RoutingContext) {
        val response = context.response()
        val params = context.request().params()

        val query = json { obj() }
        params["eventId"]?.let { query.put("eventId", it) }
        params["vehicle"]?.let { query.put("vehicle", it) }
        params["ongoing"]?.toBoolean()?.let { query.put("ongoing", it) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .find(MISSIONS_COLLECTION, query) { findOperation ->
                    if (findOperation.failed()) {
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val results: List<JsonObject> = findOperation.result()
                        if (results.isEmpty()) {
                            response.setStatusCode(NO_CONTENT.code()).end()
                        } else {
                            response.putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(results))
                        }
                    }
                }
    }

    fun retrieveMission(context: RoutingContext) {
        val response = context.response()
        val missionId: String? = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .findOne(MISSIONS_COLLECTION, query, null) { findOneOperation ->
                    if (findOneOperation.failed()) {
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val result: JsonObject? = findOneOperation.result()
                        if (result == null) {
                            response.setStatusCode(NOT_FOUND.code()).end()
                        } else {
                            response.putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(result))
                        }
                    }
                }
    }

    fun deleteMission(context: RoutingContext) {
        val response = context.response()
        val missionId: String? = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .removeDocument(MISSIONS_COLLECTION, query) { removeOperation ->
                    when {
                        removeOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        removeOperation.result().removedCount == 0L -> response.setStatusCode(NOT_FOUND.code()).end()
                        else -> response.setStatusCode(NO_CONTENT.code()).end()
                    }
                }
    }

    fun updateOngoing(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val ongoing: String = context.bodyAsString

        if (ongoing != "true" && ongoing != "false") {
            response.putHeader("Content-Type", "plain/text")
                    .setStatusCode(BAD_REQUEST.code())
                    .end("Invalid request body. Accepted values: true or false")
        } else {

            val query = json { obj("_id" to missionId) }
            val update = json { obj(
                    "\$set" to obj("ongoing" to ongoing.toBoolean())
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                        when {
                            updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docMatched == 0L -> response.setStatusCode(NOT_FOUND.code()).end()
                            else -> response.setStatusCode(NO_CONTENT.code()).end()
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
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val result: JsonObject? = findOneOperation.result()
                        if (result == null) {
                            response.setStatusCode(NOT_FOUND.code()).end()
                        } else {
                            val ongoing: Boolean? = result["ongoing"]
                            if (ongoing == null) {
                                response.setStatusCode(NO_CONTENT.code()).end()
                            } else {
                                response.putHeader("Content-Type", "text/plain")
                                        .setStatusCode(OK.code())
                                        .end(ongoing.toString())
                            }
                        }
                    }
                }
    }

    fun updateReturnInformation(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")
        val requestBody = context.bodyAsJson

        val query = json { obj("_id" to missionId) }
        val update = json { obj(
                "\$set" to obj("returnInformation" to requestBody)
        ) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                    when {
                        updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        updateOperation.result().docMatched == 0L -> response.setStatusCode(NOT_FOUND.code()).end()
                        else -> response.setStatusCode(NO_CONTENT.code()).end()
                    }
                }
    }

    fun retrieveReturnInformation(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .findOne(MISSIONS_COLLECTION, query, null) { findOneOperation ->
                    if (findOneOperation.failed()) {
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val result: JsonObject? = findOneOperation.result()
                        if (result == null) {
                            response.setStatusCode(NOT_FOUND.code()).end()
                        } else {
                            val returnInformation: JsonObject? = result["returnInformation"]
                            if (returnInformation == null) {
                                response.setStatusCode(NO_CONTENT.code()).end()
                            } else {
                                response.putHeader("Content-Type", "application/json")
                                        .setStatusCode(OK.code())
                                        .end(Json.encodePrettily(returnInformation))
                            }
                        }
                    }
                }
    }

    fun deleteReturnInformation(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        val update = json { obj(
                "\$unset" to obj("returnInformation" to 1)
        ) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                    when {
                        updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        updateOperation.result().docModified < 1L -> response.setStatusCode(NOT_FOUND.code()).end()
                        else -> response.setStatusCode(NO_CONTENT.code()).end()
                    }
                }
    }

    fun retrieveTracking(context: RoutingContext) {
        val response = context.response()
        val missionId: String = context.request().getParam("missionId")

        val query = json { obj("_id" to missionId) }
        MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                .find(MISSIONS_COLLECTION, query) { findOperation ->
                    if (findOperation.failed()) {
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    } else {
                        val results: List<JsonObject> = findOperation.result()
                        if (results.isEmpty()) {
                            response.setStatusCode(NOT_FOUND.code()).end()
                        } else {
                            val firstResult: JsonObject = results.first()
                            val tracking: JsonObject? = firstResult["tracking"]
                            if (tracking == null) {
                                response.setStatusCode(NO_CONTENT.code()).end()
                            } else {
                                response.putHeader("Content-Type", "application/json")
                                        .setStatusCode(OK.code())
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
                    .setStatusCode(NOT_FOUND.code())
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
                            updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docMatched == 0L -> response.setStatusCode(NOT_FOUND.code()).end()
                            else -> response.setStatusCode(NO_CONTENT.code()).end()
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
                    .setStatusCode(NOT_FOUND.code())
                    .end("Unknown tracking step: $step")
        } else {

            val query = json { obj("_id" to missionId) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .findOne(MISSIONS_COLLECTION, query, null) { findOneOperation ->
                        when {
                            findOneOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            findOneOperation.result() == null -> {
                                response.putHeader("Content-Type", "text/plain")
                                        .setStatusCode(NOT_FOUND.code())
                                        .end("Mission not found")
                            }
                            else -> {

                                val tracking: JsonObject? = findOneOperation.result()["tracking"]
                                if (tracking == null) {
                                    response.setStatusCode(NO_CONTENT.code()).end()
                                } else {
                                    val stepTracking: JsonObject? = tracking[trackingStepsConversions.getValue(step)]
                                    if (stepTracking == null) {
                                        response.setStatusCode(NO_CONTENT.code()).end()
                                    } else {
                                        response.putHeader("Content-Type", "application/json")
                                                .setStatusCode(OK.code())
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
                    .setStatusCode(NOT_FOUND.code())
                    .end("Unknown tracking step: $step")
        } else {

            val query = json { obj("_id" to missionId) }
            val update = json { obj(
                    "\$unset" to obj("tracking.${trackingStepsConversions[step]}" to 1)
            ) }
            MongoClient.createNonShared(Main.vertx, MONGODB_CONFIGURATION)
                    .updateCollection(MISSIONS_COLLECTION, query, update) { updateOperation ->
                        when {
                            updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            updateOperation.result().docModified < 1L -> response.setStatusCode(NOT_FOUND.code()).end()
                            else -> response.setStatusCode(NO_CONTENT.code()).end()
                        }
                    }
        }
    }
}
