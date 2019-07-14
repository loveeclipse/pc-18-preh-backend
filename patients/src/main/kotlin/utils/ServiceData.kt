package utils

object ServiceData {
    val HOST = System.getenv("PATIENTS_HOST")?.toString() ?: "localhost"
    val PORT = System.getenv("PORT")?.toInt() ?: 10002
}