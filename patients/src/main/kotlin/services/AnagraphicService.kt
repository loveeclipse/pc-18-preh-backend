package services

import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import utils.MongoUtils.FAILED_VALIDATION_MESSAGE
import utils.MongoUtils.MONGODB_CONFIGURATION

object AnagraphicService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "patients"
    private const val ANAGRAPHIC = "anagraphic"

    var vertx: Vertx? = null

    fun updateAnagraphic(routingContext: RoutingContext) {
        log.info("Request to update the anagraphic data")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val anagraphicData = routingContext.bodyAsJson
        val query = json { obj(DOCUMENT_ID to patientId) }
        val update = json { obj("\$set" to obj(ANAGRAPHIC to anagraphicData)) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .updateCollection(COLLECTION_NAME, query, update) { updateOperation ->
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

    fun retrieveAnagraphic(routingContext: RoutingContext) {
        log.info("Request to retrieve the anagraphic data")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val anagraphicData = json { obj(DOCUMENT_ID to patientId) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .find(COLLECTION_NAME, anagraphicData) { findOperation ->
                    when {
                        findOperation.succeeded() && findOperation.result().isNotEmpty() -> {
                            val foundEntry = findOperation.result().first()
                            foundEntry.remove(DOCUMENT_ID)
                            response
                                    .putHeader("Content-Type", "application/json")
                                    .setStatusCode(OK.code())
                                    .end(Json.encodePrettily(foundEntry))
                        }
                        findOperation.succeeded() ->
                            response.setStatusCode(NOT_FOUND.code()).end()
                        else ->
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
        }
    }
}