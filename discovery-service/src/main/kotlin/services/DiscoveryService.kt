package services

import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.HttpEndpoint

object DiscoveryService {

    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    private const val SERVICE_NAME = "serviceName"
    private const val SERVICE_HOST = "serviceHost"
    private const val SERVICE_PORT = "servicePort"
    private const val SERVICE_REGISTRATION = "serviceRegistration"
    private const val SERVICE_URI = ""

    private var registrationList: MutableList<String> = ArrayList()

    fun publishService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.info("Request to publish service")
        val response = routingContext.response()
        val serviceName = routingContext.request().params()[SERVICE_NAME]
        val serviceHost = routingContext.request().params()[SERVICE_HOST]
        val servicePort = routingContext.request().params()[SERVICE_PORT]
        val record = HttpEndpoint.createRecord(serviceName, serviceHost, servicePort.toInt(), SERVICE_URI)
        discovery?.publish(record) { publishOperation ->
            when {
                publishOperation.succeeded() -> {
                    registrationList.add(record.registration)
                    response
                            .putHeader("Content-Type", "text/plain")
                            .setStatusCode(CREATED.code())
                            .end(record.registration)
                }
                else ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
            }
        }
    }

    fun unpublishService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.info("Request to unpublish service")
        val response = routingContext.response()
        val recordRegistration = routingContext.request().params()[SERVICE_REGISTRATION]
        discovery?.unpublish(recordRegistration) { unpublishOperation ->
            when {
                unpublishOperation.succeeded() && recordRegistration.contains(recordRegistration) -> {
                    registrationList.remove(recordRegistration)
                    response.setStatusCode(OK.code()).end()
                }
                unpublishOperation.succeeded() ->
                    response.setStatusCode(NOT_FOUND.code()).end()
                else ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
            }
        }
    }

    fun retrieveService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.info("Request to get service location")
        val response = routingContext.response()
        val record = routingContext.request().params()[SERVICE_NAME]
        discovery?.getRecord({ r ->
            r.name == record
        }, { findOperation ->
            when {
                findOperation.succeeded() && findOperation.result() != null -> {
                    val reference = discovery.getReference(findOperation.result())
                    val location = reference.record().location.first().value.toString()
                    response
                            .putHeader("Content-Type", "text/plain")
                            .setStatusCode(OK.code())
                            .end(location)
                    reference.release()
                }
                findOperation.succeeded() ->
                    response.setStatusCode(NOT_FOUND.code()).end()
                else ->
                    response.setStatusCode(BAD_REQUEST.code()).end()
            }
        })
    }
}