package services

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.UUID

object SimpleTreatmentsService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val COLLECTION_NAME = "simpletreatments"
    private const val PATIENT_ID = "patientId"
    private const val DOCUMENT_ID = "_id"
    private const val DUPLICATED_KEY_CODE = "E11000"
    private val SIMPLE_TREATMENT_SCHEMA = listOf("name", "time")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun createSimpleTreatment(routingContext: RoutingContext) {
        log.info("Request to create a simple treatments")
        val response = routingContext.response()
        val simpleTreatmentData = routingContext.bodyAsJson
        val patientId = routingContext.request().params()[PATIENT_ID]
        val simpleTreatmentId = UUID.randomUUID().toString()
        if (checkSchema(simpleTreatmentData, SIMPLE_TREATMENT_SCHEMA, SIMPLE_TREATMENT_SCHEMA)) {
            val document = simpleTreatmentData
                    .put(DOCUMENT_ID, simpleTreatmentId)
                    .put(PATIENT_ID, patientId)
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .insert(COLLECTION_NAME, document) { insertOperation ->
                        when {
                            insertOperation.succeeded() ->
                                response
                                        .putHeader("Content-Type", "text/plain")
                                        .setStatusCode(CREATED.code())
                                        .end(simpleTreatmentId)
                            isDuplicateKey(insertOperation.cause().message) ->
                                createSimpleTreatment(routingContext)
                            else ->
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } else response.setStatusCode(BAD_REQUEST.code()).end()
    }

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    private fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }
}