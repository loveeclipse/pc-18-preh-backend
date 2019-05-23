package service

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.*
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.UpdateOptions

object VtService {

    private val log = LoggerFactory.getLogger("VtService")

    private val MONGODB_CONFIGURATION = JsonObject().put(
            "connection_string",
            "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr")
    private const val DOCUMENT_IDENTIFIER = "_id"
    private const val EVENT_IDENTIFIER = "eventId"
    private const val MISSION_IDENTIFIER = "missionId"
    private const val COLLECTION_NAME = "vehicle"
    private const val MISSIONS = "missions"
    private var vertx: Vertx? = null

    fun initializeRequestManager(vertx: Vertx) {
        VtService.vertx = vertx
    }

    fun retrieveEventTracking(routingContext: RoutingContext) {
        log.info("Request get all events details")
        val response = routingContext.response()
        val params = routingContext.request().params()
        val document = json {
            obj(DOCUMENT_IDENTIFIER to params[EVENT_IDENTIFIER])
        }
        try{
            MongoClient
                    .createNonShared(vertx, MONGODB_CONFIGURATION)
                    .find(COLLECTION_NAME, document) { result ->
                        if (result.succeeded()) {
                            try {
                                val queryResult = result.result().first()
                                queryResult.remove(DOCUMENT_IDENTIFIER)
                                if (!queryResult.isEmpty) {
                                    response
                                            .putHeader("Content-Type", "application/json")
                                            .setStatusCode(OK.code())
                                            .end(Json.encodePrettily(queryResult))
                                } else {
                                    response
                                            .setStatusCode(NO_CONTENT.code())
                                            .end()
                                }
                            } catch (e1: Exception) {
                                response.setStatusCode(BAD_REQUEST.code()).end()
                                log.info("Response status ${response.statusCode}")
                            }
                        } else {
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
            }
        } catch (e: Exception){
            response.setStatusCode(NOT_FOUND.code()).end()
            log.info("Response status ${response.statusCode}")
        }
    }

    fun retrieveMissionTracking(routingContext: RoutingContext) {
        log.info("Request get all missions details")
        val response = routingContext.response()
        val params = routingContext.request().params()
        val document = json {
            obj(DOCUMENT_IDENTIFIER to params[EVENT_IDENTIFIER],
                    "$MISSIONS.$MISSION_IDENTIFIER" to params[MISSION_IDENTIFIER])
        }
        try{
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION).findWithOptions(
                    COLLECTION_NAME,
                    document,
                    FindOptions()
                            .setFields(json {obj(DOCUMENT_IDENTIFIER to false)})
                            .setLimit(1)
            ) { result ->
                when { result.succeeded() ->
                    try {
                        val queryResult = result.result().first()
                        val res = queryResult.getJsonArray(MISSIONS).filter { a ->
                            (a as JsonObject).getString(MISSION_IDENTIFIER) == params[MISSION_IDENTIFIER]
                        }
                        when {
                            !queryResult.isEmpty ->
                                response
                                        .putHeader("Content-Type", "application/json")
                                        .setStatusCode(OK.code())
                                        .end(Json.encodePrettily(res))
                        }
                    } catch (e1: Exception) {
                        response.setStatusCode(BAD_REQUEST.code()).end()
                        log.info("Response status ${response.statusCode}")
                    }
                    else  ->  response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } catch (e: Exception) {
            response.setStatusCode(NOT_FOUND.code()).end()
            log.info("Response status ${response.statusCode}")
        }
    }

    fun handlerGetOcCall(routingContext: RoutingContext) {
        log.info("Request get OC details")
    }

    fun handlerPostOcCall(routingContext: RoutingContext) {
        log.info("Request post OC details")
        val response = routingContext.response()
        val params = routingContext.request().params()
        val document = json {
            obj(DOCUMENT_IDENTIFIER to params[EVENT_IDENTIFIER])
        }
        val mission = json {
            obj(MISSIONS to array(obj(MISSION_IDENTIFIER to params[MISSION_IDENTIFIER],
                    "missionTracking" to JsonObject())))
        }
        val update = json {
            obj("\$set" to mission)
        }

        try {
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION).updateCollectionWithOptions(
                    COLLECTION_NAME,
                    document,
                    update,
                    UpdateOptions()
                            .setUpsert(true))
            { result ->
                when { result.succeeded() ->
                    try {
                        val queryResult = result.result()
                        println(queryResult)
                    } catch (e1: Exception) {
                        response.setStatusCode(BAD_REQUEST.code()).end()
                        log.info("Response status ${response.statusCode}")
                    }

                    else -> log.info("not succeeded")
                }
            }
        } catch (e: Exception){
            response.setStatusCode(NOT_FOUND.code()).end()
            log.info("Response status ${response.statusCode}")
        }
    }

    fun handlerGetCrewDeparture(routingContext: RoutingContext) {
        log.info("Request get crew departure details")
    }

    fun handlerPostCrewDeparture(routingContext: RoutingContext) {
        log.info("Request post crew departure details")
    }

    fun handlerGetLandingOnsite(routingContext: RoutingContext) {
        log.info("Request get landing onsite details")
    }

    fun handlerPostLandingOnsite(routingContext: RoutingContext) {
        log.info("Request post landing onsite details")
    }

    fun handlerGetTakeoffOnsite(routingContext: RoutingContext) {
        log.info("Request get takeoff onsite details")
    }

    fun handlerPostTakeoffOnsite(routingContext: RoutingContext) {
        log.info("Request post takeoff onsite details")
    }

    fun handlerGetLandingHelipad(routingContext: RoutingContext) {
        log.info("Request get landing helipad details")
    }

    fun handlerPostLandingHelipad(routingContext: RoutingContext) {
        log.info("Request post landing helipad details")
    }

    fun handlerGetArrivalEr(routingContext: RoutingContext) {
        log.info("Request get arrival ER details")
    }

    fun handlerPostArrivalEr(routingContext: RoutingContext) {
        log.info("Request post arrival ER details")
    }
 }
