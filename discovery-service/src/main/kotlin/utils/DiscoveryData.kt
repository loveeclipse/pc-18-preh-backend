package utils

object DiscoveryData {
    val PORT = System.getenv("PORT")?.toInt() ?: 5150
    val HOST = System.getenv("DISCOVERY_HOST")?.toString() ?: "localhost"

    private const val DISCOVERY_BASE_PATH = "/discovery"
    const val DISCOVERY_PUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/publish"
    const val DISCOVERY_UNPUBLISH_SERVICE = "$DISCOVERY_BASE_PATH/unpublish/:serviceRegistration"
    const val DISCOVERY_GET_SERVICE = "$DISCOVERY_BASE_PATH/discover/:serviceName"
}