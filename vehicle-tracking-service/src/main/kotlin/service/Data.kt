package service

object Data {
    const val DISCOVERY_HOST = "localhost"
    const val DISCOVERY_PORT = 5150
    const val NAME = "vehicle-tracking-service"
    const val HOST = "localhost"
    const val PORT = 10000

    private const val DISCOVERY_BASE_PATH = "/v1/discovery"
    const val DISCOVERY_PUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/publish"

    const val SERVICE_NAME = "serviceName"
    const val SERVICE_HOST = "serviceHost"
    const val SERVICE_PORT = "servicePort"
}