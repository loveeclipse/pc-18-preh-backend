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

    private const val RESPONSE_PREFIX = "Response status: "
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

//        try {
//            /* Raises an IllegalArgumentException if the ID is not in the UUID format */
//            UUID.fromString(eventId)
//
//            val query = json { obj(EVENT_ID to eventId) }
//            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
//                    .find(COLLECTION_NAME, query) { findOperation ->
//                        if (findOperation.succeeded()) {
//                            val results: List<JsonObject> = findOperation.result()
//                            if (results.isEmpty()) {
//                                response.setStatusCode(NOT_FOUND.code()).end()
//                            } else {
//                                val  Result = results.first()
//                                response.putHeader("Content-Type", "application/json")
//                                        .setStatusCode(OK.code())
//                                        .end(Json.encodePrettily(firstResult[MISSION_TRACKING]))
//                            }
//                        } else {
//                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
//                        }
//                    }
//        } catch (_: IllegalArgumentException) {
//            response.setStatusCode(BAD_REQUEST.code()).end()
//        }

//        val response = context.response()
//        val params = context.request().params()
//        val document = json {
//            obj(DOCUMENT_IDENTIFIER to params[EVENT_IDENTIFIER])
//        }
//        try {
//            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
//                    .find(COLLECTION_NAME, document) { result ->
//                        if (result.succeeded()) {
//                            try {
//                                val queryResult = result.result().first()
//                                queryResult.remove(DOCUMENT_IDENTIFIER)
//                                if (!queryResult.isEmpty) {
//                                    response.putHeader("Content-Type", "application/json")
//                                            .setStatusCode(OK.code())
//                                            .end(Json.encodePrettily(queryResult.getJsonArray(MISSIONS)))
//                                } else {
//                                    response.setStatusCode(NO_CONTENT.code())
//                                            .end()
//                                }
//                            } catch (e1: Exception) {
//                                response.setStatusCode(BAD_REQUEST.code()).end()
//                                log.info(RESPONSE_PREFIX + response.statusCode)
//                            }
//                        } else {
//                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
//                        }
//                    }
//        } catch (e: Exception) {
//            response.setStatusCode(NOT_FOUND.code()).end()
//            log.info(RESPONSE_PREFIX + response.statusCode)
//        }
    }

    fun retrieveMissionTracking(context: RoutingContext) {
        log.info("Request to retrieve a mission's tracking details")
        val response = context.response()
        val eventId = context.request().params()[EVENT_ID]
        val missionId = context.request().params()[MISSION_ID]

        try {
            UUID.fromString(eventId)
            UUID.fromString(missionId)
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

    // TODO: implementarla parametrizzata con itemRep
    fun createSingleTrackingItem(context: RoutingContext, trackingItem: MissionTrackingItem) {
//        log.info("Request to create the ${trackingItem.fieldName} tracking details for a certain mission")
//        val response = context.response()
//        val params = context.request().params()
//        val document = json {
//            obj(DOCUMENT_IDENTIFIER to params[EVENT_IDENTIFIER])
//        }
//        val mission = json {
//            obj(MISSIONS to array(
//                    obj(
//                        MISSION_IDENTIFIER to params[MISSION_IDENTIFIER],
//                        MISSION_TRACKING to JsonObject()
//                    )
//            ))
//        }
//        val update = json {
//            obj("\$set" to mission)
//        }
//
//        try {
//            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
//                    .updateCollectionWithOptions(
//                        COLLECTION_NAME,
//                        document,
//                        update,
//                        UpdateOptions().setUpsert(true)) { updateOperation ->
//                            if (updateOperation.succeeded()) {
//                                try {
//                                    println(updateOperation.result())
//                                } catch (e1: Exception) {
//                                    response.setStatusCode(BAD_REQUEST.code()).end()
//                                    log.info(RESPONSE_PREFIX + response.statusCode)
//                                }
//                            } else {
//                                log.info("not succeeded")
//                            }
//                        }
//        } catch (e: Exception) {
//            response.setStatusCode(NOT_FOUND.code()).end()
//            log.info(RESPONSE_PREFIX + response.statusCode)
//        }
    }
}
