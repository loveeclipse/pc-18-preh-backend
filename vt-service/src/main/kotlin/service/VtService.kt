package service

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.UUID

object VtService {
    private const val COLLECTION_NAME = "tracking"
    private const val EVENT_ID = "eventId"
    private const val MISSION_ID = "missionId"
    private const val MISSION_TRACKING = "missionTracking"

    private val log = LoggerFactory.getLogger("VtService")
    private val MONGODB_CONFIGURATION = JsonObject().put(
            "connection_string",
            "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr")

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
        } catch (_: IllegalArgumentException) {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }

        val query = json { obj(EVENT_ID to eventId) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .find(COLLECTION_NAME, query) { findOperation ->
                    when {
                        findOperation.succeeded() -> {
                            val results: List<JsonObject> = findOperation.result()
                            if (results.isEmpty()) {
                                response.setStatusCode(NOT_FOUND.code()).end()
                            }

                            val cleanedResult = results.map { r -> json {
                                obj(
                                        MISSION_ID to r[MISSION_ID],
                                        MISSION_TRACKING to r[MISSION_TRACKING])
                            } }
                            response.putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(cleanedResult))
                        }
                        findOperation.failed() -> {
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
                }
    }

    fun retrieveMissionTracking(context: RoutingContext) {
        log.info("Request to retrieve a mission's tracking details")
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]

        try {
            UUID.fromString(eventId)
        } catch (_: IllegalArgumentException) {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }

        val query = json {
            obj(
                    EVENT_ID to eventId,
                    MISSION_ID to missionId)
        }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .find(COLLECTION_NAME, query) { findOperation ->
                    when {
                        findOperation.succeeded() -> {
                            val results: List<JsonObject> = findOperation.result()
                            if (results.isEmpty()) {
                                response.setStatusCode(NOT_FOUND.code()).end()
                            }

                            val firstResult = results.first()
                            response.putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(firstResult[MISSION_TRACKING]))
                        }
                        findOperation.failed() -> {
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
                }
    }

    fun retrieveSingleTrackingItem(context: RoutingContext, trackingItem: MissionTrackingItem) {
        log.info("Request to retrieve the ${trackingItem.fieldName} tracking details for a certain mission")
        val response = context.response()
        val params = context.request().params()
        // TODO
    }

    fun createSingleTrackingItem(context: RoutingContext, trackingItem: MissionTrackingItem) {
        log.info("Request to create the ${trackingItem.fieldName} tracking details for a certain mission")
        val response = context.response()
        val params = context.request().params()
        // TODO
    }
}
