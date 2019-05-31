package service

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
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

    private val log = LoggerFactory.getLogger("VtService")
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    private var vertx: Vertx? = null
    fun initializeRequestManager(vertx: Vertx) {
        VtService.vertx = vertx
    }

    fun retrieveEventTracking(context: RoutingContext) {
        log.info("Request to retrieve an event's tracking details")
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]

        try {
            UUID.fromString(eventId)
            val query = json { obj(EVENT_ID to eventId) }

            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .find(COLLECTION_NAME, query) { findOperation ->
                        when {
                            findOperation.succeeded() -> {
                                val results: List<JsonObject> = findOperation.result()
                                if (results.isEmpty()) {
                                    response.setStatusCode(NOT_FOUND.code()).end()
                                } else {
                                    val cleanedResults: List<JsonObject> = results.map { r ->
                                        json { obj(
                                                MISSION_ID to r[MISSION_ID],
                                                TIMELINE to r[TIMELINE]
                                        ) }
                                    }
                                    response.putHeader("Content-Type", "application/json")
                                            .setStatusCode(OK.code())
                                            .end(Json.encodePrettily(cleanedResults))
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

    fun retrieveMissionTracking(context: RoutingContext) {
        log.info("Request to retrieve a mission's tracking details")
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

    fun retrieveTimelineItem(context: RoutingContext, trackingItem: TimelineItem) {
        log.info("Request to retrieve the ${trackingItem.fieldName} details for a certain mission")
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

    fun updateTimelineItem(context: RoutingContext, trackingItem: TimelineItem) {
        log.info("Request to update the ${trackingItem.fieldName} details for a certain mission")
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
        log.info("Request to retrieve the chosen hospital for a certain mission")
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
        log.info("Request to update the chosen hospital for a certain mission")
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
