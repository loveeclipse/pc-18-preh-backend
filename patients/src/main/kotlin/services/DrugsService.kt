package services

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import java.util.UUID

import utils.MongoUtils.checkSchema
import utils.MongoUtils.isDuplicateKey
import utils.MongoUtils.MONGODB_CONFIGURATION

object DrugsService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "drugs"
    private const val PATIENT_ID = "patientId"
    private const val DOCUMENT_ID = "_id"
    private val DRUGS_SCHEMA = listOf("name", "quantity", "measurementUnit", "time")

    var vertx: Vertx? = null

    fun createDrug(routingContext: RoutingContext) {
        log.info("Request to create a drug")
        val response = routingContext.response()
        val drugData = routingContext.bodyAsJson
        val patientId = routingContext.request().params()[PATIENT_ID]
        val drugId = UUID.randomUUID().toString()
        val uri = routingContext.request().absoluteURI().plus("/$drugId")
        if (checkSchema(drugData, DRUGS_SCHEMA, DRUGS_SCHEMA)) {
            val document = drugData
                    .put(DOCUMENT_ID, drugId)
                    .put(PATIENT_ID, patientId)
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .insert(COLLECTION_NAME, document) { insertOperation ->
                        when {
                            insertOperation.succeeded() ->
                                response
                                        .putHeader("Content-Type", "text/plain")
                                        .putHeader("Location", uri)
                                        .setStatusCode(CREATED.code())
                                        .end(drugId)
                            isDuplicateKey(insertOperation.cause().message) ->
                                createDrug(routingContext)
                            else ->
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } else response.setStatusCode(BAD_REQUEST.code()).end()
    }
}