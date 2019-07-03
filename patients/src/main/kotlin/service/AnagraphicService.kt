package service

import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object AnagraphicService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val DOCUMENT_ID = "_id"
    private const val PATIENT_ID = "patientId"
    private const val COLLECTION_NAME = "patients"
    private val ANAGRAPHIC_SCHEMA = listOf("name", "surname", "residency", "birthPlace", "birthDate", "gender",
            "anticoagulants", "antiplatelets")

    var vertx: Vertx? = null
    private val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }

    fun updateAnagraphic(routingContext: RoutingContext) {
        log.info("Request to update the anagraphic data")
        val response = routingContext.response()
        val patientId = routingContext.request().params()[PATIENT_ID]
        val anagraphicData = routingContext.bodyAsJson
        if (checkSchema(anagraphicData, ANAGRAPHIC_SCHEMA, ANAGRAPHIC_SCHEMA)) {
            val query = json { obj(DOCUMENT_ID to patientId) }
            val update = json { obj("\$set" to anagraphicData) }
            MongoClient.createNonShared(vertx, MONGODB_CONFIGURATION)
                    .updateCollection(COLLECTION_NAME, query, update) { updateOperation ->
                        when {
                            updateOperation.succeeded() && updateOperation.result().docModified != 0L ->
                                response.setStatusCode(NO_CONTENT.code()).end()
                            updateOperation.succeeded() ->
                                response.setStatusCode(NOT_FOUND.code()).end()
                            else ->
                                response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
                        }
            }
        } else {
            response.setStatusCode(BAD_REQUEST.code()).end()
        }
    }

    fun retrieveAnagraphic(routingContext: RoutingContext) {
    }

    private fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }
}