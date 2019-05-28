package service

import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.servicediscovery.Record
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.HttpEndpoint

object DiscoveryService {

    private val log = LoggerFactory.getLogger("DiscoveryService")

    private const val SERVICE_NAME = "serviceName"
    private const val SERVICE_HOST = "serviceHost"
    private const val SERVICE_PORT = "servicePort"
    private const val SERVICE_REGISTRATION = "serviceRegistration"
    private const val ROOT = "/v1"

    fun publishService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.info("Request to publish service")
        val response = routingContext.response()
        val serviceName = routingContext.request().params()[SERVICE_NAME]
        val serviceHost = routingContext.request().params()[SERVICE_HOST]
        val servicePort = routingContext.request().params()[SERVICE_PORT]
        val record = HttpEndpoint.createRecord(serviceName, serviceHost, servicePort.toInt(), ROOT)
        discovery?.publish(record) { ar ->
            if (ar.succeeded()) {
                response
                        .setStatusCode(CREATED.code())
                        .end()
                log.info("Service ${record.name} successfully published.")
            } else {
                response.setStatusCode(BAD_REQUEST.code()).end()
            }
        }
    }

    fun unpublishService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.debug("Request to unpublish service")
        val response = routingContext.response()
        val recordRegistration = JsonObject(routingContext.request().params()[SERVICE_REGISTRATION])
        // TODO: record with SERVICE_REGISTRATION value
        val record = Record()
        discovery?.unpublish(record.registration) { ar ->
            if (ar.succeeded()) {
                response
                        .setStatusCode(OK.code()).end()
                log.info("Service ${record.name} successfully unpublished.")
            } else {
                response.setStatusCode(BAD_REQUEST.code()).end()
            }
        }
    }

    fun getService(routingContext: RoutingContext, discovery: ServiceDiscovery?) {
        log.debug("Request to get all service list")
        val response = routingContext.response()
        val record = routingContext.request().params()[SERVICE_NAME]
        discovery?.getRecord({ r ->
            r.name == record
        }, { ar ->
            if (ar.succeeded() && ar.result() != null) {
                // prendere location service
                val reference = discovery.getReference(ar.result())
                // Retrieve the service object
                response.setStatusCode(OK.code()).end()
                log.info("Service $record successfully found.")
            } else {
                response.setStatusCode(BAD_REQUEST.code()).end()
            }
        })
    }
}