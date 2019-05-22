package service


import io.netty.handler.codec.http.HttpResponseStatus.*
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
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "patients"
    private const val DUPLICATED_KEY_CODE = "E11000"

    fun initializeRequestManager(vertx: Vertx) {
        RequestManager.vertx = vertx
    }

    fun handleNewPatient(routingContext: RoutingContext) {
        val response = routingContext.response()
        val patientUuid = UUID.randomUUID().toString()
        val document = routingContext.bodyAsJson
        document.put(DOCUMENT_ID, patientUuid)
        println(document)

        MongoClient.createNonShared(vertx, CONFIG).insert(COLLECTION_NAME, document) { result ->
            when {
                result.succeeded() -> response
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(CREATED.code())
                        .end(Json.encodePrettily(document))
                isDuplicateKey(result.cause().message) -> handleNewPatient(routingContext)
                else -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }

    fun handleGetPatientData(routingContext: RoutingContext){
        val response = routingContext.response()
        val patientId = routingContext.request().getParam(PATIENT_ID)

        try {
            UUID.fromString(patientId)
            val query = JsonObject().put(DOCUMENT_ID, patientId)
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
                        response.setStatusCode(BAD_REQUEST.code()).end()
                    }
                } else {
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } catch (exception: IllegalArgumentException) {
            response.setStatusCode(NOT_FOUND.code()).end()
        }
    }

    fun handleUpdatePatientData(routingContext: RoutingContext) {
        val response = routingContext.response()
        val patientId = routingContext.request().getParam(PATIENT_ID)
        try {
            UUID.fromString(patientId)
            val body = routingContext.bodyAsJson
            val query = JsonObject().put(DOCUMENT_ID, patientId)
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

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

}