package service

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.CONFLICT
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object ManeuversService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "maneuvers"
    private const val PATIENT_ID = "patientId"
    private const val MANEUVER_ID = "simpleManeuver"
    private const val TIME = "time"
    private const val DOCUMENT_ID = "_id"
    private const val DUPLICATED_KEY_CODE = "E11000"

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun createSimpleManeuver(routingContext: RoutingContext) {
        log.info("Request to create a new patients")
        val response = routingContext.response()
        val maneuverId = routingContext.request().params()[MANEUVER_ID]
        val patientId = routingContext.request().params()[PATIENT_ID]
        val time = routingContext.bodyAsString
        val document = json { obj(
                DOCUMENT_ID to maneuverId,
                PATIENT_ID to patientId,
                TIME to time
        ) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .insert(COLLECTION_NAME, document) { insertOperation ->
                    when {
                        insertOperation.succeeded() ->
                            response
                                    .setStatusCode(CREATED.code())
                                    .end()
                        isDuplicateKey(insertOperation.cause().message) ->
                            response.setStatusCode(CONFLICT.code()).end()
                        else ->
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
                }
    }

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    fun deleteSimpleManeuver(routingContext: RoutingContext) {
        log.info("Request to create a new patients")
        val response = routingContext.response()
        val maneuverId = routingContext.request().params()[MANEUVER_ID]
        val patientId = routingContext.request().params()[PATIENT_ID]
        val queryManeuvers = json { obj(
                DOCUMENT_ID to maneuverId,
                PATIENT_ID to patientId
        ) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .removeDocument(COLLECTION_NAME, queryManeuvers) { deleteOperation ->
            when {
                deleteOperation.succeeded() && deleteOperation.result().removedCount != 0L ->
                    response.setStatusCode(NO_CONTENT.code()).end()
                deleteOperation.succeeded() ->
                    response.setStatusCode(NOT_FOUND.code()).end()
                else ->
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }
}