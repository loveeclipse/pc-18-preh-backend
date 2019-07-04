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

import services.utils.CheckSchema.checkSchema

object AnagraphicService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "patients"
    private const val ANAGRAPHIC = "anagraphic"
    private val ANAGRAPHIC_SCHEMA = listOf("name", "surname", "residency", "birthPlace", "birthDate", "gender",
            "anticoagulants", "antiplatelets")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun updateAnagraphic(routingContext: RoutingContext) {
        log.info("Request to update the anagraphic data")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val anagraphicData = routingContext.bodyAsJson
        if (checkSchema(anagraphicData, ANAGRAPHIC_SCHEMA, ANAGRAPHIC_SCHEMA)) {
            val query = json { obj(DOCUMENT_ID to patientId) }
            val update = json { obj("\$set" to obj(ANAGRAPHIC to anagraphicData)) }
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .updateCollection(COLLECTION_NAME, query, update) { updateOperation ->
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