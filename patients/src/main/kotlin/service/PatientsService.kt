package service

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.UUID
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.kotlin.core.json.get

object PatientsService {

    private const val DOCUMENT_ID = "_id"
    private const val ANAGRAPHIC = "anagraphic"
    private const val COLLECTION_NAME = "patients"
    private const val DUPLICATED_KEY_CODE = "E11000"
    private val PATIENT_SCHEMA = listOf("assignedEvent", "assignedMission", "anagraphic")
    private val PATIENT_REQUIRED_SCHEMA = listOf("assignedEvent", "assignedMission")
    private val ANAGRAPHIC_SCHEMA = listOf("name", "surname", "residency", "birthPlace", "birthDate", "gender",
            "anticoagulants", "antiplatelets")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun createPatient(routingContext: RoutingContext) {
        val response = routingContext.response()
        val uuid = UUID.randomUUID().toString()
        val body = routingContext.bodyAsJson
        val checkPatientSchema = checkSchema(body, PATIENT_REQUIRED_SCHEMA, PATIENT_SCHEMA)
        val checkAnagraphicSchema = body.containsKey(ANAGRAPHIC) &&
                checkSchema(body[ANAGRAPHIC], emptyList(), ANAGRAPHIC_SCHEMA)

        if (checkPatientSchema && (!body.containsKey(ANAGRAPHIC) || checkAnagraphicSchema)) {
            val document = body.put(DOCUMENT_ID, uuid)
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION).insert(COLLECTION_NAME, document) { insertOperation ->
                when {
                    insertOperation.succeeded() ->
                        response
                            .putHeader("Content-Type", "text/plain")
                            .setStatusCode(CREATED.code())
                            .end(uuid)
                    isDuplicateKey(insertOperation.cause().message) -> createPatient(routingContext)
                    else -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                }
            }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    private fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    private fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }
}