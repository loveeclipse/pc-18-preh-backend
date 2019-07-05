package service

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.core.json.get
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.ext.web.RoutingContext
import java.util.UUID

object VtService {
    private const val COLLECTION_NAME = "tracking"
    private const val EVENT_ID = "eventId"
    private const val MISSION_ID = "missionId"
    private const val TIMELINE = "timeline"
    private const val CHOSEN_HOSPITAL = "chosenHospital"

    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    private var vertx: Vertx? = null
    fun initializeRequestManager(vertx: Vertx) {
        VtService.vertx = vertx
    }

    fun createMission(context: RoutingContext) {
        val response = context.response()
        val requestBody = context.bodyAsJson
        val eventId: String? = requestBody["eventId"]
        val vehicle: String? = requestBody["vehicle"]

        if (eventId.isNullOrEmpty() || vehicle.isNullOrEmpty()) {
            response.putHeader("Content-Type", "text/plain")
                    .setStatusCode(BAD_REQUEST.code())
                    .end("Please insert \"eventId\" and \"vehicle\" fields in your request JSON body.")

        } else {
            val missionId = UUID.randomUUID()
            val newMission = json { obj(
                    "_id" to missionId.toString(),
                    "eventId" to eventId,
                    "vehicle" to vehicle,
                    "ongoing" to true
            ) }
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .save("missions", newMission) { saveOperation ->
                        if (saveOperation.succeeded()) {
                            response.putHeader("Content-Type", "text/plain")
                                    .setStatusCode(CREATED.code())
                                    .end(missionId.toString())
                        } else {
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        }
    }

    fun retrieveMissions(context: RoutingContext) {
        val response = context.response()
        val params = context.request().params()

        val query = json { obj() }
        params["ongoing"]?.toBoolean()?.let { query.put("ongoing", it) }
        params["vehicle"]?.let { query.put("vehicle", it) }
        params["eventId"]?.let { query.put("eventId", it) }

        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .find("missions", query) { findOperation ->
                    if (findOperation.succeeded()) {
                        val results: List<JsonObject> = findOperation.result()
                        if (results.isEmpty()) {
                            response.setStatusCode(NO_CONTENT.code()).end()
                        } else {
                            response.putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(results))
                        }
                    } else {
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
                }
    }

    fun retrieveMissionTracking(context: RoutingContext) {
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]

        try {
            UUID.fromString(eventId)
            val query = json { obj(
                        EVENT_ID to eventId,
                        MISSION_ID to missionId
            ) }

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .find(COLLECTION_NAME, query) { findOperation ->
                        when {
                            findOperation.succeeded() -> {
                                val results: List<JsonObject> = findOperation.result()
                                if (results.isEmpty()) {
                                    response.setStatusCode(NOT_FOUND.code()).end()
                                } else {
                                    val firstResult = results.first()
                                    response.putHeader("Content-Type", "application/json")
                                            .setStatusCode(OK.code())
                                            .end(Json.encodePrettily(firstResult[TIMELINE]))
                                }
                            }
                            findOperation.failed() -> {
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            }
                        }
                    }
        } catch (_: IllegalArgumentException) { // eventId is not an UUID
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun retrieveTimelineItem(context: RoutingContext, trackingItem: TrackingStep) {
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]

        try {
            UUID.fromString(eventId)
            val query = json { obj(
                        EVENT_ID to eventId,
                        MISSION_ID to missionId
            ) }

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .find(COLLECTION_NAME, query) { findOperation ->
                        when {
                            findOperation.succeeded() -> {
                                val results: List<JsonObject> = findOperation.result()
                                if (results.isEmpty()) {
                                    response.setStatusCode(NOT_FOUND.code()).end()
                                } else {
                                    val firstResult: JsonObject = results.first()
                                    val missionTracking: JsonObject = firstResult["missionTracking"]
                                    val desiredTrackingItem: JsonObject? = missionTracking[trackingItem.fieldName]

                                    desiredTrackingItem?.let {
                                        response.putHeader("Content-Type", "application/json")
                                                .setStatusCode(OK.code())
                                                .end(Json.encodePrettily(desiredTrackingItem))
                                    } ?: run {
                                        response.setStatusCode(NO_CONTENT.code()).end()
                                    }
                                }
                            }
                            findOperation.failed() -> {
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            }
                        }
                    }
        } catch (_: IllegalArgumentException) { // eventId is not an UUID
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun updateTimelineItem(context: RoutingContext, trackingItem: TrackingStep) {
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]
        val requestBody = context.bodyAsJson

        try {
            UUID.fromString(eventId)
            val query = json { obj(
                        EVENT_ID to eventId,
                        MISSION_ID to missionId
            ) }
            val update = json { obj(
                    "\$set" to obj(
                            "$TIMELINE.${trackingItem.fieldName}" to requestBody
                    )
            ) }
            val options = UpdateOptions(true)

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .updateCollectionWithOptions(COLLECTION_NAME, query, update, options) { updateOperation ->
                        when {
                            updateOperation.succeeded() -> response.setStatusCode(OK.code()).end()
                            updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } catch (_: IllegalArgumentException) { // eventId is not an UUID
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun retrieveChosenHospital(context: RoutingContext) {
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]

        try {
            UUID.fromString(eventId)
            val query = json { obj(
                        EVENT_ID to eventId,
                        MISSION_ID to missionId
            ) }

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .find(COLLECTION_NAME, query) { findOperation ->
                        when {
                            findOperation.succeeded() -> {
                                val results: List<JsonObject> = findOperation.result()
                                if (results.isEmpty()) {
                                    response.setStatusCode(NOT_FOUND.code()).end()
                                } else {
                                    val firstResult: JsonObject = results.first()
                                    val chosenHospital: String? = firstResult[CHOSEN_HOSPITAL]

                                    chosenHospital?.let {
                                        response.putHeader("Content-Type", "text/plain")
                                                .setStatusCode(OK.code())
                                                .end(chosenHospital)
                                    } ?: run {
                                        response.setStatusCode(NO_CONTENT.code()).end()
                                    }
                                }
                            }
                            findOperation.failed() -> {
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                            }
                        }
                    }
        } catch (_: IllegalArgumentException) { // eventId is not an UUID
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun updateChosenHospital(context: RoutingContext) {
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]
        val hospitalName = context.bodyAsString

        try {
            UUID.fromString(eventId)
            val query = json { obj(
                        EVENT_ID to eventId,
                        MISSION_ID to missionId
            ) }
            val update = json { obj(
                        "\$set" to obj(
                                CHOSEN_HOSPITAL to hospitalName)
            ) }
            val options = UpdateOptions(true)

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .updateCollectionWithOptions(COLLECTION_NAME, query, update, options) { updateOperation ->
                        when {
                            updateOperation.succeeded() -> response.setStatusCode(OK.code()).end()
                            updateOperation.failed() -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } catch (_: IllegalArgumentException) { // eventId is not an UUID
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }
}
