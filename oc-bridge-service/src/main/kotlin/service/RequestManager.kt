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
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext

import java.util.UUID

object RequestManager {
    private var vertx: Vertx? = null
    private val CONFIG = JsonObject().put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr")
    private const val DOCUMENT_ID = "_id"
    private const val EVENT_ID = "eventId"
    private const val COLLECTION_NAME = "events"
    private const val DUPLICATED_KEY_CODE = "E11000"
    private const val SECONDARY = "secondary"

    fun initializeRequestManager(vertx: Vertx) {
        RequestManager.vertx = vertx
    }

    fun createEvent(routingContext: RoutingContext) {
        val response = routingContext.response()
        val uuid = UUID.randomUUID().toString()
        val document = JsonObject().put(DOCUMENT_ID, uuid)

        MongoClient.createNonShared(vertx, CONFIG).insert(COLLECTION_NAME, document) { result ->
            when {
                result.succeeded() -> response
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(CREATED.code())
                        .end(Json.encodePrettily(document))
                isDuplicateKey(result.cause().message) -> createEvent(routingContext)
                else -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    fun retrieveEventById(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = routingContext.request().getParam(EVENT_ID)
        try {
            UUID.fromString(eventId)
            val query = JsonObject().put(DOCUMENT_ID, eventId)
            MongoClient.createNonShared(vertx, CONFIG).find(COLLECTION_NAME, query) { result ->
                if (result.succeeded()) {
                    try {
                        val resultJson = result.result()[0]
                        resultJson.remove(DOCUMENT_ID)
                        if (resultJson.size() > 0)
                            response
                                    .putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(resultJson))
                        else
                            response.setStatusCode(NO_CONTENT.code()).end()
                    } catch (ex: IndexOutOfBoundsException) {
                        response.setStatusCode(NOT_FOUND.code()).end()
                    }
                } else {
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } catch (exception: IllegalArgumentException) {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun updateEvent(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = routingContext.request().getParam(EVENT_ID)
        try {
            UUID.fromString(eventId)
            val body = routingContext.bodyAsJson
            if (body.containsKey(SECONDARY))
                body.getBoolean(SECONDARY)
            val query = JsonObject().put(DOCUMENT_ID, eventId)
            val update = JsonObject().put("\$set", body)
            MongoClient.createNonShared(vertx, CONFIG).updateCollection(COLLECTION_NAME, query, update) { res ->
                if (res.succeeded()) {
                    when {
                        res.result().docModified == 0L -> response.setStatusCode(NOT_FOUND.code()).end()
                        else -> response.setStatusCode(OK.code()).end()
                    }
                } else {
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } catch (exception: Exception) {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }
}
