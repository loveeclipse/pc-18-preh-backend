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

import services.utils.CheckSchema.checkSchema

object StatusService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "status"
    private val STATUS_SCHEMA = listOf("traumaticCondition", "closedTrauma", "penetratingTrauma", "helmetSeatbelt",
            "externalHaemorrhage", "respiratoryTract", "tachypneaDyspnoea", "thoraxDeformities", "ecofast",
            "deformedPelvis", "amputation", "sunkenSkullFracture", "otorrhagia", "paraparesis", "tetraparesis",
            "paraesthesia", "time")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun updateStatus(routingContext: RoutingContext) {
        log.info("Request to update a new patient status")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val statusData = routingContext.bodyAsJson
        if (checkSchema(statusData, STATUS_SCHEMA, STATUS_SCHEMA)) {
            val query = json { obj(DOCUMENT_ID to patientId) }
            val update = json { obj("\$set" to statusData) }
            val options = UpdateOptions(true)
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .updateCollectionWithOptions(COLLECTION_NAME, query, update, options) { updateOperation ->
                        when {
                            updateOperation.succeeded() ->
                                response.setStatusCode(NO_CONTENT.code()).end()
                            else ->
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
                    }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }
}