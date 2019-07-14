package utils

object ServiceData {
    val HOST = System.getenv("EVENTS_HOST")?.toString() ?: "localhost"
    val PORT = System.getenv("PORT")?.toInt() ?: 10000
}