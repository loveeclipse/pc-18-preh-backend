package services

import io.vertx.core.Vertx
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import java.util.UUID
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.get

import utils.MongoUtils.checkSchema
import utils.MongoUtils.isDuplicateKey
import utils.MongoUtils.MONGODB_CONFIGURATION

object PatientsService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val ANAGRAPHIC = "anagraphic"
    private const val COLLECTION_NAME = "patients"
    private val PATIENT_SCHEMA = listOf("assignedEvent", "assignedMission", "anagraphic")
    private val PATIENT_REQUIRED_SCHEMA = listOf("assignedEvent", "assignedMission")
    private val ANAGRAPHIC_SCHEMA = listOf("name", "surname", "residency", "birthPlace", "birthDate", "gender",
            "anticoagulants", "antiplatelets")

    var vertx: Vertx? = null

    fun createPatient(routingContext: RoutingContext) {
        log.info("Request to create a new patients")
        val response = routingContext.response()
        val patientId = UUID.randomUUID().toString()
        val patientData = routingContext.bodyAsJson
        val uri = routingContext.request().absoluteURI().plus("/$patientId")
        val checkPatientSchema = checkSchema(patientData, PATIENT_REQUIRED_SCHEMA, PATIENT_SCHEMA)
        val checkAnagraphicSchema = patientData.containsKey(ANAGRAPHIC) &&
                checkSchema(patientData[ANAGRAPHIC], emptyList(), ANAGRAPHIC_SCHEMA)
        if (checkPatientSchema && (!patientData.containsKey(ANAGRAPHIC) || checkAnagraphicSchema)) {
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
                    else ->
                        response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }
}