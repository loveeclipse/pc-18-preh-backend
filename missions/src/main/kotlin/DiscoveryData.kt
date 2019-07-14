object DiscoveryData {
    val DISCOVERY_HOST = System.getenv("DISCOVERY_HOST")?.toString() ?: "localhost"
    val DISCOVERY_PORT = System.getenv("DISCOVERY_PORT")?.toInt() ?: 5150

    const val DISCOVERY_PUBLISH_SERVICE = "/discovery/publish"

    const val SERVICE_NAME = "serviceName"
    const val SERVICE_HOST = "serviceHost"
    const val SERVICE_PORT = "servicePort"
}
