package services

import io.vertx.core.Vertx
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import java.util.UUID
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.logging.LoggerFactory
import utils.MongoUtils.isDuplicateKey
import utils.MongoUtils.MONGODB_CONFIGURATION
import utils.MongoUtils.FAILED_VALIDATION_MESSAGE

object PatientsService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val COLLECTION_NAME = "patients"

    var vertx: Vertx? = null

    fun createPatient(routingContext: RoutingContext) {
        log.info("Request to create a new patients")
        val response = routingContext.response()
        val patientId = UUID.randomUUID().toString()
        val patientData = routingContext.bodyAsJson
        val uri = routingContext.request().absoluteURI().plus("/$patientId")
        val document = patientData.put(DOCUMENT_ID, patientId)
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .insert(COLLECTION_NAME, document) { insertOperation ->
            when {
                insertOperation.succeeded() ->
                    response
                        .putHeader("Content-Type", "text/plain")
                        .putHeader("Location", uri)
                        .setStatusCode(CREATED.code())
                        .end(patientId)
                isDuplicateKey(insertOperation.cause().message) ->
                    createPatient(routingContext)
                insertOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
                else ->
                    response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }
    }
}