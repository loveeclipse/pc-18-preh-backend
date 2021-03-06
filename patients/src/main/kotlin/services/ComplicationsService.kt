package services

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
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
import utils.MongoUtils.isDuplicateKey
import utils.MongoUtils.FAILED_VALIDATION_MESSAGE
import utils.MongoUtils.MONGODB_CONFIGURATION

object ComplicationsService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "complications"
    private const val PATIENT_ID = "patientId"
    private const val COMPLICATION_ID = "complication"
    private const val TIME = "time"

    var vertx: Vertx? = null

    fun createComplication(routingContext: RoutingContext, complicationId: String) {
        log.info("Request to create a complication")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val time = routingContext.bodyAsString
        val document = json { obj(
                COMPLICATION_ID to complicationId,
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
                        insertOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                            response.setStatusCode(BAD_REQUEST.code()).end()
                        else ->
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
                }
    }

    fun deleteComplication(routingContext: RoutingContext, complicationId: String) {
        log.info("Request to delete a complication")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val document = json { obj(
                COMPLICATION_ID to complicationId,
                PATIENT_ID to patientId
        ) }
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .removeDocument(COLLECTION_NAME, document) { deleteOperation ->
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