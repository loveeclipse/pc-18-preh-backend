package services

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

import java.util.UUID

object RequestManager {
    var vertx: Vertx? = null
    private val CONFIG = json { obj(
            "connection_string" to System.getenv("MONGO_CONNECTION_STRING")?.toString()
    ) }
    private const val DOCUMENT_ID = "_id"
    private const val EVENT_ID = "eventId"
    private const val COLLECTION_NAME = "events"
    private const val DUPLICATED_KEY_CODE = "E11000"
    private const val FAILED_VALIDATION_MESSAGE = "Document failed validation"

    fun createEvent(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = UUID.randomUUID().toString()
        val uri = routingContext.request().absoluteURI().plus("/$eventId")
        val eventData = routingContext.bodyAsJson
        val document = eventData.put(DOCUMENT_ID, eventId).put("ongoing", true)
        MongoClient.createNonShared(vertx, CONFIG).insert(COLLECTION_NAME, document) { insertOperation ->
            when {
                insertOperation.succeeded() ->
                    response
                        .putHeader("Content-Type", "text/plain")
                        .putHeader("Location", uri)
                        .setStatusCode(CREATED.code())
                        .end(eventId)
                isDuplicateKey(insertOperation.cause().message) ->
                    createEvent(routingContext)
                insertOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
                else ->
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    fun retrieveEventById(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = routingContext.request().getParam(EVENT_ID)
        val query = json { obj(DOCUMENT_ID to eventId) }
        MongoClient.createNonShared(vertx, CONFIG).find(COLLECTION_NAME, query) { findOperation ->
            when {
                findOperation.succeeded() && findOperation.result().isNotEmpty() -> {
                    val foundItem = findOperation.result().first()
                    foundItem.remove(DOCUMENT_ID)
                    response
                            .putHeader("Content-Type", "application/json")
                            .setStatusCode(OK.code())
                            .end(Json.encodePrettily(foundItem))
                }
                findOperation.succeeded() ->
                    response.setStatusCode(NOT_FOUND.code()).end()
                else ->
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }

    fun updateEvent(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = routingContext.request().getParam(EVENT_ID)
        val eventData = routingContext.bodyAsJson
        val query = json { obj(DOCUMENT_ID to eventId) }
        val update = json { obj("\$set" to eventData) }
        MongoClient.createNonShared(vertx, CONFIG).updateCollection(COLLECTION_NAME, query, update) { updateOperation ->
            when {
                updateOperation.succeeded() && updateOperation.result().docMatched != 0L ->
                    response.setStatusCode(NO_CONTENT.code()).end()
                updateOperation.succeeded() ->
                    response.setStatusCode(NOT_FOUND.code()).end()
                updateOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
                else ->
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }
}
