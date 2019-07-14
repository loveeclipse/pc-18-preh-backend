package services

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import java.util.UUID
import utils.MongoUtils.isDuplicateKey
import utils.MongoUtils.MONGODB_CONFIGURATION
import utils.MongoUtils.FAILED_VALIDATION_MESSAGE

object IppvTreatmentsService {
    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "ippvtreatments"
    private const val PATIENT_ID = "patientId"
    private const val DOCUMENT_ID = "_id"

    var vertx: Vertx? = null

    fun createIppvTreatment(routingContext: RoutingContext) {
        log.info("Request to create a ippv treatment")
        val response = routingContext.response()
        val ippvTreatmentData = routingContext.bodyAsJson
        val patientId = routingContext.request().params()[PATIENT_ID]
        val ippvTreatmentId = UUID.randomUUID().toString()
        val uri = routingContext.request().absoluteURI().plus("/$ippvTreatmentId")
        val document = ippvTreatmentData
                .put(DOCUMENT_ID, ippvTreatmentId)
                .put(PATIENT_ID, patientId)
        MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                .insert(COLLECTION_NAME, document) { insertOperation ->
                    when {
                        insertOperation.succeeded() ->
                            response
                                    .putHeader("Content-Type", "text/plain")
                                    .putHeader("Location", uri)
                                    .setStatusCode(CREATED.code())
                                    .end(ippvTreatmentId)
                        isDuplicateKey(insertOperation.cause().message) ->
                            createIppvTreatment(routingContext)
                        insertOperation.cause().message == FAILED_VALIDATION_MESSAGE ->
                            response.setStatusCode(BAD_REQUEST.code()).end()
                        else ->
                            response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                    }
                }
    }
}