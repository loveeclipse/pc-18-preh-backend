package services

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.UUID

import services.utils.CheckSchema.checkSchema
import services.utils.DuplicatedKey.isDuplicateKey

object VitalParametersService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "vitalparameters"
    private const val PATIENT_ID = "patientId"
    private const val DOCUMENT_ID = "_id"
    private val VITAL_PARAMETERS_SCHEMA = listOf("respiratoryTract", "breathingRate", "outlyingSaturationPercentage",
            "heartbeatRate", "heartbeatType", "bloodPressure", "capRefillTime", "skinColor", "eyeOpening",
            "verbalResponse", "motorResponse", "leftPupil", "rightPupil", "leftPhotoReactive", "rightPhotoReactive",
            "temperatureInCelsius", "time")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun createVitalParameters(routingContext: RoutingContext) {
        log.info("Request to create vital parameters snapshot")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val vitalParameterId = UUID.randomUUID().toString()
        val vitalParametersData = routingContext.bodyAsJson
        val uri = routingContext.request().absoluteURI().plus("/$vitalParameterId")
        if (checkSchema(vitalParametersData, VITAL_PARAMETERS_SCHEMA, VITAL_PARAMETERS_SCHEMA)) {
            val document = vitalParametersData
                    .put(DOCUMENT_ID, vitalParameterId)
                    .put(PATIENT_ID, patientId)
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .insert(COLLECTION_NAME, document) { insertOperation ->
                        when {
                            insertOperation.succeeded() ->
                                response
                                        .putHeader("Content-Type", "text/plain")
                                        .putHeader("Location", uri)
                                        .setStatusCode(CREATED.code())
                                        .end(vitalParameterId)
                            isDuplicateKey(insertOperation.cause().message) ->
                                createVitalParameters(routingContext)
                            else ->
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } else
            response.setStatusCode(BAD_REQUEST.code()).end()
    }
}