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
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

import java.util.UUID

object RequestManager {
    var vertx: Vertx? = null
    private val CONFIG = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }
    private const val DOCUMENT_ID = "_id"
    private const val EVENT_ID = "eventId"
    private const val COLLECTION_NAME = "events"
    private const val DUPLICATED_KEY_CODE = "E11000"
    private val EVENT_SCHEMA = listOf("callTime", "address", "notes", "dispatchCode", "secondary", "dynamic",
            "patientsNumber", "ongoing")
    private val EVENT_REQUIRED_SCHEMA = listOf("callTime", "address")

    fun createEvent(routingContext: RoutingContext) {
        val response = routingContext.response()
        val eventId = UUID.randomUUID().toString()
        val eventData = routingContext.bodyAsJson
        if (checkSchema(eventData, EVENT_REQUIRED_SCHEMA, EVENT_SCHEMA)) {
            val document = eventData.put(DOCUMENT_ID, eventId)
            MongoClient.createNonShared(vertx, CONFIG).insert(COLLECTION_NAME, document) { insertOperation ->
                when {
                    insertOperation.succeeded() ->
                        response
                            .putHeader("Content-Type", "text/plain")
                            .setStatusCode(CREATED.code())
                            .end(eventId)
                    isDuplicateKey(insertOperation.cause().message) ->
                        createEvent(routingContext)
                    else ->
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
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
        if (checkSchema(eventData, emptyList(), EVENT_SCHEMA)) {
            val query = json { obj(DOCUMENT_ID to eventId) }
            val update = json { obj("\$set" to eventData) }
            MongoClient.createNonShared(vertx, CONFIG).updateCollection(COLLECTION_NAME, query, update) { updateOperation ->
                when {
                    updateOperation.succeeded() && updateOperation.result().docModified != 0L ->
                        response.setStatusCode(NO_CONTENT.code()).end()
                    updateOperation.succeeded() ->
                        response.setStatusCode(NOT_FOUND.code()).end()
                    else ->
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    private fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }
}
