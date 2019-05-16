package service

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext

import java.util.UUID

object VtService {

    private val log = LoggerFactory.getLogger("VtService")

    private val mongodb_configuration = JsonObject().put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr")
    private const val DOCUMENT_IDENTIFER = "_id"
    private const val EVENT_IDENTIFER = "eventId"
    private const val MISSION_IDENTIFIER = "missionId"
    private const val COLLACTION_NAME = "vehicle"
    private var vertx: Vertx? = null


    fun initializeRequestManager(vertx: Vertx) {
        VtService.vertx = vertx
    }

    fun handlerGetAllEventsDetails(routingContext: RoutingContext) {
        log.info("Request get all events details")
       /* val response = routingContext.response()
        val uuid = UUID.randomUUID().toString()
        val document = JsonObject().put(document_identifier, uuid)

        MongoClient.createNonShared(vertx, mongodb_configuration).insert(collection_name, document) { result ->
            when {
                result.succeeded() -> response
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(CREATED.code())
                        .end(Json.encodePrettily(document))
                isDuplicateKey(result.cause().message) -> handleCreateEvent(routingContext)
                else -> response.setStatusCode(INTERNAL_SERVER_ERROR.code()).end()
            }
        }*/
    }

    fun handlerGetOcCall(routingContext: RoutingContext) {
        log.info("Request get OC details")
    }

    fun handlerPostOcCall(routingContext: RoutingContext) {
        log.info("Request post OC details")
    }

    fun handlerGetCrewDeparture(routingContext: RoutingContext) {
        log.info("Request get crew departure details")
    }

    fun handlerPostCrewDeparture(routingContext: RoutingContext) {
        log.info("Request post crew departure details")
    }

    fun handlerGetLandingOnsite(routingContext: RoutingContext) {
        log.info("Request get landing onsite details")
    }

    fun handlerPostLandingOnsite(routingContext: RoutingContext) {
        log.info("Request post landing onsite details")
    }

    fun handlerGetTakeoffOnsite(routingContext: RoutingContext) {
        log.info("Request get takeoff onsite details")
    }

    fun handlerPostTakeoffOnsite(routingContext: RoutingContext) {
        log.info("Request post takeoff onsite details")
    }

    fun handlerGetLandingHelipad(routingContext: RoutingContext) {
        log.info("Request get landing helipad details")
    }

    fun handlerPostLandingHelipad(routingContext: RoutingContext) {
        log.info("Request post landing helipad details")
    }

    fun handlerGetArrivalEr(routingContext: RoutingContext) {
        log.info("Request get arrival ER details")
    }

    fun handlerPostArrivalEr(routingContext: RoutingContext) {
        log.info("Request post arrival ER details")
    }
 }
