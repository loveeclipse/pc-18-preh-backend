package services

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import utils.MongoUtils.MONGODB_CONFIGURATION
import utils.MongoUtils.FAILED_VALIDATION_MESSAGE

object StatusService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "status"

    var vertx: Vertx? = null

    fun updateStatus(routingContext: RoutingContext) {
        log.info("Request to update a new patient status")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val statusData = routingContext.bodyAsJson
        val query = json { obj(DOCUMENT_ID to patientId) }
        val update = json { obj("\$set" to statusData) }
        val options = UpdateOptions(true)
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .updateCollectionWithOptions(COLLECTION_NAME, query, update, options) { updateOperation ->
                    when {
                        updateOperation.succeeded() ->
                            response.setStatusCode(NO_CONTENT.code()).end()
                        updateOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                            response.setStatusCode(BAD_REQUEST.code()).end()
                        else ->
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
                }
    }
}