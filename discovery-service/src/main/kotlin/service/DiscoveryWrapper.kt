package service

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient

data class DiscoveryWrapper(val discoveryHost: String, val discoveryPort: Int) {

    private val log = LoggerFactory.getLogger("DiscoveryVerticle")

    private val client: WebClient = WebClient.create(Vertx.vertx())

    fun publish(name: String, host: String, port: Int) {
        log.info("Publish wrappering")
        client.post(PORT, HOST, DISCOVERY_PUBLISH_SERVICE)
                .addQueryParam(SERVICE_NAME, name)
                .addQueryParam(SERVICE_HOST, host)
                .addQueryParam(SERVICE_PORT, port.toString())
                /*.send { result ->
                    if (result.succeeded()) {
                        val response = result.result()
                        log.info("Received response with status code${response.statusCode()}")
                    } else {
                        log.info("Something went wrong ${result.cause()}")
                    }
                }*/
    }

    fun unpublish(name: String, host: String, port: Int) {
        client.delete(DISCOVERY_UNPUBLISH_SERVICE)
                .addQueryParam(SERVICE_NAME, name)
                .addQueryParam(SERVICE_HOST, host)
                .addQueryParam(SERVICE_PORT, port.toString())
    }

    fun getService(name: String, host: String, port: Int) {
        client.get(DISCOVERY_GET_SERVICE)
                .addQueryParam(SERVICE_NAME, name)
                .addQueryParam(SERVICE_HOST, host)
                .addQueryParam(SERVICE_PORT, port.toString())
    }

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 5150
        private const val DISCOVERY_BASE_PATH = "/v1/discovery"
        private const val DISCOVERY_PUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/publish"
        private const val DISCOVERY_UNPUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/unpublish"
        private const val DISCOVERY_GET_SERVICE = "$DISCOVERY_BASE_PATH/discover"
        private const val SERVICE_NAME = "serviceName"
        private const val SERVICE_HOST = "serviceHost"
        private const val SERVICE_PORT = "servicePort"
        private const val SERVICE_REGISTRATION = "serviceRegistration"
    }
}